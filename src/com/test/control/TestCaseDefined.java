package com.test.control;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.test.base.Config;
import com.test.implement.TestCase;
import com.test.widget.Create;
import com.test.widget.Excute;
import com.test.widget.LogicHandle;
import com.test.widget.Open;

public class TestCaseDefined {

	private Open open;

	private Create create;

	private Excute excute;

	private LogicHandle ifh;

	private ArrayList<String> ml;

	private RegExp re;

	private String description;

	private String replaceDesc;

	private Map<String, String> param;

	public TestCaseDefined() {
		param = new HashMap<String, String>();
		re = new RegExp();
		open = new Open();
		create = new Create();
		excute = new Excute();
		ifh = new LogicHandle(excute);
	}

	public Map<String, String> getParam() {
		return param;
	}

	private void getTestCase(String testCaseName) {
		TestCase tc = null;
		if (!Config.getConfig("testCase").equals(""))
			try {
				tc = (TestCase) Class.forName(Config.getConfig("testCase"))
						.newInstance();
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		else
			tc = new TestCase();
		ml = tc.getTestCase(testCaseName);
	}

	public void run(String fileName, String[] parameter)
			throws DefinedException {
		int count = 0;
		for (String p : parameter) {
			param.put("args[" + String.valueOf(count) + "]", p);
			count++;
		}
		this.run(fileName);
	}

	public void run(String fileName, Map<String, String> param)
			throws DefinedException {
		this.param = param;
		this.run(fileName);
	}

	private void run(String fileName) throws DefinedException {
		create.createLog();
		create.createAssert();
		//获取当前对象的操作步骤的text文本集合
		this.getTestCase(fileName);
		//遍历操作文本
		for (int i = 0; i < ml.size(); i++) {
			//每一句操作文本
			description = ml.get(i);
			replaceDesc = description;
			//匹配正则表达式
			if (!re.isComments(description))
				this.selectObjectByKeyword();
		}
		if (ifh.isLogicFlag()) {
			ifh.setParam(param);
			String index = String.valueOf(1);
			if (re.isIfCase(ifh.getLogicTitle().get(index)))
				ifh.executeIfBlock(index);
			if (re.isForCase(ifh.getLogicTitle().get(index)))
				ifh.executeForBlock(index);
		}
	}
//通过关键字找对象
	private void selectObjectByKeyword() throws DefinedException {
		if (ifh.isLogicFlag() && re.getLogicLevel(description) == 0) {
			String backupDesc = description;
			String replaceBackupDesc = replaceDesc;
			ifh.setParam(param);
			String index = String.valueOf(1);
			if (re.isIfCase(ifh.getLogicTitle().get(index)))
				ifh.executeIfBlock(index);
			if (re.isForCase(ifh.getLogicTitle().get(index)))
				ifh.executeForBlock(index);
			ifh.setLogicTitle(new HashMap<String, String>());
			ifh.setLogicBlock(new HashMap<String, ArrayList<String>>());
			ifh.setMultiLogic(new HashMap<Integer, Integer>());
			ifh.setLogicIndex(0);
			ifh.setLogicFlag(false);
			description = backupDesc;
			replaceDesc = replaceBackupDesc;
		}
		//关键字是否是open
		if (re.isOpen(description)) {
			open.setParam(param);
			open.setDescription(description);
			open.setReplaceDesc(replaceDesc);
			open.open();
		} else if (re.isImport(description)) {
			create.setDescription(description);
			create.createPage();
		} else if (re.isIfCase(description) || re.isForCase(description))
			ifh.logicCaseCollect(description);
		else if (ifh.isLogicFlag() && re.isLogicBlock(description))
			ifh.logicCaseCollect(description);
//		 if (re.isPageOrObject(description))
		else {
			excute.setParam(param);
			excute.setDescription(description);
			excute.setReplaceDesc(replaceDesc);
			excute.excute();
		}

	}

}
