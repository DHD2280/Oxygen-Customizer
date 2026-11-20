package it.dhd.oxygencustomizer.xposed.utils;

import android.app.WallpaperColors;
import android.app.WallpaperManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.RenderEffect;
import android.graphics.RuntimeShader;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.TextView;

import org.lsposed.hiddenapibypass.HiddenApiBypass;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.WeakHashMap;

import it.dhd.oxygencustomizer.xposed.utils.toolkit.Logger;

public final class GlassTextEffect {

    private static final int COLOR_DIGIT = 0xFFFFFFFF;
    private static final int COLOR_EXTRA_INFO = 0xFF808080;
    private static final float LIGHT_LUMINANCE = 0.55f;
    private static final int THUMB_SIZE = 48;
    private static final float SDF_BLUR_RADIUS = 10f;
    private static final float MAX_SAMPLE_RADIUS = 2f;

    private static final String SHADER_HEAD = """
            uniform shader clock;
            uniform shader wallpaper;
            uniform float2 resolution;
            uniform float2 screen;
            uniform float light;
            uniform float alpha;
            """;

    private static final String SDF_MULTI = """
            uniform shader sdfTex;
            float sdf(float2 p) { return sdfTex.eval(p).a; }
            """;

    private static final String SDF_SINGLE = """
            float sdf(float2 p) {
                float s = clock.eval(p).a * 2.0;
                for (int i = 0; i < 16; i++) {
                    float a = float(i) * 0.3926991;
                    float2 o = float2(cos(a), sin(a));
                    s += clock.eval(p + o * 4.0).a + clock.eval(p + o * 9.0).a;
                }
                return s / 34.0;
            }
            """;

    private static final String SHADER_BODY = """
            float lum(float3 c) { return dot(c, float3(0.2126, 0.7152, 0.0722)); }
            
            float overlay1(float s, float d) { return mix(2.0 * s * d, 1.0 - 2.0 * (1.0 - s) * (1.0 - d), step(0.5, s)); }
            float3 overlay(float3 s, float3 d) { return float3(overlay1(s.r, d.r), overlay1(s.g, d.g), overlay1(s.b, d.b)); }
            
            float3 screenBlend(float3 b, float3 g) { return 1.0 - (1.0 - b) * (1.0 - g); }
            
            float3 clipColor(float3 c) {
                float l = lum(c);
                float n = min(min(c.r, c.g), c.b);
                float x = max(max(c.r, c.g), c.b);
                c = mix(c, l + ((c - l) * l) / max(l - n, 0.0001), step(n, 0.0));
                c = mix(c, l + ((c - l) * (1.0 - l)) / max(x - l, 0.0001), step(1.0, x));
                return c;
            }
            
            float3 luminosity(float3 dst, float target, float a) { return mix(dst, clipColor(dst + (target - lum(dst))), a); }
            
            float3 dodge(float3 dst, float3 src, float a) { return mix(dst, min(float3(1.0), dst / max(1.0 - src, float3(0.0001))), a); }
            
            float3 tint(float3 base, float digit) {
                float3 dark = mix(base, overlay(base, float3(1.0)), 0.2);
                dark = min(float3(1.0), dark + float3(0.8549) * mix(0.55, 0.45, digit));
                dark = mix(dark, overlay(dark, float3(0.4)), 0.6);
                float3 bright = luminosity(base, 0.10196, mix(0.85, 0.75, digit));
                bright = dodge(bright, float3(0.2), 0.4);
                return mix(dark, bright, light);
            }
            
            float directional(float2 g, float2 dirScale) {
                float len2 = dot(g, g);
                float2 w = len2 > 1e-8 ? (g * g) / len2 : float2(0.5);
                return 1.0 / (w.x / max(dirScale.x, 0.0001) + w.y / max(dirScale.y, 0.0001));
            }
            
            float edge(float s, float2 g, float power, float2 dirScale, float3 lightDir, float focus) {
                float len2 = dot(g, g);
                float2 n2 = len2 > 1e-8 ? g / sqrt(len2) : float2(0.0);
                float3 n = normalize(float3(n2, 0.5));
                float mask = pow(1.0 - smoothstep(0.0, 0.5, max(0.0, s - 0.5)), max(power * directional(g, dirScale), 0.01));
                return mask * pow(abs(dot(n, normalize(lightDir))), focus);
            }
            
            half4 main(float2 p) {
                float4 c = clock.eval(p);
                float s = sdf(p);
                if (s <= 0.0001 && c.a <= 0.0001) return half4(0.0);
                float2 g = float2(sdf(p + float2(1.0, 0.0)) - sdf(p - float2(1.0, 0.0)), sdf(p + float2(0.0, 1.0)) - sdf(p - float2(0.0, 1.0))) * 0.5;
                float digit = step(0.75, c.r / max(c.a, 0.001));
                float2 ref = max(screen, resolution);
            
                float3 wall = wallpaper.eval(p).rgb;
                float gl = length(g);
                float2 gm = g * (min(gl, 0.05) / max(gl, 0.0001));
                float weight = 1.0 - smoothstep(0.0, 0.55 * directional(gm, float2(1.0)), abs(s - 0.5));
                float2 offset = gm * ref * 0.55 * weight;
                float2 dir = gl > 0.001 ? g / gl : float2(0.0);
                float2 disp = dir * ref.x * 0.2 * gl * weight;
                float3 refracted = float3(
                    wallpaper.eval(p + offset + disp).r,
                    wallpaper.eval(p + offset).g,
                    wallpaper.eval(p + offset - disp).b);
            
                float3 color = tint(mix(wall, refracted, digit), digit);
                float dark = (1.0 - light) * digit;
                float glow = edge(s, g, 2.2, float2(2.0, 1.0), float3(0.0, 1.0, -0.07), 6.5);
                float stroke = edge(s, g, 1.0, float2(1.0), float3(0.5, -0.2, -0.5), 1.0);
                color = screenBlend(color, float3(exp2(0.05) * glow * dark));
                color = screenBlend(color, wall * 2.0 * stroke * 0.7 * dark);
                color = clamp(color, 0.0, 1.0);
            
                float shadow = smoothstep(-1.5, 0.5, s) * s * 0.07 * (1.0 - light);
                float4 result = float4(color * c.a, c.a) * alpha + float4(0.0, 0.0, 0.0, shadow) * (1.0 - c.a);
                return half4(clamp(result, 0.0, 1.0));
            }
            """;

