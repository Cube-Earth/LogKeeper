package earth.cube.tools.logkeeper.watcher;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import earth.cube.tools.logkeeper.core.Globals;
import earth.cube.tools.logkeeper.core.LogLevel;
import earth.cube.tools.logkeeper.core.LogMessage;
import earth.cube.tools.logkeeper.core.forwarders.LogForwarder;
import earth.cube.tools.logkeeper.pipe_sender.loggers.ScratchPipe;
import earth.cube.tools.logkeeper.watcher.config.Config;
import earth.cube.tools.logkeeper.watcher.config.ILogConfig;
import earth.cube.tools.logkeeper.watcher.config.LinePatternConfig;
import earth.cube.tools.logkeeper.watcher.config.LogConfigFiles;
import earth.cube.tools.logkeeper.watcher.config.LogConfigStructuredPipe;
import earth.cube.tools.logkeeper.watcher.config.LogConfigTextPipe;
import earth.cube.tools.logkeeper.watcher.health_check.HealthCheck;
import earth.cube.tools.logkeeper.watcher.utils.CmdLineArgs;
import earth.cube.tools.logkeeper.watcher.utils.DateUtil;
import earth.cube.tools.logkeeper.watcher.utils.ShowHelpException;

public class Application {

	protected final static String PRODUCER = "LogKeeper-Watcher";

	public static final int DEFAULT_HOUSE_KEEPING_INTERVAL = 15000;

	protected final Logger _log = LogManager.getLogger(getClass());

	private Config _config;

	private DirWatcher _watcher;

	private PositionTracker _positions;

	private byte[] _buf = new byte[0x10000000];

	private DateTimeFormatter _df = DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss.SSS");

	private List<Path> _pendingLogs = new ArrayList<>();

	private List<IConsumer> _threads = new ArrayList<>();

	private HouseKeeper _houseKeeper;

	private int _nHouseKeepingInterval;

	private boolean _bShutdown;

	private boolean _bPing;
	

	public Application(Path configFile, Path trackerFile, int nHouseKeeperInterval, boolean bPing)
			throws JsonParseException, JsonMappingException, IOException {
		_bPing = bPing;
		if(bPing)
			Globals.setVerbose(true);
		
		if(configFile != null) {
			_config = Config.read(configFile);
		}
		else {
			_config = Config.read(getClass().getResourceAsStream("/LogKeeper.yml"));
			if(_config == null)
				throw new FileNotFoundException("Configuration file could not be found!");
		}
		_positions = new PositionTracker(trackerFile == null ? null : trackerFile.toFile());
		_nHouseKeepingInterval = nHouseKeeperInterval;
	}

