import {registerPlugin} from '@capacitor/core';

import {InstagramPluginProtocol, InstagramShareOptions} from './definitions';

const InstagramPlugin = registerPlugin<Instagram>('Instagram');

export class Instagram implements InstagramPluginProtocol {
  shareLocalMedia(options: InstagramShareOptions): Promise<{ results: any[]; }> {
    return InstagramPlugin.shareLocalMedia(options);
  }
}
