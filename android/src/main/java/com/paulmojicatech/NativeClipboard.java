package com.paulmojicatech;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import com.getcapacitor.Logger;
import com.getcapacitor.JSObject;

public class NativeClipboard {
    private Context context;
    private ActionMode.Callback actionModeCallback;
    private boolean enableCopy = true;
    private boolean enablePaste = true;
    private boolean enableCut = true;
    private boolean enableSelectAll = true;
    private ClipboardActionListener listener;

    public NativeClipboard(Context context) {
        this.context = context;
    }

    public String echo(String value) {
        Logger.info("Echo", value);
        return value;
    }

    public String readClipboard() {
        try {
            ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            if (clipboard != null && clipboard.hasPrimaryClip()) {
                ClipData clipData = clipboard.getPrimaryClip();
                if (clipData != null && clipData.getItemCount() > 0) {
                    ClipData.Item item = clipData.getItemAt(0);
                    CharSequence text = item.getText();
                    return text != null ? text.toString() : "";
                }
            }
        } catch (Exception e) {
            Logger.error("Error reading clipboard", e);
        }
        return "";
    }

    public void writeClipboard(String text) {
        try {
            ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            if (clipboard != null) {
                ClipData clip = ClipData.newPlainText("text", text);
                clipboard.setPrimaryClip(clip);
            }
        } catch (Exception e) {
            Logger.error("Error writing to clipboard", e);
        }
    }

    public void enableContextMenu(
            boolean enableCopy,
            boolean enablePaste,
            boolean enableCut,
            boolean enableSelectAll,
            ClipboardActionListener listener
    ) {
        this.enableCopy = enableCopy;
        this.enablePaste = enablePaste;
        this.enableCut = enableCut;
        this.enableSelectAll = enableSelectAll;
        this.listener = listener;
        
        this.actionModeCallback = createActionModeCallback();
    }

    public void disableContextMenu() {
        this.actionModeCallback = null;
        this.listener = null;
    }

    public ActionMode.Callback getActionModeCallback() {
        return actionModeCallback;
    }

    private ActionMode.Callback createActionModeCallback() {
        return new ActionMode.Callback() {
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                menu.clear();
                
                if (enableCopy) {
                    menu.add(Menu.NONE, android.R.id.copy, 1, "Copy");
                }
                if (enableCut) {
                    menu.add(Menu.NONE, android.R.id.cut, 2, "Cut");
                }
                if (enablePaste) {
                    menu.add(Menu.NONE, android.R.id.paste, 3, "Paste");
                }
                if (enableSelectAll) {
                    menu.add(Menu.NONE, android.R.id.selectAll, 4, "Select All");
                }
                
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                // Remove default items we don't want
                menu.removeItem(android.R.id.shareText);
                return true;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                if (listener == null) return false;
                
                JSObject result = new JSObject();
                int itemId = item.getItemId();

                if (itemId == android.R.id.copy) {
                    result.put("action", "copy");
                    String selectedText = listener.getSelectedText();
                    if (selectedText != null && !selectedText.isEmpty()) {
                        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText("text", selectedText);
                        clipboard.setPrimaryClip(clip);
                        result.put("selectedText", selectedText);
                    }
                    listener.onClipboardAction(result);
                    mode.finish();
                    return true;
                    
                } else if (itemId == android.R.id.cut) {
                    result.put("action", "cut");
                    String selectedText = listener.getSelectedText();
                    if (selectedText != null && !selectedText.isEmpty()) {
                        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText("text", selectedText);
                        clipboard.setPrimaryClip(clip);
                        result.put("selectedText", selectedText);
                    }
                    listener.onClipboardAction(result);
                    mode.finish();
                    return true;
                    
                } else if (itemId == android.R.id.paste) {
                    result.put("action", "paste");
                    ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                    if (clipboard.hasPrimaryClip() && clipboard.getPrimaryClipDescription().hasMimeType("text/plain")) {
                        ClipData.Item clipItem = clipboard.getPrimaryClip().getItemAt(0);
                        String pasteData = clipItem.getText().toString();
                        result.put("text", pasteData);
                    }
                    listener.onClipboardAction(result);
                    mode.finish();
                    return true;
                    
                } else if (itemId == android.R.id.selectAll) {
                    result.put("action", "selectAll");
                    listener.onClipboardAction(result);
                    // Don't finish mode for select all
                    return true;
                }
                
                return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                // Cleanup if needed
            }
        };
    }

    public interface ClipboardActionListener {
        void onClipboardAction(JSObject event);
        String getSelectedText();
    }
}