    private static boolean sLoadAttempted = false;
    private static Method sMultiInput;
    private static Bitmap sWallpaper;
    private static float sLight = 0f;
    private static boolean sWallpaperListenerRegistered = false;
    private static boolean sAod = false;

    private static final Map<View, GlassTarget> sTargets = new WeakHashMap<>();
    private static final Map<View, Bitmap> sFrames = new WeakHashMap<>();
    private static final Map<Bitmap, Bitmap> sThumbs = new WeakHashMap<>();

    private GlassTextEffect() {
    }

    public static boolean isSupported() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU;
    }

    public static void applyWithTag(View root, String glassTag, String digitTag) {
        if (root == null || !isSupported()) return;
        if (ViewHelper.hasTagToken(root, glassTag)) {
            encodeRegions(root, digitTag);
            attach(root);
        }
        if (root instanceof ViewGroup viewGroup) {
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                applyWithTag(viewGroup.getChildAt(i), glassTag, digitTag);
            }
        }
    }

    public static void applyToView(View view) {
        if (view == null || !isSupported()) return;
        GlassTarget target = sTargets.get(view);
        if (target != null) {
            target.refreshIfMoved();
            return;
        }
        attach(view);
    }

    public static void setAod(boolean aod) {
        if (sAod == aod) return;
        sAod = aod;
        refreshAll();
    }

    public static void setFrame(View frame, Bitmap wallpaper) {
        if (frame == null) return;
        if (wallpaper == null) sFrames.remove(frame);
        else sFrames.put(frame, wallpaper);
        refreshAll();
    }

    public static void clear(View view) {
        if (view == null || sTargets.remove(view) == null) return;
        view.setRenderEffect(null);
    }

    private static void refreshAll() {
        for (GlassTarget target : sTargets.values()) {
            target.refresh();
        }
    }

    private static void attach(View view) {
        load();
        GlassTarget target = sTargets.get(view);
        if (target == null) {
            target = new GlassTarget(view);
            sTargets.put(view, target);
            target.attach();
        }
        ensureWallpaper(view.getContext());
        target.refresh();
    }

    private static void encodeRegions(View view, String digitTag) {
        if (view instanceof TextView textView) {
            textView.setTextColor(ViewHelper.hasTagToken(textView, digitTag) ? COLOR_DIGIT : COLOR_EXTRA_INFO);
        } else if (view instanceof ViewGroup viewGroup) {
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                encodeRegions(viewGroup.getChildAt(i), digitTag);
            }
        }
    }

    private static synchronized void load() {
        if (sLoadAttempted) return;
        sLoadAttempted = true;
        try {
            HiddenApiBypass.addHiddenApiExemptions("Landroid/graphics/RenderEffect;");
        } catch (Throwable ignored) {
        }
        try {
            Method method = RenderEffect.class.getDeclaredMethod("createRuntimeShaderEffect", RuntimeShader.class, String[].class, RenderEffect[].class, float.class);
            method.setAccessible(true);
            sMultiInput = method;
        } catch (Throwable t) {
            sMultiInput = null;
        }
    }

    private static void ensureWallpaper(Context context) {
        if (!sWallpaperListenerRegistered) {
            sWallpaperListenerRegistered = true;
            WallpaperManager.getInstance(context).addOnColorsChangedListener((colors, which) -> {
                sWallpaper = null;
                loadWallpaper(context);
                refreshAll();
            }, new Handler(Looper.getMainLooper()));
            KeyguardWallpaper.addListener(() -> {
                sWallpaper = KeyguardWallpaper.get();
                refreshAll();
            });
        }
        if (sWallpaper == null) loadWallpaper(context);
    }

    private static void loadWallpaper(Context context) {
        try {
            Bitmap keyguard = KeyguardWallpaper.get();
            WallpaperManager manager = WallpaperManager.getInstance(context);
            int which = WallpaperManager.FLAG_LOCK;
            Drawable drawable = manager.getDrawable(which);
            if (drawable == null) {
                which = WallpaperManager.FLAG_SYSTEM;
                drawable = manager.getDrawable(which);
            }
            if (keyguard != null) {
                sWallpaper = keyguard;
            } else if (drawable instanceof BitmapDrawable bitmapDrawable) {
                sWallpaper = bitmapDrawable.getBitmap();
            } else if (drawable != null) {
                sWallpaper = DrawableConverter.drawableToBitmap(drawable);
            }
            WallpaperColors colors = manager.getWallpaperColors(which);
            sLight = colors != null && (colors.getColorHints() & WallpaperColors.HINT_SUPPORTS_DARK_TEXT) != 0 ? 1f : 0f;
        } catch (Throwable t) {
            Logger.log("GlassTextEffect wallpaper failed: " + t);
        }
    }

    private static float lightFor(Bitmap wallpaper, int screenWidth, int screenHeight, float x, float y, int width, int height) {
        if (screenWidth <= 0 || screenHeight <= 0) return sLight;
        Bitmap thumb = sThumbs.get(wallpaper);
        if (thumb == null) {
            Bitmap source = wallpaper.getConfig() == Bitmap.Config.HARDWARE ? wallpaper.copy(Bitmap.Config.ARGB_8888, false) : wallpaper;
            if (source == null) return sLight;
            float ratio = Math.min(1f, (float) THUMB_SIZE / Math.max(source.getWidth(), source.getHeight()));
            thumb = Bitmap.createScaledBitmap(source, Math.max(1, Math.round(source.getWidth() * ratio)), Math.max(1, Math.round(source.getHeight() * ratio)), true);
            sThumbs.put(wallpaper, thumb);
        }
        float scale = Math.max((float) screenWidth / thumb.getWidth(), (float) screenHeight / thumb.getHeight());
        float offsetX = (thumb.getWidth() * scale - screenWidth) / 2f;
        float offsetY = (thumb.getHeight() * scale - screenHeight) / 2f;
        int left = clamp((int) ((x + offsetX) / scale), thumb.getWidth() - 1);
        int top = clamp((int) ((y + offsetY) / scale), thumb.getHeight() - 1);
        int right = clamp((int) Math.ceil((x + width + offsetX) / scale), thumb.getWidth());
        int bottom = clamp((int) Math.ceil((y + height + offsetY) / scale), thumb.getHeight());
        double sum = 0;
        int count = 0;
        for (int py = top; py < Math.max(bottom, top + 1); py++) {
            for (int px = left; px < Math.max(right, left + 1); px++) {
                int color = thumb.getPixel(px, py);
                sum += (0.2126 * Color.red(color) + 0.7152 * Color.green(color) + 0.0722 * Color.blue(color)) / 255.0;
                count++;
            }
        }
        if (count == 0) return sLight;
        return sum / count > LIGHT_LUMINANCE ? 1f : 0f;
    }

    private static int clamp(int value, int max) {
        return Math.max(0, Math.min(value, max));
    }

    private static View frameOf(View view) {
        if (sFrames.isEmpty()) return null;
        ViewParent parent = view.getParent();
        while (parent instanceof View parentView) {
            if (sFrames.containsKey(parentView)) return parentView;
            parent = parentView.getParent();
        }
        return null;
    }

    private static float[] areaOf(View root, View frame) {
        int[] position = new int[2];
        int[] origin = new int[2];
        if (frame != null) frame.getLocationOnScreen(origin);
        float left = Float.MAX_VALUE, top = Float.MAX_VALUE, right = -Float.MAX_VALUE, bottom = -Float.MAX_VALUE;
        for (View view : sTargets.keySet()) {
            if (view == null || !view.isAttachedToWindow() || view.getWidth() <= 0) continue;
            View viewRoot = frameOf(view) != null ? frameOf(view) : view.getRootView();
            if (viewRoot != root) continue;
            view.getLocationOnScreen(position);
            float x = position[0] - origin[0];
            float y = position[1] - origin[1];
            left = Math.min(left, x);
            top = Math.min(top, y);
            right = Math.max(right, x + view.getWidth());
            bottom = Math.max(bottom, y + view.getHeight());
        }
        if (left > right) return new float[]{0f, 0f, root.getWidth(), root.getHeight()};
        return new float[]{left, top, right - left, bottom - top};
    }

    private static RenderEffect createEffect(RuntimeShader shader, boolean multi) {
        if (multi) {
            try {
                RenderEffect blur = RenderEffect.createBlurEffect(SDF_BLUR_RADIUS, SDF_BLUR_RADIUS, Shader.TileMode.CLAMP);
                return (RenderEffect) sMultiInput.invoke(null, shader, new String[]{"sdfTex", "clock"}, new RenderEffect[]{blur, null}, MAX_SAMPLE_RADIUS);
            } catch (Throwable t) {
                Logger.log("GlassTextEffect multi input failed: " + t);
                sMultiInput = null;
                return null;
            }
        }
        return RenderEffect.createRuntimeShaderEffect(shader, "clock");
    }

    private static final class GlassTarget {
        private final View view;
        private final int[] location = new int[2];
        private final int[] frameLocation = new int[2];
        private final Matrix matrix = new Matrix();
        private RuntimeShader shader;
        private boolean multi;
        private int lastX = Integer.MIN_VALUE, lastY = Integer.MIN_VALUE, lastW = -1, lastH = -1;

        GlassTarget(View view) {
            this.view = view;
        }

        void attach() {
            view.addOnLayoutChangeListener((v, l, t, r, b, ol, ot, or, ob) -> refreshIfMoved());
            view.getViewTreeObserver().addOnGlobalLayoutListener(this::refreshIfMoved);
        }

        void refreshIfMoved() {
            if (sTargets.get(view) != this || !view.isAttachedToWindow()) return;
            view.getLocationOnScreen(location);
            if (location[0] == lastX && location[1] == lastY && view.getWidth() == lastW && view.getHeight() == lastH)
                return;
            refresh();
        }

        private RuntimeShader shader() {
            boolean wantMulti = sMultiInput != null;
            if (shader == null || multi != wantMulti) {
                multi = wantMulti;
                shader = new RuntimeShader(SHADER_HEAD + (multi ? SDF_MULTI : SDF_SINGLE) + SHADER_BODY);
            }
            return shader;
        }

        void refresh() {
            if (sTargets.get(view) != this) return;
            View frame = frameOf(view);
            Bitmap wallpaper = frame != null ? sFrames.get(frame) : sWallpaper;
            if (sAod || wallpaper == null) {
                view.setRenderEffect(null);
                return;
            }
            int width = view.getWidth();
            int height = view.getHeight();
            if (width <= 0 || height <= 0 || !view.isAttachedToWindow()) return;
            view.getLocationOnScreen(location);
            lastX = location[0];
            lastY = location[1];
            lastW = width;
            lastH = height;
            try {
                View root = frame != null ? frame : view.getRootView();
                float x = location[0];
                float y = location[1];
                if (frame != null) {
                    frame.getLocationOnScreen(frameLocation);
                    x -= frameLocation[0];
                    y -= frameLocation[1];
                }
                int screenWidth = root.getWidth();
                int screenHeight = root.getHeight();
                float scale = Math.max((float) screenWidth / wallpaper.getWidth(), (float) screenHeight / wallpaper.getHeight());
                matrix.setScale(scale, scale);
                matrix.postTranslate((screenWidth - wallpaper.getWidth() * scale) / 2f - x, (screenHeight - wallpaper.getHeight() * scale) / 2f - y);
                BitmapShader wallpaperShader = new BitmapShader(wallpaper, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
                wallpaperShader.setLocalMatrix(matrix);

                float[] area = areaOf(root, frame);
                float light = lightFor(wallpaper, screenWidth, screenHeight, area[0], area[1], (int) area[2], (int) area[3]);
                RenderEffect effect = null;
                for (int attempt = 0; attempt < 2 && effect == null; attempt++) {
                    RuntimeShader runtimeShader = shader();
                    runtimeShader.setInputShader("wallpaper", wallpaperShader);
                    runtimeShader.setFloatUniform("resolution", width, height);
                    runtimeShader.setFloatUniform("screen", Math.max(1, screenWidth), Math.max(1, screenHeight));
                    runtimeShader.setFloatUniform("light", light);
                    runtimeShader.setFloatUniform("alpha", 1f);
                    effect = createEffect(runtimeShader, multi);
                }
                view.setRenderEffect(effect);
            } catch (Throwable t) {
                Logger.log("GlassTextEffect refresh failed: " + t);
                view.setRenderEffect(null);
            }
        }
    }
}
