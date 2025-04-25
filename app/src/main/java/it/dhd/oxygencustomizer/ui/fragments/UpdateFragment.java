package it.dhd.oxygencustomizer.ui.fragments;

import static android.content.Context.RECEIVER_EXPORTED;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileUtils;
import android.util.JsonReader;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.topjohnwu.superuser.Shell;

import net.lingala.zip4j.util.UnzipUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import javax.security.auth.callback.Callback;

import br.tiagohm.markdownview.css.InternalStyleSheet;
import br.tiagohm.markdownview.css.styles.Github;
import it.dhd.oxygencustomizer.BuildConfig;
import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.databinding.FragmentUpdatesBinding;
import it.dhd.oxygencustomizer.ui.activity.MainActivity;
import it.dhd.oxygencustomizer.ui.base.BaseFragment;
import it.dhd.oxygencustomizer.ui.dialogs.LoadingDialog;
import it.dhd.oxygencustomizer.utils.RootUtil;
import it.dhd.oxygencustomizer.utils.ThemeUtils;

public class UpdateFragment extends BaseFragment {

    public static final String MOD_NAME = "OxygenCustomizer";
    public static final String UPDATES_CHANNEL_ID = "Updates";

    // Links
    private static final String commitURL = "https://github.com/DHD2280/Oxygen-Customizer/commits/beta/";
    private static final String stableUpdatesURL = "https://raw.githubusercontent.com/DHD2280/Oxygen-Customizer/stable/latestStable.json";
    private static final String betaUpdatesURL = "https://raw.githubusercontent.com/DHD2280/Oxygen-Customizer/beta/latestBeta.json";
    private static final String nightlyUpdatesURL = "https://raw.githubusercontent.com/DHD2280/Oxygen-Customizer/nightly-versioning/latestNightly.json";
    private static final String NIGHTLY_LINK = "https://nightly.link/DHD2280/Oxygen-Customizer/actions/runs/%s";
    private static final String NIGHTLY_DOWNLOAD = "https://nightly.link/DHD2280/Oxygen-Customizer/actions/runs/%s/Oxygen%%20Customizer%%20nightly-202%%20Dev%%20(%%23%s).zip";

    // Flavor
    public static enum Flavor {
        STABLE,
        BETA,
        NIGHTLY,
        ALL
    }

    private Flavor mCurrentFlavor = Flavor.ALL;
    private boolean mNightlyDownloaded = false;
    DownloadManager downloadManager;
    long downloadID = 0; //from download manager
    static boolean betaUpdate = BuildConfig.VERSION_NAME.toLowerCase().contains("beta");
    static boolean nightlyUpdate = BuildConfig.VERSION_NAME.toLowerCase().contains("nightly");
    HashMap<String, Object> latestVersion = null;
    private String downloadedFilePath;
    private LoadingDialog mLoadingDialog;

