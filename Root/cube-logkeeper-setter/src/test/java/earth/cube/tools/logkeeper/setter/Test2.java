package earth.cube.tools.logkeeper.setter;

import java.io.IOException;
import java.util.Map.Entry;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.config.builder.api.AppenderComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilderFactory;
import org.apache.logging.log4j.core.config.builder.api.LoggerComponentBuilder;
import org.apache.logging.log4j.core.config.builder.impl.BuiltConfiguration;
import org.junit.Test;

public class Test2 {
	
	protected void dump() {
		LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
		System.out.println("root - " + ctx.getRootLogger().getLevel());
		for(Entry<String, LoggerConfig> e : ctx.getConfiguration().getLoggers().entrySet()) {
			System.out.println(e.getKey() + " - " + e.getValue().getLevel());
		}
	}
	
	@Test
	public void test() throws IOException {
//		ConfigurationBuilder<BuiltConfiguration> builder
//		 = ConfigurationBuilderFactory.newConfigurationBuilder();
		
		Logger logger = LogManager.getLogger(this);
		logger.info("abc");
		logger.error("def");
		
		dump();
		
		LoggerContext ctx = (LoggerContext) LogManager.getContext(false); 
		LoggerConfig logConf = ctx.getConfiguration().getLoggerConfig(getClass().getCanonicalName()); 
		logConf.setLevel(Level.INFO);
		logger.info("abc");
		logger.error("def");
		
		ConfigurationBuilder<BuiltConfiguration> builder = ConfigurationBuilderFactory.newConfigurationBuilder();
		AppenderComponentBuilder console  = builder.newAppender("stdout", "Console"); 
		builder.add(console);
		LoggerComponentBuilder logger2 = builder.newLogger(getClass().getCanonicalName(), Level.DEBUG);
		logger2.add(builder.newAppenderRef("stdout"));
		logger2.addAttribute("additivity", false);
		builder.add(logger2);
		builder.writeXmlConfiguration(System.out);
		Configurator.initialize(builder.build());		
		logger.info("abc");
		logger.error("def");
		
		Configurator.setLevel(getClass().getCanonicalName(), Level.DEBUG);
//		Configurator.setRootLevel(Level.DEBUG);
		logger.info("abc");
		logger.error("def");
		
		dump();
	}

}
