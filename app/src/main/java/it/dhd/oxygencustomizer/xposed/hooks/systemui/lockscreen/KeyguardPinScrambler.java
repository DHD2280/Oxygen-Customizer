package it.dhd.oxygencustomizer.xposed.hooks.systemui.lockscreen;

import static de.robv.android.xposed.XposedHelpers.getObjectField;
import static de.robv.android.xposed.XposedHelpers.setAdditionalInstanceField;
import static de.robv.android.xposed.XposedHelpers.setObjectField;
import static it.dhd.oxygencustomizer.utils.Constants.Packages.SYSTEM_UI;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.Lockscreen.LOCKSCREEN_KEEP_SHUFFLING;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.Lockscreen.LOCKSCREEN_SHUFFLE_PIN;
import static it.dhd.oxygencustomizer.xposed.XPrefs.Xprefs;

import android.content.Context;
import android.util.Log;

import com.coui.appcompat.lockview.COUINumericKeyboard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import de.robv.android.xposed.callbacks.XC_LoadPackage;
import it.dhd.oxygencustomizer.xposed.XposedMods;
import it.dhd.oxygencustomizer.xposed.utils.toolkit.ReflectedClass;

public class KeyguardPinScrambler extends XposedMods {

    private static final String listenPackage = SYSTEM_UI;
    private boolean shufflePinEnabled = false;
    private boolean keepShuffling = false;

    public KeyguardPinScrambler(Context context) {
        super(context);
    }

    @Override
    public void updatePrefs(String... Key) {
        shufflePinEnabled = Xprefs.getBoolean(LOCKSCREEN_SHUFFLE_PIN, false);
        keepShuffling = Xprefs.getBoolean(LOCKSCREEN_KEEP_SHUFFLING, false);
    }

    final List<Integer> digits = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, -1, 0, -1);


    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        ReflectedClass COUINumericKeyboardClass = ReflectedClass.of("com.coui.appcompat.lockview.COUINumericKeyboard");

        ReflectedClass.ReflectionConsumer pinShuffleHook = param -> {
            if (!shufflePinEnabled) return;

            List<Integer> valuesToShuffle = new ArrayList<>();
            for (Integer digit : digits) {
                if (digit != -1) {
                    valuesToShuffle.add(digit);
                }
            }
            Collections.shuffle(valuesToShuffle);
            List<Integer> result = new ArrayList<>();
            Iterator<Integer> iterator = valuesToShuffle.iterator();
            for (Integer digit : digits) {
                if (digit == -1) {
                    result.add(-1);
                } else {
                    result.add(iterator.next());
                }
            }
            COUINumericKeyboard.Cell[][] sCells = (COUINumericKeyboard.Cell[][]) getObjectField(param.thisObject, "sCells");
            String[] stringArray =
                    mContext.getResources().getStringArray(mContext.getResources().getIdentifier("coui_number_keyboard_letters", "array", SYSTEM_UI));
            int i5 = 0;
            int i4 = 0;
            while (i5 < 4) {
                int i6 = 0;
                while (i6 < 3) {
                    COUINumericKeyboard.Cell cell = sCells[i5][i6];
                    int i7 = (i5 * 3) + i6;
                    setAdditionalInstanceField(cell, "actualNumber", result.get(i7));
                    try {
                        setObjectField(cell, "cellLettersStr", stringArray[i7]);
                    } catch (Throwable ignored) {} // field not exists in 15.0.1
                    int i8 = result.get(i7);
                    if (i8 > -1) {
                        cell.cellNumberStr = String.format(Locale.getDefault(), "%d", Integer.valueOf(i8));
                    }
                    i6++;
                }
                i5++;
                i4 = 0;
            }
            setObjectField(param.thisObject , "mKeyboardNumbers", result.stream().mapToInt(i -> i).toArray());
            setObjectField(param.thisObject, "sCells", sCells);

            Log.d("KeyguardPinSrambler", "Shuffled PIN numbers: " + Arrays.toString(result.stream().mapToInt(i -> i).toArray()));

        };

        COUINumericKeyboardClass
                .before("callback")
                        .run(param -> {
                            if (!shufflePinEnabled) return;
                            int[] mKeyboardNumbers = (int[]) getObjectField(param.thisObject, "mKeyboardNumbers");
                            int clickedIndex = (int) param.args[0];
                            if (clickedIndex < 0 || clickedIndex >= mKeyboardNumbers.length) {
                                param.setResult(null);
                                return;
                            }
                            COUINumericKeyboard.OnClickItemListener listener = (COUINumericKeyboard.OnClickItemListener) getObjectField(param.thisObject, "mOnClickItemListener");
                            if (clickedIndex == 11) {
                                if (listener != null) {
                                    listener.onClickRight();
                                    if (keepShuffling) {
                                        pinShuffleHook.run(param);
                                    }
                                    return;
                                }
                            }
                            int correctNumber = mKeyboardNumbers[clickedIndex];
                            if (listener != null) {
                                listener.onClickNumber(correctNumber);
                                if (keepShuffling) {
                                    pinShuffleHook.run(param);
                                }
                                param.setResult(null);
                            }
                        });

        COUINumericKeyboardClass.afterConstruction().run(pinShuffleHook);
    }

    @Override
    public boolean listensTo(String packageName) {
        return listenPackage.equals(packageName);
    }
}
