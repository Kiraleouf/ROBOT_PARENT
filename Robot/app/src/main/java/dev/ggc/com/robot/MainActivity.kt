package dev.ggc.com.robot

import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import com.github.nkzawa.emitter.Emitter
import com.github.nkzawa.socketio.client.IO
import com.github.nkzawa.socketio.client.Socket
import com.google.android.things.pio.Gpio
import com.google.android.things.pio.PeripheralManagerService
import com.google.android.things.pio.Pwm

/**
 * Skeleton of an Android Things activity.
 *
 * Android Things peripheral APIs are accessible through the class
 * PeripheralManagerService. For example, the snippet below will open a GPIO pin and
 * set it to HIGH:
 *
 * <pre>{@code
 * val service = PeripheralManagerService()
 * val mLedGpio = service.openGpio("BCM6")
 * mLedGpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW)
 * mLedGpio.value = true
 * }</pre>
 * <p>
 * For more complex peripherals, look for an existing user-space driver, or implement one if none
 * is available.
 *
 * @see <a href="https://github.com/androidthings/contrib-drivers#readme">https://github.com/androidthings/contrib-drivers#readme</a>
 *
 */
class MainActivity : Activity() {
    val TAG = "MainActivity"

    private val MOTOR1_PWM = "PWM0" // Mauve -> BCM18
    //    private val MOTOR2_PWM = "PWM1" // ? -> BCM13
    private val MOTOR1_A = "BCM23"// Gris
    private val MOTOR1_B = "BCM24" // Blanc

    private lateinit var peripheralManagerService: PeripheralManagerService

    private lateinit var gpioMotorPwm: Pwm
    private lateinit var gpioMotorA: Gpio
    private lateinit var gpioMotorB: Gpio

    private val handler = Handler()

    private val LEFT = 0
    private val TOP_LEFT = 1
    private val TOP = 2
    private val TOP_RIGHT = 3
    private val RIGHT = 4
    private val BOTTOM_RIGHT = 5
    private val BOTTOM = 6
    private val BOTTOM_LEFT = 7
    private val IDLE = -1
    private var currentDir = ""

    private var socket = IO.socket("http://192.168.0.26:4000")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d(TAG, "onCreate")

        // periph
        initPeriph()
        initMotor()
        testMotor()

        // socket
        connect()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.w(TAG, "onDestroy")
        gpioMotorPwm.setPwmFrequencyHz(0.0)
        gpioMotorPwm.setPwmDutyCycle(0.0)
        gpioMotorPwm.setEnabled(false)
        gpioMotorPwm.close()
        gpioMotorA.close()
        gpioMotorB.close()
    }

    private fun initPeriph() {
        Log.d(TAG, "Init PeripheralManagerService")
        peripheralManagerService = PeripheralManagerService()
        Log.d(TAG, "PWM pin available: " + peripheralManagerService.pwmList.forEach { pwm -> Log.d(TAG, pwm) })
    }

    private fun initMotor() {
        Log.d(TAG, "Init motor 1")
        gpioMotorA = peripheralManagerService.openGpio(MOTOR1_A)
        gpioMotorA.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW)
        gpioMotorB = peripheralManagerService.openGpio(MOTOR1_B)
        gpioMotorB.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW)
        gpioMotorPwm = peripheralManagerService.openPwm(MOTOR1_PWM)
        gpioMotorPwm.setPwmFrequencyHz(50.0)
        gpioMotorPwm.setPwmDutyCycle(100.0)
    }

    private fun testMotor() {
        gpioMotorA.value = false
        gpioMotorB.value = true
        gpioMotorPwm.setEnabled(true)

        handler.postDelayed({
            Log.w(TAG, "post delay stop motor after 5sec")
            gpioMotorPwm.setEnabled(false)
        }, 5000)
    }

    private fun connect() {
        socket.connect()
                .on(Socket.EVENT_CONNECT, Emitter.Listener {
                    Log.d(TAG, "connected")
                    socket.emit("Robot connected!")
                })
                .on(Socket.EVENT_DISCONNECT, {
                    Log.d(TAG, "disconnected")
                })
                .on("message", fun(message) {
                    Log.i(TAG, "message: " + message[0])
                })
                .on("event", fun(message) {
                    Log.i(TAG, "event: " + message[0])
                })
    }

}
