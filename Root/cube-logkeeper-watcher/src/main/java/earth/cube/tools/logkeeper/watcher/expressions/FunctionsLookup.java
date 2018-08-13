package earth.cube.tools.logkeeper.watcher.expressions;

import java.time.LocalDateTime;

import org.apache.commons.lang3.text.StrLookup;

public class FunctionsLookup extends StrLookup<String> {

	private Context _ctx;

	public FunctionsLookup(Context ctx) {
		_ctx = ctx;
	}

	@Override
	public String lookup(String sKey) {
		String sValue = null;
		switch (sKey) {
			case "now":
				sValue = _ctx.getDateTimeFormatter().format(LocalDateTime.now());
				break;
		}
		return sValue;
	}

}
