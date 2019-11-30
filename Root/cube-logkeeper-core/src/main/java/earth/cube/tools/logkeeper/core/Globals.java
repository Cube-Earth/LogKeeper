package earth.cube.tools.logkeeper.core;

import java.text.SimpleDateFormat;

public class Globals {

//	public static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss.SSS");
	public static final SimpleDateFormat DTF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

	public static final String ZMQ_HOST = "127.0.0.1";
	
	public static final int ZMQ_PORT = 2120;
	
	public static boolean _bVerbose;
	
	public static void setVerbose(boolean bVerbose) {
		_bVerbose = bVerbose;
	}
	
	public static boolean isVerbose() {
		return _bVerbose;
	}
	
}
