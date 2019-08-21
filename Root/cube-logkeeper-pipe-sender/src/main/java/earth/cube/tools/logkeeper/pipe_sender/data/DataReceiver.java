package earth.cube.tools.logkeeper.pipe_sender.data;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.DatatypeConverter;

import earth.cube.tools.logkeeper.pipe_sender.utils.ShouldNotHappenException;

public class DataReceiver {
	
	private DataInput _dataIn;
	
	public DataReceiver(InputStream in) {
		_dataIn = new DataInputStream(in);
	}


	private String readString() throws IOException {
		return _dataIn.readUTF();
	}	
	

	private Object readValue() throws IOException {
		Object v;
		DataType type = DataType.fromValue(_dataIn.readByte());
		switch(type) {
			case NULL:
				v = null;
				break;
		
			case BOOLEAN:
				v = _dataIn.readBoolean();
				break;

			case INTEGER:
				v = _dataIn.readInt();
				break;
			
			case DATE:
				String sDate = _dataIn.readUTF();
				Calendar c = DatatypeConverter.parseDateTime(sDate);
				v = c.getTime();
				break;
			
			case STRING:
				v = _dataIn.readUTF();
				break;
				
			default:
				throw new ShouldNotHappenException(type.toString());
		}
		
		return v;
	}

	
	public Map<String, Object> read() throws IOException {
		Map<String,Object> map = new HashMap<>();
		int nVer = _dataIn.readInt();
		if(nVer != 1)
			throw new IllegalStateException("Version 1 expected but received " + nVer + "!");
		int n = _dataIn.readInt();
		while(n > 0) {
			String sName = readString();
			Object value = readValue();
			map.put(sName, value);
			n--;
		}
		return map;
	}


}
