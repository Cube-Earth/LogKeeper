package earth.cube.tools.logkeeper.core;

import java.time.format.DateTimeFormatter;

public class Globals {

	public static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss.SSS");

	public static final String ZMQ_HOST = "127.0.0.1";
	
	public static final int ZMQ_PORT = 2120;
	
}
