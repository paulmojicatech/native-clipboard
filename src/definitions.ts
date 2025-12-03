export interface NativeClipboardPlugin {
  echo(options: { value: string }): Promise<{ value: string }>;
  
  /**
   * Enable native long press context menu for copy/paste
   * This enables system-level gesture recognition on the WebView
   */
  enableContextMenu(options?: {
    enableCopy?: boolean;
    enablePaste?: boolean;
    enableCut?: boolean;
    enableSelectAll?: boolean;
  }): Promise<void>;
  
  /**
   * Disable native long press context menu
   */
  disableContextMenu(): Promise<void>;
  
  /**
   * Add a listener for clipboard context menu events
   */
  addListener(
    eventName: 'clipboardMenuAction',
    listenerFunc: (event: ClipboardMenuActionEvent) => void,
  ): Promise<PluginListenerHandle>;
  
  /**
   * Remove all listeners for this plugin
   */
  removeAllListeners(): Promise<void>;
}

export interface ClipboardMenuActionEvent {
  action: 'copy' | 'paste' | 'cut' | 'selectAll';
  text?: string; // Present when action is 'paste' - the text that was pasted
  selectedText?: string; // Present when action is 'copy' or 'cut' - the text that was selected/cut
}

export interface PluginListenerHandle {
  remove: () => Promise<void>;
}
