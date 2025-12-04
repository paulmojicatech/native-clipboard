package com.paulmojicatech;

import android.os.Build;
import android.view.ActionMode;
import androidx.annotation.RequiresApi;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

@CapacitorPlugin(name = "NativeClipboard")
public class NativeClipboardPlugin extends Plugin {

    private NativeClipboard implementation;
    private ActionMode.Callback customActionModeCallback;

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
                        if (webViewObject instanceof android.webkit.WebView) {
                            android.webkit.WebView webView = (android.webkit.WebView) webViewObject;
                            webView.evaluateJavascript(
                                "(function() { return window.getSelection().toString(); })();",
                                value -> {
                                    if (value != null && !value.equals("null")) {
                                        result[0] = value.replace("\"", "");
                                    }
                                }
                            );
                        }
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
            });
        } else {
            call.resolve();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void setCustomSelectionActionMode(PluginCall call) {
        try {
            // Get the underlying WebView
            Object webViewObject = bridge.getWebView();
            if (webViewObject instanceof android.webkit.WebView) {
                android.webkit.WebView webView = (android.webkit.WebView) webViewObject;
                
                customActionModeCallback = new ActionMode.Callback() {
                    private ActionMode.Callback customCallback = implementation.getActionModeCallback();

                    @Override
                    public boolean onCreateActionMode(ActionMode mode, android.view.Menu menu) {
                        return customCallback != null && customCallback.onCreateActionMode(mode, menu);
                    }

                    @Override
                    public boolean onPrepareActionMode(ActionMode mode, android.view.Menu menu) {
                        return customCallback != null && customCallback.onPrepareActionMode(mode, menu);
                    }

                    @Override
                    public boolean onActionItemClicked(ActionMode mode, android.view.MenuItem item) {
                        return customCallback != null && customCallback.onActionItemClicked(mode, item);
                    }

                    @Override
                    public void onDestroyActionMode(ActionMode mode) {
                        if (customCallback != null) {
                            customCallback.onDestroyActionMode(mode);
                        }
                    }
                };

                webView.setCustomSelectionActionModeCallback(customActionModeCallback);
            }
        } catch (Exception e) {
            // Log error but don't crash
            android.util.Log.e("NativeClipboard", "Error setting custom selection action mode", e);
        }
        call.resolve();
    }

    @PluginMethod
    public void disableContextMenu(PluginCall call) {
        implementation.disableContextMenu();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            bridge.getActivity().runOnUiThread(() -> {
                disableCustomSelectionActionMode();
            });
        }

        call.resolve();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void disableCustomSelectionActionMode() {
        try {
            Object webViewObject = bridge.getWebView();
            if (webViewObject instanceof android.webkit.WebView) {
                android.webkit.WebView webView = (android.webkit.WebView) webViewObject;
                webView.setCustomSelectionActionModeCallback(null);
            }
        } catch (Exception e) {
            android.util.Log.e("NativeClipboard", "Error disabling custom selection action mode", e);
        }
        customActionModeCallback = null;
    }
}

