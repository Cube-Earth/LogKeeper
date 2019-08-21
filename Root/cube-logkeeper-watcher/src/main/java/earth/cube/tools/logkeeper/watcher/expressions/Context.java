package earth.cube.tools.logkeeper.watcher.expressions;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.text.StrLookup;
import org.apache.commons.lang3.text.StrSubstitutor;

public class Context extends StrLookup<String> {
	
	private String _sAssignment;
	private String _sScope;
	private DataType _type;
	private String _sFormat;
	public DateTimeFormatter _df = DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss.SSS");

	protected Map<String,StrLookup<String>> _scopes = new HashMap<>();
	
	
	public Context() {
		_scopes.put("sys", new SystemPropertiesLookup());
		_scopes.put("env", new EnvironmentLookup());
		_scopes.put("func", new FunctionsLookup(this));
	}
	
	public void addScope(String sScope, StrLookup<String> lookup) {
		_scopes.put(sScope, lookup);
	}
	
	public String lookup(String sScope, String sName) {
		StrLookup<String> lookup = _scopes.get(sScope);
		return lookup == null ? null : lookup.lookup(sName);
	}

	protected void analyze(String sAtomicExpression) {
		Pattern p;
		Matcher m;
		
		p = Pattern.compile("(?:([a-zA-Z0-9]+):)?(.*)");
		m = p.matcher(sAtomicExpression);
		if(m.find()) {
			_sScope = m.group(1);
			sAtomicExpression = m.group(2);
		}
		
		p = Pattern.compile("(?:'((?:[^'\\\\]|\\\\.)*)'|((?<!')[^,']+))(, *)?");
		m = p.matcher(sAtomicExpression);
		List<String> parts = new ArrayList<>();
		while(m.find())
			parts.add(m.group(1) != null ? m.group(1) : m.group(2));
		if(parts.size() < 1)
			throw new IllegalArgumentException("Invalid expression '" + sAtomicExpression + "'");
		_sAssignment = parts.get(0);
		_type = parts.size() > 1 ? Enum.valueOf(DataType.class, parts.get(1).toUpperCase()) : DataType.STRING;
		_sFormat = parts.size() > 2 ? parts.get(2) : null;
	}

	/**
	 * Examples:
	 *       env:USER
	 *       sys:user.home
	 *       pattern:$1
	 *       pattern:$1:date:'uuuu-MM-dd HH:mm:ss.SSS'
	 */
	@Override
	public String lookup(String sName) {
		analyze(sName);
		String sValue = lookup(_sScope, _sAssignment);
		switch(_type) {
			case STRING:
				break;
				
			case DATE:
				DateTimeFormatter df = DateTimeFormatter.ofPattern(_sFormat);
				LocalDateTime d = LocalDateTime.parse(sValue, df);
				sValue = _df.format(d);
				break;
				
			default:
				throw new IllegalStateException("Unhandled data type: " + _type);
		}
		return sValue;
	}

	
	public void setDateTimeFormat(String sFormat) {
		_df = DateTimeFormatter.ofPattern(sFormat);
	}
	
	public DateTimeFormatter getDateTimeFormatter() {
		return _df;
	}
	
	public String resolve(String sExpression) {
		StrSubstitutor subst = new StrSubstitutor(this);
		return subst.replace(sExpression);
	}
	
}
