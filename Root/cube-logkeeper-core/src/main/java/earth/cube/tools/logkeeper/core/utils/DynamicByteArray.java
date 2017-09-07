package earth.cube.tools.logkeeper.core.utils;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class DynamicByteArray {

	private final static int DEFAULT_ARRAY_SIZE = 0x100;
	
	private List<byte[]> _arrays = new ArrayList<>();
	private byte[] _currArray;
	private int _nArraySize = DEFAULT_ARRAY_SIZE;
	private int _nIdx;
	
	public DynamicByteArray() {
	}
	
	public DynamicByteArray(int nArraySize) {
		_nArraySize = nArraySize;
	}

	public void add(int b) {
		if(_currArray == null || _nIdx >= _nArraySize) {
			_currArray = new byte[_nArraySize];
			_arrays.add(_currArray);
			_nIdx = 0;
		}
		_currArray[_nIdx++] = (byte) b;
	}
	
	public byte[] get() {
		int n = _arrays.size() == 0 ? 0 : (_arrays.size() - 1) *  _nArraySize + _nIdx;
		byte[] buf = new byte[n];
		for(int i = 0; i < _arrays.size() - 1; i++)
			System.arraycopy(_arrays.get(i), 0, buf, i * _nArraySize, _nArraySize);
		if(_currArray != null)
			System.arraycopy(_currArray, 0, buf, (_arrays.size() - 1) * _nArraySize, _nIdx);
		return buf;
	}
	
	public String getAsString(String sCharSet) {
		try {
			return new String(get(), sCharSet);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}
	
	public String getAsString() {
		return getAsString(Charset.defaultCharset().name());
	}
	
	public void clear() {
		_arrays.clear();
		_currArray = null;
	}

}
