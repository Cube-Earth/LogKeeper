package earth.cube.tools.logkeeper.watcher.expressions;

import java.util.Map;

import org.apache.commons.lang3.text.StrLookup;

public class MapLookup extends StrLookup<String> {
	
	private Map<String, String> _map;

	public MapLookup(Map<String,String> map) {
		_map = map;
		
	}

	@Override
	public String lookup(String sName) {
		return _map == null ? null : _map.get(sName);
	}

}
