package it.dhd.oxygencustomizer.xposed.hooks.systemui.statusbar;

import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.getObjectField;
import static de.robv.android.xposed.XposedHelpers.setObjectField;
import static it.dhd.oxygencustomizer.utils.Constants.Packages.SYSTEM_UI;
import static it.dhd.oxygencustomizer.xposed.ResourceManager.modRes;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import it.dhd.oxygencustomizer.BuildConfig;
import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.xposed.XposedMods;
import it.dhd.oxygencustomizer.xposed.hooks.systemui.ControllersProvider;
import it.dhd.oxygencustomizer.xposed.utils.ActivityLauncherUtils;
import it.dhd.oxygencustomizer.xposed.utils.toolkit.ReflectedClass;

public class QsMenuItems extends XposedMods {

    private static final String listenPackage = SYSTEM_UI;

    private Context mMenuContext;
    private Context mSeparateMenuContext;
    private ActivityLauncherUtils mActivityLauncherUtils;

    private final ArrayList<Object[]> mMenuOptions = new ArrayList<>(){{
        add(new Object[]{R.string.qs_header_title, "qs_header_options"});
        add(new Object[]{R.string.quick_settings_widgets, "qs_widget_options"});
    }};

    private final ArrayList<Object[]> mSeparateMenuOptions = new ArrayList<>(){{
        add(new Object[]{R.string.quick_settings_widgets, "qs_widget_options"});
        add(new Object[]{R.string.qs_separate_layout, "qs_separate_layout"});
    }};

    public QsMenuItems(Context context) {
        super(context);
    }

