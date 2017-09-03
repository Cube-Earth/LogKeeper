package earth.cube.tools.logkeeper.watcher.utils;

import java.io.PrintWriter;
import java.util.function.Consumer;

public class SystemOutPrintWriter {
	
	public static void print(Consumer<PrintWriter> func) {
		PrintWriter out = new PrintWriter(System.out);
		try {
			func.accept(out);
		}
		finally {
			out.flush();
		}
	}

}
