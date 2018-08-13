package earth.cube.tools.logkeeper.watcher;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import earth.cube.tools.logkeeper.watcher.utils.FileUtil;
import net.openhft.chronicle.map.ChronicleMap;
import net.openhft.chronicle.map.ChronicleMapBuilder;

public class PositionTracker {
	
	protected static class LogPosition {
		public LogPosition(int i) {
			iNode = i;
		}

		public int iNode;
		public long offset;
	}
	
	private ChronicleMap<Path, LogPosition> _map;
	
	public PositionTracker(File trackerFile) throws IOException {
		_map = createBase().createOrRecoverPersistedTo(getFile(trackerFile));
	}
	
	protected static File getFile(File trackerFile) {
		return trackerFile != null ? trackerFile : new File(System.getProperty("java.io.tmpdir") + "/LogTracker.dat");
	}
	
	protected static ChronicleMapBuilder<Path, LogPosition> createBase() {
		return ChronicleMap
			    .of(Path.class, LogPosition.class)
			    .entries(50)
			    .averageKeySize(200)
			    .averageValueSize(5);
	}
	
	public static void recover(Path trackerFile) throws IOException {
		createBase().recoverPersistedTo(getFile(trackerFile.toFile()), true);
	}
	
	private String getString(Path file) {
		String s = file.toString();
//		s = s.replaceAll("=", "__0x3D__");
		return s;
	}
	
	public void remove(Path file) {
		_map.remove(getString(file));
	}
	
	public void set(Path file, long nPos) throws IOException {
		LogPosition pos = _map.get(file);
		if(pos == null)
			pos = new LogPosition(FileUtil.getINode(file));
		pos.offset = nPos;
		_map.put(file, pos);
	}
	
	public long get(Path file) throws IOException {
		long j = 0;
		int i = FileUtil.getINode(file);
		LogPosition pos = _map.get(file);
		if(pos == null) {
			pos = new LogPosition(i);
			_map.put(file, pos);
		}
		else
			if(pos.iNode != i)
				_map.remove(file);
			else
				j = pos.offset;
		return j;
	}

	public void increment(Path file, int n) throws IOException {
		long j = 0;
		int i = FileUtil.getINode(file);
		LogPosition pos = _map.get(file);
		if(pos == null) {
			pos = new LogPosition(i);
			pos.offset = n;
			_map.put(file, pos);
		}
		else
			if(pos.iNode == i) {
				pos.offset += j;
				_map.put(file, pos);
			}
	}

}
