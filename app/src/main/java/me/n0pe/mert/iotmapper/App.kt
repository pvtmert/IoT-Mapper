package me.n0pe.mert.iotmapper

import android.app.Application
import android.location.Location
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.storage.FirebaseStorage
import java.util.*


class App: Application() {

	var TAG = javaClass.simpleName

	companion object {
		var lastLoc: Location? = null
		lateinit var mFirebaseApp: FirebaseApp
		lateinit var mFirebaseRdb: FirebaseDatabase
		lateinit var mFirebaseMsg: FirebaseMessaging
		lateinit var mFirebaseErr: FirebaseAnalytics
		lateinit var mFirebasePerf: FirebasePerformance
		lateinit var mFirebaseData: FirebaseFirestore
		lateinit var mFirebaseStor: FirebaseStorage
		lateinit var mFirebaseAuth: FirebaseAuth
	}

	init {
		Log.d(TAG, "App Started")
	}

	override fun onCreate() {
		super.onCreate()
		Log.d(TAG, "created")

		mFirebaseApp = FirebaseApp.initializeApp(this)!!
		mFirebaseRdb = FirebaseDatabase.getInstance()
		mFirebaseMsg = FirebaseMessaging.getInstance()
		mFirebaseErr = FirebaseAnalytics.getInstance(this)
		mFirebasePerf = FirebasePerformance.getInstance()
		mFirebaseData = FirebaseFirestore.getInstance()
		mFirebaseStor = FirebaseStorage.getInstance()
		mFirebaseAuth = FirebaseAuth.getInstance()

		mFirebaseMsg.setAutoInitEnabled(true)
		mFirebaseRdb.setPersistenceEnabled(true)
		mFirebaseErr.setAnalyticsCollectionEnabled(true)
		mFirebasePerf.setPerformanceCollectionEnabled(true)
		mFirebaseAuth.signInAnonymously().addOnCompleteListener {
			Log.d(TAG, "logging in: ${it.result.user}")
		}

		var last: Long = 0
		Utils.startBleScan(this, {
			if (Date().time - last < 1) {
				return@startBleScan
			}
			last = Date().time
			var device = Device(it!!)
			device.location?.randomize(if (lastLoc != null) lastLoc?.accuracy!! else 0F)
			var reference = App.mFirebaseRdb.getReference("devices").child(device.id)
			reference.setValue(device).addOnCompleteListener {
				reference.addListenerForSingleValueEvent(object: ValueEventListener {
					override fun onCancelled(p0: DatabaseError?) {
						Log.e(TAG, "${p0?.message}\n${p0?.details}")
						return
					}
					override fun onDataChange(p0: DataSnapshot?) {
						Map.mDbListener?.onChildChanged(p0, null)
						return
					}
				})
				return@addOnCompleteListener
			}
			//Log.d(TAG, it.toString())
			return@startBleScan
		})

		//LocationServices.getFusedLocationProviderClient(this).requestLocationUpdates(LocationRequest.create(), Pend)
		return
	}

	override fun onLowMemory() {
		super.onLowMemory()
		return
	}

	override fun onTrimMemory(level: Int) {
		super.onTrimMemory(level)
		return
	}

	override fun onTerminate() {
		super.onTerminate()
		Log.d(TAG, "terminating")
		return
	}
}
