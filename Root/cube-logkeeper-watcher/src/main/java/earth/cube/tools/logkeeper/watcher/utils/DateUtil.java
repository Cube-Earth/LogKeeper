package earth.cube.tools.logkeeper.watcher.utils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

public class DateUtil {

	public static LocalDateTime toUtc(LocalDateTime dt) {
		ZonedDateTime utc = dt.atZone(ZoneId.systemDefault()).withZoneSameInstant(ZoneOffset.UTC);
		return utc.toLocalDateTime();
	}
	
	public static LocalDateTime nowUtc() {
		return LocalDateTime.now(ZoneOffset.UTC);
	}
	
}
