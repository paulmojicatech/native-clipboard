package com.paulmojicatech;

import android.os.Build;
import android.view.ActionMode;
import android.view.View;
import androidx.annotation.RequiresApi;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import java.lang.reflect.Method;

@CapacitorPlugin(name = "NativeClipboard")
public class NativeClipboardPlugin extends Plugin {

    private NativeClipboard implementation;
    private ActionMode.Callback customActionModeCallback;
    private View.OnLongClickListener longPressListener;

    @Override
    public void load() {
        implementation = new NativeClipboard(getContext());
    }

    @PluginMethod
    public void echo(PluginCall call) {
        String value = call.getString("value");

        JSObject ret = new JSObject();
        ret.put("value", implementation.echo(value));
        call.resolve(ret);
    }

    @PluginMethod
    public void read(PluginCall call) {
        String value = implementation.readClipboard();
        JSObject ret = new JSObject();
        ret.put("value", value != null ? value : "");
        call.resolve(ret);
    }

    @PluginMethod
    public void write(PluginCall call) {
        String text = call.getString("string");
        if (text != null) {
            implementation.writeClipboard(text);
        }
        call.resolve();
    }

    @PluginMethod
    public void enableContextMenu(PluginCall call) {
        boolean enableCopy = call.getBoolean("enableCopy", true);
        boolean enablePaste = call.getBoolean("enablePaste", true);
        boolean enableCut = call.getBoolean("enableCut", true);
        boolean enableSelectAll = call.getBoolean("enableSelectAll", true);

        implementation.enableContextMenu(
            enableCopy,
            enablePaste,
            enableCut,
            enableSelectAll,
            new NativeClipboard.ClipboardActionListener() {
                @Override
                public void onClipboardAction(JSObject event) {
                    notifyListeners("clipboardMenuAction", event);
                }

                @Override
                public String getSelectedText() {
                    // Get selected text from WebView via JavaScript
                    final String[] result = {""};
                    try {
                        Object webViewObject = bridge.getWebView();
                        // Use reflection to call evaluateJavascript
                        Method method = webViewObject.getClass().getMethod("evaluateJavascript", String.class, android.webkit.ValueCallback.class);
                        method.invoke(webViewObject,
                            "(function() { return window.getSelection().toString(); })();",
                            (android.webkit.ValueCallback<String>) value -> {
                                if (value != null && !value.equals("null")) {
                                    result[0] = value.replace("\"", "");
                                }
                            }
                        );
                    } catch (Exception e) {
                        android.util.Log.e("NativeClipboard", "Error getting selected text", e);
                    }
                    return result[0];
                }
            }
        );

        // Override the WebView's action mode callback (API 23+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            bridge.getActivity().runOnUiThread(() -> {
                setCustomSelectionActionMode(call);
                setupLongPressListener();
            });
        } else {
            bridge.getActivity().runOnUiThread(() -> {
                setupLongPressListener();
            });
            call.resolve();
        }
    }

    private void setupLongPressListener() {
        try {
            Object webViewObject = bridge.getWebView();
            
            longPressListener = v -> {
                android.util.Log.d("NativeClipboard", "Long press detected - showing paste menu");
                
                // Show paste menu on UI thread
                bridge.getActivity().runOnUiThread(() -> {
                    implementation.showPopupMenu((View) webViewObject, null);
                });
                
                return true;
            };
            
            // Set the long click listener
            Method setLongClickMethod = webViewObject.getClass().getMethod("setOnLongClickListener", View.OnLongClickListener.class);
            setLongClickMethod.invoke(webViewObject, longPressListener);
            
            android.util.Log.d("NativeClipboard", "Long press listener set successfully");
        } catch (Exception e) {
            android.util.Log.e("NativeClipboard", "Error setting long press listener", e);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void setCustomSelectionActionMode(PluginCall call) {
        try {
            // Get the underlying WebView
            Object webViewObject = bridge.getWebView();
            
            android.util.Log.d("NativeClipboard", "Setting custom selection action mode");

            customActionModeCallback = new ActionMode.Callback() {
                private ActionMode.Callback customCallback = implementation.getActionModeCallback();

                @Override
                public boolean onCreateActionMode(ActionMode mode, android.view.Menu menu) {
                    android.util.Log.d("NativeClipboard", "onCreateActionMode called");
                    return customCallback != null && customCallback.onCreateActionMode(mode, menu);
                }

                @Override
                public boolean onPrepareActionMode(ActionMode mode, android.view.Menu menu) {
                    android.util.Log.d("NativeClipboard", "onPrepareActionMode called");
                    return customCallback != null && customCallback.onPrepareActionMode(mode, menu);
                }

                @Override
                public boolean onActionItemClicked(ActionMode mode, android.view.MenuItem item) {
                    android.util.Log.d("NativeClipboard", "onActionItemClicked: " + item.getTitle());
                    return customCallback != null && customCallback.onActionItemClicked(mode, item);
                }

                @Override
                public void onDestroyActionMode(ActionMode mode) {
                    android.util.Log.d("NativeClipboard", "onDestroyActionMode called");
                    if (customCallback != null) {
                        customCallback.onDestroyActionMode(mode);
                    }
                }
            };

            // Use reflection to call setCustomSelectionActionModeCallback
            Method method = webViewObject.getClass().getMethod("setCustomSelectionActionModeCallback", ActionMode.Callback.class);
            method.invoke(webViewObject, customActionModeCallback);
            android.util.Log.d("NativeClipboard", "Custom selection action mode callback set successfully");
        } catch (Exception e) {
            // Log error but don't crash
            android.util.Log.e("NativeClipboard", "Error setting custom selection action mode", e);
        }
        call.resolve();
    }

    @PluginMethod
    public void disableContextMenu(PluginCall call) {
        implementation.disableContextMenu();

        bridge.getActivity().runOnUiThread(() -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                disableCustomSelectionActionMode();
            }
            removeLongPressListener();
        });

        call.resolve();
    }

    private void removeLongPressListener() {
        try {
            Object webViewObject = bridge.getWebView();
            Method setLongClickMethod = webViewObject.getClass().getMethod("setOnLongClickListener", View.OnLongClickListener.class);
            setLongClickMethod.invoke(webViewObject, (View.OnLongClickListener) null);
            longPressListener = null;
            android.util.Log.d("NativeClipboard", "Long press listener removed");
        } catch (Exception e) {
            android.util.Log.e("NativeClipboard", "Error removing long press listener", e);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void disableCustomSelectionActionMode() {
        try {
            Object webViewObject = bridge.getWebView();
            // Use reflection to call setCustomSelectionActionModeCallback
            Method method = webViewObject.getClass().getMethod("setCustomSelectionActionModeCallback", ActionMode.Callback.class);
            method.invoke(webViewObject, (ActionMode.Callback) null);
        } catch (Exception e) {
            android.util.Log.e("NativeClipboard", "Error disabling custom selection action mode", e);
        }
        customActionModeCallback = null;
    }
}

