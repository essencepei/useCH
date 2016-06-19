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
			//调用处理xml文件的方法,2的返回值为map
			//{sendKeys=sendKeys, element_b=(, 增加参数=addValueToParam, parameter_b={, 
			//for="循环".*从.*到.*, parameter_e=}, 点击=click, method_b=[, method_e=], Assert=Assert, 
			//清空=clear, 输入=sendKeys, open="打开", Log=Log, page="(页面|对象)", 信息=comment, if="如果", 
			//getElement=getElement, import="创建页面对象", 属性值=getAttribute, 
			//executePageMethod=executePageMethod, isEquals=(|不)等于, 比较=assertEquals, equals=等于,
			//importName="对象名", or="或者", element_e_=>, return="返回值", 等待=sleep, logicBlock=-+, 
			//notEquals=不等于, getElementNoWait=getElementNoWait, and="并且", 搜索=search, object_b=",
			//element_b_=<, element_e=)}
			keyword = p.parser2Xml(new File("config/keyword.xml")
					.getAbsolutePath());
		}
	}
//处理从keyword.xml中读取的关键字，如将open，翻译成“打开”
	public static String getKeyword(String key) {
		String value = null;
		if (keyword == null)
			Keyword.getKeywordMap();
		if (keyword.containsKey(key))
			value = keyword.get(key);		
		return value;
	}

}
