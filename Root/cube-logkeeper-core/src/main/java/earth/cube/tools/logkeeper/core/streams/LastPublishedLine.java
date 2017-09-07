package earth.cube.tools.logkeeper.core.streams;

public class LastPublishedLine {

	private String _sLine;
	private long _accessed;

	public void touch() {
		_accessed = System.currentTimeMillis();
	}
	
	public long getTimeStamp() {
		return _accessed;
	}
	
	public boolean isEqual(String sLine) {
		return _sLine != null && _sLine.equals(sLine);
	}
	
	public void set(String sLine) {
		_sLine = sLine;
	}

}
