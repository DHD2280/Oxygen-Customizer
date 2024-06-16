// IRootProviderProxy.aidl
package it.dhd.oxygencustomizer;

// Declare any non-default types here with import statements

interface IRootProviderProxy {
	/**
	 * Demonstrates some basic types that you can use as parameters
	 * and return values in AIDL.
	 */
	String[] runCommand(String command);
	void extractSubject(in Bitmap input, String resultPath);
	void applyTheme(in String theme);
}