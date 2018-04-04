package dev.ggc.com.robot

import android.app.Activity
import android.os.Bundle
import android.telecom.Call
import android.util.Log
import com.github.nkzawa.emitter.Emitter
import com.github.nkzawa.socketio.client.IO
import com.github.nkzawa.socketio.client.Socket

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

    private var socket = IO.socket("http://192.168.8.196:4000")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        connect()
    }

    private fun connect() {
        socket.connect()
                .on(Socket.EVENT_CONNECT,
                        {
                            Log.d("TAG", "connected")
                        })
                .on(Socket.EVENT_DISCONNECT,
                        {
                            Log.d("TAG", "disconnected")
                        })
                .on("message", fun(message){
                    //Data is in message[0]
                })
    }

}
