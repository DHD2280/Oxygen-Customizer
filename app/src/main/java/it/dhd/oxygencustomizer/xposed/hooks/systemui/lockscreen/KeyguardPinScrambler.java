package it.dhd.oxygencustomizer.xposed.hooks.systemui.lockscreen;

import static de.robv.android.xposed.XposedHelpers.getObjectField;
import static de.robv.android.xposed.XposedHelpers.setAdditionalInstanceField;
import static de.robv.android.xposed.XposedHelpers.setObjectField;
import static it.dhd.oxygencustomizer.utils.Constants.Packages.SYSTEM_UI;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.Lockscreen.LOCKSCREEN_KEEP_SHUFFLING;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.Lockscreen.LOCKSCREEN_SHUFFLE_PIN;
import static it.dhd.oxygencustomizer.xposed.XPrefs.Xprefs;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;
import android.view.View;

import com.coui.appcompat.lockview.COUINumericKeyboard;

import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import it.dhd.oxygencustomizer.xposed.XposedMods;
import it.dhd.oxygencustomizer.xposed.utils.toolkit.ReflectedClass;

public class KeyguardPinScrambler extends XposedMods {

    private static final String listenPackage = SYSTEM_UI;
    private boolean shufflePinEnabled = false;
    private boolean keepShuffling = false;
    private static WeakReference<View> mCurrentDial;

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
            XposedBridge.log("KeyguardPinScrambler: Shuffling PIN numbers");
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

            XposedBridge.log("KeyguardPinScrambler: Shuffled PIN numbers: " + Arrays.toString(result.stream().mapToInt(i -> i).toArray()));

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

        //COS17
        ReflectedClass COUIPasswordDial = ReflectedClass.ofIfPossible("com.coui.appcompat.passworddial.COUIPasswordDial");
        Method deliverMethod = null;
        if (COUIPasswordDial.getClazz() != null) {
            for (Method m : COUIPasswordDial.getClazz().getDeclaredMethods()) {
                if (Modifier.isStatic(m.getModifiers())
                        && m.getReturnType() == void.class
                        && m.getParameterCount() == 2
                        && m.getParameterTypes()[0] == COUIPasswordDial.getClazz()
                        && m.getParameterTypes()[1] == int.class) {
                    deliverMethod = m;
                    break;
                }
            }
        }
        if (deliverMethod != null) {
            XposedBridge.hookMethod(deliverMethod, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam p) {
                    if (!shufflePinEnabled) return;
                    try {
                        Object dial = p.args[0];
                        int digit = (int) p.args[1];
                        int[] map = (int[]) XposedHelpers.getAdditionalInstanceField(dial, "pinScrambleMap");
                        if (map == null) return;
                        int pos = (digit == 0) ? 10 : digit - 1;
                        if (pos >= 0 && pos < map.length && map[pos] >= 0) {
                            Log.d("KeyguardPinScrambler", "translate " + digit + " -> " + map[pos]);
                            p.args[1] = map[pos];
                        }
                    } catch (Throwable t) {
                        Log.e("KeyguardPinScrambler", "deliver before", t);
                    }
                }

                @Override
                protected void afterHookedMethod(MethodHookParam p) {
                    if (!shufflePinEnabled || !keepShuffling) return;
                    try {
                        View dial = (View) p.args[0];
                        dial.post(() -> doShuffle(dial));
                    } catch (Throwable t) {
                        Log.e("KeyguardPinScrambler", "deliver after", t);
                    }
                }
            });
        }

        COUIPasswordDial
                .afterConstruction()
                .run(p -> {
                    if (!shufflePinEnabled) return;
                    if (XposedHelpers.getAdditionalInstanceField(p.thisObject, "pinScrambleMap") != null)
                        return;
                    View dial = (View) p.thisObject;
                    mCurrentDial = new WeakReference<>(dial);
                    doShuffle(dial);
                });

        ReflectedClass.ofIfPossible("com.oplus.keyguard.security.view.OplusKeyguardFixedPinView")
                .before("resetPasswordText")
                .run(p -> {
                    if (!shufflePinEnabled && !keepShuffling) return;
                    try {
                        boolean isWrong = (boolean) p.args[0];
                        if (!isWrong) return;

                        View dial = mCurrentDial.get() != null ? mCurrentDial.get() : null;
                        if (dial == null) {
                            // fallback
                            View self = (View) p.thisObject;
                            int id = self.getResources().getIdentifier(
                                    "pinColorNumericKeyboard", "id", "com.android.systemui");
                            dial = self.findViewById(id);
                        }
                        if (dial == null) {
                            XposedBridge.log("KeyguardPinScrambler reset: dial null");
                            return;
                        }

                        final View target = dial;
                        target.post(() -> doShuffle(target));
                        XposedBridge.log("KeyguardPinScrambler reset(wrong): re-shuffle");
                    } catch (Throwable t) {
                        XposedBridge.log("KeyguardPinScrambler reset hook " + Arrays.toString(t.getStackTrace()));
                    }
                });

    }

    static void doShuffle(View dial) {
        try {
            int[] layout = {1, 2, 3, 4, 5, 6, 7, 8, 9, -1, 0, -1};
            List<Integer> pool = new ArrayList<>();
            for (int d : layout) if (d != -1) pool.add(d);
            Collections.shuffle(pool);

            int[] map = new int[12];
            Iterator<Integer> it = pool.iterator();
            for (int i = 0; i < 12; i++) map[i] = (layout[i] == -1) ? -1 : it.next();

            Resources res = dial.getResources();
            String pkg = "com.android.systemui";
            String[] ids = {
                    "password_dial_key_00", "password_dial_key_01", "password_dial_key_02",
                    "password_dial_key_10", "password_dial_key_11", "password_dial_key_12",
                    "password_dial_key_20", "password_dial_key_21", "password_dial_key_22",
                    "password_dial_key_30", "password_dial_key_31", "password_dial_key_32"
            };
            for (int i = 0; i < 12; i++) {
                if (map[i] == -1) continue;
                int id = res.getIdentifier(ids[i], "id", pkg);
                View key = dial.findViewById(id);
                if (key instanceof android.widget.TextView) {
                    ((android.widget.TextView) key).setText(String.valueOf(map[i]));
                }
            }
            XposedHelpers.setAdditionalInstanceField(dial, "pinScrambleMap", map);
            XposedBridge.log("KeyguardPinScrambler shuffle: " + Arrays.toString(map));
        } catch (Throwable t) {
            XposedBridge.log("KeyguardPinScrambler " + Arrays.toString(t.getStackTrace()));
        }
    }

    @Override
    public boolean listensTo(String packageName) {
        return listenPackage.equals(packageName);
    }
}
