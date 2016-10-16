package com.example.bluetooth;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class Tools {

	// ·¢ÏûÏ¢
	public static void senMsg(Handler handler, int msgWhat) {
		Message msg = new Message();
		msg.what = msgWhat;
		handler.sendMessage(msg);
	}

	public static void LogMsg(String title, String content) {
		Log.i(title, content);
	}

}
