import { registerPlugin } from '@capacitor/core';

import type { NativeClipboardPlugin } from './definitions';

const NativeClipboard = registerPlugin<NativeClipboardPlugin>('NativeClipboard', {
  web: () => import('./web').then((m) => new m.NativeClipboardWeb()),
});

export * from './definitions';
export { NativeClipboard };
