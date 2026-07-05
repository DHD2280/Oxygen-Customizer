package it.dhd.oxygencustomizer.xposed.hooks.launcher;

import static de.robv.android.xposed.XposedBridge.hookAllConstructors;
import static de.robv.android.xposed.XposedBridge.hookAllMethods;
import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.getBooleanField;
import static de.robv.android.xposed.XposedHelpers.getObjectField;
import static de.robv.android.xposed.XposedHelpers.getStaticObjectField;
import static de.robv.android.xposed.XposedHelpers.setBooleanField;
import static de.robv.android.xposed.XposedHelpers.setObjectField;
import static it.dhd.oxygencustomizer.utils.Constants.ACTIONS_OVERRIDE_BACK;
import static it.dhd.oxygencustomizer.utils.Constants.ACTIONS_SWITCH_APP;
import static it.dhd.oxygencustomizer.utils.Constants.ACTIONS_TOGGLE_ONE_HANDED;
import static it.dhd.oxygencustomizer.utils.Constants.ACTIONS_TOGGLE_PANEL;
import static it.dhd.oxygencustomizer.utils.Constants.ACTION_INTENT_SCREENSHOT;
import static it.dhd.oxygencustomizer.utils.Constants.ACTION_INTENT_SCREENSHOT_PARTIAL;
import static it.dhd.oxygencustomizer.utils.Constants.ACTION_INTENT_SCREENSHOT_SCROLL;
import static it.dhd.oxygencustomizer.utils.Constants.Packages.LAUNCHER;
import static it.dhd.oxygencustomizer.xposed.XPrefs.Xprefs;
import static it.dhd.oxygencustomizer.xposed.utils.SystemUtils.goToSleep;
import static it.dhd.oxygencustomizer.xposed.utils.SystemUtils.openQs;
import static it.dhd.oxygencustomizer.xposed.utils.SystemUtils.takeScreenshot;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Build;
import android.os.Handler;
import android.os.Process;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.widget.Toast;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Optional;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import it.dhd.oxygencustomizer.utils.AppUtils;
import it.dhd.oxygencustomizer.xposed.XposedMods;
import it.dhd.oxygencustomizer.xposed.utils.ScreenshotUtils;
import it.dhd.oxygencustomizer.xposed.utils.SystemUtils;
import it.dhd.oxygencustomizer.xposed.utils.toolkit.ReflectedClass;

/** @noinspection ConstantValue*/
@SuppressWarnings("RedundantThrows")
public class CustomNavGestures extends XposedMods {
	private static final String listenPackage = LAUNCHER;

	private static final int NO_ACTION = -1;
	private static final int ACTION_SCREENSHOT = 1;
	private static final int ACTION_OPEN_QUICK_SETTINGS = 2;
	private static final int ACTION_KILL_APP = 3;
	private static final int ACTION_NOTIFICATION = 4;
	private static final int ACTION_ONE_HANDED = 5;
	private static final int ACTION_SLEEP = 6;
	private static final int ACTION_SWITCH_APP = 7;
	private static final int ACTION_SCREENSHOT_PARTIAL = 8;
	private static final int ACTION_SCREENSHOT_SCROLL = 9;
	private static final int ACTION_CIRCLE_TO_SEARCH = 10;

	private static final int SWIPE_NONE = 0;
	private static final int SWIPE_LEFT = 1;
	private static final int SWIPE_RIGHT = 2;
	private static final int SWIPE_TWO_FINGER = 3;

	private boolean FCHandled = false;
	private float leftSwipeUpPercentage = 0.25f, rightSwipeUpPercentage = 0.25f;
	private int displayW = -1, displayH = -1;
	private static float swipeUpPercentage = 0.2f;
	private int swipeType = SWIPE_NONE;

	@SuppressWarnings("FieldCanBeLocal")
	private boolean isLandscape = false;
	private float mSwipeUpThreshold = 0;
	private float mLongThreshold = 0;
	private static boolean FCLongSwipeEnabled = false;
	private static boolean mCircleToSearch = false;
	private Object mSystemUIProxy = null;
	private static int leftSwipeUpAction = NO_ACTION, rightSwipeUpAction = NO_ACTION, twoFingerSwipeUpAction = NO_ACTION;
	private Object mSysUiProxy;
	private Object currentFocusedTask = null;
	private Class<?> OplusInputInterceptHelper = null;
	private boolean mOverrideBack = false;
	private int overrideMode = 0;
	private int overrideLeft = 0;
	private int overrideRight = 0;

	private static final long LONG_PRESS_THRESHOLD = 500;
	private final int TOUCH_SLOP = ViewConfiguration.get(mContext).getScaledTouchSlop();
	private int mInitialTouchX;
	private boolean isLongPress = false;
	private final Handler longPressHandler = new Handler();
	private final Runnable longPressRunnable = () -> {
        isLongPress = true;
        if (isLongPress && mCircleToSearch) AppUtils.circleToSearch();
    };

