package earth.cube.tools.logkeeper.pipe_sender.data;

public enum DataType {
	
	NULL(0),
	BOOLEAN(1),
	INTEGER(2),
	DATE(3),
	STRING(4);
		
	private int _nType;
	
	private DataType(int nDataType) {
		_nType = nDataType;
	}
	
	public int getValue() {
		return _nType;
	}
	
	public static DataType fromValue(byte nValue) {
		return DataType.values()[nValue];
	}
	

}
