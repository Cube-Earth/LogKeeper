package earth.cube.tools.logkeeper.watcher.utils;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class CmdLineArgs {
	
	private static int WIDTH = 80;
	
	private static int SPACES_BEFORE_OPTION = 5;
	
	private static int SPACES_BEFORE_DESCRIPTION = 3;
	
	private String _sCommand;

	private Options _options;
	
	private CommandLine _cmdLine;
	
	
	public CmdLineArgs(String sCommand) {
		_sCommand = sCommand;
	}
	
	public void setOptions(Options options) {
		_options = options;
	}
	
	public void printUsage() {
		SystemOutPrintWriter.print(out -> {
			final HelpFormatter usageFormatter = new HelpFormatter();
			usageFormatter.printUsage(out, WIDTH, _sCommand, _options);
		});
	}


	public void printHelp() {
		SystemOutPrintWriter.print(out -> {
			final HelpFormatter helpFormatter = new HelpFormatter();
			helpFormatter.printHelp(out, WIDTH, _sCommand, "\noptions:", _options, SPACES_BEFORE_OPTION, SPACES_BEFORE_DESCRIPTION, "", true);
		});
	}
	
	
	public CommandLine parse(String[] saArgs) {
		final CommandLineParser parser = new DefaultParser();

		try {
			_cmdLine = parser.parse(_options, saArgs);
		} catch (ParseException e) {
			throw new ShowHelpException(e);
		}
		
		return _cmdLine;
	}
	
	
	public CommandLine getCommandLine() {
		return _cmdLine;
	}


}