    final BroadcastReceiver downloadCompletionReceiver = new BroadcastReceiver() {

        @SuppressLint("MissingPermission")
        @Override
        public void onReceive(Context context, Intent intent) {
            if (getContext() != null)
                getContext().unregisterReceiver(downloadCompletionReceiver);


            boolean successful = false;
            if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(intent.getAction()) && intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1) == downloadID) {
                try (Cursor downloadData = downloadManager.query(
                        new DownloadManager.Query()
                                .setFilterById(downloadID))) {
                    downloadData.moveToFirst();

                    int uriColIndex = downloadData.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI);

                    File downloadedFile = new File(URI.create(downloadData.getString(uriColIndex)));

                    if (downloadedFile.exists()) {
                        downloadedFilePath = new File(URI.create(downloadData.getString(uriColIndex))).getAbsolutePath();

                        notifyInstall();
                        successful = true;
                    }
                } catch (Throwable ignored) {
                }
            }

            if (!successful) {
                NotificationCompat.Builder builder = new NotificationCompat.Builder(requireContext(), UPDATES_CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_notification_foreground)
                        .setContentTitle(requireContext().getText(R.string.download_failed))
                        .setContentText(requireContext().getText(R.string.try_again_later))
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT);

                NotificationManagerCompat.from(requireContext()).notify(2, builder.build());
            }
        }
    };
    private FragmentUpdatesBinding binding;
    private int currentVersionCode = -1;
    private int currentVersionType = -1;
    private String currentVersionName = "";
    private String currentNightlyVersion = "";
    private boolean rebootPending = false;
    //	private boolean downloadStarted = false;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        //noinspection ConstantConditions
        downloadManager = (DownloadManager) requireContext().getSystemService(Context.DOWNLOAD_SERVICE);

        //finally
        binding = FragmentUpdatesBinding.inflate(inflater, container, false);

        mLoadingDialog = new LoadingDialog(requireContext());

        if (getArguments() != null && getArguments().getBoolean("updateTapped", false)) {
            String downloadPath = getArguments().getString("filePath");
            boolean isNightly = getArguments().getBoolean("isNightly", false);
            if (isNightly) {
                requireActivity().runOnUiThread(() -> mLoadingDialog.show(getString(R.string.loading_dialog_wait)));
                installNightly(downloadPath);
            } else {
                installApk(downloadPath);
            }
        }

        return binding.getRoot();
    }

    @Override
    public String getTitle() {
        return getString(R.string.update);
    }

    @Override
    public boolean backButtonEnabled() {
        return true;
    }

    private void installNightly(String downloadPath) {
        unzip(downloadPath, apkFile -> {
            requireActivity().runOnUiThread(() -> mLoadingDialog.dismiss());
            installApk(apkFile.getPath());
        });
    }

    private void installApk(String downloadPath) {
        Intent promptInstall = new Intent(Intent.ACTION_VIEW).setDataAndType(
                FileProvider.getUriForFile(getContext(), BuildConfig.APPLICATION_ID + ".fileprovider", new File(downloadPath)),
                "application/vnd.android.package-archive");
        promptInstall.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        promptInstall.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        getContext().startActivity(promptInstall);
    }

    public void unzip(String fileName, UnZipCallback callback) {
        File unzippedFile = null;
        File fileToUnzip = new File(fileName);
        try {

            //unzip once, IF double zipped
            ZipFile unzipper = new ZipFile(fileToUnzip);
            File parentDirectory = fileToUnzip.getParentFile();
            String fileNameWithoutExtension = fileToUnzip.getName().substring(0, fileToUnzip.getName().lastIndexOf('.'));
            unzippedFile = new File(parentDirectory, fileNameWithoutExtension + ".apk");

            if (unzipper.stream().count() == 1) {
                try (FileOutputStream unzipOutputStream = new FileOutputStream(unzippedFile)) {
                    FileUtils.copy(unzipper.getInputStream(unzipper.entries().nextElement()), unzipOutputStream);
                }
            } else {
                unzippedFile = new File(fileName);
            }
        } catch (Exception e) {
            Log.e("UpdateFragment", "zip install error: ", e);
        }
        callback.onFinished(unzippedFile);
    }

    public interface UnZipCallback extends Callback {
        void onFinished(File apkFile);
    }

    public String intToHex(int colorValue) {
        return String.format("#%06X", (0xFFFFFF & colorValue));
    }

    public @ColorInt int getColorFromAttribute(Context context, int attr) {
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(attr, typedValue, true);
        return typedValue.data;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //Android 13 requires notification permission to be granted or it won't allow it
        Shell.cmd(String.format("pm grant %s android.permission.POST_NOTIFICATIONS", BuildConfig.APPLICATION_ID)).exec(); //will ask root if not granted yet

        if (!RootUtil.isDeviceRooted()) {
            currentVersionName = getString(R.string.root_not_here);
            currentVersionType = -1;
            currentVersionCode = 9999;
        } else {
            getCurrentVersion();
        }

        String pendingRebootString = (rebootPending) ? " - " + getString(R.string.reboot_pending) : "";
        ((TextView) view.findViewById(R.id.currentVersionValueID)).setText(String.format("%s (%s)%s", currentVersionName, currentVersionCode, pendingRebootString));

        if (rebootPending) {
            binding.updateBtn.setEnabled(true);
            binding.updateBtn.setText(R.string.reboot_word);
        }

        @SuppressLint("NonConstantResourceId") RadioGroup.OnCheckedChangeListener onCheckChangedListener = (radioGroup, i) -> {
            mCurrentFlavor = switch (i) {
                case R.id.stableID -> Flavor.STABLE;
                case R.id.betaID -> Flavor.BETA;
                case R.id.nightlyID -> Flavor.NIGHTLY;
                default -> Flavor.ALL;
            };
            betaUpdate = ((RadioButton) radioGroup.findViewById(R.id.betaID)).isChecked();
            nightlyUpdate = ((RadioButton) radioGroup.findViewById(R.id.nightlyID)).isChecked();
            ((TextView) view.findViewById(R.id.latestVersionValueID)).setText(R.string.update_checking);
            binding.updateBtn.setEnabled(rebootPending);

            checkUpdates(mCurrentFlavor, result -> {
                latestVersion = result;

                if (getActivity() != null) {
                    requireActivity().runOnUiThread(() -> {
                        try {
                            InternalStyleSheet css = new Github();
                            css.addRule("body, kbd", "background-color: " + intToHex(ThemeUtils.getAttrColor(requireContext(), R.attr.preferenceBackgroundColor)));
                            css.addRule("body, p, h1, h2, h3, h4, h5, h6, span, div", "color: " + intToHex(ContextCompat.getColor(requireContext(), R.color.textColorPrimary)));
                            css.addRule("kbd", "border-color: " + intToHex(ThemeUtils.getAttrColor(requireContext(), R.attr.preferenceBackgroundColor)));
                            css.addRule("kbd", "color: " + intToHex(ContextCompat.getColor(requireContext(), R.color.textColorPrimary)));
                            css.addRule("a", "color: " + intToHex(getColorFromAttribute(requireContext(), R.attr.colorPrimary)));
                            binding.changelogView.addStyleSheet(css);
                            binding.changelogView.loadMarkdownFromUrl((String) result.get("changelog"));
                            if (mCurrentFlavor == Flavor.NIGHTLY) {
                                binding.webView.loadUrl(commitURL);
                                binding.changelogView.setVisibility(View.GONE);
                                binding.webView.setVisibility(View.VISIBLE);
                            } else {
                                binding.changelogView.setVisibility(View.VISIBLE);
                                binding.webView.setVisibility(View.GONE);
                            }
                        } catch (Throwable ignored) {
                        }
                    });
                }

                if (getActivity() != null) {
                    requireActivity().runOnUiThread(() -> {
                        ((TextView) view.findViewById(R.id.latestVersionValueID)).setText(
                                String.format("%s (%s)", result.get("version"),
                                        result.get("versionCode")));
                        int latestCode;
                        int BtnText = R.string.update_word;

                        boolean enable = false;
                        try {
                            //noinspection ConstantConditions
                            latestCode = (int) result.get("versionCode");

                            if (rebootPending) {
                                enable = true;
                                BtnText = R.string.reboot_word;
                            } else if (!betaUpdate) //stable selected
                            {
                                if (currentVersionName.contains("-")) //currently beta installed
                                {
                                    BtnText = R.string.switch_branches;
                                } else if (latestCode == currentVersionCode) //already up to date
                                {
                                    BtnText = R.string.reinstall_word;
                                }
                                enable = true; //stable version is ALWAYS flashable, so that user can revert from beta or repair installation
                            } else {
                                if (latestCode > currentVersionCode || (currentVersionType == 1)) {
                                    enable = true;
                                }
                            }
                        } catch (Exception ignored) {
                        }
                        view.findViewById(R.id.updateBtn).setEnabled(enable);
                        ((Button) view.findViewById(R.id.updateBtn)).setText(BtnText);
                    });
                }
            });
        };

        binding.updateChannelRadioGroup.setOnCheckedChangeListener(onCheckChangedListener);

        binding.updateBtn.setOnClickListener(view1 -> {
            if (rebootPending) {
                Shell.cmd("reboot");
            } else {
                String zipURL = (String) latestVersion.get("apkUrl");
                if (zipURL == null) zipURL = (String) latestVersion.get("apkUrl");
                if (mCurrentFlavor == Flavor.NIGHTLY) {
                    mNightlyDownloaded = true;
                    zipURL = String.format(
                            NIGHTLY_DOWNLOAD,
                            latestVersion.get("actionRun"),
                            latestVersion.get("devBuild"));
                }

                //noinspection ConstantConditions
                startDownload(zipURL, latestVersion);
                binding.updateBtn.setEnabled(false);
//				downloadStarted = true;
                binding.updateBtn.setText(R.string.update_download_started);
            }
        });

        if (currentVersionName.toLowerCase().contains("nightly")) {
            mCurrentFlavor = Flavor.NIGHTLY;
            ((RadioButton) view.findViewById(R.id.nightlyID)).setChecked(true);
        } else if (currentVersionName.toLowerCase().contains("beta")) {
            mCurrentFlavor = Flavor.BETA;
            ((RadioButton) view.findViewById(R.id.betaID)).setChecked(true);
        } else {
            mCurrentFlavor = Flavor.STABLE;
            ((RadioButton) view.findViewById(R.id.stableID)).setChecked(true);
        }
    }

