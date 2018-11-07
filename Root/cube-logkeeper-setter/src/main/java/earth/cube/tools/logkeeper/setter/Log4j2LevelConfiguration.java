package earth.cube.tools.logkeeper.setter;

import java.util.Map.Entry;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.LoggerConfig;

public class Log4j2LevelConfiguration {
	
	public static String encodeJson(String s) {
		return s.replaceAll("\\", "\\\\").replaceAll("\"", "\\\"");
	}
	
	public static String toJson(LoggerConfig config) {
		return String.format("{ \"name\": \"%s\"; \"level\": \"%s\" }", encodeJson(config.getName()), config.getLevel().toString());
	}
	
	public static String toJson() {
		StringBuilder sb = new StringBuilder("[ ");
		LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
		sb.append(ctx.getRootLogger());
		for(Entry<String, LoggerConfig> e : ctx.getConfiguration().getLoggers().entrySet()) {
			sb.append(", ").append(toJson(e.getValue()));
		}
		sb.append(" ]");
		return sb.toString();
	}
	
	public static void set(String sName, String sLevel) {
		Level level = Level.getLevel(sName);
		if(level == null)
			throw new IllegalArgumentException("Unknown level '" + sLevel + "'!");
		if(sName == null || sName.length() == 0)
			Configurator.setRootLevel(level);
		else
			Configurator.setLevel(sName, level);
	}

}
