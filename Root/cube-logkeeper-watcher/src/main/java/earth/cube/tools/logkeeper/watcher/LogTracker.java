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

import earth.cube.tools.logkeeper.watcher.config.Config;
import earth.cube.tools.logkeeper.watcher.config.LinePatternConfig;
import earth.cube.tools.logkeeper.watcher.config.LogConfig;
import earth.cube.tools.logkeeper.watcher.utils.CmdLineArgs;
import earth.cube.tools.logkeeper.watcher.utils.DateUtil;
import earth.cube.tools.logkeeper.watcher.utils.ShowHelpException;

public class LogTracker {
	
	protected final static String PRODUCER = "LogTracker-Tail";

	protected final Logger _log = LogManager.getLogger(getClass());
	
	
	private Config _config;
	
	private DirWatcher _watcher;
	

	private PositionTracker _positions;
	
	private byte[] _buf = new byte[0x10000000];
	
	private DateTimeFormatter _df = DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss.SSS");

	private List<Path> _pendingLogs = new ArrayList<>();
	
	
	
	public LogTracker(Path configFile, Path trackerFile) throws JsonParseException, JsonMappingException, IOException {
		_config = Config.read(configFile);
		_positions = new PositionTracker(trackerFile == null ? null : trackerFile.toFile());
	}
	
	private void init() throws IOException {
		for(LogConfig logConfig : _config.getLogConfigs()) {
			logConfig.mkdirs();
			_watcher.addDir(logConfig.getDirectory());
			if(logConfig.shouldClean())
				try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(logConfig.getDirectory(), logConfig.getGlobString())) {
					directoryStream.forEach(p -> {
						if(Files.exists(p)) {
							try {
								deleteIfNotOpened(p);
								processUnpublishedLines(p);
							} catch (IOException e) {
								_log.warn("init: Exception occurred -->", e);
							}
						}
					});
		        }
		}
	}

	public void run() throws IOException {
		_watcher = new DirWatcher(this);
		init();
	}

	public void close() throws IOException {
		_watcher.close();
	}


	
	private void deleteIfNotOpened(Path file) throws IOException {
		if(file.toFile().renameTo(file.toFile())) {  // check if file is opened/locked
			_log.debug("deleteIfNotOpened: deleting file '" + file + "' ...");
			Files.delete(file);
		}
		else
			_log.debug("deleteIfNotOpened: file '" + file + "' is locked");
	}
	
	private String toUtcString(LocalDateTime date) {
		return _df.format(DateUtil.toUtc(date));
	}

	
	private void publishEntry(Map<String, String> props) {
		// TODO Auto-generated method stub
		
	}

	
	private void publishLine(LogConfig logConfig, Path logFile, int nOffs, int nLen, long nNewFilePosition) throws IOException {
		String sLine = new String(_buf, nOffs, nLen, logConfig.getEncoding());
		sLine = sLine.trim();
		
		LinePatternConfig lineConfig = null;
		Matcher m = null;
		for(LinePatternConfig tmpLineConfig : logConfig.getLineRules()) {
			m = tmpLineConfig.getTextPattern().matcher(sLine);
			if(m.matches()) {
				lineConfig = tmpLineConfig;
				break;
			}
		}
		
		Map<String,String> props = new HashMap<>();
		
		if(lineConfig == null) {  // catch all
			props.put("msg", sLine);
		} else {
			for(Entry<String, String> entry : lineConfig.getFields().entrySet()) {
				String[] saFieldAssignment = entry.getValue().split(",");
				
				String sValue = saFieldAssignment[0].trim();
				if(sValue.startsWith("$"))
					sValue = m.group(Integer.parseInt(sValue.substring(1)));
				if(saFieldAssignment.length > 1)
					if(saFieldAssignment[1].equals("date")) {
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
		LogConfig logConfig = _config.getLogConfig(logFile);
		if(logConfig == null) {
			_log.debug("processUnpublishedLines: log file '" + logFile + "' is not covered by the configuration file and wll be skipped ...");
			return;
		}
		assert(!logConfig.isInvalid());
		
		_log.debug("processUnpublishedLines: log file '" + logFile + "' is beeing processed ...");

		try (RandomAccessFile file = new RandomAccessFile(logFile.toFile(), "r"))
		{
			long j = _positions.get(logFile);
			file.seek(j);
			int i = 0, k;
			
			while(true) {
				int n = file.read(_buf);
				if (n == -1)
					break;
				while(i < n) {
					k = i;
					while(i < n && _buf[i] != 10)
						i++;
					if(i < n) {
						j = i + 1;
						publishLine(logConfig, logFile, k, k-i+1, j);
					}
				}
				if(i < n)
					System.arraycopy(_buf, i, _buf, 0, n-i);
			}
// TODO HOWTO:		Stream.of(s.split("(?<=\n)")).filter(t -> t.endsWith("\n") && !t.matches("[ \t\r\n]*")).forEach(t -> { System.out.println("x: " + StringUtils.chomp(t)); _tracker.increment(_path, t.length()); } );;
		}
	}

	public void addPendingLog(Path logFile) {
		_pendingLogs.add(logFile);
		// TODO signal worker thread
	}
	


	public static Options buildOptions() {
		final Options options = new Options();
		options.addOption("c", "config", false, "Path to configuration file")
			.addOption("r", "recover", false, "recover internal database");
		return options;
	}

	public static void main(final String[] saArgs) throws IOException {
		CmdLineArgs args = new CmdLineArgs("java LogManager"); // TODO adjust sCommand
		args.setOptions(buildOptions());
		try {
			if (saArgs.length == 0)
				throw new ShowHelpException();
			
			CommandLine cmdLine = args.parse(saArgs);
			
			Path trackerFile = null;
			if (cmdLine.hasOption("tacker")) {
				String s = cmdLine.getOptionValue("tracker");
				trackerFile = s == null || s.length() == 0 ? null : Paths.get(s);
			}

			if (cmdLine.hasOption("recover")) {
				PositionTracker.recover(trackerFile);
				return;
			}
			
			Path configFile = null;
			if (cmdLine.hasOption("config")) {
				String s = cmdLine.getOptionValue("config");
				configFile = s == null || s.length() == 0 ? null : Paths.get(s);
			}
			if(configFile == null)
				throw new IllegalArgumentException("Path to configuration file ist mandatory!");
			if(!Files.exists(configFile))
				throw new FileNotFoundException(configFile.toString());
			
			LogTracker tracker = new LogTracker(configFile, trackerFile);
			tracker.run();
		}
		catch(ShowHelpException e) {
			System.out.println();
			if(e.getCause() != null)
				System.out.println("ERROR: " + e.getCause().getMessage());
			args.printHelp();
			System.exit(1);
		}
	}

	public void remove(Path log) {
		_positions.remove(log);
	}
}
