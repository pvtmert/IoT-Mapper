package me.n0pe.mert.iotmapper

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.*
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.support.v4.app.FragmentActivity
import android.util.Log
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMapOptions
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.*
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import java.util.*
import kotlin.concurrent.thread

class Map: FragmentActivity(), OnMapReadyCallback {

	var TAG = javaClass.simpleName

	companion object {
		var sensor: Sensor? = null
		var mGMap: GoogleMap? = null
		var mMapView: MapView? = null
		var service: SensorManager? = null
		var mDbListener: DbListener? = null
		var mOptions: GoogleMapOptions? = GoogleMapOptions()
			.compassEnabled(true)
			.ambientEnabled(true)
			.zoomGesturesEnabled(true)
			.tiltGesturesEnabled(true)
			.rotateGesturesEnabled(true)
			.scrollGesturesEnabled(true)
			.mapType(GoogleMap.MAP_TYPE_NORMAL)

		var mSensorHandler = SensorHandler({ sensor, data -> Utils.callbackHandler(mMapView?.context, mGMap, sensor,
			data) })

	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		initMap(savedInstanceState)
		setContentView(mMapView)
		service = getSystemService(Context.SENSOR_SERVICE) as SensorManager
		sensor = service?.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR)
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
		service?.unregisterListener(mSensorHandler)
		return
	}

	override fun onResume() {
		super.onResume()
		mMapView?.onResume()
		return
	}

	override fun onPostResume() {
		super.onPostResume()
		service?.registerListener(mSensorHandler, sensor, SensorManager.SENSOR_DELAY_UI)
		return
	}

	override fun onDestroy() {
		super.onDestroy()
		mMapView?.onDestroy()
		return
	}

	fun initMap(bundle: Bundle?) {
		mMapView = MapView(this, mOptions)
//		mMapView?.onStart()
//		mMapView?.onResume()
		mMapView?.onCreate(bundle)
		mMapView?.getMapAsync(this)
		return
	}

	override fun onMapReady(map: GoogleMap?) {

		mGMap = map
		mDbListener = DbListener(this, map)
		App.mFirebaseRdb.reference.child("devices").addListenerForSingleValueEvent(object: ValueEventListener {
			override fun onDataChange(p0: DataSnapshot?) {
				p0?.children?.forEach {
					mDbListener?.onChildChanged(it, null)
				}
				return
			}

			override fun onCancelled(p0: DatabaseError?) {
				return
			}
		})
		App.mFirebaseRdb.reference.child("devices").addChildEventListener(mDbListener)

		map?.setOnMapLoadedCallback {
			Utils.focusLocation(this, mGMap,
				Runnable {
					service?.unregisterListener(mSensorHandler)
				},
				Runnable {
					service?.registerListener(mSensorHandler, sensor, SensorManager.SENSOR_DELAY_UI)
				}
			)
			return@setOnMapLoadedCallback
		}

		try {

			map?.setIndoorEnabled(true)
			map?.setTrafficEnabled(true)
			map?.setBuildingsEnabled(true)
			map?.setMyLocationEnabled(false
				|| Build.VERSION.SDK_INT < Build.VERSION_CODES.M
				|| checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
			)

			var uiSettings = map?.uiSettings
			uiSettings?.setCompassEnabled(true)
			uiSettings?.setMapToolbarEnabled(true)
			uiSettings?.setAllGesturesEnabled(true)
			uiSettings?.setTiltGesturesEnabled(true)
			uiSettings?.setZoomControlsEnabled(true)
			uiSettings?.setZoomGesturesEnabled(true)
			uiSettings?.setRotateGesturesEnabled(true)
			uiSettings?.setScrollGesturesEnabled(true)
			uiSettings?.setMyLocationButtonEnabled(true)
			uiSettings?.setIndoorLevelPickerEnabled(true)

		} catch (e: SecurityException) {
			Log.d(TAG, e.message)
		}

		var handler = MapHandler(this)
		map?.setOnPoiClickListener(handler)
		map?.setOnMapClickListener(handler)
		map?.setOnCameraMoveListener(handler)
		map?.setOnCameraIdleListener(handler)
		map?.setOnMarkerDragListener(handler)
		map?.setOnMarkerClickListener(handler)
		map?.setOnCircleClickListener(handler)
		map?.setOnMapLongClickListener(handler)
		map?.setOnPolygonClickListener(handler)
		map?.setOnPolylineClickListener(handler)
		map?.setOnMyLocationClickListener(handler)
		map?.setOnInfoWindowClickListener(handler)
		map?.setOnInfoWindowCloseListener(handler)
		map?.setOnGroundOverlayClickListener(handler)
		map?.setOnInfoWindowLongClickListener(handler)
		map?.setOnMyLocationButtonClickListener(handler)

		return
	}
}

