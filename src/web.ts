import { WebPlugin } from '@capacitor/core';

import type { NativeClipboardPlugin } from './definitions';

export class NativeClipboardWeb extends WebPlugin implements NativeClipboardPlugin {
  async echo(options: { value: string }): Promise<{ value: string }> {
    console.log('ECHO', options);
    return options;
  }
}
