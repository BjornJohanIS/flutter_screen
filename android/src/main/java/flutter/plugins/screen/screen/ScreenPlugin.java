package flutter.plugins.screen.screen;

import android.app.Activity;
import android.content.Context;
import android.provider.Settings;
import android.view.WindowManager;

import androidx.annotation.NonNull;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;

/**
 * ScreenPlugin
 */
public class ScreenPlugin implements FlutterPlugin, MethodCallHandler, ActivityAware {

    private MethodChannel channel;
    private Context applicationContext;
    private Activity activity;

    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding binding) {
        applicationContext = binding.getApplicationContext();
        channel = new MethodChannel(binding.getBinaryMessenger(), "github.com/clovisnicolas/flutter_screen");
        channel.setMethodCallHandler(this);
    }

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
        channel.setMethodCallHandler(null);
        channel = null;
    }

    @Override
    public void onAttachedToActivity(@NonNull ActivityPluginBinding binding) {
        activity = binding.getActivity();
    }

    @Override
    public void onDetachedFromActivityForConfigChanges() {
        activity = null;
    }

    @Override
    public void onReattachedToActivityForConfigChanges(@NonNull ActivityPluginBinding binding) {
        activity = binding.getActivity();
    }

    @Override
    public void onDetachedFromActivity() {
        activity = null;
    }

    @Override
    public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
        if (activity == null) {
            result.error("NO_ACTIVITY", "Plugin not attached to an activity", null);
            return;
        }

        switch (call.method) {
            case "brightness":
                result.success(getBrightness());
                break;
            case "setBrightness":
                double brightness = call.argument("brightness");
                WindowManager.LayoutParams layoutParams = activity.getWindow().getAttributes();
                layoutParams.screenBrightness = (float) brightness;
                activity.getWindow().setAttributes(layoutParams);
                result.success(null);
                break;
            case "isKeptOn":
                int flags = activity.getWindow().getAttributes().flags;
                result.success((flags & WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) != 0);
                break;
            case "keepOn":
                Boolean on = call.argument("on");
                if (on != null && on) {
                    System.out.println("Keeping screen on");
                    activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                } else {
                    System.out.println("Not keeping screen on");
                    activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                }
                result.success(null);
                break;
            default:
                result.notImplemented();
                break;
        }
    }

    private float getBrightness() {
        float brightness = activity.getWindow().getAttributes().screenBrightness;
        if (brightness < 0) { // the application is using the system brightness
            try {
                brightness = Settings.System.getInt(applicationContext.getContentResolver(),
                        Settings.System.SCREEN_BRIGHTNESS) / 255f;
            } catch (Settings.SettingNotFoundException e) {
                brightness = 1.0f;
                e.printStackTrace();
            }
        }
        return brightness;
    }
}
