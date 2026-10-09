
package com.example.maddlab05

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity(), SensorEventListener {

    // Accelerometer-related variables
    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null

    // UI components
    private lateinit var movementArea: FrameLayout
    private lateinit var androidImage: ImageView

    // Smoothed accelerometer readings
    private var filteredX = 0f
    private var filteredY = 0f

    // Initial sensor readings used for calibration
    private var referenceX = 0f
    private var referenceY = 0f
    private var isCalibrated = false

    // Movement settings
    private val smoothingFactor = 0.15f
    private val movementSensitivity = 28f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        // Hide the ActionBar for a simple full-screen design
        supportActionBar?.hide()

        // Connect Kotlin variables with XML views
        movementArea = findViewById(R.id.movementArea)
        androidImage = findViewById(R.id.androidImage)

        // Get the Android SensorManager
        sensorManager = getSystemService(
            Context.SENSOR_SERVICE
        ) as SensorManager

        // Get the accelerometer sensor
        accelerometer = sensorManager.getDefaultSensor(
            Sensor.TYPE_ACCELEROMETER
        )

        // Check whether the accelerometer is available
        if (accelerometer == null) {
            Toast.makeText(
                this,
                "Accelerometer sensor is not available",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onResume() {
        super.onResume()

        // Recalibrate when the app becomes active
        isCalibrated = false

        // Reset the image to the center
        androidImage.translationX = 0f
        androidImage.translationY = 0f

        // Start receiving accelerometer data
        accelerometer?.let { sensor ->
            sensorManager.registerListener(
                this,
                sensor,
                SensorManager.SENSOR_DELAY_GAME
            )
        }
    }

    override fun onPause() {
        // Stop listening when the app is not active
        sensorManager.unregisterListener(this)
        super.onPause()
    }

    override fun onSensorChanged(event: SensorEvent?) {

        // Accept only accelerometer events
        if (event?.sensor?.type != Sensor.TYPE_ACCELEROMETER) {
            return
        }

        // Current accelerometer values
        val x = event.values[0]
        val y = event.values[1]

        // Use the initial position as the center
        if (!isCalibrated) {
            referenceX = x
            referenceY = y

            filteredX = x
            filteredY = y

            isCalibrated = true
            return
        }

        // Apply a low-pass filter for smoother movement
        filteredX += smoothingFactor * (x - filteredX)
        filteredY += smoothingFactor * (y - filteredY)

        // Convert movement sensitivity from dp to pixels
        val density = resources.displayMetrics.density
        val sensitivityPx = movementSensitivity * density

        // Calculate the desired image movement
        val targetX = (referenceX - filteredX) * sensitivityPx
        val targetY = (filteredY - referenceY) * sensitivityPx

        // Calculate allowed movement within the screen
        val maxMoveX = (
                (movementArea.width - androidImage.width) / 2f
                ).coerceAtLeast(0f)

        val maxMoveY = (
                (movementArea.height - androidImage.height) / 2f
                ).coerceAtLeast(0f)

        // Keep the image inside the screen boundaries
        val safeX = targetX.coerceIn(-maxMoveX, maxMoveX)
        val safeY = targetY.coerceIn(-maxMoveY, maxMoveY)

        // Move the Android image
        androidImage.translationX = safeX
        androidImage.translationY = safeY
    }

    override fun onAccuracyChanged(
        sensor: Sensor?,
        accuracy: Int
    ) {
        // No additional action is required for this lab
    }
}
