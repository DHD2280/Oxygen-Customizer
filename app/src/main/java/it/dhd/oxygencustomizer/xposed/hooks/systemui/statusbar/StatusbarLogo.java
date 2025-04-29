package it.dhd.oxygencustomizer.xposed.hooks.systemui.statusbar;

import static de.robv.android.xposed.XposedHelpers.getObjectField;
import static it.dhd.oxygencustomizer.utils.Constants.Packages.SYSTEM_UI;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.Statusbar.STATUSBAR_LOGO_APPLY_TINT;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.Statusbar.STATUSBAR_LOGO_POSITION;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.Statusbar.STATUSBAR_LOGO_SIZE;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.Statusbar.STATUSBAR_LOGO_STYLE;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.Statusbar.STATUSBAR_LOGO_SWITCH;
import static it.dhd.oxygencustomizer.utils.Constants.STATUSBAR_LOGO_FILE;
import static it.dhd.oxygencustomizer.xposed.ResourceManager.modRes;
import static it.dhd.oxygencustomizer.xposed.XPrefs.Xprefs;
import static it.dhd.oxygencustomizer.xposed.utils.ViewHelper.dp2px;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import de.robv.android.xposed.callbacks.XC_LoadPackage;
import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.xposed.XposedMods;
import it.dhd.oxygencustomizer.xposed.utils.toolkit.ReflectedClass;
import it.dhd.oxygencustomizer.xposed.views.statusbar.LogoView;

public class StatusbarLogo extends XposedMods {

    private static final String listenPackage = SYSTEM_UI;

    private ViewGroup mStatusbarStartSide = null;
    private LinearLayout mSystemIconArea = null;
    private Object mCollapsedStatusBarFragment = null;

    private boolean mStatusbarLogo = false;
    private int mLogoStyle = 0;
    private int mLogoPosition = 0;
    private int mLogoSize = 18;
    private boolean mTintLogo = false;

    // Default Res
    private int mPaddingStart = 0;

    private LogoView mStatusbarLogoView = new LogoView(mContext);

    public StatusbarLogo(Context context) {
        super(context);
        mPaddingStart = mContext.getResources().getDimensionPixelSize(
                mContext.getResources().getIdentifier("status_bar_clock_starting_padding", "dimen", SYSTEM_UI));
    }

