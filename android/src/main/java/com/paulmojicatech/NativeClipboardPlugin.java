package com.paulmojicatech;

import android.view.ActionMode;
import android.webkit.WebView;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

@CapacitorPlugin(name = "NativeClipboard")
public class NativeClipboardPlugin extends Plugin {

    private NativeClipboard implementation;
    private ActionMode.Callback originalCallback;

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
                    // Get selected text from WebView
                    final String[] result = {""};
                    WebView webView = (WebView) bridge.getWebView();
                    
                    webView.evaluateJavascript(
                        "(function() { return window.getSelection().toString(); })();",
                        value -> {
                            if (value != null && !value.equals("null")) {
                                result[0] = value.replace("\"", "");
                            }
                        }
                    );
                    
                    return result[0];
                }
            }
        );

        // Override the WebView's action mode callback
        bridge.getActivity().runOnUiThread(() -> {
            WebView webView = (WebView) bridge.getWebView();
            webView.setCustomSelectionActionModeCallback(new ActionMode.Callback() {
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
            });
        });

        call.resolve();
    }

    @PluginMethod
    public void disableContextMenu(PluginCall call) {
        implementation.disableContextMenu();

        bridge.getActivity().runOnUiThread(() -> {
            WebView webView = (WebView) bridge.getWebView();
            webView.setCustomSelectionActionModeCallback(null);
        });

        call.resolve();
    }
}

