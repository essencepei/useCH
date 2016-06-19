package com.test.control;

import java.io.File;
import java.util.HashMap;

import com.test.util.ParserXml;

public class Keyword {

	private static HashMap<String, String> keyword = null;
//读取keyword.xml关键字配置文件，关键字为testcase中使用的关键字
	private static synchronized void getKeywordMap() {
		if (keyword == null) {
			ParserXml p = new ParserXml();
			//调用2,2的返回值为map
			keyword = p.parser2Xml(new File("config/keyword.xml")
					.getAbsolutePath());
		}
	}
//处理从keyword.xml中的关键字，如将open，翻译成“打开”
	public static String getKeyword(String key) {
		String value = null;
		if (keyword == null)
			Keyword.getKeywordMap();
		if (keyword.containsKey(key))
			value = keyword.get(key);		
		return value;
	}

}
