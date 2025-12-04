# @paulmojicatech/native-clipboard

Native context menu with copy, cut, paste, and select all functionality for iOS, Android, and Web.

## Features

- 📋 **Direct Clipboard Access**: Read from and write to the system clipboard
- 🎯 **Native Gesture Recognition**: Leverages platform-native WebView capabilities
- 🔄 **Cross-App Support**: Access clipboard content from any app on the device
- 📝 **Full Context Menu**: Copy, Cut, Paste, and Select All actions
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

### Direct Clipboard Access

```typescript
import { NativeClipboard } from '@paulmojicatech/native-clipboard';

// Read from clipboard (works with text copied from any app)
const result = await NativeClipboard.read();
console.log('Clipboard:', result.value);

// Write to clipboard
await NativeClipboard.write({ string: 'Hello World!' });
```

### Context Menu

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
* [`read()`](#read)
* [`write(...)`](#write)
* [`enableContextMenu(...)`](#enablecontextmenu)
* [`disableContextMenu()`](#disablecontextmenu)
* [`addListener('clipboardMenuAction', ...)`](#addlistenerclipboardmenuaction-)
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

### read()

```typescript
read() => Promise<{ value: string; }>
```

Read text from the native clipboard

**Returns:** <code>Promise&lt;{ value: string; }&gt;</code>

--------------------

### write(...)

```typescript
write(options: { string: string; }) => Promise<void>
```

Write text to the native clipboard

| Param         | Type                             |
| ------------- | -------------------------------- |
| **`options`** | <code>{ string: string; }</code> |

--------------------


### enableContextMenu(...)

```typescript
enableContextMenu(options?: { enableCopy?: boolean | undefined; enablePaste?: boolean | undefined; enableCut?: boolean | undefined; enableSelectAll?: boolean | undefined; } | undefined) => Promise<void>
```

Enable native long press context menu for copy/paste
This enables system-level gesture recognition on the WebView

| Param         | Type                                                                                                          |
| ------------- | ------------------------------------------------------------------------------------------------------------- |
| **`options`** | <code>{ enableCopy?: boolean; enablePaste?: boolean; enableCut?: boolean; enableSelectAll?: boolean; }</code> |

--------------------


### disableContextMenu()

```typescript
disableContextMenu() => Promise<void>
```

Disable native long press context menu

--------------------


### addListener('clipboardMenuAction', ...)

```typescript
addListener(eventName: 'clipboardMenuAction', listenerFunc: (event: ClipboardMenuActionEvent) => void) => Promise<PluginListenerHandle>
```

Add a listener for clipboard context menu events

| Param              | Type                                                                                              |
| ------------------ | ------------------------------------------------------------------------------------------------- |
| **`eventName`**    | <code>'clipboardMenuAction'</code>                                                                |
| **`listenerFunc`** | <code>(event: <a href="#clipboardmenuactionevent">ClipboardMenuActionEvent</a>) =&gt; void</code> |

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

| Prop         | Type                                      |
| ------------ | ----------------------------------------- |
| **`remove`** | <code>() =&gt; Promise&lt;void&gt;</code> |


#### ClipboardMenuActionEvent

| Prop               | Type                                                   |
| ------------------ | ------------------------------------------------------ |
| **`action`**       | <code>'copy' \| 'paste' \| 'cut' \| 'selectAll'</code> |
| **`text`**         | <code>string</code>                                    |
| **`selectedText`** | <code>string</code>                                    |

</docgen-api>

## Use Cases

### Reading Clipboard from Other Apps

```typescript
// User copies text in another app (e.g., Notes, Messages, Browser)
// Then opens your app

const clipboardContent = await NativeClipboard.read();
console.log('Got text from other app:', clipboardContent.value);

// Use it in your app
document.getElementById('myInput').value = clipboardContent.value;
```

### Sharing Data Between Apps

```typescript
// Copy data to clipboard so user can paste in another app
await NativeClipboard.write({ 
  string: 'https://example.com/share/12345' 
});

// Show confirmation
alert('Link copied! You can now paste it anywhere.');
```

## How It Works

### iOS
- Uses `UIPasteboard.general` for direct clipboard access
- Adds a `UILongPressGestureRecognizer` to the WebView's scroll view
- Shows native iOS context menu using `UIMenu` (iOS 13+)
- Integrates seamlessly with system clipboard

### Android  
- Uses `ClipboardManager` for direct clipboard access
- Overrides WebView's `CustomSelectionActionModeCallback`
- Uses native Android `ActionMode` for context menu
- Automatically handles text selection and clipboard operations

### Web
- Uses browser's Clipboard API for read/write operations
- Intercepts `contextmenu` events (right-click)
- Shows custom-styled menu using Clipboard API
- Falls back to browser's default menu if disabled

## Permissions

### Android
No special permissions required. Clipboard access is automatic.

### iOS
No special permissions required. Clipboard access is automatic.

### Web
The Clipboard API requires:
- HTTPS (or localhost for development)
- User interaction (satisfied by user-initiated actions)


