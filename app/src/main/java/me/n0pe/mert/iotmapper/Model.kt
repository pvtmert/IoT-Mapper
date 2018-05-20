package me.n0pe.mert.iotmapper

import android.bluetooth.le.ScanResult
import android.location.Location
import android.support.annotation.Keep
import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.database.Exclude
import java.io.Serializable
import java.util.*
import kotlin.math.pow

@Keep
data class Position(var x: Double = 0.0, var y: Double = 0.0, var z: Double = 0.0): Comparable<Position> {

	constructor(location: LatLng): this(location.latitude, location.longitude)
	constructor(location: Location): this(location.latitude, location.longitude, location.altitude)

	override fun compareTo(other: Position): Int {
		/*
		if(other.x == null || x == null) {
			return 0
		}
		if(other.y == null || y == null) {
			return 0
		}
		if(other.z == null || z == null) {
			return 0
		}
		*/
		if (x < other.x || y < other.y || z < other.z) {
			return -1
		}
		if (x > other.x || y > other.y || z > other.z) {
			return 1
		}
		return 0
	}

	fun distanceTo(p: Position): Double {
		return (0
			+ (x - p.x).pow(2)
			+ (y - p.y).pow(2)
			+ (z - p.z).pow(2)
			).pow(0.5)
	}

	fun magnitude(): Double {
		return (x.pow(2) + y.pow(2) + z.pow(2)).pow(0.5)
	}

	fun toLatLng(): LatLng {
		return LatLng(x, y)
	}

	fun randomize(dist: Float = 0F) {
		var values = doubleArrayOf(
			Math.random() * dist,
			Math.random() * dist,
			Math.random() * dist,
			dist.toDouble()
		)
		x += (values[0] - (0.5 * values[3])).div(100_000F)
		y += (values[1] - (0.5 * values[3])).div(100_000F)
		z += (values[2] - (0.5 * values[3])).div(100_000F)
		return
	}

}


@Keep
data class Device(
	@get:Exclude
	var id: String? = null,
	var name: String? = null,
	var pics: List<String>? = null,
	var location: Position? = null,
	var lastPingTime: Date? = null,
	var users: List<String>? = null,
	var details: Device.Details? = null,
	var services: Device.Services? = null
): Serializable {

	companion object {
		var count = 0
	}

	@Keep
	class Locator(
		var rssi: List<Double>? = null,
		var callback: (() -> Unit)? = null,
		var locations: List<Location>? = null
	): Comparable<Position> {
		override fun compareTo(other: Position): Int {
			return 0
		}
	}

	@Keep
	data class Details(
		var type: Int? = null,
		var rssi: Double? = null,
		var json: String? = null,
		var note: String? = null,
		var pass: String? = null,
		var connectable: Boolean? = null
	): Serializable {

		var TAG = javaClass.simpleName

		init {
			Log.i(TAG, "init")
		}
	}

	@Keep
	data class Services(var uuid: String? = null): Serializable {

		data class Characteristics(var uuid: String? = null): Serializable {

			var TAG = javaClass.simpleName

			init {
				Log.i(TAG, "init")
			}

		}

		@Keep
		data class Info(var port: Int? = null, var header: ByteArray? = null): Serializable {

			var TAG = javaClass.simpleName

			init {
				Log.i(TAG, "init")
			}
		}

		var TAG = javaClass.simpleName

		init {
			Log.i(TAG, "init")
		}
	}

	var TAG = javaClass.simpleName

	init {
		Log.i(TAG, "init")
		count += 1
	}

	constructor(result: ScanResult): this(
		result.device.address,
		result.device.name,
		LinkedList<String>(),
		if (App.lastLoc != null) Position(App.lastLoc!!) else null,
		Date(),
		LinkedList<String>(),
		//Device.Details(result.device.type, result.rssi.toDouble(), null, null, null, true),
		null,
		null
	) {
		return
	}
}

@Keep
data class User(
	@get:Exclude
	var id: String? = null,
	var name: String? = null,
	var location: Position? = null,
	var lastPingTime: Date? = null,
	var devices: List<String>? = null,
	var pics: List<String>? = null
): Serializable {

	var TAG = javaClass.simpleName

	init {
		Log.i(TAG, "init")
	}
}