    @Override
    public void updatePrefs(String... Key) {

        mStatusbarLogo = Xprefs.getBoolean(STATUSBAR_LOGO_SWITCH, false);
        mLogoStyle = Integer.parseInt(Xprefs.getString(STATUSBAR_LOGO_STYLE, "0"));
        mLogoPosition = Integer.parseInt(Xprefs.getString(STATUSBAR_LOGO_POSITION, "0"));
        mLogoSize = Xprefs.getSliderInt(STATUSBAR_LOGO_SIZE, 18);
        mTintLogo = Xprefs.getBoolean(STATUSBAR_LOGO_APPLY_TINT, false);

        if (Key.length > 0) {
            if (Key[0].equals(STATUSBAR_LOGO_SWITCH) ||
                    Key[0].equals(STATUSBAR_LOGO_STYLE) ||
                    Key[0].equals(STATUSBAR_LOGO_POSITION) ||
                    Key[0].equals(STATUSBAR_LOGO_SIZE) ||
                    Key[0].equals(STATUSBAR_LOGO_APPLY_TINT)) {
                placeLogo();
            }
        }

    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {

        mStatusbarLogoView.setLayoutParams(new ViewGroup.LayoutParams(dp2px(mContext, mLogoSize), dp2px(mContext, mLogoSize)));
        mStatusbarLogoView.setScaleType(ImageView.ScaleType.FIT_CENTER);

        ReflectedClass CollapsedStatusBarFragmentClass = ReflectedClass.of("com.android.systemui.statusbar.phone.fragment.CollapsedStatusBarFragment");
        CollapsedStatusBarFragmentClass
                .afterConstruction()
                .run(param -> mCollapsedStatusBarFragment = param.thisObject);

        CollapsedStatusBarFragmentClass
                .after("onViewCreated")
                .run(param -> {

                    ViewGroup mStatusBar = (ViewGroup) getObjectField(mCollapsedStatusBarFragment, "mStatusBar");
                    try {
                        mStatusbarStartSide = mStatusBar.findViewById(mContext.getResources().getIdentifier("status_bar_start_side_except_heads_up", "id", mContext.getPackageName()));
                    } catch (Throwable t) {
                        mStatusbarStartSide = null;
                    }

                    try {
                        mSystemIconArea = mStatusBar.findViewById(mContext.getResources().getIdentifier("statusIcons", "id", mContext.getPackageName()));
                    } catch (Throwable t) {
                        mSystemIconArea = mStatusBar.findViewById(mContext.getResources().getIdentifier("system_icon_area", "id", mContext.getPackageName())); // OOS 13
                    }

                    placeLogo();
                });

    }

    @Override
    public boolean listensTo(String packageName) {
        return listenPackage.equals(packageName);
    }

    private Drawable getLogo(int style) {

        List<Integer> predefinedLogos = Arrays.asList(
                R.drawable.ic_android_logo,
                R.drawable.ic_adidas,
                R.drawable.ic_alien,
                R.drawable.ic_apple_logo,
                R.drawable.ic_avengers,
                R.drawable.ic_batman,
                R.drawable.ic_batman_tdk,
                R.drawable.ic_beats,
                R.drawable.ic_biohazard,
                R.drawable.ic_blackberry,
                R.drawable.ic_cannabis,
                R.drawable.ic_emoticon_cool,
                R.drawable.ic_emoticon_devil,
                R.drawable.ic_fire,
                R.drawable.ic_heart,
                R.drawable.ic_nike,
                R.drawable.ic_pac_man,
                R.drawable.ic_puma,
                R.drawable.ic_rog,
                R.drawable.ic_spiderman,
                R.drawable.ic_superman,
                R.drawable.ic_windows,
                R.drawable.ic_xbox,
                R.drawable.ic_ghost,
                R.drawable.ic_ninja,
                R.drawable.ic_robot,
                R.drawable.ic_ironman,
                R.drawable.ic_captain_america,
                R.drawable.ic_flash,
                R.drawable.ic_tux_logo,
                R.drawable.ic_ubuntu_logo,
                R.drawable.ic_mint_logo,
                R.drawable.ic_amogus
        );

        if (style == -1) {
            try {
                Bitmap customBitmap = ImageDecoder.decodeBitmap(ImageDecoder.createSource(new File(STATUSBAR_LOGO_FILE)));
                RoundedBitmapDrawable roundedDrawable = RoundedBitmapDrawableFactory.create(mContext.getResources(), customBitmap);
                roundedDrawable.setCornerRadius(customBitmap.getHeight());
                return roundedDrawable;
            } catch (Throwable t) {
                return ResourcesCompat.getDrawable(modRes, predefinedLogos.get(0), mContext.getTheme());
            }
        } else {
            return ResourcesCompat.getDrawable(modRes, predefinedLogos.get(style), mContext.getTheme());
        }
    }

    private void placeLogo() {
        ViewGroup targetArea = null;
        Integer index = null;

        if (mLogoPosition == 0) { // LEFT
            targetArea = mStatusbarStartSide;
            index = 0;
        } else {
            targetArea = ((ViewGroup) mSystemIconArea.getParent());
            index = -1;
        }

        LinearLayout.LayoutParams logoParams = new LinearLayout.LayoutParams(
                dp2px(mContext, mLogoSize),
                dp2px(mContext, mLogoSize)
        );
        logoParams.gravity = Gravity.CENTER_VERTICAL;
        logoParams.setMargins(
                mPaddingStart, 0, mPaddingStart, 0
        );
        mStatusbarLogoView.setLayoutParams(logoParams);
        if (targetArea != null) {
            try {
                ((ViewGroup) mStatusbarLogoView.getParent()).removeView(mStatusbarLogoView);
            } catch (Throwable ignored) {
            } // Make sure to remove the view from its parent
            if (index == -1) {
                targetArea.addView(mStatusbarLogoView, targetArea.getChildCount());
            } else {
                targetArea.addView(mStatusbarLogoView, index);
            }
        }
        mStatusbarLogoView.setForceTint(mLogoStyle == -1 ? mTintLogo : true);
        mStatusbarLogoView.setImageDrawable(getLogo(mLogoStyle));

        if (mStatusbarLogo) {
            mStatusbarLogoView.setVisibility(View.VISIBLE);
        } else {
            mStatusbarLogoView.setVisibility(View.GONE);
        }
    }

}
