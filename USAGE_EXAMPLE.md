# Native Clipboard Plugin - Usage Example

This plugin provides native WebView-level context menu support for copying, cutting, pasting, and selecting text.

## Installation

```bash
npm install @paulmojicatech/native-clipboard
npx cap sync
```

## Basic Usage

### 1. Enable Context Menu

```typescript
import { NativeClipboard } from '@paulmojicatech/native-clipboard';

// Enable native context menu with all actions
await NativeClipboard.enableContextMenu({
  enableCopy: true,
  enablePaste: true,
  enableCut: true,
  enableSelectAll: true
});
```

### 2. Listen for Clipboard Menu Actions

```typescript
// Add a listener for clipboard events
NativeClipboard.addListener('clipboardMenuAction', (event) => {
  console.log('Clipboard action:', event.action);
  
  switch (event.action) {
    case 'copy':
      console.log('User copied:', event.selectedText);
      // Text is automatically copied to clipboard
      break;
      
    case 'cut':
      console.log('User cut:', event.selectedText);
      // Text is automatically copied to clipboard
      // You should remove the selected text from your UI
      break;
      
    case 'paste':
      console.log('User wants to paste:', event.text);
      // Insert the pasted text into your active element
      const activeElement = document.activeElement;
      if (activeElement instanceof HTMLInputElement || activeElement instanceof HTMLTextAreaElement) {
        const start = activeElement.selectionStart || 0;
        const end = activeElement.selectionEnd || 0;
        const currentValue = activeElement.value;
        activeElement.value = currentValue.substring(0, start) + event.text + currentValue.substring(end);
      }
      break;
      
    case 'selectAll':
      console.log('User selected all text');
      // Selection is automatically handled
      break;
  }
});
```

### 3. Disable When Done

```typescript
// Disable context menu when no longer needed
await NativeClipboard.disableContextMenu();

// Remove all event listeners
await NativeClipboard.removeAllListeners();
```

## Complete Example (React)

```typescript
import React, { useEffect, useRef } from 'react';
import { NativeClipboard } from '@paulmojicatech/native-clipboard';

const TextEditor = () => {
  const textAreaRef = useRef<HTMLTextAreaElement>(null);

  useEffect(() => {
    // Enable context menu
    NativeClipboard.enableContextMenu({
      enableCopy: true,
      enablePaste: true,
      enableCut: true,
      enableSelectAll: true
    });

    // Add event listener
    const listener = NativeClipboard.addListener('clipboardMenuAction', (event) => {
      const textarea = textAreaRef.current;
      if (!textarea) return;

      switch (event.action) {
        case 'copy':
          console.log('Copied:', event.selectedText);
          break;
          
        case 'cut':
          console.log('Cut:', event.selectedText);
          // Remove selected text
          const start = textarea.selectionStart || 0;
          const end = textarea.selectionEnd || 0;
          textarea.value = 
            textarea.value.substring(0, start) + 
            textarea.value.substring(end);
          textarea.setSelectionRange(start, start);
          break;
          
        case 'paste':
          // Insert pasted text
          if (event.text) {
            const start = textarea.selectionStart || 0;
            const end = textarea.selectionEnd || 0;
            const currentValue = textarea.value;
            
            textarea.value = 
              currentValue.substring(0, start) + 
              event.text + 
              currentValue.substring(end);
            
            const newPosition = start + event.text.length;
            textarea.setSelectionRange(newPosition, newPosition);
            textarea.focus();
          }
          break;
          
        case 'selectAll':
          textarea.select();
          break;
      }
    });

    // Cleanup
    return () => {
      NativeClipboard.disableContextMenu();
      listener.then(handle => handle.remove());
    };
  }, []);

  return (
    <div>
      <h2>Text Editor</h2>
      <textarea
        ref={textAreaRef}
        placeholder="Right-click or long-press to see native context menu..."
        style={{
          width: '100%',
          minHeight: '200px',
          padding: '10px',
          fontSize: '16px'
        }}
      />
    </div>
  );
};

export default TextEditor;
```

## Complete Example (Vue)