    @Override
    public void updatePrefs(String... Key) {

    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {

        if (Build.VERSION.SDK_INT < 35) return; // Don't care about this on previous versions\

        ReflectedClass PopupListItemBuilder = ReflectedClass.of("com.coui.appcompat.poplist.PopupListItem$Builder");

        ReflectedClass MoreButtonPopupWindow = ReflectedClass.of("com.oplus.systemui.qs.widget.MoreButtonPopupWindow");
        MoreButtonPopupWindow
                .beforeConstruction()
                .run(param -> {
                    mActivityLauncherUtils = new ActivityLauncherUtils(mContext, ControllersProvider.getActivityStarterExternal());
                    mMenuContext = (Context) param.args[0];
                });

        MoreButtonPopupWindow
                .before("initList")
                .run(param -> {
                    List<Object> arrayList = new ArrayList<>();
                    Object ListBuilder = PopupListItemBuilder.getClazz().getConstructor().newInstance();
                    View view = (View) getObjectField(param.thisObject, "editButton");
                    if (view != null && view.getVisibility() == View.VISIBLE) {
                        Object builder = callMethod(ListBuilder, "setItemType", 2);
                        builder = callMethod(builder, "setCustomItemView", view);
                        arrayList.add(callMethod(builder, "build"));
                    }
                    View view2 = (View) getObjectField(param.thisObject, "outSwitchButton");
                    if (view2 != null && view2.getVisibility() == View.VISIBLE) {
                        Object builder = callMethod(ListBuilder, "reset");
                        builder = callMethod(builder, "setItemType", 2);
                        builder = callMethod(builder, "setCustomItemView", view2);
                        arrayList.add(callMethod(builder, "build"));
                    }
                    View view3 = (View) getObjectField(param.thisObject, "myDeviceLayout");//this.myDeviceLayout;
                    if (view3 != null && view3.getVisibility() == View.VISIBLE) {
                        Object builder = callMethod(ListBuilder, "reset");
                        builder = callMethod(builder, "setItemType", 2);
                        builder = callMethod(builder, "setCustomItemView", view3);
                        arrayList.add(callMethod(builder, "build"));
                    }
                    View view4 = (View) getObjectField(param.thisObject, "settingsView");
                    if (view4 != null && view4.getVisibility() == View.VISIBLE) {
                        Object builder = callMethod(ListBuilder, "reset");
                        builder = callMethod(builder, "setItemType", 2);
                        builder = callMethod(builder, "setCustomItemView", view4);
                        arrayList.add(callMethod(builder, "build"));
                    }
                    View view5 = (View) getObjectField(param.thisObject, "fgsButton");
                    if (view5 != null && view5.getVisibility() == View.VISIBLE) {
                        Object builder = callMethod(ListBuilder, "reset");
                        builder = callMethod(builder, "setItemType", 2);
                        builder = callMethod(builder, "setCustomItemView", view5);
                        arrayList.add(callMethod(builder, "build"));
                    }

                    for (Object[] menuOption : mMenuOptions) {
                        final ViewGroup v = (ViewGroup) LayoutInflater.from(mMenuContext).inflate(mContext.getResources().getIdentifier(
                                "oplus_more_menu_item_qs_cc_setting", "layout", SYSTEM_UI), null);
                        TextView textView = (TextView) v.getChildAt(0);
                        final ClickListener clickListener = new ClickListener();
                        v.setOnClickListener(clickListener);
                        v.setTag(menuOption[1]);
                        textView.setText(modRes.getString((Integer) menuOption[0]));
                        Object builder = callMethod(ListBuilder, "reset");
                        builder = callMethod(builder, "setItemType", 2);
                        builder = callMethod(builder, "setCustomItemView", v);
                        arrayList.add(callMethod(builder, "build"));
                    }

                    setObjectField(param.thisObject, "mCustomList", arrayList);

                    callMethod(param.thisObject, "setItemList", getObjectField(param.thisObject, "mCustomList"));

                    param.setResult(null);

                });

        ReflectedClass OplusQSMoreButtonPopupWindow = ReflectedClass.of("com.oplus.systemui.plugins.qs.quickentrance.popupwindow.OplusQSMoreButtonPopupWindow");
        OplusQSMoreButtonPopupWindow
                .beforeConstruction()
                .run(param -> {
                    if (mActivityLauncherUtils == null) {
                        mActivityLauncherUtils = new ActivityLauncherUtils(mContext, ControllersProvider.getActivityStarterExternal());
                    }
                    mSeparateMenuContext = (Context) param.args[0];
                });
        OplusQSMoreButtonPopupWindow
                .before("initList")
                .run(param -> {
                    List<Object> arrayList = new ArrayList<>();
                    Object ListBuilder = PopupListItemBuilder.getClazz().getConstructor().newInstance();
                    View view = (View) getObjectField(param.thisObject, "usersView");
                    if (view != null && view.getVisibility() == View.VISIBLE) {
                        Object builder = callMethod(ListBuilder, "setItemType", 2);
                        builder = callMethod(builder, "setCustomItemView", view);
                        arrayList.add(callMethod(builder, "build"));
                    }
                    View view2 = (View) getObjectField(param.thisObject, "fgsView");
                    if (view2 != null && view2.getVisibility() == View.VISIBLE) {
                        Object builder = callMethod(ListBuilder, "reset");
                        builder = callMethod(builder, "setItemType", 2);
                        builder = callMethod(builder, "setCustomItemView", view2);
                        arrayList.add(callMethod(builder, "build"));
                    }
                    View view3 = (View) getObjectField(param.thisObject, "qsSettingsView");
                    if (view3 != null && view3.getVisibility() == View.VISIBLE) {
                        Object builder = callMethod(ListBuilder, "reset");
                        builder = callMethod(builder, "setItemType", 2);
                        builder = callMethod(builder, "setCustomItemView", view3);
                        arrayList.add(callMethod(builder, "build"));
                    }
                    View view4 = (View) getObjectField(param.thisObject, "myDeviceView");
                    if (view4 != null && view4.getVisibility() == View.VISIBLE) {
                        Object builder = callMethod(ListBuilder, "reset");
                        builder = callMethod(builder, "setItemType", 2);
                        builder = callMethod(builder, "setCustomItemView", view4);
                        arrayList.add(callMethod(builder, "build"));
                    }
                    View view5 = (View) getObjectField(param.thisObject, "ccSettingView");
                    if (view5 != null && view5.getVisibility() == View.VISIBLE) {
                        Object builder = callMethod(ListBuilder, "reset");
                        builder = callMethod(builder, "setItemType", 2);
                        builder = callMethod(builder, "setCustomItemView", view5);
                        arrayList.add(callMethod(builder, "build"));
                    }

                    for (Object[] menuOption : mSeparateMenuOptions) {
                        final ViewGroup v = (ViewGroup) LayoutInflater.from(mSeparateMenuContext).inflate(mContext.getResources().getIdentifier(
                                "oplus_more_menu_item_qs_cc_setting", "layout", SYSTEM_UI), null);
                        TextView textView = (TextView) v.getChildAt(0);
                        final ClickListener clickListener = new ClickListener();
                        v.setOnClickListener(clickListener);
                        v.setTag(menuOption[1]);
                        textView.setText(modRes.getString((Integer) menuOption[0]));
                        Object builder = callMethod(ListBuilder, "reset");
                        builder = callMethod(builder, "setItemType", 2);
                        builder = callMethod(builder, "setCustomItemView", v);
                        arrayList.add(callMethod(builder, "build"));
                    }

                    setObjectField(param.thisObject, "mCustomList", arrayList);

                    callMethod(param.thisObject, "setItemList", getObjectField(param.thisObject, "mCustomList"));

                    param.setResult(null);
                });

    }

    class ClickListener implements View.OnClickListener {
        public ClickListener() {
        }

        @Override
        public void onClick(View v) {
            XposedBridge.log("QS Menu Item clicked");
            Object tag = v.getTag();
            if (tag != null) {
                XposedBridge.log("QS Menu Item tag: " + tag.toString());
                launch(tag.toString());
            }
        }
    }

    private void launch(String what) {
        Intent intent = mContext.getPackageManager().getLaunchIntentForPackage(BuildConfig.APPLICATION_ID);
        intent.putExtra("launch", what);
        mActivityLauncherUtils.launchIntent(intent, true);
    }

    @Override
    public boolean listensTo(String packageName) {
        return listenPackage.equals(packageName);
    }
}
