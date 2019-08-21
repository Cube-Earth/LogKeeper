package earth.cube.tools.logkeeper.watcher.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import earth.cube.tools.logkeeper.watcher.LogTest;

public class ConfigTest {
	
	private static Config _config;
	
	private static Path _logDir;

	@BeforeClass
	public static void setUp() throws JsonParseException, JsonMappingException, IOException {
		_logDir = Paths.get(Files.createTempDirectory("junit-" + LogTest.class.getSimpleName()).toString(), "logkeeper.tmp");
		Files.createDirectories(_logDir);
		// TODO delete _logdir recursively
		
		System.setProperty("LOG_DIR", _logDir.toString());
		String sResName = ConfigTest.class.getPackage().getName().replace('.', '/') + "/LogKeeper.yml";
		_config = Config.read(ConfigTest.class.getClassLoader().getResourceAsStream(sResName));
		Assert.assertNotNull(_config);
	}
	
	protected <T> void assertEquals(T[] expected, List<T> actual) {
		int n = Math.min(expected.length, actual.size());
		for(int i = 0; i < n; i++) {
			T e = expected[i];
			T a = actual.get(i);
			if(e instanceof Pattern) {
				Assert.assertEquals(((Pattern) e).pattern(), ((Pattern) a).pattern());
				Assert.assertEquals(((Pattern) e).flags(), ((Pattern) a).flags());
			}
			else
				Assert.assertEquals(Integer.toString(i), e, a);
		}
		Assert.assertEquals(expected.length, actual.size());
	}
	
	@Test
	public void test_1() {
		Assert.assertEquals(3, _config.getLogConfigs().size());
	}

	@Test
	public void test_files_1() {
		LogConfigFiles c = (LogConfigFiles) _config.getLogConfigs().get(0);
		Assert.assertEquals(LogConfigType.FILES, c.getConfigType());
		Assert.assertEquals("server1", c.getApplication());
		Assert.assertEquals("system-logs", c.getSource());
		Assert.assertEquals("json", c.getType());
		Assert.assertEquals(_logDir.resolve("system/logs"), c.getDirectory());
		Assert.assertEquals("iso-8859-1", c.getEncoding());
		Assert.assertEquals("*.log", c.getGlobString());
		Assert.assertEquals(".*\\.log", c.getGlobPattern().pattern());
	}

    @Test
	public void test_files_1_lines_1() {
		LogConfigFiles c = (LogConfigFiles) _config.getLogConfigs().get(0);

		Assert.assertEquals(1, c.getLineRules().size());
		
		LinePatternConfig cl = c.getLineRules().get(0);
		Assert.assertEquals("([0-9]{4}-[0-9]{2}-[0-9]{2}) ([a-zA-Z]) (.*)", cl.getTextPattern().toString());
		Assert.assertFalse(cl.shouldSkip());
	}

	@Test
	public void test_files_1_lines_fields_1() {
		Map<String, String> c = ((LogConfigFiles) _config.getLogConfigs().get(0)).getLineRules().get(0).getFields();
		Assert.assertEquals(3, c.size());
		Assert.assertEquals("$1, date, yyyy-MM-dd HH:mm:ss, yyyy-MM-dd HH:mm:ss", c.get("date"));
		Assert.assertEquals("$2", c.get("level"));
		Assert.assertEquals("$3", c.get("msg"));
	}
	
	@Test
	public void test_files_1_permissions_1() throws IOException {
		Assert.assertEquals("joerg", Files.getOwner(_logDir).getName());
		
		Set<PosixFilePermission> perms = new HashSet<>();
		perms.add(PosixFilePermission.OWNER_READ);
		perms.add(PosixFilePermission.OWNER_WRITE);
		perms.add(PosixFilePermission.OWNER_EXECUTE);
		perms.add(PosixFilePermission.GROUP_READ);
        perms.add(PosixFilePermission.GROUP_EXECUTE);		

        Assert.assertEquals(perms, Files.getPosixFilePermissions(_logDir.resolve("system/logs")));
	}
	
	@Test
	public void test_textPipe_1() {
		LogConfigTextPipe c = (LogConfigTextPipe) _config.getLogConfigs().get(1);
		Assert.assertEquals(LogConfigType.PIPE_TEXT, c.getConfigType());
		Assert.assertEquals("server2", c.getApplication());
		Assert.assertEquals("text1", c.getSource());
		Assert.assertEquals("json", c.getType());
		Assert.assertEquals(_logDir.resolve("pipes/text1"), c.getPath());
		Assert.assertEquals("utf-8", c.getEncoding());
	}
	
    @Test
	public void test_textPipe_1_lines_1() {
    	LogConfigTextPipe c = (LogConfigTextPipe) _config.getLogConfigs().get(1);

		Assert.assertEquals(1, c.getLineRules().size());
		
		LinePatternConfig cl = c.getLineRules().get(0);
		Assert.assertEquals("([0-9]{4}-[0-9]{2}-[0-9]{2}) ([a-zA-Z]) (.*)", cl.getTextPattern().toString());
		Assert.assertFalse(cl.shouldSkip());
	}

	@Test
	public void test_textPipe_1_lines_fields_1() {
		Map<String, String> c = ((LogConfigTextPipe) _config.getLogConfigs().get(1)).getLineRules().get(0).getFields();
		Assert.assertEquals(3, c.size());
		Assert.assertEquals("$1, date, yyyy-MM-dd HH:mm:ss, yyyy-MM-dd HH:mm:ss", c.get("date"));
		Assert.assertEquals("$2", c.get("level"));
		Assert.assertEquals("$3", c.get("msg"));
	}
	
	@Test
	public void test_structuredPipe_1() {
		LogConfigStructuredPipe c = (LogConfigStructuredPipe) _config.getLogConfigs().get(2);
		Assert.assertEquals(LogConfigType.PIPE_STRUCTURED, c.getConfigType());
		Assert.assertEquals("server3", c.getApplication());
		Assert.assertEquals("structured1", c.getSource());
		Assert.assertEquals("json", c.getType());
		Assert.assertEquals(_logDir.resolve("pipes/structured1"), c.getPath());
	}

	@Test
	public void test_healthCheck_1() {
		HealthConfig c = (HealthConfig) _config.getHealthConfig();
		Assert.assertEquals(8800, c.getPort());
		Assert.assertEquals(_logDir.resolve("health/unhealthy"), c.getStateFile());
		assertEquals(new Pattern[] { Pattern.compile("OutOfMemoryException", Pattern.DOTALL | Pattern.CASE_INSENSITIVE), Pattern.compile("panic", Pattern.DOTALL | Pattern.CASE_INSENSITIVE) }, new ArrayList<>(c.getMessagePatterns()));
		assertEquals(new Pattern[] { Pattern.compile("java\\.lang\\.OutOfMemoryException", Pattern.DOTALL | Pattern.CASE_INSENSITIVE), Pattern.compile("fatal", Pattern.DOTALL | Pattern.CASE_INSENSITIVE) }, c.getThrowablePatterns());
	}
}
