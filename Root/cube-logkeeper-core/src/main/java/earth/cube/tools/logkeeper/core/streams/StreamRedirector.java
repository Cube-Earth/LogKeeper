package earth.cube.tools.logkeeper.core.streams;

import java.io.IOException;
import java.io.PrintStream;

import earth.cube.tools.logkeeper.core.Parameter;

public class StreamRedirector {
	
	private static final int DEFAULT_HOUSEKEEPER_WAIT_TIME = 15000; // in msec
	
	private static LoggingOutputStream _stdOut;
	private static LoggingOutputStream _stdErr;
	
	private static PrintStream _oldStdOut;
	private static PrintStream _oldStdErr;
	
	private static volatile int _nHouseKeeperWaitTime = DEFAULT_HOUSEKEEPER_WAIT_TIME;
	
	private static HouseKeeper _houseKeeper;
	
	
	public static void setHouseKeeperWaitTime(int nHouseKeeperWaitTime) {
		_nHouseKeeperWaitTime = nHouseKeeperWaitTime;
	}
	
	
	public static void set() {
		synchronized(StreamRedirector.class) {
			if(_oldStdOut != null)
				return;
			
			if(_stdOut == null) {
				String sApplication = Parameter.getString("logkeeper.application", "application");
				_stdOut = new LoggingOutputStream(sApplication, StreamType.STDOUT);
				_stdErr = new LoggingOutputStream(sApplication, StreamType.STDERR);
			}
			
			_oldStdOut = System.out;
			_oldStdErr = System.err;
			
			_houseKeeper = new HouseKeeper(_nHouseKeeperWaitTime);
			_houseKeeper.start();
			
			System.setOut(new PrintStream(_stdOut));
			System.setErr(new PrintStream(_stdErr));
		}
	}
	
	
	public static void unset() {
		synchronized(StreamRedirector.class) {
			if(_oldStdOut == null)
				return;
			
			System.setOut(_oldStdOut);
			System.setErr(_oldStdErr);
			
			_houseKeeper.quit();
			_houseKeeper = null;
			
			try {
				_stdOut.flush();
			} catch (IOException e) {
				e.printStackTrace(System.out);
			}
			try {
				_stdErr.flush();
			} catch (IOException e) {
				e.printStackTrace(System.err);
			}

			_oldStdOut = null;
			_oldStdErr = null;
		}
	}
	
	public static void flushOverdue() {
		if(_oldStdOut != null) {
			_stdOut.flushOverdue();
			_stdErr.flushOverdue();
		}
	}


	public static void flush() {
		if(_oldStdOut != null) {
			try {
				_stdOut.flush();
			} catch (IOException e) {
				e.printStackTrace(System.out);
			}
			try {
				_stdErr.flush();
			} catch (IOException e) {
				e.printStackTrace(System.err);
			}
		}
	}

}
