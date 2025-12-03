# @paulmojicatech/native-clipboard

Native context menu with copy, cut, paste, and select all functionality for iOS, Android, and Web.

## Features

- 🎯 **Native Gesture Recognition**: Leverages platform-native WebView capabilities
- 📋 **Full Context Menu**: Copy, Cut, Paste, and Select All actions
- 🔔 **Event Dispatching**: Get notified when users perform clipboard actions
- 🌐 **Cross-Platform**: Works on iOS, Android, and Web
- ⚡ **Zero Configuration**: Works across the entire app without element targeting
- 🎨 **Native UI**: Uses platform-native context menus for consistent UX

## Install

```bash
npm install @paulmojicatech/native-clipboard
npx cap sync
```

## Quick Start

```typescript
import { NativeClipboard } from '@paulmojicatech/native-clipboard';

// Enable native context menu
await NativeClipboard.enableContextMenu({
  enableCopy: true,
  enablePaste: true,
  enableCut: true,
  enableSelectAll: true
});

// Listen for clipboard actions
NativeClipboard.addListener('clipboardMenuAction', (event) => {
  switch (event.action) {
    case 'copy':
      console.log('User copied:', event.selectedText);
      break;
    case 'paste':
      console.log('User pasted:', event.text);
      // Insert the pasted text into your active element
      break;
    case 'cut':
      console.log('User cut:', event.selectedText);
      // Remove selected text from your UI
      break;
    case 'selectAll':
      console.log('User selected all');
      break;
  }
});
```

For complete examples with React and Vue, see [USAGE_EXAMPLE.md](./USAGE_EXAMPLE.md).

## API

<docgen-index>

* [`echo(...)`](#echo)
* [`enableContextMenu(...)`](#enablecontextmenu)
* [`disableContextMenu()`](#disablecontextmenu)
* [`addListener('clipboardMenuAction', ...)`](#addlistenerclipboardmenuaction)
* [`removeAllListeners()`](#removealllisteners)
* [Interfaces](#interfaces)

</docgen-index>

<docgen-api>
<!--Update the source file JSDoc comments and rerun docgen to update the docs below-->

### echo(...)

```typescript
echo(options: { value: string; }) => Promise<{ value: string; }>
```

| Param         | Type                            |
| ------------- | ------------------------------- |
| **`options`** | <code>{ value: string; }</code> |

**Returns:** <code>Promise&lt;{ value: string; }&gt;</code>

--------------------

### enableContextMenu(...)

```typescript
enableContextMenu(options?: { enableCopy?: boolean; enablePaste?: boolean; enableCut?: boolean; enableSelectAll?: boolean; }) => Promise<void>
```

Enable native long press context menu for copy/paste.
This enables system-level gesture recognition on the WebView.

| Param         | Type                                                                                                              |
| ------------- | ----------------------------------------------------------------------------------------------------------------- |
| **`options`** | <code>{ enableCopy?: boolean; enablePaste?: boolean; enableCut?: boolean; enableSelectAll?: boolean; }</code> |

--------------------

### disableContextMenu()

```typescript
disableContextMenu() => Promise<void>
```

Disable native long press context menu.

--------------------

### addListener('clipboardMenuAction', ...)

```typescript
addListener(eventName: 'clipboardMenuAction', listenerFunc: (event: ClipboardMenuActionEvent) => void) => Promise<PluginListenerHandle>
```

Add a listener for clipboard context menu events

| Param              | Type                                                                                  |
| ------------------ | ------------------------------------------------------------------------------------- |
| **`eventName`**    | <code>'clipboardMenuAction'</code>                                                    |
| **`listenerFunc`** | <code>(event: <a href="#clipboardmenuactionevent">ClipboardMenuActionEvent</a>) => void</code> |

**Returns:** <code>Promise&lt;<a href="#pluginlistenerhandle">PluginListenerHandle</a>&gt;</code>

--------------------

### removeAllListeners()

```typescript
removeAllListeners() => Promise<void>
```

Remove all listeners for this plugin

--------------------

### Interfaces

#### PluginListenerHandle

| Prop         | Type                      |
| ------------ | ------------------------- |
| **`remove`** | <code>() => Promise&lt;void&gt;</code> |

#### ClipboardMenuActionEvent

| Prop               | Type                                                | Description                                         |
| ------------------ | --------------------------------------------------- | --------------------------------------------------- |
| **`action`**       | <code>'copy' \| 'paste' \| 'cut' \| 'selectAll'</code> | The action selected from the menu               |
| **`text`**         | <code>string</code>                                 | Present when action is 'paste' - the text to paste |
| **`selectedText`** | <code>string</code>                                 | Present when action is 'copy' or 'cut'             |

</docgen-api>

## How It Works

### iOS
- Adds a `UILongPressGestureRecognizer` to the WebView's scroll view
- Shows native iOS context menu using `UIMenu` (iOS 13+)
- Integrates seamlessly with system clipboard

### Android  
- Overrides WebView's `CustomSelectionActionModeCallback`
- Uses native Android `ActionMode` for context menu
- Automatically handles text selection and clipboard operations

### Web
- Intercepts `contextmenu` events (right-click)
- Shows custom-styled menu using Clipboard API
- Falls back to browser's default menu if disabled


