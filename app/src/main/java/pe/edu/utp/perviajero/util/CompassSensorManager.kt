package pe.edu.utp.perviajero.util

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager

data class CompassReading(
    val azimuthDegrees: Float,
    val cardinal: String,
    val accuracy: Int,
    val accelValues: FloatArray,
    val magnetValues: FloatArray
)

/**
 * Controlador de sensores para Brújula Offline (Acelerómetro + Magnetómetro).
 * Se registra en onResume y se libera en onPause para optimizar consumo de batería.
 */
class CompassSensorManager(
    context: Context,
    private val onReadingChanged: (CompassReading) -> Unit,
    private val onSensorsUnavailable: () -> Unit
) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val magnetometer: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

    private val gravity = FloatArray(3)
    private val geomagnetic = FloatArray(3)
    private val rotationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)

    private var hasGravity = false
    private var hasMagnetic = false
    private var currentAzimuth = 0f
    private var currentAccuracy = SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM

    fun hasRequiredSensors(): Boolean = accelerometer != null && magnetometer != null

    fun start() {
        if (!hasRequiredSensors()) {
            onSensorsUnavailable()
            return
        }
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI)
        sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI)
    }

    fun stop() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        val alpha = 0.18f // Filtro paso-bajo para suavizar vibraciones de la mano
        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                gravity[0] = alpha * event.values[0] + (1 - alpha) * gravity[0]
                gravity[1] = alpha * event.values[1] + (1 - alpha) * gravity[1]
                gravity[2] = alpha * event.values[2] + (1 - alpha) * gravity[2]
                hasGravity = true
            }
            Sensor.TYPE_MAGNETIC_FIELD -> {
                geomagnetic[0] = alpha * event.values[0] + (1 - alpha) * geomagnetic[0]
                geomagnetic[1] = alpha * event.values[1] + (1 - alpha) * geomagnetic[1]
                geomagnetic[2] = alpha * event.values[2] + (1 - alpha) * geomagnetic[2]
                hasMagnetic = true
            }
        }

        if (hasGravity && hasMagnetic &&
            SensorManager.getRotationMatrix(rotationMatrix, null, gravity, geomagnetic)
        ) {
            SensorManager.getOrientation(rotationMatrix, orientationAngles)
            val azimuthRad = orientationAngles[0]
            val rawDegrees = ((Math.toDegrees(azimuthRad.toDouble()) + 360.0) % 360.0).toFloat()

            // Suavizado circular evitando salto al cruzar 0°/360°
            val delta = GeoMath.relativeTurnAngle(currentAzimuth, rawDegrees)
            currentAzimuth = (currentAzimuth + delta * 0.35f + 360f) % 360f

            onReadingChanged(
                CompassReading(
                    azimuthDegrees = currentAzimuth,
                    cardinal = GeoMath.cardinalPoint(currentAzimuth),
                    accuracy = currentAccuracy,
                    accelValues = gravity.copyOf(),
                    magnetValues = geomagnetic.copyOf()
                )
            )
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        if (sensor?.type == Sensor.TYPE_MAGNETIC_FIELD) {
            currentAccuracy = accuracy
        }
    }
}
