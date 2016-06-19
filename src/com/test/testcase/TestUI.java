package com.test.testcase;

import java.util.Map;
import org.testng.annotations.Test;

import com.test.base.TestBase;
import com.test.control.DefinedException;
import com.test.control.TestCaseDefined;

public class TestUI extends TestBase {

	@Test(dataProvider = "dataProvider")
	public void testUI(Map<String, String> param) throws DefinedException {
		//testCase从testng.xml中读取 parameter value<parameter name="testCase" value="TestBaidu"/>
		new TestCaseDefined().run(testCase, param);
	}
}