package org.xudifsd;

import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;

import java.util.Map;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class Utils {
	public static Map<String, String> getConfigFromFile(String filePath) throws Exception {
		Pattern pa = Pattern.compile("=");
		FileReader reader = new FileReader(filePath);
		BufferedReader buf = new BufferedReader(reader);
		HashMap<String, String> map = new HashMap<String, String>();
		String line;
		for (int lineNo = 1;(line = buf.readLine()) != null; lineNo++) {
			if (line.startsWith("#"))
				continue;
			String[] keyValue = pa.split(line);
			if (keyValue.length != 2) {
				System.err.format("wrong format of %s in line %d\n", filePath, lineNo);
				continue;//ignore wrong format
			}
			map.put(keyValue[0].trim(), keyValue[1].trim());
		}
		buf.close();
		return map;
	}
	public static String getIfNotNull(Map<String, String> map, String key, String defaultValue) {
		String result = map.get(key);
		if (result == null) {
			System.err.println("info: " + key + " is not in config, using " + defaultValue);
			return defaultValue;
		} else {
			System.err.println("info: setting " + key + " to be " + result);
			return result;
		}
	}

	public static Channel getChannel(String host, int port, String username, String password) throws IOException {
		ConnectionFactory factory = new ConnectionFactory();
		if (host != null)
			factory.setHost(host);
		if (port > 0)
			factory.setPort(port);
		if (username != null)
			factory.setUsername(username);
		if (password != null)
			factory.setPassword(password);
		Connection connection = factory.newConnection();
		return connection.createChannel();
	}
}
