package earth.cube.tools.logkeeper.watcher.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileUtil {
	
	public static int getINode(Path path) throws IOException {
		return (int) Files.getAttribute(path, "unix:inod");
	}
	
}
