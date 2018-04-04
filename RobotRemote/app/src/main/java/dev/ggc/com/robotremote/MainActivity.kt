package dev.ggc.com.robotremote

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.erz.joysticklibrary.JoyStick
import kotlinx.android.synthetic.main.activity_main.*
import com.github.nkzawa.socketio.client.IO
import com.github.nkzawa.socketio.client.Socket


class MainActivity : AppCompatActivity(), JoyStick.JoyStickListener {

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
    private var previousDir = ""

    private var socket = IO.socket("http://192.168.8.196:4000")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        joystick.setListener(this)
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
                .on("message",fun(message)
                        {
                            Log.d("TAG", "message received : "+ message[0])
                        })
    }

    override fun onTap() {
        socket.emit("message", "TAP")
    }

    override fun onDoubleTap() {
        socket.emit("message", "DOUBLE_TAP")
    }

    override fun onMove(joyStick: JoyStick?, angle: Double, power: Double, direction: Int) {
        previousDir = currentDir
        when (direction) {
            LEFT -> currentDir = "LEFT"
            TOP_LEFT -> currentDir = "TOP_LEFT"
            TOP -> currentDir = "TOP"
            TOP_RIGHT -> currentDir = "TOP_RIGHT"
            RIGHT -> currentDir = "RIGHT"
            BOTTOM_RIGHT -> currentDir = "BOTTOM_RIGHT"
            BOTTOM -> currentDir = "BOTTOM"
            BOTTOM_LEFT -> currentDir = "BOTTOM_LEFT"
            IDLE -> currentDir = "IDLE"
        }
        if(currentDir != previousDir){
            socket.emit("message", currentDir)
        }
    }
}
