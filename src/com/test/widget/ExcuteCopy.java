package com.test.widget;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import com.test.base.Config;
import com.test.base.Page;
import com.test.control.DefinedException;
import com.test.control.LoadAllPages;
import com.test.control.ObjectManager;
import com.test.control.RegExp;
import com.test.control.Keyword;
import com.test.implement.LoadAllMethods;
import com.test.interfaces.Linked;
import com.test.util.Log;

public class ExcuteCopy {

	private RegExp re;

	private LoadAllPages allPages;

	private Linked linked;

	private Map<String, String> param;

	private String description;

	private String replaceDesc;

	private String currentObject = null;

	private String currentMethod;

	public ExcuteCopy() {
		this.re = new RegExp();
		this.allPages = new LoadAllPages();
		this.getLinked();
	}

	private void getLinked() {
		if (!Config.getConfig("linked").equals(""))
			try {
				linked = (Linked) Class.forName(Config.getConfig("linked"))
						.newInstance();
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		else
			linked = new LoadAllMethods();
	}

	public void setParam(Map<String, String> param) {
		this.param = param;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setReplaceDesc(String replaceDesc) {
		this.replaceDesc = replaceDesc;
	}

	@SuppressWarnings("rawtypes")
	public void excute() throws DefinedException {
		Object object = this.getObject();
		Class clazz = object.getClass();
		if (object instanceof Class) {
			clazz = (Class) object;
		}
		Method[] methods = clazz.getMethods();
		Method action = null;
		Object[] param = new Object[] {};
		param = this.handleParamter(param);
		action = this.handleMethod(object, methods, param.length);
		param = this.handleSpecialParamter(action.getName(), param);
		this.invokeMethod(object, action, param);
	}

	private Object[] parameterReplace(String pName) throws DefinedException {
		Object[] param = null;
		if (!pName.equals("")) {
			param = pName.split("(?<!\\\\),");
			for (int i = 0; i < param.length; i++) {
				if (this.param.containsKey(param[i]))
					param[i] = this.param.get(param[i])
							.replaceAll("\\\\,", ",");
				else
					param[i] = param[i].toString().replaceAll("\\\\,", ",");
			}
			replaceDesc = re.replaceParameter(replaceDesc,
					this.join(param, ","));
		} else
			throw new DefinedException("Parameter should not be null!");
		return param;
	}

	private Object[] handleParamter(Object[] param) throws DefinedException {
		if (re.hasParameter(description)) {
			String pName = re.getMethodParameterName(description).get(0);
			param = this.parameterReplace(pName);
		}
		return param;
	}

	private Object[] handleElementParamter(Object[] param)
			throws DefinedException {
		if (re.isElementParameter(description)) {
			String pName = re.getElementParameter(description).get(0);
			param = this.parameterReplace(pName);
		}
		return param;
	}

	private Object[] handleSpecialParamter(String methodName, Object[] param)
			throws DefinedException {
		if (methodName.equals(Keyword.getKeyword("sendKeys"))) {
			if (param != null) {
				CharSequence[] cs = new CharSequence[] { (String) param[0] };
				param = new Object[] { cs };
			} else
				throw new DefinedException("Method: " + methodName
						+ " parameter is not exist or null!");
		}
		if (methodName.equals(Keyword.getKeyword("executePageMethod"))) {
			String[] p = new String[] {};
			if (param.length > 0)
				p = (String[]) param;
			String className = currentObject;
			String mName = currentMethod;
			currentObject = null;
			currentMethod = null;
			if (linked.returnIsExist(className, mName)
					&& !linked.getReturn(className, mName).equals(""))
				param = new Object[] { p, linked.getReturn(className, mName),
						linked.getLinked(className, mName) };
			else
				param = new Object[] { p, linked.getLinked(className, mName) };
		}
		return param;
	}

	private String join(Object[] o, String flag) {
		StringBuffer str_buff = new StringBuffer();
		for (int i = 0, len = o.length; i < len; i++) {
			str_buff.append(String.valueOf(o[i]));
			if (i < len - 1)
				str_buff.append(flag);
		}
		return str_buff.toString();
	}

	private Method handleMethodFromPage(Method[] methods, int paramCount,
			String getMethod) {
		Method action = null;
		for (Method m : methods) {
			if (m.getName().equals(getMethod)
					&& m.getParameterAnnotations().length == paramCount) {
				action = m;
				break;
			}
		}
		return action;
	}

	@SuppressWarnings("rawtypes")
	private Method handleMethodFromLinked(Object object, Method[] methods,
			String getMethod) throws DefinedException {
		Method action = null;
		String className = currentObject;
		currentMethod = getMethod;
		if (linked.methodIsExist(className, getMethod)) {
			Class[] parameterTypes = null;
			if (linked.returnIsExist(className, getMethod)
					&& !linked.getReturn(className, getMethod).equals(""))
				parameterTypes = new Class[] { String[].class, String.class,
						String.class };
			else
				parameterTypes = new Class[] { String[].class, String.class };
			try {
				action = object.getClass()
						.getMethod(Keyword.getKeyword("executePageMethod"),
								parameterTypes);
			} catch (SecurityException e) {
				throw new DefinedException("Can not find the method: "
						+ Keyword.getKeyword("executePageMethod")
						+ " Page class");
			} catch (NoSuchMethodException e) {
				throw new DefinedException("Can not find the method: "
						+ Keyword.getKeyword("executePageMethod")
						+ " Page class");
			}
		}
		return action;
	}

	private Method handleMethod(Object object, Method[] methods, int paramCount)
			throws DefinedException {		
		Method action = null;
		if (re.hasMethod(description)
				&& !re.methodName(description).get(0).equals("")) {
			String getMethod = re.methodName(description).get(0);
			if (Keyword.getKeyword(getMethod) != null)
				getMethod = Keyword.getKeyword(getMethod);
			action = this.handleMethodFromPage(methods, paramCount, getMethod);
			if (action == null)
				action = this
						.handleMethodFromLinked(object, methods, getMethod);
			if (action == null)
				throw new DefinedException("Had not defined the method: "
						+ getMethod + " in LinkedPages or Page class");
		}
		return action;
	}

	private void invokeMethod(Object object, Method action, Object[] param)
			throws DefinedException {
		try {			
			if (re.hasReturnValue(description)
					&& re.returnName(description) != null) {
				String returnValue = action.invoke(object, param).toString();
				this.param.put(re.returnName(description).get(0), returnValue);
				replaceDesc = re.replaceReturn(replaceDesc, returnValue);
				Log.commentStep(replaceDesc);
			} else {
				Log.commentStep(replaceDesc);
				action.invoke(object, param);
			}
		} catch (IllegalArgumentException e) {			
			throw new DefinedException(object.getClass().getName() + " "
					+ action.getName() + " argument error!");
		} catch (IllegalAccessException e) {
			throw new DefinedException(object.getClass().getName() + " "
					+ action.getName() + " is invoked error!");
		} catch (InvocationTargetException e) {
			throw new DefinedException(e.getTargetException().toString());
		} catch (Exception e) {
			throw new DefinedException(object.getClass().getName() + " "
					+ action.getName() + " " + this.join(param, ",")
					+ " error!");
		}
	}

	private Object getObject() throws DefinedException {
		Object object = null;
		Method method = null;
		List<String> objectNames = re.pageOrObjectName(description);
		if ((objectNames == null || objectNames.get(0).equals(""))
				&& currentObject == null) {
			throw new DefinedException("Get object name failed from "
					+ description);
		}
		String objectName;
		if (objectNames != null)
			objectName = objectNames.get(0);
		else
			objectName = String.valueOf(
					currentObject.substring(0, 1).toLowerCase()).concat(
					currentObject.substring(1));
		String upOn = String.valueOf(objectName.substring(0, 1).toUpperCase())
				.concat(objectName.substring(1));
		if (objectName.equals("this"))
			return this;
		if (objectNames != null)
			currentObject = upOn;
		if (!ObjectManager.objectMap().keySet().contains(objectName)) {
			allPages.setPackagePath(Config.getConfig("rootPages"));
			allPages.loadAllPages(allPages.getPackagePath(), objectName);
			if (allPages.getReturnPath() != null)
				allPages.createInstance(allPages.getReturnPath(), objectName);
			else {
				ObjectManager.objectMap().put(objectName, new Page(upOn));
			}
		}
		if (ObjectManager.objectMap().keySet().contains(objectName)) {
			object = ObjectManager.objectMap().get(objectName);
			object = this.getWebElement(object, method);
		} else {
			throw new DefinedException(objectName + " object is not created!");
		}

		return object;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Object getWebElement(Object object, Method method)
			throws DefinedException {
		String element = null;
		Class[] c = null;
		Object[] args = null;
		try {
			if (re.hasElement(description)) {
				element = re.elementName(description).get(0);
				if (element.equals(""))
					throw new DefinedException("element can not be null in "
							+ description);
				String[] pa = null;
				if (re.isElementParameter(description)) {
					pa = (String[]) this.handleElementParamter(new Object[] {});
				}
				if (pa != null) {
					c = new Class[] { element.getClass(), pa.getClass() };
					args = new Object[] { element, pa };
				} else {
					c = new Class[] { element.getClass() };
					args = new Object[] { element };
				}
				Class clazz = object.getClass();
				String methodName = null;
				if (re.hadElement1(description))
					methodName = Keyword.getKeyword("getElement");
				if (re.hadElement2(description))
					methodName = Keyword.getKeyword("getElementNoWait");
				method = clazz.getMethod(methodName, c);
				object = method.invoke(object, args);
			}
		} catch (SecurityException e) {
			e.printStackTrace();
			throw new DefinedException("Get locator: " + element + " error!");
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
			throw new DefinedException("Get locator: " + element + " error!");
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			throw new DefinedException("Get locator: " + element + " error!");
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			throw new DefinedException("Get locator: " + element + " error!");
		} catch (InvocationTargetException e) {
			e.printStackTrace();
			throw new DefinedException("Get locator: " + element + " error!");
		} catch (Exception e) {
			e.printStackTrace();
			throw new DefinedException("Get locator: " + element + " error!");
		}
		return object;
	}

	public void addValueToParam(String key, String value) {
		this.param.put(key, value);
	}
}
