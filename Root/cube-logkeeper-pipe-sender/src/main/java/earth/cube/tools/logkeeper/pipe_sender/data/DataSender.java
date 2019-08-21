package earth.cube.tools.logkeeper.pipe_sender.data;

import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.bind.DatatypeConverter;

public class DataSender {
	
	private OutputStream _out;
	private DataOutput _dataOut;
	private Map<String,Object> _map;
	
	public DataSender(Map<String,Object> map, OutputStream out) {
		_map = map;
		_out = out;
		_dataOut = new DataOutputStream(out);
	}


	private void writeString(String s) throws IOException {
		_dataOut.writeUTF(s);
	}	
	

	private void writeValue(Object v) throws IOException {
		if(v == null)
			_dataOut.writeByte(DataType.NULL.getValue());
		else {
			String s = v.getClass().getCanonicalName();
			switch(s) {
				case "java.lang.Boolean":
					_dataOut.writeByte(DataType.BOOLEAN.getValue());
					_dataOut.writeBoolean((Boolean) v);
					break;

				case "java.lang.Integer":
					_dataOut.writeByte(DataType.INTEGER.getValue());
					_dataOut.writeInt((Integer) v);
					break;
				
				case "java.util.Date":
					_dataOut.writeByte(DataType.DATE.getValue());
					Calendar c = Calendar.getInstance();
					c.setTime((Date) v);
					_dataOut.writeUTF(DatatypeConverter.printDateTime(c));
					break;
				
				case "java.lang.String":
					_dataOut.writeByte(DataType.STRING.getValue());
					_dataOut.writeUTF((String) v);
					break;
					
				case "java.lang.StringBuilder":
					_dataOut.writeByte(DataType.STRING.getValue());
					_dataOut.writeUTF(v.toString());
					break;
					
				default:
					throw new IllegalArgumentException("Unsupported type '" + v.getClass().getCanonicalName() + "'!");

			}
		}
	}

	public void send() throws IOException {
		_dataOut.writeInt(1);
		_dataOut.writeInt(_map.size());
		for(Entry<String, Object> e : _map.entrySet()) {
			writeString(e.getKey());
			writeValue(e.getValue());
		}
		_out.flush();
	}


}
