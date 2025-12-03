export interface NativeClipboardPlugin {
  echo(options: { value: string }): Promise<{ value: string }>;
}