```vue
<template>
  <div>
    <h2>Text Editor</h2>
    <textarea
      ref="textarea"
      v-model="content"
      placeholder="Right-click or long-press to see native context menu..."
      style="width: 100%; min-height: 200px; padding: 10px; font-size: 16px;"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue';
import { NativeClipboard, type PluginListenerHandle } from '@paulmojicatech/native-clipboard';

const content = ref('');
const textarea = ref<HTMLTextAreaElement | null>(null);
let listenerHandle: PluginListenerHandle | null = null;

onMounted(async () => {
  // Enable context menu
  await NativeClipboard.enableContextMenu({
    enableCopy: true,
    enablePaste: true,
    enableCut: true,
    enableSelectAll: true
  });

  // Add event listener
  listenerHandle = await NativeClipboard.addListener('clipboardMenuAction', (event) => {
    const el = textarea.value;
    if (!el) return;

    switch (event.action) {
      case 'copy':
        console.log('Copied:', event.selectedText);
        break;
        
      case 'cut':
        console.log('Cut:', event.selectedText);
        const start = el.selectionStart || 0;
        const end = el.selectionEnd || 0;
        content.value = 
          content.value.substring(0, start) + 
          content.value.substring(end);
        setTimeout(() => el.setSelectionRange(start, start), 0);
        break;
        
      case 'paste':
        if (event.text) {
          const start = el.selectionStart || 0;
          const end = el.selectionEnd || 0;
          content.value = 
            content.value.substring(0, start) + 
            event.text + 
            content.value.substring(end);
          setTimeout(() => {
            const newPosition = start + event.text!.length;
            el.setSelectionRange(newPosition, newPosition);
            el.focus();
          }, 0);
        }
        break;
        
      case 'selectAll':
        el.select();
        break;
    }
  });
});

onUnmounted(async () => {
  await NativeClipboard.disableContextMenu();
  if (listenerHandle) {
    await listenerHandle.remove();
  }
});
</script>
```

## API Reference

### Methods

#### `enableContextMenu(options)`
Enables native context menu on the WebView with specified actions.

- **Parameters:**
  - `enableCopy` (boolean, optional): Enable copy menu item. Default: `true`
  - `enablePaste` (boolean, optional): Enable paste menu item. Default: `true`
  - `enableCut` (boolean, optional): Enable cut menu item. Default: `true`
  - `enableSelectAll` (boolean, optional): Enable select all menu item. Default: `true`

#### `disableContextMenu()`
Disables the native context menu.

#### `addListener(eventName, callback)`
Adds a listener for clipboard menu actions.

- **Parameters:**
  - `eventName`: Must be `'clipboardMenuAction'`
  - `callback`: Function to handle the event

- **Returns:** `Promise<PluginListenerHandle>` with a `remove()` method

#### `removeAllListeners()`
Removes all event listeners for this plugin.

### Events

#### `clipboardMenuAction`
Fired when a user selects an action from the context menu.

- **Event Data:**
  - `action` ('copy' | 'paste' | 'cut' | 'selectAll'): The action selected
  - `selectedText` (string, optional): Present when action is 'copy' or 'cut'
  - `text` (string, optional): Present when action is 'paste'

## Platform-Specific Notes

### Web
- Overrides default right-click context menu
- Uses custom styled menu with clipboard API
- Requires clipboard permissions for paste functionality

### Android
- Uses `setCustomSelectionActionModeCallback` to override WebView's text selection menu
- Integrates with native Android ActionMode
- Automatically handles clipboard permissions

### iOS
- Uses `UILongPressGestureRecognizer` on WebView's scroll view
- Shows native iOS context menu (UIMenu on iOS 13+)
- Automatically handles clipboard permissions

## Permissions

### Android
No special permissions required. Clipboard access is automatic.

### iOS
No special permissions required. Clipboard access is automatic.

### Web
The Clipboard API requires:
- HTTPS (or localhost for development)
- User interaction (satisfied by context menu gesture)


## Complete Example (React)

```typescript
import React, { useEffect, useRef } from 'react';
import { NativeClipboard } from '@paulmojicatech/native-clipboard';

const TextEditor = () => {
  const textAreaRef = useRef<HTMLTextAreaElement>(null);
  const elementId = 'editor-textarea';

  useEffect(() => {
    // Register long press listener
    NativeClipboard.registerLongPressListener({
      elementId,
      enableCopy: true,
      enablePaste: true
    });

    // Add event listener
    const listener = NativeClipboard.addListener('clipboardMenuAction', (event) => {
      if (event.elementId !== elementId) return;

      if (event.action === 'copy') {
        console.log('Text copied:', event.selectedText);
        // Optional: show a toast notification
      } else if (event.action === 'paste') {
        const textarea = textAreaRef.current;
        if (textarea && event.text) {
          const start = textarea.selectionStart || 0;
          const end = textarea.selectionEnd || 0;
          const currentValue = textarea.value;
          
          // Insert pasted text at cursor position
          textarea.value = 
            currentValue.substring(0, start) + 
            event.text + 
            currentValue.substring(end);
          
          // Update cursor position
          const newPosition = start + event.text.length;
          textarea.setSelectionRange(newPosition, newPosition);
          textarea.focus();
        }
      }
    });

    // Cleanup
    return () => {
      NativeClipboard.unregisterLongPressListener({ elementId });
      listener.then(handle => handle.remove());
    };
  }, []);

  return (
    <div>
      <h2>Text Editor</h2>
      <textarea
        ref={textAreaRef}
        id={elementId}
        placeholder="Long press to see copy/paste menu..."
        style={{
          width: '100%',
          minHeight: '200px',
          padding: '10px',
          fontSize: '16px'
        }}
      />
    </div>
  );
};

export default TextEditor;
```

