package earth.cube.tools.logkeeper.core;

public class Parameter {
	
	public static String getString(String sName) {
		String s = System.getProperty(sName);
		if(s == null)
			s = System.getenv(sName.toUpperCase().replaceAll("[-.]", "_"));
		return s;
	}
	
	public static String getString(String sName, String sDefault) {
		String s = getString(sName);
		return s != null ? s : sDefault;
	}

	public static int getInt(String sName, int nDefault) {
		String s = getString(sName);
		return s != null ? Integer.parseInt(s) : nDefault;
	}
	
	public static <T> T get(String sName, IObjectCreator<T> creator) {
		return creator.create(getString(sName));
	}

}
