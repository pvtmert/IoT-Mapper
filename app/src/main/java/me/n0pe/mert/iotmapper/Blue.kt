package me.n0pe.mert.iotmapper

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.IBinder

class Blue: Service() {

	var TAG = javaClass.simpleName

	override fun onBind(p0: Intent?): IBinder? {
		return null
	}

	override fun onCreate() {
		super.onCreate()
		return
	}

	override fun onLowMemory() {
		super.onLowMemory()
		return
	}

	override fun onDestroy() {
		super.onDestroy()
		return
	}
}

class Receiver: BroadcastReceiver() {
	override fun onReceive(p0: Context?, p1: Intent?) {
		return
	}
}
