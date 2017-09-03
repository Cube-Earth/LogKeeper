package earth.cube.tools.logkeeper.watcher;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.BeforeClass;
import org.junit.Test;

import earth.cube.tools.logkeeper.watcher.DirWatcher;
import earth.cube.tools.logkeeper.watcher.LogTracker;

public class LogTest {
	
	private static Path _logDir;
	private Path _logPath;

	@BeforeClass
	public static void setUp() throws IOException {
		_logDir = Files.createTempDirectory("junit-" + LogTest.class.getSimpleName());
		System.out.println("log dir = " + _logDir);
	}
	
	@Test
	public void test_1() throws InterruptedException, IOException {
		LogTracker tracker = new LogTracker(null, Paths.get(_logDir.toString(),"tracker.tmp"));   // TODO
		
		DirWatcher watcher = new DirWatcher(tracker);
		watcher.addDir(_logDir);
		Thread t2 = new Thread(watcher);
		t2.start();
		System.out.println(t2.getName());
		_logPath = Paths.get(_logDir.toString(), "log1.txt");
		System.out.println("log path = " + _logPath);
		PrintWriter out = new PrintWriter(_logPath.toFile());
		out.println("line1");
		out.println("line2");
		out.flush();
		System.out.println("line3");
		out.println("line3");
		Thread.sleep(5000);
		System.out.println("line4");
		out.println("line4");
		out.flush();
		System.out.println("line5");
		out.println("line5");
		Thread.sleep(5000);
		System.out.println("line6");
		out.println("line6");
		System.out.println("line7");
		out.println("line7");
		System.out.println("line8");
		out.println("line8");
		out.close();
//		t.stop();
		watcher.close();
//		t2.stop();
		
	}

}
