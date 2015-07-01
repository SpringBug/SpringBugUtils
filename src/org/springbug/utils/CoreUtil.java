package org.springbug.utils;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.Collection;
import java.util.Map;

public class CoreUtil {

	private static String[] HanDigiStr = { "零", "壹", "贰", "叁", "肆", "伍", "陆",
			"柒", "捌", "玖" };
	private static String[] HanDiviStr = { "", "拾", "佰", "仟", "万", "拾", "佰",
			"仟", "亿", "拾", "佰", "仟", "万", "拾", "佰", "仟", "亿", "拾", "佰", "仟",
			"万", "拾", "佰", "仟" };

	@SuppressWarnings("rawtypes")
	public static boolean isEmpty(Object pObj) {
		if (pObj == null) {
			return true;
		}
		if (pObj == "") {
			return true;
		}
		if ((pObj instanceof String)) {
			if (((String) pObj).length() == 0) {
				return true;
			}
		} else if ((pObj instanceof Collection)) {
			if (((Collection) pObj).size() == 0) {
				return true;
			}
		} else if (((pObj instanceof Map)) && (((Map) pObj).size() == 0)) {
			return true;
		}
		return false;
	}

	public static String numToRMBStr(double val) {
		String SignStr = "";
		String TailStr = "";
		if (val < 0.0D) {
			val = -val;
			SignStr = "负";
		}
		if ((val > 100000000000000.0D) || (val < -100000000000000.0D)) {
			return "数值位数过大!";
		}
		long temp = Math.round(val * 100.0D);
		long integer = temp / 100L;
		long fraction = temp % 100L;
		int jiao = (int) fraction / 10;
		int fen = (int) fraction % 10;
		if ((jiao == 0) && (fen == 0)) {
			TailStr = "整";
		} else {
			TailStr = HanDigiStr[jiao];
			if (jiao != 0) {
				TailStr = TailStr + "角";
			}
			if ((integer == 0L) && (jiao == 0)) {
				TailStr = "";
			}
			if (fen != 0) {
				TailStr = TailStr + HanDigiStr[fen] + "分";
			}
		}
		return SignStr + PositiveIntegerToHanStr(String.valueOf(integer)) + "元"
				+ TailStr;
	}

	private static String PositiveIntegerToHanStr(String NumStr) {
		String RMBStr = "";
		boolean lastzero = false;
		boolean hasvalue = false;

		int len = NumStr.length();
		if (len > 15) {
			return "数值过大!";
		}
		for (int i = len - 1; i >= 0; i--) {
			if (NumStr.charAt(len - i - 1) != ' ') {
				int n = NumStr.charAt(len - i - 1) - '0';
				if ((n < 0) || (n > 9)) {
					return "输入含非数字字符!";
				}
				if (n != 0) {
					if (lastzero) {
						RMBStr = RMBStr + HanDigiStr[0];
					}
					if ((n != 1) || (i % 4 != 1) || (i != len - 1)) {
						RMBStr = RMBStr + HanDigiStr[n];
					}
					RMBStr = RMBStr + HanDiviStr[i];
					hasvalue = true;
				} else if ((i % 8 == 0) || ((i % 8 == 4) && (hasvalue))) {
					RMBStr = RMBStr + HanDiviStr[i];
				}
				if (i % 8 == 0) {
					hasvalue = false;
				}
				lastzero = (n == 0) && (i % 4 != 0);
			}
		}
		if (RMBStr.length() == 0) {
			return HanDigiStr[0];
		}
		return RMBStr;
	}

	@SuppressWarnings("rawtypes")
	public static String getPathFromClass(Class cls) {
		String path = null;
		if (cls == null) {
			throw new NullPointerException();
		}
		URL url = getClassLocationURL(cls);
		if (url != null) {
			path = url.getPath();
			if ("jar".equalsIgnoreCase(url.getProtocol())) {
				try {
					path = new URL(path).getPath();
				} catch (MalformedURLException localMalformedURLException) {
				}
				int location = path.indexOf("!/");
				if (location != -1) {
					path = path.substring(0, location);
				}
			}
			File file = new File(path);
			try {
				path = file.getCanonicalPath();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return path;
	}

	@SuppressWarnings("rawtypes")
	public static String getFullPathRelateClass(String relatedPath, Class cls) {
		String path = null;
		if (relatedPath == null) {
			throw new NullPointerException();
		}
		String clsPath = getPathFromClass(cls);
		File clsFile = new File(clsPath);
		String tempPath = clsFile.getParent() + File.separator + relatedPath;
		File file = new File(tempPath);
		try {
			path = file.getCanonicalPath();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return path;
	}

	@SuppressWarnings("rawtypes")
	private static URL getClassLocationURL(Class cls) {
		if (cls == null) {
			throw new IllegalArgumentException("null input: cls");
		}
		URL result = null;
		String clsAsResource = cls.getName().replace('.', '/').concat(".class");
		ProtectionDomain pd = cls.getProtectionDomain();
		if (pd != null) {
			CodeSource cs = pd.getCodeSource();
			if (cs != null) {
				result = cs.getLocation();
			}
			if ((result != null) && ("file".equals(result.getProtocol()))) {
				try {
					if ((result.toExternalForm().endsWith(".jar"))
							|| (result.toExternalForm().endsWith(".zip"))) {
						result = new URL("jar:".concat(result.toExternalForm())
								.concat("!/").concat(clsAsResource));
					} else if (new File(result.getFile()).isDirectory()) {
						result = new URL(result, clsAsResource);
					}
				} catch (MalformedURLException localMalformedURLException) {
				}
			}
		}
		if (result == null) {
			ClassLoader clsLoader = cls.getClassLoader();
			result = clsLoader != null ? clsLoader.getResource(clsAsResource)
					: ClassLoader.getSystemResource(clsAsResource);
		}
		return result;
	}

}