class MapHandler(var ctx: Context):
	GoogleMap.OnPoiClickListener,
	GoogleMap.OnMapClickListener,
	GoogleMap.OnCameraMoveListener,
	GoogleMap.OnCameraIdleListener,
	GoogleMap.OnMarkerDragListener,
	GoogleMap.OnMarkerClickListener,
	GoogleMap.OnCircleClickListener,
	GoogleMap.OnMapLongClickListener,
	GoogleMap.OnPolygonClickListener,
	GoogleMap.OnPolylineClickListener,
	GoogleMap.OnMyLocationClickListener,
	GoogleMap.OnInfoWindowClickListener,
	GoogleMap.OnInfoWindowCloseListener,
	GoogleMap.OnGroundOverlayClickListener,
	GoogleMap.OnInfoWindowLongClickListener,
	GoogleMap.OnMyLocationButtonClickListener {

	var TAG = javaClass.simpleName

	override fun onCameraMove() {
		return
	}

	override fun onPoiClick(p0: PointOfInterest?) {
		AlertDialog.Builder(ctx)
			.setMessage("${p0?.latLng}")
			.setTitle("${p0?.name}")
			.show()
		return
	}

	override fun onMapClick(p0: LatLng?) {
		return
	}

	override fun onCameraIdle() {
		return
	}

	override fun onMarkerDragEnd(p0: Marker?) {
		return
	}

	override fun onMarkerDragStart(p0: Marker?) {
		return
	}

	override fun onMarkerDrag(p0: Marker?) {
		return
	}

	override fun onMarkerClick(p0: Marker?): Boolean {
		if (p0 == null) {
			return false
		}
		if (p0.isInfoWindowShown) {
			p0.hideInfoWindow()
		} else {
			p0.showInfoWindow()
		}
		return true
	}

	override fun onCircleClick(p0: Circle?) {
		return
	}

	override fun onMapLongClick(p0: LatLng?) {
		return
	}

	override fun onPolygonClick(p0: Polygon?) {
		return
	}

	override fun onPolylineClick(p0: Polyline?) {
		return
	}

	override fun onMyLocationClick(p0: Location) {
		AlertDialog.Builder(ctx)
			.setTitle("Devices found so far:")
			.setMessage("${Device.count}")
			.show()
		return
	}

	override fun onInfoWindowClick(p0: Marker?) {
		ctx.startActivity(
			Intent(ctx, Details::class.java)
				.setAction(p0?.tag as String)
		)
		return
	}

	override fun onInfoWindowClose(p0: Marker?) {
		return
	}

	override fun onGroundOverlayClick(p0: GroundOverlay?) {
		return
	}

	override fun onInfoWindowLongClick(p0: Marker?) {
		return
	}

	override fun onMyLocationButtonClick(): Boolean {
		Map.service?.unregisterListener(Map.mSensorHandler)
		Utils.focusLocation(ctx, Map.mGMap,
			Runnable {
				Map.service?.unregisterListener(Map.mSensorHandler)
			},
			Runnable {
				Map.service?.registerListener(Map.mSensorHandler, Map.sensor, SensorManager.SENSOR_DELAY_UI)
			}
		)
		return true
	}
}

class SensorHandler(var callback: ((Sensor?, FloatArray?) -> Unit)?): SensorEventListener, SensorEventListener2 {

	var TAG = javaClass.simpleName

	override fun onAccuracyChanged(ev: Sensor?, p1: Int) {
		return
	}

	override fun onSensorChanged(ev: SensorEvent?) {
		return callback?.invoke(ev?.sensor, ev?.values)!!
	}

	override fun onFlushCompleted(sensor: Sensor?) {
		return
	}
}

class DbListener(var ctx: Activity, var map: GoogleMap? = null): ChildEventListener {

	var TAG = javaClass.simpleName
	var mMarkers = LinkedList<Marker>()
	var mChanged = LinkedList<Marker>()

	init {
		map?.setOnInfoWindowCloseListener {
			if (it in mChanged) {
				mChanged.remove(it)
				it.remove()
			}
		}
	}

	override fun onCancelled(p0: DatabaseError?) {
		return
	}

	override fun onChildMoved(p0: DataSnapshot?, p1: String?) {
		return
	}

	override fun onChildChanged(p0: DataSnapshot?, p1: String?) {
		var marker = retrieve(p0)
		if (marker == null) {
			return onChildAdded(p0, p1)
		}
		if (marker.isInfoWindowShown) {
			mMarkers.remove(marker)
			mChanged.add(marker)
			return
		}
		marker.snippet = p1
		var pos = p0?.getValue(Device::class.java)?.location?.toLatLng()
		if (pos != null) {
			marker.position = pos
		}
		return
	}

	override fun onChildAdded(p0: DataSnapshot?, p1: String?) {
		if(map == null) {
			return
		}
		thread(false, true) {
			try {
				var device = p0?.getValue(Device::class.java)
				if(device?.id == null) {
					device?.id = p0?.key
				}
				if (App.lastLoc != null && device?.location != null &&
					device.location!!.distanceTo(Position(App.lastLoc!!)) > 10.0) {
					Log.e(TAG, "too damn far! ${device}")
					return@thread
				}
				var option = Utils.Make.marker(device!!)
				ctx.runOnUiThread {
					var marker = map?.addMarker(option)
					if (marker != null) {
						mMarkers.add(marker)
						marker.snippet = p1
						marker.tag = p0?.key
					}
					return@runOnUiThread
				}
			} catch (e: Exception) {
				Log.d(TAG, e.message)
			}
			return@thread
		}.start()
		return
	}

	override fun onChildRemoved(p0: DataSnapshot?) {
		retrieve(p0)?.let {
			mMarkers.remove(it)
			it.remove()
		}
		return
	}

	fun retrieve(p: DataSnapshot?): Marker? {
		return mMarkers.find {
			it.tag == p?.key
		}
	}
}