	private boolean mBroadcastRegistered = false;

	private final BroadcastReceiver mSystemUiReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			switch (intent.getAction()) {
				case ACTIONS_TOGGLE_ONE_HANDED:
					startOneHandedMode();
					break;
				case ACTIONS_TOGGLE_PANEL:
					toggleNotification();
					break;
				case ACTION_INTENT_SCREENSHOT:
					takeScreenshot(ScreenshotUtils.ScreenshotType.FULL);
					break;
				case ACTION_INTENT_SCREENSHOT_PARTIAL:
					takeScreenshot(ScreenshotUtils.ScreenshotType.PARTIAL);
					break;
				case ACTION_INTENT_SCREENSHOT_SCROLL:
					takeScreenshot(ScreenshotUtils.ScreenshotType.SCROLL);
					break;
			}
		}
	};

	public CustomNavGestures(Context context) {
		super(context);
	}

	@Override
	public void updatePrefs(String... Key) {
		FCLongSwipeEnabled = Xprefs.getBoolean("FCLongSwipeEnabled", false);
		leftSwipeUpAction = readAction(Xprefs, "leftSwipeUpAction");
		rightSwipeUpAction = readAction(Xprefs, "rightSwipeUpAction");
		twoFingerSwipeUpAction = readAction(Xprefs, "twoFingerSwipeUpAction");
		leftSwipeUpPercentage = Xprefs.getSliderFloat( "leftSwipeUpPercentage", 25f) / 100f;
		rightSwipeUpPercentage = Xprefs.getSliderFloat( "rightSwipeUpPercentage", 25f) / 100f;
		swipeUpPercentage = Xprefs.getSliderFloat( "swipeUpPercentage", 25f) / 100f;
		mCircleToSearch = Xprefs.getBoolean("circleToSearchEnabled", false);
		mOverrideBack = Xprefs.getBoolean("gesture_override_holdback", false);
		overrideMode = Integer.parseInt(Xprefs.getString("gesture_override_holdback_mode", "0"));
		overrideLeft = Integer.parseInt(Xprefs.getString("gesture_override_holdback_left", "0"));
		overrideRight = Integer.parseInt(Xprefs.getString("gesture_override_holdback_right", "0"));
	}

	private static int readAction(SharedPreferences xprefs, String prefName) {
		try {
			return Integer.parseInt(xprefs.getString(prefName, "").trim());
		} catch (Exception ignored) {
			return NO_ACTION;
		}
	}

	@Override
	public boolean listensTo(String packageName) {
		return listenPackage.equals(packageName);
	}

	@Override
	public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpParam) throws Throwable {

		if (!mBroadcastRegistered) {
			IntentFilter filter = new IntentFilter();
			filter.addAction(ACTIONS_TOGGLE_PANEL);
			filter.addAction(ACTIONS_TOGGLE_ONE_HANDED);
			filter.addAction(ACTION_INTENT_SCREENSHOT);
			filter.addAction(ACTION_INTENT_SCREENSHOT_PARTIAL);
			filter.addAction(ACTION_INTENT_SCREENSHOT_SCROLL);
			mContext.registerReceiver(mSystemUiReceiver, filter, Context.RECEIVER_EXPORTED);
			mBroadcastRegistered = true;
		}

		OplusInputInterceptHelper = findClass("com.oplus.quickstep.gesture.helper.OplusInputInterceptHelper", lpParam.classLoader);
		Class<?> OtherActivityInputConsumerClass = findClass("com.android.quickstep.inputconsumers.OtherActivityInputConsumer", lpParam.classLoader); //When apps are open
		Class<?> OplusOverviewInputConsumerImpl = findClass("com.android.quickstep.inputconsumers.OplusOverviewInputConsumerImpl", lpParam.classLoader); //When on Home screen and Recents
		Class<?> SystemUiProxyClass = findClass("com.android.quickstep.SystemUiProxy", lpParam.classLoader);
		Class<?> RecentTasksListClass = findClass("com.android.quickstep.RecentTasksList", lpParam.classLoader);

		Rect displayBounds = SystemUtils.WindowManager().getMaximumWindowMetrics().getBounds();
		displayW = Math.min(displayBounds.width(), displayBounds.height());
		displayH = Math.max(displayBounds.width(), displayBounds.height());

		hookAllConstructors(RecentTasksListClass, new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				mSysUiProxy = getObjectField(param.thisObject, "mSysUiProxy");
			}
		});

		hookAllConstructors(SystemUiProxyClass, new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				mSystemUIProxy = param.thisObject;
			}
		});

		hookAllMethods(OtherActivityInputConsumerClass, "onMotionEvent", new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
				onMotionEvent(param, false);
			}
		});

		hookAllMethods(OplusOverviewInputConsumerImpl, "onMotionEvent", new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
				onMotionEvent(param, true);
			}
		});

		ReflectedClass OplusAbsOverviewProxyImpl = ReflectedClass.of("com.oplus.quickstep.proxy.OplusAbsOverviewProxyImpl");
		if (OplusAbsOverviewProxyImpl.getClazz() != null) {
			OplusAbsOverviewProxyImpl
				.before("switchPreApp")
					.run(param -> {
						if (!mOverrideBack) return;

						int side = (int) param.args[0];
						if (overrideMode == 0 && overrideLeft == 0) return;
						else if (overrideMode == 1) {
							if (side == 0 && overrideLeft == 0) return;
							else if (side == 1 && overrideRight == 0) return;
						}
						overrideBack(side);
						param.setResult(null);
					});
		}

	}

	private void onMotionEvent(XC_MethodHook.MethodHookParam param, boolean isOverViewListener) {
		MotionEvent e = (MotionEvent) param.args[0];

		boolean mPassedWindowMoveSlop = isOverViewListener //if it's overview page (read: home page) we don't need this. true is good
				|| getBooleanField(param.thisObject, "mPassedWindowMoveSlop"); //checking if they've swiped long enough to cancel touch for app

		int action = e.getActionMasked();
		final int x = (int) e.getRawX();
		int pointers = e.getPointerCount();

		if (action == MotionEvent.ACTION_DOWN) //Let's get ready
		{
			isLandscape = e.getY() < displayW; //launcher rotation can be always 0. So....
			int currentW = isLandscape ? displayH : displayW;

			mInitialTouchX = x;
			isLongPress = false; // Reset flag
			if (pointers == 1 && isInPill(currentW, x)) longPressHandler.postDelayed(longPressRunnable, LONG_PRESS_THRESHOLD);

			saveFocusedTask();

			FCHandled = false;
			swipeType = SWIPE_NONE;

			if (isOverViewListener
					&& getBooleanField(param.thisObject, "mStartingInActivityBounds")) {
				return;
			}

			mSwipeUpThreshold = e.getY() * (1f - swipeUpPercentage);
			mLongThreshold = e.getY() / 10f;

			if (pointers == 1) {
				if (leftSwipeUpAction != NO_ACTION
						&& e.getX() < currentW * leftSwipeUpPercentage) {
					swipeType = SWIPE_LEFT;
				} else if (rightSwipeUpAction != NO_ACTION
						&& e.getX() > currentW * (1f - rightSwipeUpPercentage)) {
					swipeType = SWIPE_RIGHT;
				}
			}
		}
		if (action == MotionEvent.ACTION_MOVE) {
			if (Math.abs(e.getX() - mInitialTouchX) > TOUCH_SLOP) {
				longPressHandler.removeCallbacks(longPressRunnable);
			}
		}

		if (twoFingerSwipeUpAction != NO_ACTION
				&& pointers == 2
				&& swipeType != SWIPE_TWO_FINGER) { //must be outside down. usually down is one finger
			swipeType = SWIPE_TWO_FINGER;
		}

		if (mPassedWindowMoveSlop
				&& swipeType != SWIPE_NONE) { //shouldn't reach the main code anymore
			param.setResult(null);
		}

		if (pointers == 1) {
			boolean FCAllowed = swipeType == SWIPE_NONE;

			if (FCAllowed
					&& FCLongSwipeEnabled
					&& e.getY() < mLongThreshold
					&& !FCHandled
					&& !isOverViewListener) { //swiped up too much
				setObjectField(param.thisObject, "mActivePointerId", MotionEvent.INVALID_POINTER_ID);
				setObjectField(param.thisObject, "mPassedWindowMoveSlop", false);
				FCHandled = true;
				runAction(ACTION_KILL_APP, false);
			}
		}

		if (action == MotionEvent.ACTION_UP
				&& swipeType != SWIPE_NONE) {
			if (!isOverViewListener) {
				setObjectField(param.thisObject, "mActivePointerId", MotionEvent.INVALID_POINTER_ID);
				setObjectField(param.thisObject, "mPassedWindowMoveSlop", false);
				setBooleanField(param.thisObject, "mPassedPilferInputSlop", false);
				try {
					Object instance = getStaticObjectField(OplusInputInterceptHelper, "INSTANCE");
					Object result = callMethod(instance, "get");
					if (Build.VERSION.SDK_INT >= 35) {
						callMethod(result, "forceCancelGesture", true);
					} else {
						callMethod(result, "forceCancelGesture");
					}
					callMethod(param.thisObject, "finishTouchTracking", e);
				} catch (Throwable t) {
					log(t);
				}
			}

			if (e.getY() < mSwipeUpThreshold) {
				switch (swipeType) {
					case SWIPE_LEFT:
						runAction(leftSwipeUpAction, true);
						break;
					case SWIPE_RIGHT:
						runAction(rightSwipeUpAction, false);
						break;
					case SWIPE_TWO_FINGER:
						runAction(twoFingerSwipeUpAction, false);
						break;
				}
				swipeType = SWIPE_NONE;
			}

			currentFocusedTask = null;
		} else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
			longPressHandler.removeCallbacks(longPressRunnable);
		}
	}

	private boolean isInPill(int displayW, int x) {
		int pillWidth = displayW / 3;
		int pillCenter = displayW / 2;
		int pillStart = pillCenter - pillWidth / 2;
		int pillEnd = pillCenter + pillWidth / 2;
		return x >= pillStart && x <= pillEnd;
	}

	String mTasksFieldName = null; // in case the code was obfuscated
	private void saveFocusedTask() {
		if (mSysUiProxy == null) return;
		try {
			ArrayList<?> recentTaskList = (ArrayList<?>) callMethod(
					mSysUiProxy,
					"getRecentTasks",
					1,
					callMethod(Process.myUserHandle(), "getIdentifier"));

            if (recentTaskList == null || recentTaskList.isEmpty()) return;

            if (mTasksFieldName == null) {
				for(Field f : recentTaskList.get(0).getClass().getDeclaredFields()) {
					if(f.getType().getName().contains("RecentTaskInfo")) {
						mTasksFieldName = f.getName();
					}
				}
			}

            if (mTasksFieldName == null) return;

			Optional<?> focusedTask = recentTaskList.stream().filter(recentTask ->
					(boolean) getObjectField(
							((Object[]) getObjectField(recentTask, mTasksFieldName))[0],
							"isFocused"
					)).findFirst();

			currentFocusedTask = focusedTask.map(o -> ((Object[]) getObjectField(o, mTasksFieldName))[0]).orElse(null);
		}
		catch (Throwable t){
			log(t);
		}
	}

	private void runAction(int action, boolean isOnLeftEdge) {
		switch (action) {
			case ACTION_OPEN_QUICK_SETTINGS:
				openQs();
				break;
			case ACTION_SCREENSHOT:
				takeScreenshot(ScreenshotUtils.ScreenshotType.FULL);
				break;
			case ACTION_SCREENSHOT_PARTIAL:
				takeScreenshot(ScreenshotUtils.ScreenshotType.PARTIAL);
				break;
			case ACTION_SCREENSHOT_SCROLL:
				takeScreenshot(ScreenshotUtils.ScreenshotType.SCROLL);
				break;
			case ACTION_NOTIFICATION:
				toggleNotification();
				break;
			case ACTION_KILL_APP:
				killForeground();
				break;
			case ACTION_ONE_HANDED:
				startOneHandedMode();
				break;
			case ACTION_SWITCH_APP:
				switchApp(isOnLeftEdge);
				break;
			case ACTION_SLEEP:
				goToSleep();
				break;
			case ACTION_CIRCLE_TO_SEARCH:
				AppUtils.circleToSearch();
				break;
		}
	}

	private void switchApp(boolean isOnLeftEdge) {
		Intent intent = new Intent(ACTIONS_SWITCH_APP);
		intent.putExtra("side", !isOnLeftEdge ? 1 : 0);
		intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
		mContext.sendBroadcast(intent);
	}

	private void overrideBack(int side) {
		Intent intent = new Intent(ACTIONS_OVERRIDE_BACK);
		intent.putExtra("side", side);
		intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
		mContext.sendBroadcast(intent);
	}

	private void killForeground() {
		if (currentFocusedTask == null) {
			XposedBridge.log("CustomNavGestures - killForeground: currentFocusedTask is null");
			return;
		}

		try {
			Toast.makeText(mContext, "App Killed", Toast.LENGTH_SHORT).show();

			callMethod(mContext.getSystemService(Context.ACTIVITY_SERVICE),
					"forceStopPackageAsUser",
					((ComponentName) getObjectField(currentFocusedTask, "realActivity")).getPackageName(),
					getObjectField(currentFocusedTask, "userId"));

			goHome();

		}
		catch (Throwable t) {
			log(t);
		}
	}

	private void goHome() {
		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_HOME);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		mContext.startActivity(intent);
	}

	private void startOneHandedMode() {
		callMethod(getObjectField(mSystemUIProxy, "mOneHanded"), "startOneHanded");
	}

	private void toggleNotification() {
		callMethod(mSystemUIProxy, "toggleNotificationPanel");
	}

}