package earth.cube.tools.logkeeper.watcher.expressions;

import org.apache.commons.lang3.text.StrLookup;

public class EnvironmentLookup extends StrLookup<String> {

	@Override
	public String lookup(String sName) {
		return System.getenv(sName);
	}

}