	private void init() throws IOException {
		for (ILogConfig logConfig : _config.getLogConfigs()) {
			switch(logConfig.getConfigType()) {
			
				case FILES:
					LogConfigFiles fileConfig = (LogConfigFiles) logConfig;
					_watcher.addDir(fileConfig.getDirectory());
					if (fileConfig.shouldClean())
						try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(fileConfig.getDirectory(),
								fileConfig.getGlobString())) {
							directoryStream.forEach(p -> {
								if (Files.exists(p)) {
									try {
										deleteIfNotOpened(p);
										processUnpublishedLines(p);
									} catch (IOException e) {
										_log.warn("init: Exception occurred -->", e);
									}
								}
							});
						}
					break;
					
				case PIPE_TEXT:
					LogConfigTextPipe textPipeConfig = (LogConfigTextPipe) logConfig;
					_threads.add(new TextPipeConsumer(textPipeConfig));
					break;
					
				case PIPE_STRUCTURED:
					LogConfigStructuredPipe structuredPipeConfig = (LogConfigStructuredPipe) logConfig;
					_threads.add(new StructuredPipeConsumer(structuredPipeConfig));
					break;
					
			}
		}
	}
	
	public void install() {
		// After deserialization of each config element, needed directories / pipes
		// are automatically created. So no explicit steps are required.
	}
	
	private void startThreads() {
		for(IConsumer thread : _threads) {
			thread.start();
		}
	}

	private void stopThreads() throws InterruptedException {
		for(IConsumer thread : _threads) {
			thread.interrupt();
		}
		for(IConsumer thread : _threads) {
			thread.join();
		}
	}

	
	private void printInitialized() {
		LogMessage msg = new LogMessage();
		msg.setMessage("started");
		msg.setApplication("LogKeeper-Watcher");
		msg.setLevel(LogLevel.INFO);
		
		LogForwarder.get().forward(msg);
	}

	
	private void printInitialized2() {
		for(ILogConfig cnf : _config.getLogConfigs()) {
			switch(cnf.getConfigType()) {
				case PIPE_STRUCTURED:
					LogConfigStructuredPipe conf = (LogConfigStructuredPipe) cnf;
					ScratchPipe pipe = new ScratchPipe(conf.getPath().toFile());
					pipe.send("ping");
					break;

				case PIPE_TEXT:
					break;
				
				default:
					break;
			}
		}
	}

	
	public void run() throws IOException, InterruptedException {
		HealthCheck.createInstance(_config != null ? _config.getHealthConfig() : null);
		
		LogForwarder.get().connect();
		
		if(_bPing)
			printInitialized();
		
		_watcher = new DirWatcher(this);
		_houseKeeper = new HouseKeeper(_nHouseKeepingInterval, this);
		_houseKeeper.start();
		init();
		startThreads();
		
		if(_bPing)
			printInitialized2();

		_houseKeeper.join();
		close();
	}

	public void close() throws IOException, InterruptedException {
		if(!_bShutdown) {
			_bShutdown = true;
			_watcher.close();
			
			if(HealthCheck.getInstance() != null)
				HealthCheck.getInstance().close();
			if(_houseKeeper != null)
				_houseKeeper.quit();
			flush();
			stopThreads();
			// TODO: FileMessageConcentrator interrupt & join
			flush();
			LogForwarder.get().close();
		}
	}

	private void deleteIfNotOpened(Path file) throws IOException {
		if (file.toFile().renameTo(file.toFile())) { // check if file is
														// opened/locked
			_log.debug("deleteIfNotOpened: deleting file '" + file + "' ...");
			Files.delete(file);
		} else
			_log.debug("deleteIfNotOpened: file '" + file + "' is locked");
	}

	private String toUtcString(LocalDateTime date) {
		return _df.format(DateUtil.toUtc(date));
	}

	private void publishEntry(Map<String, String> props) {
		// TODO Auto-generated method stub

	}

	private void publishLine(LogConfigFiles logConfig, Path logFile, int nOffs, int nLen, long nNewFilePosition)
			throws IOException {
		String sLine = new String(_buf, nOffs, nLen, logConfig.getEncoding());
		sLine = sLine.trim();

		LinePatternConfig lineConfig = null;
		Matcher m = null;
		for (LinePatternConfig tmpLineConfig : logConfig.getLineRules()) {
			m = tmpLineConfig.getTextPattern().matcher(sLine);
			if (m.matches()) {
				lineConfig = tmpLineConfig;
				break;
			}
		}

		Map<String, String> props = new HashMap<>();

		if (lineConfig == null) { // catch all
			props.put("msg", sLine);
		} else {
			for (Entry<String, String> entry : lineConfig.getFields().entrySet()) {
				String[] saFieldAssignment = entry.getValue().split(",");

				String sValue = saFieldAssignment[0].trim();
				if (sValue.startsWith("$"))
					sValue = m.group(Integer.parseInt(sValue.substring(1)));
				if (saFieldAssignment.length > 1)
					if (saFieldAssignment[1].equals("date")) {
						DateTimeFormatter df = DateTimeFormatter.ofPattern(saFieldAssignment[2]);
						LocalDateTime dt = LocalDateTime.parse(sValue, df);
						sValue = toUtcString(dt);
					}
				props.put(entry.getKey(), sValue);
			}

		}
		props.put("publish_date", toUtcString(LocalDateTime.now()));

		publishEntry(props);

		_positions.set(logFile, nNewFilePosition);
	}


	private void processUnpublishedLines(Path logFile) throws FileNotFoundException, IOException {
		LogConfigFiles logConfig = _config.getLogConfig(logFile);
		if (logConfig == null) {
			_log.debug("processUnpublishedLines: log file '" + logFile
					+ "' is not covered by the configuration file and wll be skipped ...");
			return;
		}

		_log.debug("processUnpublishedLines: log file '" + logFile + "' is beeing processed ...");

		try (RandomAccessFile file = new RandomAccessFile(logFile.toFile(), "r")) {
			long j = _positions.get(logFile);
			file.seek(j);
			int i = 0, k;

			while (true) {
				int n = file.read(_buf);
				if (n == -1)
					break;
				while (i < n) {
					k = i;
					while (i < n && _buf[i] != 10)
						i++;
					if (i < n) {
						j = i + 1;
						publishLine(logConfig, logFile, k, k - i + 1, j);
					}
				}
				if (i < n)
					System.arraycopy(_buf, i, _buf, 0, n - i);
			}
			// TODO HOWTO: Stream.of(s.split("(?<=\n)")).filter(t ->
			// t.endsWith("\n") && !t.matches("[ \t\r\n]*")).forEach(t -> {
			// System.out.println("x: " + StringUtils.chomp(t));
			// _tracker.increment(_path, t.length()); } );;
		}
	}

	public void addPendingLog(Path logFile) {
		_pendingLogs.add(logFile);
		// TODO signal worker thread
	}

	public static Options buildOptions() {
		final Options options = new Options();
		options.addOption("c", "config", true, "Path to configuration file")
				.addOption("r", "recover", false, "Recover internal database")
				.addOption("t", "tracker", true, "Path to tracker file")
				.addOption("i", "interval", true, "House keeping interval (in sec)")
				.addOption("p", "ping", false, "Write ping(s) to pipes")
				.addOption("h", "help", true, "Show help")
				.addOption(null, "install", false, "Install watcher (e.g. creating pipes and directories)");
		return options;
	}

	public static void main(final String[] saArgs) throws IOException, InterruptedException {
		CmdLineArgs args = new CmdLineArgs("java Watcher"); // TODO adjust
															// sCommand
		args.setOptions(buildOptions());
		try {
			CommandLine cmdLine = args.parse(saArgs);

			if (cmdLine.hasOption("h"))
				throw new ShowHelpException();

			Path trackerFile = null;
			if (cmdLine.hasOption("tracker")) {
				String s = cmdLine.getOptionValue("tracker");
				trackerFile = s == null || s.length() == 0 ? null : Paths.get(s);
			}

			if (cmdLine.hasOption("recover")) {
				PositionTracker.recover(trackerFile);
				return;
			}

			int nHouseKeepingInterval = DEFAULT_HOUSE_KEEPING_INTERVAL;
			if (cmdLine.hasOption("interval")) {
				nHouseKeepingInterval = Integer.parseInt(cmdLine.getOptionValue("interval")) * 1000;
			}

			Path configFile = null;
			if (cmdLine.hasOption("config")) {
				String s = cmdLine.getOptionValue("config");
				configFile = s == null || s.length() == 0 ? null : Paths.get(s);
			}
/*
			if (configFile == null) {
				String s = System.getProperty("LOGKEEPER_CONFIG");
				if(s == null || s.length() == 0)
					throw new IllegalArgumentException("Path to configuration file is mandatory!");
			}
			else
				if (!Files.exists(configFile))
					throw new FileNotFoundException(configFile.toString());
*/
			
			Application app = new Application(configFile, trackerFile, nHouseKeepingInterval, cmdLine.hasOption("p"));
			if(cmdLine.hasOption("install"))
				app.install();
			else
				app.run();
		} catch (ShowHelpException e) {
			System.out.println();
			if (e.getCause() != null)
				System.out.println("ERROR: " + e.getCause().getMessage());
			args.printHelp();
			System.exit(1);
		}
	}

	public void remove(Path log) {
		_positions.remove(log);
	}

	public void flushOverdue() {
		for(IConsumer thread : _threads) {
			thread.flushOverdue();
		}

		// TODO: flushOverdue for FileMessageConcentrator
	}

	public void flush() {
		for(IConsumer thread : _threads) {
			thread.flush();
		}

		// TODO: flushOverdue for FileMessageConcentrator
	}
}
