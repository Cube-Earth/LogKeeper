package earth.cube.tools.logkeeper.watcher.events;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

public class EventQueue {
	
	private Map<Integer,FileEntry> _inodes = new HashMap<>();
	private Map<Path,FileEntry> _paths = new HashMap<>();
	private Stack<FileEntry> _queue = new Stack<>();
	
	public synchronized void push(EventType event, Path file) throws IOException {
		FileEntry current;
		switch(event) {
			case Updated:
				FileEntry updated = new FileEntry(file);
				current = _inodes.get(updated.getINode());
				if(current == null) {
					updated.lock();
					_inodes.put(updated.getINode(), updated);
					current = updated;
				}
				_paths.put(file, current);
				current.addEvent(EventType.Updated);
				_queue.push(current);
				break;
				
			case Deleted:
				current = _paths.get(file);
				if(current != null)
					current.addEvent(EventType.Deleted);
				_paths.remove(file);
				_queue.push(current);
				break;
				
			default:
				throw new IllegalArgumentException("Unknown event type: " + event);
		}
	}
	
	
	
	public synchronized FileEntry pop() {
		return _queue.pop();
	}	

}
