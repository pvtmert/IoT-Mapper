package me.n0pe.mert.iotmapper

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Bundle
import android.widget.TextView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class Details: Activity(), OnMapReadyCallback, ValueEventListener {

	var TAG = javaClass.simpleName

	var mGMap: GoogleMap? = null
	var mMapView: MapView? = null

	var mDevice: Device? = null
	var sensors: Sensor? = null
	var service: SensorManager? = null
	var handler = SensorHandler({ sensor, data -> Utils.callbackHandler(this, mGMap, sensor, data) })


	override fun onCancelled(p0: DatabaseError?) {
		return
	}

	override fun onDataChange(p0: DataSnapshot?) {
		mDevice = p0?.getValue(Device::class.java)
		mMapView?.getMapAsync(this)
		this.findViewById<TextView>(R.id.text).text = (p0?.ref?.toString() + ".json\n"
			+ p0?.toString()?.replace(",".toRegex(), "\n")
		)
		return
	}

	override fun onMapReady(p0: GoogleMap?) {
		mGMap = p0
		mGMap?.addMarker(Utils.Make.marker(mDevice!!))?.showInfoWindow()
		mGMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(mDevice?.location?.toLatLng(), 20F))
		return
	}

	override fun onPostResume() {
		super.onPostResume()
		service?.registerListener(handler, sensors, SensorManager.SENSOR_DELAY_UI)
		return
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.layout)
		service = getSystemService(Context.SENSOR_SERVICE) as SensorManager
		sensors = service?.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR)
		mMapView = findViewById(R.id.map)
		mMapView?.onCreate(savedInstanceState)
		App.mFirebaseRdb.getReference("devices").child(intent.action).addListenerForSingleValueEvent(this)
		getActionBar()?.setDisplayHomeAsUpEnabled(true)
		return
	}

	override fun onNewIntent(intent: Intent?) {
		super.onNewIntent(intent)
		if(intent?.action == null) {
			finish()
		}
		return
	}

	override fun onStart() {
		super.onStart()
		mMapView?.onStart()
		return
	}

	override fun onStop() {
		super.onStop()
		mMapView?.onStop()
		return
	}

	override fun onPause() {
		super.onPause()
		mMapView?.onPause()
		service?.unregisterListener(handler)
		return
	}

	override fun onResume() {
		super.onResume()
		mMapView?.onResume()
		return
	}

	override fun onDestroy() {
		super.onDestroy()
		mMapView?.onDestroy()
		return
	}

}
