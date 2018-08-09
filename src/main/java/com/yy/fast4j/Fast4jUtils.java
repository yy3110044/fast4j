package com.yy.fast4j;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.context.WebApplicationContext;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Fast4jUtils {
	private Fast4jUtils() {}
	private static final Logger logger = LogManager.getLogger(Fast4jUtils.class);
	
	public static boolean empty(String str) {
		return str == null || str.trim().isEmpty();
	}
	public static boolean empty(String ... strs) {
		for(String str : strs) {
			if(empty(str)) {
				return true;
			}
		}
		return false;
	}
	
	public static String getBasePath(HttpServletRequest request) {
		StringBuilder sb = new StringBuilder();
		sb.append(request.getScheme()).append("://").append(request.getServerName());
		int port = request.getServerPort();
		if(port != 80) {
			sb.append(':').append(port);
		}
		sb.append(request.getContextPath()).append('/');
		return sb.toString();
	}
	
	//url编码
	public static String urlEncode(String str) {
		if(str != null) {
			try {
				return URLEncoder.encode(str, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				logger.error(e);
				throw new RuntimeException(e);
			}
		} else {
			return "";
		}
	}
	
	//url解码
	public static String urlDecode(String str) {
		if(str != null) {
			try {
				return URLDecoder.decode(str, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				logger.error(e);
				throw new RuntimeException(e);
			}
		} else {
			return "";
		}
	}
	
	//得到每一天的开始时间和结束时间
	private static final long _24HoursMillis = 86400000L; //24小时的毫秒数
	private static final long _8HoursMillis = 28800000L; //8小时的毫秒数
	private static final long _jiange = 57600000L;
	public static long getEveryDayStartTime(long millis) {
		if(millis % _24HoursMillis >= _jiange) {
			return millis / _24HoursMillis * _24HoursMillis - _8HoursMillis + _24HoursMillis;
		} else {
			return millis / _24HoursMillis * _24HoursMillis - _8HoursMillis;
		}
	}
	//得到时间部分的毫秒数
	public static long getTimeMillis(long millis) {
		millis = millis % _24HoursMillis;
		if(millis >= _jiange) {
			return millis - _24HoursMillis;
		} else {
			return millis;
		}
	}
	
	//将对象转换成json字符串
	private static ObjectMapper objectMapper = new ObjectMapper().setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
	public static String ObjecttoJson(Object o) {
		try {
			return objectMapper.writeValueAsString(o);
		} catch (JsonProcessingException e) {
			logger.error(o.toString() + "：转换json错误：" + e.toString());
			throw new RuntimeException(e);
		}
	}
	//将json字符串转换成对象
	public static <T> T jsonToObject(String jsonStr, Class<T> cls) {
		try {
			return objectMapper.readValue(jsonStr, cls);
		} catch (IOException e) {
			logger.error(jsonStr + "，json转对象错误：" + e.toString());
			throw new RuntimeException(e);
		}
	}
	
	//返回spring的bean
	public static Object getBean(String name, ServletContext sc) {
		WebApplicationContext context = (WebApplicationContext)sc.getAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE);
		return context.getBean(name);
	}
	public static <T> T getBean(Class<T> requiredType, ServletContext sc) {
		WebApplicationContext context = (WebApplicationContext)sc.getAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE);
		return context.getBean(requiredType);
	}
	//返回application.xml中配置的值
	public static String getProperty(String name, ServletContext sc) {
		WebApplicationContext context = (WebApplicationContext)sc.getAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE);
		return context.getEnvironment().getProperty(name);
	}
	
	//根据枚举返回select html字符串
	public static String getSelectHtmlStr(Class<? extends Enum<?>> cls, String selectId, String style, String[] extraOption, Enum<?> ... except) {
		Enum<?>[] ts = null;
		try {
			ts = (Enum<?>[])cls.getMethod("values").invoke(null);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			logger.error(e.toString());
			throw new RuntimeException(e);
		}
		List<Enum<?>> list = new ArrayList<Enum<?>>(Arrays.asList(ts));
		if(except != null && except.length > 0) {
			list.removeAll(Arrays.asList(except));
		}
		StringBuilder sb = new StringBuilder();
		sb.append("<select");
		if(selectId != null) {
			sb.append(" id=\"" + selectId + "\"");
		}
		if(style != null) {
			sb.append(" style=\"" + style + "\"");
		}
		sb.append(">");
		
		if(extraOption != null && extraOption.length > 0) {
			for(String extra : extraOption) {
				sb.append("<option>").append(extra).append("</option>");
			}
		}

		for(Enum<?> e : list) {
			sb.append("<option>").append(e.name()).append("</option>");
		}
		sb.append("</select>");
		return sb.toString();
	}
	public static String getSelectHtmlStr(Class<? extends Enum<?>> cls) {
		return getSelectHtmlStr(cls, null, null, null);
	}
	public static String getSelectHtmlStr(Class<? extends Enum<?>> cls, String selectId) {
		return getSelectHtmlStr(cls, selectId, null, null);
	}
	public static String getSelectHtmlStr(Class<? extends Enum<?>> cls, String selectId, String style) {
		return getSelectHtmlStr(cls, selectId, style, null);
	}

	//将对象转为map
	public static JsonResultMap ObjectToMap(Object obj) {
		Field[] fields = obj.getClass().getFields();
		JsonResultMap map = new JsonResultMap();
		try {
			for(Field field : fields) {
				field.setAccessible(true);
				map.put(field.getName(), field.get(obj));
			}
		} catch (SecurityException | IllegalArgumentException | IllegalAccessException e) {
			logger.error(e);
			throw new RuntimeException(e);
		}
		return map;
	}
	
	//将map里的参数转为url参数字符串
	public static String mapToUrlParamStr(Map<String, Object> params) {
		StringBuilder sb = new StringBuilder();
		int i = 0, size = params.size();
		for(Entry<String, Object> entry : params.entrySet()) {
			sb.append(entry.getKey()).append('=').append(entry.getValue());
			if(++i < size) {
				sb.append('&');
			}
		}
		return sb.toString();
	}
	
	/**
	 * 发起一个post请求
	 * @param urlStr
	 * @param contentType application/x-www-form-urlencoded、application/json;charset=utf-8、application/xml;charset=utf-8
	 * @param content post到服务器的内容
	 * @return
	 */
	public static String requestPost(String urlStr, String contentType, String content) {
		BufferedWriter bw = null;
		BufferedReader br = null;
		StringBuilder result = new StringBuilder();
		
		try {
			URL url = new URL(urlStr);
			HttpURLConnection conn = (HttpURLConnection)url.openConnection();
			conn.setRequestMethod("POST");
			conn.setDoInput(true);
			conn.setConnectTimeout(10000);
			conn.setReadTimeout(10000);
			
			if(content != null) {
				conn.setDoOutput(true);
				if(contentType == null) {//默认为application/x-www-form-urlencoded
					conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
				} else {
					conn.setRequestProperty("Content-Type", contentType);
				}
				bw = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream(), "UTF-8"));
				bw.write(content);
				bw.flush();
			}
			
			if(conn.getResponseCode() == 200) {
				br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
				String str = null;
				while((str = br.readLine()) != null) {
					result.append(str).append(System.lineSeparator());
				}
			} else {
				logger.warn(urlStr + "，post请求返回代码为：" + conn.getResponseCode());
				InputStream errorStream = conn.getErrorStream();
				if(errorStream != null) {
					br = new BufferedReader(new InputStreamReader(errorStream, "UTF-8"));
					String str = null;
					while((str = br.readLine()) != null) {
						result.append(str).append(System.lineSeparator());
					}
				}
			}
		} catch (IOException e) {
			logger.error(e);
		} finally {
			try {
				if(bw != null) bw.close();
				if(br != null) br.close();
			} catch (IOException e) {
				logger.error(e);
			}
		}
		return result.toString();
	}
	
	//发起一个get请求
	public static String requestGet(String urlStr) {
		BufferedReader br = null;
		StringBuilder result = new StringBuilder();
		
		try {
			URL url = new URL(urlStr);
			HttpURLConnection conn = (HttpURLConnection)url.openConnection();
			conn.setRequestMethod("GET");
			conn.setConnectTimeout(10000);
			conn.setReadTimeout(10000);
			conn.setDoInput(true);
			
			if(conn.getResponseCode() == 200) {
				br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
				String str = null;
				while((str = br.readLine()) != null) {
					result.append(str).append(System.lineSeparator());
				}
			} else {
				logger.warn(urlStr + "，get请求返回代码为：" + conn.getResponseCode());
				InputStream errorStream = conn.getErrorStream();
				if(errorStream != null) {
					br = new BufferedReader(new InputStreamReader(errorStream, "UTF-8"));
					String str = null;
					while((str = br.readLine()) != null) {
						result.append(str).append(System.lineSeparator());
					}
				}
			}
		} catch (IOException e) {
			logger.error(e);
		} finally {
			try {
				if(br != null) br.close();
			} catch (IOException e) {
				logger.error(e);
			}
		}
		return result.toString();
	}
}