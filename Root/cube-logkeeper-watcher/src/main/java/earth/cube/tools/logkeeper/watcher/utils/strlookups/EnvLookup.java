package earth.cube.tools.logkeeper.watcher.utils.strlookups;

import org.apache.commons.lang3.text.StrLookup;

public class EnvLookup extends StrLookup<String> {

	@Override
	public String lookup(String sName) {
		String sValue;
		
		String[] sa = sName.split(":");
		switch(sa[0]) {
			case "env":
				sValue = System.getenv(sa[1]);
				break;
				
			case "sys":
				sValue = System.getProperty(sa[1]);
				break;
				
			default:
				sValue = "UNKNOWN";
		}
		
		return sValue;
	}

}
