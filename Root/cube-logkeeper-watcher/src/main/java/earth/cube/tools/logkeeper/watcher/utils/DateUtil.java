package earth.cube.tools.logkeeper.watcher.utils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Date;

public class DateUtil {

	public static LocalDateTime toUtc(LocalDateTime dt) {
		ZonedDateTime utc = dt.atZone(ZoneId.systemDefault()).withZoneSameInstant(ZoneOffset.UTC);
		return utc.toLocalDateTime();
	}
	
	public static LocalDateTime nowUtc() {
		return LocalDateTime.now(ZoneOffset.UTC);
	}
	
	public static Date toDate(LocalDateTime ldt) {
		return Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());		
	}
	
}
