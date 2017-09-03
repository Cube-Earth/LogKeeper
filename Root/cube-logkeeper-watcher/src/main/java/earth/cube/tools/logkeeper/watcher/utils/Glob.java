package earth.cube.tools.logkeeper.watcher.utils;

import java.util.regex.Pattern;

public class Glob {

	public static String toRegExp(String sGlob) {
		int n = sGlob.length();
		StringBuilder sb = new StringBuilder(n);
		boolean bEscaping = false;
		int nCurlies = 0;
		for (char c : sGlob.toCharArray()) {
			switch (c) {
			case '*':
				if (bEscaping)
					sb.append("\\*");
				else
					sb.append(".*");
				bEscaping = false;
				break;
			case '?':
				if (bEscaping)
					sb.append("\\?");
				else
					sb.append('.');
				bEscaping = false;
				break;
			case '.':
			case '(':
			case ')':
			case '+':
			case '|':
			case '^':
			case '$':
			case '@':
			case '%':
				sb.append('\\');
				sb.append(c);
				bEscaping = false;
				break;
			case '\\':
				if (bEscaping) {
					sb.append("\\\\");
					bEscaping = false;
				} else
					bEscaping = true;
				break;
			case '{':
				if (bEscaping) {
					sb.append("\\{");
				} else {
					sb.append('(');
					nCurlies++;
				}
				bEscaping = false;
				break;
			case '}':
				if (nCurlies > 0 && !bEscaping) {
					sb.append(')');
					nCurlies--;
				} else if (bEscaping)
					sb.append("\\}");
				else
					sb.append("}");
				bEscaping = false;
				break;
			case ',':
				if (nCurlies > 0 && !bEscaping) {
					sb.append('|');
				} else if (bEscaping)
					sb.append("\\,");
				else
					sb.append(",");
				break;
			default:
				bEscaping = false;
				sb.append(c);
			}
		}
		return sb.toString();
	}

	public static Pattern toPattern(String sGlob) {
		return Pattern.compile(sGlob, Pattern.CASE_INSENSITIVE);
	}

}
