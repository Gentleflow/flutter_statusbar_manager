package com.foo.flutterstatusbarmanager;

import static android.view.View.SYSTEM_UI_FLAG_IMMERSIVE;
import static android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
import static android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.ActivityManager;
import android.annotation.TargetApi;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.Registrar;

/**
 * FlutterStatusbarManagerPlugin
 */
public class FlutterStatusbarManagerPlugin implements FlutterPlugin, ActivityAware, MethodCallHandler {
    private static final String channelName = "flutter_statusbar_manager";

    private MethodChannel channel;
    private static Activity activity;

    /**
     * Plugin registration.
     */

    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding binding) {
        // The plugin is now attached to a Flutter experience
        Log.d("FlutterStatusbarManager", "FlutterStatusbarManager: Attached to Flutter Engine");
        channel = new MethodChannel(binding.getBinaryMessenger(), channelName);
        channel.setMethodCallHandler(this);
    }

    public static void registerWith(Registrar registrar) {
        // For compatibility of apps not using the v2 Android embedding
        Log.d("FlutterStatusbarManager", "FlutterStatusbarManager: Registered with Compatibility");
        activity = registrar.activity();
        final MethodChannel channel = new MethodChannel(registrar.messenger(), channelName);
        FlutterStatusbarManagerPlugin instance = new FlutterStatusbarManagerPlugin();
        channel.setMethodCallHandler(instance);
    }

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
        // The plugin is no longer attached to a Flutter experience
        Log.d("FlutterStatusbarManager", "FlutterStatusbarManager: Detached from Flutter Engine");
        channel.setMethodCallHandler(null);
    }

    /**
     * Activity registration.
     */

    @Override
    public void onAttachedToActivity(ActivityPluginBinding binding) {
        // The plugin is now attached to an Activity.
        Log.d("FlutterStatusbarManager", "FlutterStatusbarManager: Attached to Activity");
        activity = binding.getActivity();
    }

    @Override
    public void onDetachedFromActivityForConfigChanges() {
        // The attached Activity was destroyed to change configuration.
        // This call will be followed by onReattachedToActivityForConfigChanges().
        Log.d("FlutterStatusbarManager", "FlutterStatusbarManager: Detached from Activity for Config changes");
        activity = null;
    }

    @Override
    public void onReattachedToActivityForConfigChanges(ActivityPluginBinding binding) {
        // The plugin is now attached to a new Activity after a configuration change.
        Log.d("FlutterStatusbarManager", "FlutterStatusbarManager: Reattached to Activity for Config changes");
        activity = binding.getActivity();
    }

    @Override
    public void onDetachedFromActivity() {
        // The plugin is no longer associated with an Activity. Clean up references.
        Log.d("FlutterStatusbarManager", "FlutterStatusbarManager: Detached from Activity");
        activity = null;
    }

    /**
     * Method handling.
     */

    @Override
    public void onMethodCall(MethodCall call, Result result) {
        switch (call.method) {
            case "getPlatformVersion":
                result.success("Android " + android.os.Build.VERSION.RELEASE);
                break;
            case "setColor":
                handleSetColor(call, result);
                break;
            case "setTranslucent":
                handleSetTranslucent(call, result);
                break;
            case "setHidden":
                handleSetHidden(call, result);
                break;
            case "setStyle":
                handleSetStyle(call, result);
                break;
            case "getHeight":
                handleGetHeight(call, result);
                break;
            case "getNavigationHeight":
                handleNavigationHeight( result);
                break;
            case "setNetworkActivityIndicatorVisible":
                result.success(true);
                break;
            case "setNavigationBarColor":
                handleSetNavigationBarColor(call, result);
                break;
            case "setNavigationBarStyle":
                handleSetNavigationBarStyle(call, result);
                break;
            case "setFullscreen":
                handleSetFullscreen(call, result);
                break;
            default:
                result.notImplemented();
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void handleSetColor(MethodCall call, Result result) {
        if (activity == null) {
            Log.e("FlutterStatusbarManager",
                    "FlutterStatusbarManager: Ignored status bar change, current activity is null.");
            result.error("FlutterStatusbarManager",
                    "FlutterStatusbarManager: Ignored status bar change, current activity is null.", null);
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            final int color = ((Number) call.argument("color")).intValue();
            final boolean animated = call.argument("animated");

            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

            if (animated) {
                int curColor = activity.getWindow().getStatusBarColor();
                ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), curColor, color);

                colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                        activity.getWindow().setStatusBarColor((Integer) valueAnimator.getAnimatedValue());
                    }
                });
                colorAnimation.setDuration(300).setStartDelay(0);
                colorAnimation.start();
                result.success(true);
            } else {
                activity.getWindow().setStatusBarColor(color);
                result.success(true);
            }

        } else {
            Log.e("FlutterStatusbarManager",
                    "FlutterStatusbarManager: Can not change status bar color in pre lollipop android versions.");
            result.error("FlutterStatusbarManager",
                    "FlutterStatusbarManager: Can not change status bar color in pre lollipop android versions.", null);
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void handleSetTranslucent(MethodCall call, Result result) {
        if (activity == null) {
            Log.e("FlutterStatusbarManager",
                    "FlutterStatusbarManager: Ignored status bar change, current activity is null.");
            result.error("FlutterStatusbarManager",
                    "FlutterStatusbarManager: Ignored status bar change, current activity is null.", null);
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            final boolean translucent = call.argument("translucent");
            View decorView = activity.getWindow().getDecorView();
            if (translucent) {
                decorView.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
                    @Override
                    public WindowInsets onApplyWindowInsets(View v, WindowInsets insets) {
                        WindowInsets defaultInsets = v.onApplyWindowInsets(insets);
                        return defaultInsets.replaceSystemWindowInsets(defaultInsets.getSystemWindowInsetLeft(), 0,
                                defaultInsets.getSystemWindowInsetRight(), defaultInsets.getSystemWindowInsetBottom());
                    }
                });
            } else {
                decorView.setOnApplyWindowInsetsListener(null);
            }
            ViewCompat.requestApplyInsets(decorView);
            result.success(true);
        } else {
            Log.e("FlutterStatusbarManager",
                    "FlutterStatusbarManager: Can not change status bar color in pre lollipop android versions.");
            result.error("FlutterStatusbarManager",
                    "FlutterStatusbarManager: Can not change status bar color in pre lollipop android versions.", null);
        }
    }

    private void handleSetHidden(MethodCall call, Result result) {
        if (activity == null) {
            Log.e("FlutterStatusbarManager", "FlutterStatusbarManager: Ignored status bar change, current activity is null.");
            result.error("FlutterStatusbarManager", "FlutterStatusbarManager: Ignored status bar change, current activity is null.", null);
            return;
        }

        final boolean hidden = call.argument("hidden");
        Window window = activity.getWindow();
        if (hidden) {
            window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            window.clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                WindowManager.LayoutParams layoutParams = window.getAttributes();
                layoutParams.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
                window.setAttributes(layoutParams);
            }
        } else {
            window.addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
            window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        result.success(true);
    }

    private void handleSetFullscreen(MethodCall call, Result result) {
        if (activity == null) {
            Log.e("FlutterStatusbarManager", "FlutterStatusbarManager: Ignored status bar change, current activity is null.");
            result.error("FlutterStatusbarManager", "FlutterStatusbarManager: Ignored status bar change, current activity is null.", null);
            return;
        }
        final boolean hidden = call.argument("hidden");
        Window window = activity.getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowInsetsController insetsController = window.getInsetsController();
            assert insetsController != null;
            if (hidden) {
                insetsController.hide(WindowInsets.Type.statusBars());
                insetsController.hide(WindowInsets.Type.navigationBars());
            } else {
                insetsController.show(WindowInsets.Type.statusBars());
                insetsController.show(WindowInsets.Type.navigationBars());
            }
        }
        int flag = (SYSTEM_UI_FLAG_LAYOUT_STABLE | SYSTEM_UI_FLAG_IMMERSIVE | SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            boolean inMultiWindowMode = activity.isInMultiWindowMode();
            if (!inMultiWindowMode) {
                flag = flag | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
            }
        }
        flag = flag | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
        if (hidden) {
            flag = flag | View.SYSTEM_UI_FLAG_FULLSCREEN;
            flag = flag | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        }
        handleSetHidden(call, result);
        window.getDecorView().setSystemUiVisibility(flag);
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void handleSetStyle(MethodCall call, Result result) {
        if (activity == null) {
            Log.e("FlutterStatusbarManager",
                    "FlutterStatusbarManager: Ignored status bar change, current activity is null.");
            result.error("FlutterStatusbarManager",
                    "FlutterStatusbarManager: Ignored status bar change, current activity is null.", null);
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            final String style = call.argument("style");

            View decorView = activity.getWindow().getDecorView();
            int flags = decorView.getSystemUiVisibility();
            if (style.equals("dark-content")) {
                flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            } else {
                flags &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            }
            decorView.setSystemUiVisibility(flags);
            result.success(true);
        } else {
            Log.e("FlutterStatusbarManager",
                    "FlutterStatusbarManager: Can not change status bar style in pre M android versions.");
            result.error("FlutterStatusbarManager",
                    "FlutterStatusbarManager: Can not change status bar style in pre M android versions.", null);
        }
    }

    private void handleGetHeight(MethodCall call, Result result) {
        int height = 0;
        int resourceId = activity.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            height = activity.getResources().getDimensionPixelSize(resourceId);
        }
        result.success((double) toDIPFromPixel(height));
    }

    boolean isResultHeight = false;
    public void handleNavigationHeight(final Result result) {
        if (activity == null) {
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            activity.getWindow().getDecorView().setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
                @Override
                public WindowInsets onApplyWindowInsets(View v, WindowInsets windowInsets) {
                    if (windowInsets != null) {
                        int b = windowInsets.getSystemWindowInsetBottom();
                        if(!isResultHeight){
                            isResultHeight = true;
                            result.success((double) toDIPFromPixel(b));
                        }
                    }
                    return windowInsets;
                }
            });
        }
    }

    @TargetApi((Build.VERSION_CODES.LOLLIPOP))
    private void handleSetNavigationBarColor(MethodCall call, Result result) {
        if (activity == null) {
            Log.e("FlutterStatusbarManager",
                    "FlutterStatusbarManager: Ignored status bar change, current activity is null.");
            result.error("FlutterStatusbarManager",
                    "FlutterStatusbarManager: Ignored status bar change, current activity is null.", null);
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            @SuppressWarnings("unkchecked") final int color = ((Number) call.argument("color")).intValue();
            final boolean animated = call.argument("animated");

            if (animated) {
                int curColor = activity.getWindow().getNavigationBarColor();
                ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), curColor, color);

                colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                        activity.getWindow().setNavigationBarColor((Integer) valueAnimator.getAnimatedValue());
                    }
                });
                colorAnimation.setDuration(300).setStartDelay(0);
                colorAnimation.start();
                result.success(true);
            } else {
                activity.getWindow().setNavigationBarColor(color);
                result.success(true);
            }
        } else {
            Log.e("FlutterStatusbarManager",
                    "FlutterStatusbarManager: Can not change status bar style in pre M android versions.");
            result.error("FlutterStatusbarManager",
                    "FlutterStatusbarManager: Can not change status bar style in pre M android versions.", null);
        }
    }

    @TargetApi((Build.VERSION_CODES.O))
    private void handleSetNavigationBarStyle(MethodCall call, Result result) {
        if (activity == null) {
            Log.e("FlutterStatusbarManager",
                    "FlutterStatusbarManager: Ignored status bar change, current activity is null.");
            result.error("FlutterStatusbarManager",
                    "FlutterStatusbarManager: Ignored status bar change, current activity is null.", null);
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            final String style = call.argument("style");

            View decorView = activity.getWindow().getDecorView();
            int flags = decorView.getSystemUiVisibility();
            if (style.equals("dark")) {
                flags &= ~View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
            } else {
                flags |= View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
            }
            decorView.setSystemUiVisibility(flags);
            result.success(true);
        } else {
            Log.e("FlutterStatusbarManager",
                    "FlutterStatusbarManager: Can not change status bar style in pre M android versions.");
            result.error("FlutterStatusbarManager",
                    "FlutterStatusbarManager: Can not change status bar style in pre M android versions.", null);
        }
    }

    private int toDIPFromPixel(int pixel) {
        float scale = getDensity();
        return (int) ((pixel - 0.5f) / scale);
    }

    private int toPixelFromDPI(int dip) {
        float scale = getDensity();
        return (int) (dip * scale + 0.5f);
    }

    private float getDensity() {
        return activity.getResources().getDisplayMetrics().density;
    }

}