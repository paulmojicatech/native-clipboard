import { WebPlugin } from '@capacitor/core';

import type { NativeClipboardPlugin } from './definitions';

export class NativeClipboardWeb extends WebPlugin implements NativeClipboardPlugin {
  private isEnabled = false;
  private enabledActions = {
    copy: true,
    paste: true,
    cut: true,
    selectAll: true,
  };

  async echo(options: { value: string }): Promise<{ value: string }> {
    console.log('ECHO', options);
    return options;
  }

  async read(): Promise<{ value: string }> {
    try {
      const text = await navigator.clipboard.readText();
      return { value: text };
    } catch (error) {
      console.error('Failed to read clipboard:', error);
      return { value: '' };
    }
  }

  async write(options: { string: string }): Promise<void> {
    try {
      await navigator.clipboard.writeText(options.string);
    } catch (error) {
      console.error('Failed to write to clipboard:', error);
      throw error;
    }
  }

  async enableContextMenu(options?: {
    enableCopy?: boolean;
    enablePaste?: boolean;
    enableCut?: boolean;
    enableSelectAll?: boolean;
  }): Promise<void> {
    this.enabledActions.copy = options?.enableCopy ?? true;
    this.enabledActions.paste = options?.enablePaste ?? true;
    this.enabledActions.cut = options?.enableCut ?? true;
    this.enabledActions.selectAll = options?.enableSelectAll ?? true;

    if (!this.isEnabled) {
      document.addEventListener('contextmenu', this.handleContextMenu);
      this.isEnabled = true;
    }
  }

  async disableContextMenu(): Promise<void> {
    if (this.isEnabled) {
      document.removeEventListener('contextmenu', this.handleContextMenu);
      this.isEnabled = false;
    }
  }

  private handleContextMenu = (e: MouseEvent) => {
    e.preventDefault();
    this.showContextMenu(e.clientX, e.clientY);
  };

  private showContextMenu(x: number, y: number): void {
    // Remove any existing context menu
    const existingMenu = document.querySelector('.native-clipboard-context-menu');
    if (existingMenu) {
      existingMenu.remove();
    }

    const menu = document.createElement('div');
    menu.className = 'native-clipboard-context-menu';
    menu.style.cssText = `
      position: fixed;
      left: ${x}px;
      top: ${y}px;
      background: white;
      border: 1px solid #ccc;
      border-radius: 4px;
      box-shadow: 0 2px 10px rgba(0,0,0,0.2);
      z-index: 10000;
      min-width: 120px;
      font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, sans-serif;
    `;

    const selection = window.getSelection();
    const selectedText = selection?.toString() || '';
    const hasSelection = selectedText.length > 0;

    // Copy option
    if (this.enabledActions.copy && hasSelection) {
      const copyItem = this.createMenuItem('Copy', () => {
        navigator.clipboard.writeText(selectedText).then(() => {
          this.notifyListeners('clipboardMenuAction', {
            action: 'copy',
            selectedText,
          });
        });
        menu.remove();
      });
      menu.appendChild(copyItem);
    }

    // Cut option
    if (this.enabledActions.cut && hasSelection) {
      const cutItem = this.createMenuItem('Cut', () => {
        navigator.clipboard.writeText(selectedText).then(() => {
          this.notifyListeners('clipboardMenuAction', {
            action: 'cut',
            selectedText,
          });
          
          // Delete selected text
          const activeElement = document.activeElement;
          if (activeElement instanceof HTMLInputElement || activeElement instanceof HTMLTextAreaElement) {
            const start = activeElement.selectionStart || 0;
            const end = activeElement.selectionEnd || 0;
            const value = activeElement.value;
            activeElement.value = value.substring(0, start) + value.substring(end);
            activeElement.setSelectionRange(start, start);
          }
        });
        menu.remove();
      });
      menu.appendChild(cutItem);
    }

    // Paste option
    if (this.enabledActions.paste) {
      const pasteItem = this.createMenuItem('Paste', async () => {
        try {
          const text = await navigator.clipboard.readText();
          this.notifyListeners('clipboardMenuAction', {
            action: 'paste',
            text,
          });
        } catch (err) {
          console.error('Failed to read clipboard:', err);
        }
        menu.remove();
      });
      menu.appendChild(pasteItem);
    }

    // Select All option
    if (this.enabledActions.selectAll) {
      const selectAllItem = this.createMenuItem('Select All', () => {
        const selection = window.getSelection();
        const activeElement = document.activeElement;
        
        if (activeElement instanceof HTMLInputElement || activeElement instanceof HTMLTextAreaElement) {
          activeElement.select();
        } else if (selection) {
          const range = document.createRange();
          range.selectNodeContents(document.body);
          selection.removeAllRanges();
          selection.addRange(range);
        }
        
        this.notifyListeners('clipboardMenuAction', {
          action: 'selectAll',
        });
        menu.remove();
      });
      menu.appendChild(selectAllItem);
    }

    // Only show menu if there are items
    if (menu.children.length > 0) {
      document.body.appendChild(menu);

      // Position menu to stay within viewport
      const rect = menu.getBoundingClientRect();
      if (rect.right > window.innerWidth) {
        menu.style.left = `${window.innerWidth - rect.width - 10}px`;
      }
      if (rect.bottom > window.innerHeight) {
        menu.style.top = `${window.innerHeight - rect.height - 10}px`;
      }

      // Close menu when clicking outside
      const closeMenu = (e: MouseEvent) => {
        if (!menu.contains(e.target as Node)) {
          menu.remove();
          document.removeEventListener('mousedown', closeMenu);
        }
      };
      setTimeout(() => {
        document.addEventListener('mousedown', closeMenu);
      }, 100);
    }
  }

  private createMenuItem(label: string, onClick: () => void): HTMLElement {
    const item = document.createElement('div');
    item.textContent = label;
    item.style.cssText = `
      padding: 8px 16px;
      cursor: pointer;
      user-select: none;
      font-size: 14px;
    `;
    item.addEventListener('mouseenter', () => {
      item.style.background = '#f0f0f0';
    });
    item.addEventListener('mouseleave', () => {
      item.style.background = 'white';
    });
    item.addEventListener('click', onClick);
    return item;
  }
}