## Complete Example (Vue)

```vue
<template>
  <div>
    <h2>Text Editor</h2>
    <textarea
      :id="elementId"
      ref="textarea"
      v-model="content"
      placeholder="Long press to see copy/paste menu..."
      style="width: 100%; min-height: 200px; padding: 10px; font-size: 16px;"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue';
import { NativeClipboard, type PluginListenerHandle } from '@paulmojicatech/native-clipboard';

const elementId = 'editor-textarea';
const content = ref('');
const textarea = ref<HTMLTextAreaElement | null>(null);
let listenerHandle: PluginListenerHandle | null = null;

onMounted(async () => {
  // Register long press listener
  await NativeClipboard.registerLongPressListener({
    elementId,
    enableCopy: true,
    enablePaste: true
  });

  // Add event listener
  listenerHandle = await NativeClipboard.addListener('clipboardMenuAction', (event) => {
    if (event.elementId !== elementId) return;

    if (event.action === 'copy') {
      console.log('Text copied:', event.selectedText);
    } else if (event.action === 'paste' && event.text) {
      const el = textarea.value;
      if (el) {
        const start = el.selectionStart || 0;
        const end = el.selectionEnd || 0;
        
        content.value = 
          content.value.substring(0, start) + 
          event.text + 
          content.value.substring(end);
        
        // Update cursor position
        setTimeout(() => {
          const newPosition = start + event.text.length;
          el.setSelectionRange(newPosition, newPosition);
          el.focus();
        }, 0);
      }
    }
  });
});

onUnmounted(async () => {
  await NativeClipboard.unregisterLongPressListener({ elementId });
  if (listenerHandle) {
    await listenerHandle.remove();
  }
});
</script>
```

## API Reference

### Methods

#### `registerLongPressListener(options)`
Registers a long press listener on an HTML element.

- **Parameters:**
  - `elementId` (string, required): The ID of the HTML element
  - `enableCopy` (boolean, optional): Enable copy menu item. Default: `true`
  - `enablePaste` (boolean, optional): Enable paste menu item. Default: `true`

#### `unregisterLongPressListener(options)`
Removes a previously registered long press listener.

- **Parameters:**
  - `elementId` (string, required): The ID of the HTML element

#### `addListener(eventName, callback)`
Adds a listener for clipboard menu actions.

- **Parameters:**
  - `eventName`: Must be `'clipboardMenuAction'`
  - `callback`: Function to handle the event

- **Returns:** `Promise<PluginListenerHandle>` with a `remove()` method

#### `removeAllListeners()`
Removes all event listeners for this plugin.

### Events

#### `clipboardMenuAction`
Fired when a user selects an action from the context menu.

- **Event Data:**
  - `action` ('copy' | 'paste'): The action selected
  - `elementId` (string): The ID of the element
  - `selectedText` (string, optional): Present when action is 'copy'
  - `text` (string, optional): Present when action is 'paste'

## Platform-Specific Notes

### Web
- Uses browser's native context menu styling on desktop
- Custom styled menu on mobile/touch devices
- Requires clipboard permissions for paste functionality

### Android
- Uses native Android ActionMode for context menu
- Automatically handles clipboard permissions

### iOS
- Uses UIMenuController for native iOS context menu
- Automatically handles clipboard permissions
- Works with iOS 13+ optimized menu presentation

## Permissions

### Android
No special permissions required. Clipboard access is automatic.

### iOS
No special permissions required. Clipboard access is automatic.

### Web
The Clipboard API requires:
- HTTPS (or localhost for development)
- User interaction (satisfied by long press gesture)
