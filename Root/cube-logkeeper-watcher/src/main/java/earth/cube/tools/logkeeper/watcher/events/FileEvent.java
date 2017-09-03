package earth.cube.tools.logkeeper.watcher.events;

import java.nio.file.Path;

public class FileEvent {
	
	private EventType _event;

	private Path _file;

	
	public FileEvent(EventType event, Path file) {
		_event = event;
		_file = file;
	}
	
	public EventType getEvent() {
		return _event;
	}

	public Path getFile() {
		return _file;
	}
	
}
