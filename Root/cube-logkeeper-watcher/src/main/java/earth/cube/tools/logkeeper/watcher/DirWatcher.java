package earth.cube.tools.logkeeper.watcher;

import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Modifier;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class DirWatcher implements Runnable, Closeable {
	
	private static Modifier HIGH = get_com_sun_nio_file_SensitivityWatchEventModifier_HIGH();

	private Logger _log = LogManager.getLogger(getClass());
	
	private List<Path> _dirs = new ArrayList<>();
	
	private WatchService _watcher;

	private LogTracker _tracker;

	
	public DirWatcher(LogTracker logTracker) throws IOException {
		_tracker = logTracker;
	}

	public void addDir(Path dir) throws IOException {
		_dirs.add(dir);
		if(_watcher != null) {
        	_log.info("addDir: watching directory '{}' ...", dir);
        	dir.register(_watcher, new WatchEvent.Kind<?>[] { StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.OVERFLOW }, HIGH);
		}
	}
	
	private static Modifier get_com_sun_nio_file_SensitivityWatchEventModifier_HIGH() {
		try {
			Class<?> c = Class.forName("com.sun.nio.file.SensitivityWatchEventModifier");
			Field f = c.getField("HIGH");
			return (Modifier) f.get(c);
		} catch (Exception e) {
			return null;
		}
	}
	
	public void run() {
		_log.debug("run: entered");
		try {
	        _watcher = FileSystems.getDefault().newWatchService();
	        for(Path dir :_dirs) {
	        	_log.info("run: watching directory '{}' ...", dir);
	        	dir.register(_watcher, new WatchEvent.Kind<?>[] { StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.OVERFLOW }, HIGH);
	        }
	        	
	        while(true) {
	        	WatchKey watchKey;
	            try {
	            	watchKey = _watcher.take();
	            }
	            catch(InterruptedException | IllegalMonitorStateException | ClosedWatchServiceException e) {
		        	_log.debug("run: exiting ...");
	            	return;
	            }
		        
		        List<WatchEvent<?>> events = watchKey.pollEvents();
		        for (WatchEvent<?> event : events) {
			        System.out.println(event.kind() + ":" + event.context().toString());
		        	if(event.kind() == StandardWatchEventKinds.OVERFLOW)
			        	_log.debug("run: overflow occurred ...");
		        	else {
		        		Path relPath = ((WatchEvent<Path>) event).context();
		        		Path dir = (Path)watchKey.watchable();
		        		Path log = dir.resolve(relPath);
		        		
			        	if(event.kind() == StandardWatchEventKinds.ENTRY_DELETE)
			        		_tracker.remove(log);   // TODO push to queue
			        	else
			        		if(!log.endsWith(".tmp"))
			        			new Log(log, _tracker).tail(); // TODO push to queue
		        	}
		
		        }
	        }
		}
		catch(IOException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public void close() throws IOException {
		if(_watcher != null)
			_watcher.close();
	}

}
