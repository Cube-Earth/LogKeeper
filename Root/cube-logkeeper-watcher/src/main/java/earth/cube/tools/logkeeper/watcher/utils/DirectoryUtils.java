package earth.cube.tools.logkeeper.watcher.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class DirectoryUtils {
	
	public static void mkdirs(Path dir, FilePermissions perms) {
		if(dir == null || perms == null)
			return;
		if(!Files.exists(dir)) {
			mkdirs(dir.getParent(), perms);
			try {
				Files.createDirectory(dir);
				perms.apply(dir);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

}
