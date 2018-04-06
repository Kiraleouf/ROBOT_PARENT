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
    private val MOTOR1_A = "BCM23"// Grey
    private val MOTOR1_B = "BCM24" // White

    private val MOTOR2_PWM = "PWM1" // Orange -> BCM13
    private val MOTOR2_A = "BCM19"// Yellow
    private val MOTOR2_B = "BCM26" // Green

    private lateinit var peripheralManagerService: PeripheralManagerService

    private lateinit var motor1Pwm: Pwm
    private lateinit var motor1A: Gpio
    private lateinit var motor1B: Gpio

    private lateinit var motor2Pwm: Pwm
    private lateinit var motor2A: Gpio
    private lateinit var motor2B: Gpio

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

    private var socket = IO.socket("http://192.168.0.26:4000")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d(TAG, "onCreate")

        // periph
        initPeriph()
        initMotors()
        testMotors()

        // socket
        connect()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.w(TAG, "onDestroy")
        motor1Pwm.setPwmFrequencyHz(0.0)
        motor1Pwm.setPwmDutyCycle(0.0)
        motor1Pwm.setEnabled(false)
        motor1Pwm.close()
        motor1A.close()
        motor1B.close()
    }

    private fun initPeriph() {
        Log.d(TAG, "Init PeripheralManagerService")
        peripheralManagerService = PeripheralManagerService()
        Log.d(TAG, "PWM pin available: " + peripheralManagerService.pwmList.forEach { pwm -> Log.d(TAG, pwm) })
    }

    private fun initMotors() {
        Log.d(TAG, "Init motors")
        motor1A = peripheralManagerService.openGpio(MOTOR1_A)
        motor1A.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW)
        motor1B = peripheralManagerService.openGpio(MOTOR1_B)
        motor1B.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW)
        motor1Pwm = peripheralManagerService.openPwm(MOTOR1_PWM)
        motor1Pwm.setPwmFrequencyHz(50.0)
        motor1Pwm.setPwmDutyCycle(100.0)

        motor2A = peripheralManagerService.openGpio(MOTOR2_A)
        motor2A.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW)
        motor2B = peripheralManagerService.openGpio(MOTOR2_B)
        motor2B.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW)
        motor2Pwm = peripheralManagerService.openPwm(MOTOR2_PWM)
        motor2Pwm.setPwmFrequencyHz(50.0)
        motor2Pwm.setPwmDutyCycle(100.0)
    }

    private fun testMotors() {
        motor1A.value = false
        motor1B.value = true
        motor1Pwm.setEnabled(true)

        motor2A.value = false
        motor2B.value = true
        motor2Pwm.setEnabled(true)

        handler.postDelayed({
            Log.w(TAG, "post delay stop motor after 5sec")
            motor1Pwm.setEnabled(false)
            motor2Pwm.setEnabled(false)
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
                    val direction = message[0]
                    when (direction) {
                        IDLE -> {
                            motor1Pwm.setEnabled(false)
                            motor2Pwm.setEnabled(false)
                        }
                        TOP, TOP_RIGHT, TOP_LEFT -> {
                            motor1Pwm.setEnabled(false)
                            motor1A.value = false
                            motor1B.value = true
                            motor1Pwm.setEnabled(true)
                            motor2Pwm.setEnabled(false)
                            motor2A.value = false
                            motor2B.value = true
                            motor2Pwm.setEnabled(true)
                        }
                        BOTTOM, BOTTOM_RIGHT, BOTTOM_LEFT -> {
                            motor1Pwm.setEnabled(false)
                            motor1A.value = true
                            motor1B.value = false
                            motor1Pwm.setEnabled(true)
                            motor2Pwm.setEnabled(false)
                            motor2A.value = true
                            motor2B.value = false
                            motor2Pwm.setEnabled(true)
                        }
                        else -> {
                            motor1Pwm.setEnabled(true)
                            motor2Pwm.setEnabled(true)
                        }
                    }
                })
    }

}
