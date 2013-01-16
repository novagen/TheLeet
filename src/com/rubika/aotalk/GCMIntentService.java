package com.rubika.aotalk;

import android.content.Context;
import android.content.Intent;
import android.app.NotificationManager;
import android.app.PendingIntent; 
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;

import com.google.android.gcm.GCMBaseIntentService;
import com.rubika.aotalk.R;
import com.rubika.aotalk.map.Map;
import com.rubika.aotalk.towerwars.Towerwars;
import com.rubika.aotalk.util.Logging;

public class GCMIntentService extends GCMBaseIntentService {
	long[] pattern = { 0, 200, 500, 100, 100, 300, 100, 500 };

	@Override
	protected void onMessage(Context context, Intent intent) {
		int requestID = (int) System.currentTimeMillis();
		
		Logging.log(TAG, "Message received");
		Logging.log(TAG, intent.getExtras().getString("message") + ", " + intent.getExtras().getString("zone") + ", " + intent.getExtras().getString("x") + ", " + intent.getExtras().getString("y"));
		
		Intent i = new Intent(context, Map.class);
		i.putExtra("name", intent.getExtras().getString("message"));
		i.putExtra("zone", intent.getExtras().getString("zone"));
		i.putExtra("x", Integer.parseInt(intent.getExtras().getString("x")));
		i.putExtra("y", Integer.parseInt(intent.getExtras().getString("y")));
		
		PendingIntent towerIntent = PendingIntent.getActivity(getApplicationContext(), 1, new Intent(context, Towerwars.class), Intent.FLAG_ACTIVITY_CLEAR_TASK);
		PendingIntent leetIntent = PendingIntent.getActivity(getApplicationContext(), 2, new Intent(context, AOTalk.class), Intent.FLAG_ACTIVITY_CLEAR_TASK);
		PendingIntent mapIntent = PendingIntent.getActivity(getApplicationContext(), requestID, i, PendingIntent.FLAG_ONE_SHOT);

		NotificationCompat.Builder builder = new Builder(this);
		builder.setSmallIcon(R.drawable.ic_notification);
		builder.setTicker(intent.getExtras().getString("tickerText"));
		builder.setContentTitle(intent.getExtras().getString("contentTitle"));
		builder.setContentText(intent.getExtras().getString("message"));
		builder.setAutoCancel(true);
		builder.setLights(0xffff0000, 200, 200);
		builder.setVibrate(pattern);
		builder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION), AudioManager.STREAM_NOTIFICATION);
		builder.setContentIntent(leetIntent);
		builder.setOngoing(false);
		builder.addAction(android.R.drawable.ic_menu_compass, "Show recent", towerIntent);
		builder.addAction(android.R.drawable.ic_menu_mylocation, "Show on map", mapIntent);
		builder.setUsesChronometer(true);
		
		NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		manager.notify(0, builder.build());
	}

	@Override
	protected void onError(Context context, String errorId) {
		Logging.log(TAG, "onError: " + errorId);
	}

	@Override
	protected void onRegistered(Context context, String registrationId) {
		Logging.log(TAG, "onRegistered: " + registrationId);
		/** TODO
		 * Add registration id to account on site
		 */
	}

	@Override
	protected void onUnregistered(Context context, String registrationId) {
		Logging.log(TAG, "onUnregistered: " + registrationId);
		/** TODO
		 * Remove registration id from account on site
		 */
	}
}
