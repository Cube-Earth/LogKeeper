package earth.cube.tools.logkeeper.watcher.utils;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

public class FileUtil {
	
	public static int getINode(Path path) throws IOException {
		return (int) Files.getAttribute(path, "unix:inod");
	}

	public static void createPipe(Path file, FilePermissions perms) {
		if(file == null || Files.exists(file) || perms == null)
			return;
		
		if(file.toAbsolutePath().startsWith("/dev/"))
			return;
		
		DirectoryUtils.mkdirs(file.getParent(), perms);
		
		try {
			Process process = new ProcessBuilder().command("/usr/bin/env", "mkfifo", file.toAbsolutePath().toString()).inheritIO().start();
			if(!process.waitFor(5, TimeUnit.SECONDS))
				throw new IllegalStateException("Timed out!");
			
			if(process.exitValue() != 0)
				throw new IllegalStateException("Exit code = " + process.exitValue());
			
			perms.apply(file);
		}
		catch(IOException | InterruptedException e) {
			throw new IllegalArgumentException(e);
		}
	}
	
	public static File toFile(URL url) {
		File f;
		try {
		  f = new File(url.toURI());
		} catch(URISyntaxException e) {
		  f = new File(url.getPath());
		}
		return f;
	}
	
}