/*    private void getChangelog(String URL, TaskDoneCallback callback) {
        new ChangelogReceiver(URL, callback).start();
    }*/

    private void getCurrentVersion() {
        rebootPending = false;
        currentVersionName = BuildConfig.VERSION_NAME;
        currentVersionCode = BuildConfig.VERSION_CODE;
        if (currentVersionName.contains("nightly")) {
            currentNightlyVersion = currentVersionName.substring(currentVersionName.indexOf("#") + 1, currentVersionName.lastIndexOf(")"));
        }
    }

    public void checkUpdates(Flavor flavor, TaskDoneCallback callback) {
        new updateChecker(callback, flavor).start();
    }

    public void startDownload(String zipURL, HashMap<String, Object> versionInfo) {
        IntentFilter filters = new IntentFilter();
        filters.addAction(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        filters.addAction(DownloadManager.ACTION_NOTIFICATION_CLICKED);

        String fileName = "OxygenCustomizer-%s.%s";
        downloadID = downloadManager.enqueue(new DownloadManager.Request(Uri.parse(zipURL))
                .setTitle("Oxygen Customizer Update Package")
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,
                        String.format(fileName,
                                mNightlyDownloaded ?
                                        "nightly-" + versionInfo.get("versionCode") + "-" + versionInfo.get("devBuild") :
                                        versionInfo.get("version") + "-" + versionInfo.get("versionCode"),
                                mNightlyDownloaded ? "zip" : "apk"))
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE));

        //noinspection ConstantConditions
        if (getContext() != null) {
            getContext().registerReceiver(downloadCompletionReceiver, filters, RECEIVER_EXPORTED);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @SuppressLint("MissingPermission")
    public void notifyInstall() {
        if (getContext() == null) {
            Log.w("UpdateFragment", "notifyInstall: context is null");
            return;
        }

        Intent notificationIntent = new Intent(getContext(), MainActivity.class);
        notificationIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        notificationIntent.setAction(Intent.ACTION_RUN);
        notificationIntent.addCategory(Intent.CATEGORY_DEFAULT);
        notificationIntent.putExtra("updateTapped", true);
        notificationIntent.putExtra("filePath", downloadedFilePath);
        notificationIntent.putExtra("isNightly", mNightlyDownloaded);

        PendingIntent pendingIntent = PendingIntent.getActivity(getContext(), 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        //noinspection ConstantConditions
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getContext(), UPDATES_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification_foreground)
                .setContentTitle(requireContext().getString(R.string.update_notification_title))
                .setContentText(requireContext().getString(R.string.update_notification_text))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat.from(getContext()).notify(1, builder.build());
    }

    public interface TaskDoneCallback extends Callback {
        void onFinished(HashMap<String, Object> result);
    }

    public static class updateChecker extends Thread {
        private final TaskDoneCallback mCallback;
        private final Flavor mFlavor;

        public updateChecker(TaskDoneCallback callback, Flavor flavor) {
            mCallback = callback;
            mFlavor = flavor;
        }

        @Override
        public void run() {
            try {
                Thread.sleep(200); //waiting for betaUpdate variable to initialize
                HashMap<String, Object> best;
                // Check all updates, stable, beta and nightly so later we will ensure that updater
                // will always have the latest version available
                if (mFlavor == Flavor.ALL) {
                    HashMap<String, Object> stable = loadVersionInfoFromUrl(stableUpdatesURL);
                    HashMap<String, Object> beta = loadVersionInfoFromUrl(betaUpdatesURL);
                    HashMap<String, Object> nightly = loadVersionInfoFromUrl(nightlyUpdatesURL);

                    best = stable;
                    int bestCode = (int) stable.get("versionCode");

                    if (beta != null && (int) beta.get("versionCode") > bestCode) {
                        best = beta;
                        bestCode = (int) beta.get("versionCode");
                    }
                    if (nightly != null && (int) nightly.get("versionCode") >= bestCode) {
                        best = nightly;
                    }
                } else {
                    best = loadVersionInfoFromUrl(
                            mFlavor == Flavor.NIGHTLY ?
                                    nightlyUpdatesURL :
                                    (mFlavor == Flavor.BETA ? betaUpdatesURL : stableUpdatesURL));
                }

                mCallback.onFinished(best);
            } catch (Exception e) {
                HashMap<String, Object> error = new HashMap<>();
                error.put("version", "Connection Error");
                error.put("versionCode", -1);
                mCallback.onFinished(error);
                Log.e("UpdateFragment", "updateChecker: ", e);
            }
        }

        private HashMap<String, Object> loadVersionInfoFromUrl(String urlString) {
            try {
                URL url = new URL(urlString);
                InputStream s = url.openStream();
                InputStreamReader r = new InputStreamReader(s);
                JsonReader jsonReader = new JsonReader(r);

                HashMap<String, Object> versionInfo = new HashMap<>();
                jsonReader.beginObject();
                while (jsonReader.hasNext()) {
                    String name = jsonReader.nextName();
                    switch (name) {
                        case "actionRun":
                            versionInfo.put(name, jsonReader.nextLong());
                            break;
                        case "versionCode":
                        case "devBuild":
                            versionInfo.put(name, jsonReader.nextInt());
                            break;
                        case "apkUrl":
                        case "version":
                        case "changelog":
                        default:
                            versionInfo.put(name, jsonReader.nextString());
                            break;
                    }
                }
                jsonReader.endObject();
                jsonReader.close();
                Log.d("UpdateChecker", "Loaded version info: " + versionInfo + " from: " + urlString);
                return versionInfo;
            } catch (Exception e) {
                Log.e("UpdateChecker", "Failed to load version info from: " + urlString, e);
                return null;
            }
        }

    }
}
