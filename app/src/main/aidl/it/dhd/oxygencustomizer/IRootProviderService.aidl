// IRootProviderService.aidl
package it.dhd.oxygencustomizer;

// Declare any non-default types here with import statements

interface IRootProviderService {
	boolean checkLSPosedDB(String packageName);
	boolean isPackageInstalled(String packageName);
	boolean activateInLSPosed(String packageName);
	IBinder getFileSystemService();
}