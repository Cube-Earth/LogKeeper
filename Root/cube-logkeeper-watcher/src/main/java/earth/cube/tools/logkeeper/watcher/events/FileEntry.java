package earth.cube.tools.logkeeper.watcher.events;

import java.io.Closeable;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Path;
import java.util.EnumSet;

import earth.cube.tools.logkeeper.watcher.IFunc;
import earth.cube.tools.logkeeper.watcher.utils.FileUtil;

public class FileEntry implements Closeable {
	
	private Path _path;
	
	private int _nINode;
	
	private RandomAccessFile _file;
	
	private EnumSet<EventType> _pendingEvents = EnumSet.noneOf(EventType.class);

	private boolean _bDelete;
	
	private static byte[] _buf = new byte[0x100000];
	

	public FileEntry(Path path) throws IOException {
		_path = path;
		_nINode = FileUtil.getINode(path);
	}
	
	public int getINode() {
		return _nINode;
	}
	
	public Path getOriginalPath() {
		return _path;
	}
	
	public void lock() throws FileNotFoundException {
		_file = new RandomAccessFile(_path.toFile(), "r");
	}
	
	public void addEvent(EventType event) {
		_bDelete = event == EventType.Deleted;
		_pendingEvents.add(event);
	}
	
	public void clearEvents() {
		_pendingEvents.clear();
	}
	
	public boolean hasEvents() {
		return _pendingEvents.size() > 0;
	}

	@Override
	public void close() throws IOException {
		_file.close();
	}
	
	public void processLines(long nStartPosition, IPublishLineFunc publishFunc, IFunc<FileEntry> flushFunc, IFunc<FileEntry> releaseFunc) throws IOException {
		long j = nStartPosition;
		_file.seek(j);
		int i = 0, k;
		
		while(true) {
			int n = _file.read(_buf, i, _buf.length - i + 1);
			if (n == -1)
				break;
			while(i < n) {
				k = i;
				while(i < n && _buf[i] != 10)
					i++;
				if(i < n) {
					j = i + 1;
					publishFunc.publishLine(_buf, k, k-i+1);
				}
			}
			if(i < n) {
				System.arraycopy(_buf, i, _buf, 0, n-i);
				i = n - i;
			}
		}
		if(_bDelete) {
			if(i > 0)
				publishFunc.publishLine(_buf, i, i+1);
			flushFunc.execute(this);
		}
		releaseFunc.execute(this);
		
	}
		
}
