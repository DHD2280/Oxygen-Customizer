package it.dhd.oxygencustomizer.xposed.utils;

import static de.robv.android.xposed.XposedBridge.hookMethod;
import static de.robv.android.xposed.XposedBridge.log;
import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.findClassIfExists;

import android.annotation.SuppressLint;
import android.util.ArraySet;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.regex.Pattern;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/** @noinspection unused, RedundantThrows */
public class ReflectionTools {
	@NonNull
	public static Class<?> findAndDumpClass(String className, ClassLoader classLoader)
	{
		dumpClass(className, classLoader);
		return findClass(className, classLoader);
	}

	public static Class<?> findAndDumpClassIfExists(String className, ClassLoader classLoader)
	{
		dumpClass(className, classLoader);
		return findClassIfExists(className, classLoader);
	}

	public static Set<XC_MethodHook.Unhook> hookAllMethods(Class<?> clazz, String method, XC_MethodHook callback)
	{
		XC_MethodHook hook = new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
				if(true) {
					log(param.method.getName() + " called");
				}
				callMethod(callback, "beforeHookedMethod", param);
			}

			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				callMethod(callback, "afterHookedMethod", param);
			}
		};

		Set<XC_MethodHook.Unhook> result = XposedBridge.hookAllMethods(clazz, method, hook);
		if(true) {
			Throwable t = new Throwable();
			log(t.getStackTrace()[1].getClassName() + " " + t.getStackTrace()[1].getLineNumber() + " hook size " + result.size());
		}
		return result;
	}

	public static Set<XC_MethodHook.Unhook> hookAllConstructors(Class<?> clazz, XC_MethodHook callback)
	{
		XC_MethodHook hook = new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
				if(true) {
					log(param.method.getName() + " called");
				}
				callMethod(callback, "beforeHookedMethod", param);
			}

			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				callMethod(callback, "afterHookedMethod", param);
			}
		};


		Set<XC_MethodHook.Unhook> result = XposedBridge.hookAllConstructors(clazz, hook);
		if(true) {
			Throwable t = new Throwable();
			log(t.getStackTrace()[1].getClassName() + " " + t.getStackTrace()[1].getLineNumber() + " hook size " + result.size());
		}
		return result;
	}

	public static void runDelayedOnMainThread(View viewObject, long delay, Runnable runnable)
	{
		new Thread(() -> {
			try
			{
				Thread.sleep(delay);
				viewObject.post(runnable);
			}
			catch (Throwable ignored)
			{}
		}).start();
	}

	public static void reAddView(ViewGroup parentView, View childView) {
		if(childView == null) return;

		try {
			((ViewGroup)childView.getParent()).removeView(childView);
		} catch (Throwable ignored) {}
		parentView.addView(childView);
	}
	public static void reAddView(ViewGroup parentView, View childView, int index)
	{
		if(childView == null) return;

		try {
			((ViewGroup)childView.getParent()).removeView(childView);
		} catch (Throwable ignored) {}
		parentView.addView(childView, index);
	}


	public static Set<XC_MethodHook.Unhook> hookAllMethodsMatchPattern(Class<?> clazz, String namePatter, XC_MethodHook callback)
	{
		Set<XC_MethodHook.Unhook> result = new ArraySet<>();

		for(Method method : findMethods(clazz, namePatter))
		{
			result.add(hookMethod(method, callback));
		}
		return result;
	}

	public static Set<Method> findMethods(Class<?> clazz, String namePattern)
	{
		Set<Method> result = new ArraySet<>();

		Method[] methods = clazz.getMethods();

		for(Method method : methods)
		{
			if(Pattern.matches(namePattern, method.getName()))
			{
				result.add(method);
			}
		}
		return result;
	}

	public static Method findMethod(Class<?> clazz, String namePattern)
	{
		Method[] methods = clazz.getMethods();

		for(Method method : methods)
		{
			if(Pattern.matches(namePattern, method.getName()))
			{
				return method;
			}
		}
		return null;
	}

	@SuppressWarnings("unused")
	public static void dumpClass(String className, ClassLoader classLoader)
	{
		Class<?> ourClass = findClassIfExists(className, classLoader);
		if (ourClass == null) {
			log("Class: " + className + " not found");
			return;
		}
		dumpClass(ourClass);
	}

	public static void dumpClass(Class<?> ourClass) {
		Method[] ms = ourClass.getDeclaredMethods();
		log("ReflectionTools - Class: " + ourClass.getName());
		log("ReflectionTools - extends: " + ourClass.getSuperclass().getName());
		log("ReflectionTools - Subclasses:");
		Class<?>[] scs = ourClass.getClasses();
		for(Class <?> c : scs)
		{
			log(c.getName());
		}
		log("ReflectionTools - Methods:");

		Constructor<?>[] cons = ourClass.getDeclaredConstructors();
		for (Constructor<?> m : cons) {
			log(m.getName() + " - " + " - " + m.getParameterCount());
			Class<?>[] cs = m.getParameterTypes();
			for (Class<?> c : cs) {
				log("\t\t" + c.getTypeName());
			}
		}


		for (Method m : ms) {
			log(m.getName() + " - " + m.getReturnType() + " - " + m.getParameterCount());
			Class<?>[] cs = m.getParameterTypes();
			for (Class<?> c : cs) {
				log("\t\t" + c.getTypeName());
			}
		}
		log("ReflectionTools - Fields:");

		Field[] fs = ourClass.getDeclaredFields();
		for (Field f : fs) {
			log("\t\t" + f.getName() + "-" + f.getType().getName());
		}
		log("ReflectionTools - End dump");
	}

	public static void tryHookAllMethods(Class<?> clazz, String method, XC_MethodHook hook) {
		try {
			XposedBridge.hookAllMethods(clazz, method, hook);
		} catch (Throwable ignored) {
		}
	}

	public static void tryHookAllConstructors(Class<?> clazz, XC_MethodHook hook) {
		try {
			XposedBridge.hookAllConstructors(clazz, hook);
		} catch (Throwable ignored) {
		}
	}

	public static void dumpView(View view) {
		StringBuilder result = new StringBuilder();
		dumpView(view, result, "");
		log("Dump View :\n" + result.toString());
	}

	private static void dumpView(View view, StringBuilder result, String prefix) {
		String className = view.getClass().getName();

		String baseClass = getBaseClass(view);

		result.append(prefix)
				.append(className);

		if (baseClass != null) {
			result.append(" (extends ").append(baseClass).append(")");
		}

		result.append(" ").append(getViewDetails(view)).append("\n");

		if (view instanceof ViewGroup viewGroup) {
            result.append(" [children: ").append(viewGroup.getChildCount()).append("]\n");
			for (int i = 0; i < viewGroup.getChildCount(); i++) {
				dumpView(viewGroup.getChildAt(i), result, prefix + "  ");
			}
		} else {
			result.append("\n");
		}
	}

	@SuppressLint("DefaultLocale")
    private static String getViewDetails(View view) {
		ViewGroup.LayoutParams params = view.getLayoutParams();

		String width = getSizeDescription(params != null ? params.width : -1);
		String height = getSizeDescription(params != null ? params.height : -1);

		int paddingLeft = view.getPaddingLeft();
		int paddingRight = view.getPaddingRight();
		int paddingTop = view.getPaddingTop();
		int paddingBottom = view.getPaddingBottom();

		String marginDetails = "";
		if (params instanceof ViewGroup.MarginLayoutParams marginParams) {
            marginDetails = String.format(
					"Margins [L:%d, T:%d, R:%d, B:%d]",
					marginParams.leftMargin,
					marginParams.topMargin,
					marginParams.rightMargin,
					marginParams.bottomMargin
			);
		}

		return String.format(
				"Size [W:%s, H:%s], Padding [L:%d, T:%d, R:%d, B:%d]%s",
				width, height, paddingLeft, paddingTop, paddingRight, paddingBottom,
				marginDetails.isEmpty() ? "" : ", " + marginDetails
		);
	}

	private static String getSizeDescription(int size) {
        return switch (size) {
            case ViewGroup.LayoutParams.MATCH_PARENT -> "MATCH_PARENT";
            case ViewGroup.LayoutParams.WRAP_CONTENT -> "WRAP_CONTENT";
            default -> size >= 0 ? size + "dp" : "UNKNOWN";
        };
	}

	private static String getBaseClass(View view) {
		Class<?> currentClass = view.getClass();
		while (currentClass != null) {
			currentClass = currentClass.getSuperclass();
			if (currentClass != null && isAndroidViewClass(currentClass)) {
				return currentClass.getName();
			}
		}
		return null;
	}

	private static boolean isAndroidViewClass(Class<?> clazz) {
		return clazz.getName().startsWith("android.view.") ||
				clazz.getName().startsWith("android.widget.") ||
				clazz.getName().startsWith("android.webkit.");
	}

	public static void dumpParentIDs(View v)
	{
		dumpParentIDs(v, 0);
	}

	private static void dumpParentIDs(View v, int level) {
		dumpID(v, level);
		try {
			if(v.getParent() instanceof View)
			{
				dumpParentIDs((View) v.getParent(), level+1);
			}
		}
		catch (Throwable ignored){}
	}

	public static void dumpIDs(View v)
	{
		dumpIDs(v, 0);
	}

	private static void dumpIDs(View v, int level)
	{
		dumpID(v, level);
		if(v instanceof ViewGroup)
		{
			for(int i = 0; i < ((ViewGroup) v).getChildCount(); i++)
			{
				dumpIDs(((ViewGroup) v).getChildAt(i), level+1);
			}
		}
	}
	private static void dumpID(View v, int level)
	{
		String name = "**";
		StringBuilder str = new StringBuilder();
		for(int i = 0; i < level; i++)
		{
			str.append("\t");
		}

		try {
			name = v.getContext().getResources().getResourceName(v.getId());
		}
		catch (Throwable ignored){}

		log(str+ "id " + name + " type " + v.getClass().getName());
	}

	public static boolean isMethodAvailable(
			Object target,
			String methodName,
			Class<?>... parameterTypes
	) {
		if (target == null) return false;

		try {
			target.getClass().getMethod(methodName, parameterTypes);
			return true;
		} catch (NoSuchMethodException e) {
			return false;
		}
	}

	public static Class<?> findClassInArray(
			XC_LoadPackage.LoadPackageParam lpparam,
			String...classes
	) {
		for(String clazz : classes) {
			try {
				Class<?> mClazz = findClass(clazz, lpparam.classLoader);
				if (mClazz != null) {
					return mClazz;
				}
			} catch (Throwable ignored) {}
		}
		return null;
	}

}
