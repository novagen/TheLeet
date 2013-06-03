/*   
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.rubika.aotalk.music;

import com.rubika.aotalk.service.ClientService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Receives broadcasted intents. In particular, we are interested in the
 * android.media.AUDIO_BECOMING_NOISY and android.intent.action.MEDIA_BUTTON intents, which is
 * broadcast, for example, when the user disconnects the headphones. This class works because we are
 * declaring it in a &lt;receiver&gt; tag in AndroidManifest.xml.
 */
public class MusicIntentReceiver extends BroadcastReceiver {
	@Override
    public void onReceive(Context context, Intent intent) {
    	if (intent.getAction().equals(android.media.AudioManager.ACTION_AUDIO_BECOMING_NOISY)) {
            //Toast.makeText(context, "Headphones disconnected", Toast.LENGTH_SHORT).show();
        	context.startService(new Intent(ClientService.ACTION_STOP));
        }
    }
}
