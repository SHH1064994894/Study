
package com.example.serialutil;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class LoaderReceiver extends BroadcastReceiver {
    private static final String TAG = "LoaderReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
    	 Intent mIntent = new Intent(context, SerialActivity.class);
    	 mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
         context.startActivity(mIntent);
    }
}
