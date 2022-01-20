export interface InstagramShareOptions {
  medias: string[];
  mediaType: string;
  target: string;
}

export interface InstagramPluginProtocol {
  shareLocalMedia(options: InstagramShareOptions): Promise<{results: any[]}>;
}
