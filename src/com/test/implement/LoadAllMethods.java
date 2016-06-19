package com.test.implement;

import java.util.HashMap;

import com.test.control.DefinedException;
import com.test.control.XMLHandler;
import com.test.interfaces.Linked;

public class LoadAllMethods implements Linked {

	private XMLHandler xh;

	public LoadAllMethods() {
		xh = new XMLHandler("config/LinkedPages.xml");
	}

	public boolean methodIsExist(String className, String methodName) {
		String path = "//" + className + "/method[@name='" + methodName + "']";
		return xh.isExist(path);
	}

	private HashMap<String, String> getMethodAtrribute(String className,
			String methodName) {
		String path = "//" + className + "/method[@name='" + methodName + "']";
		return xh.getElementAttributes(path);
	}

	public boolean returnIsExist(String className, String methodName) {
		return this.getMethodAtrribute(className, methodName).containsKey(
				"return");
	}

	public String getReturn(String className, String methodName) {
		return this.getMethodAtrribute(className, methodName).get("return");
	}

	public String getLinked(String className, String methodName) {
		return this.getMethodAtrribute(className, methodName).get("linked");
	}

	public static void main(String[] args) throws DefinedException {
		LoadAllMethods lm = new LoadAllMethods();
		System.out.println(lm.getLinked("TestBaidu", "search"));
	}

}
