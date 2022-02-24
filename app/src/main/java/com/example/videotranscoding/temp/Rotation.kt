package com.example.videotranscoding.temp

import android.content.Context
import android.hardware.display.DisplayManager
import android.view.Display
import android.view.Surface
import java.io.IOException

enum class Rotation {
    NORMAL, ROTATION_90, ROTATION_180, ROTATION_270;
    /**
     * Retrieves the int representation of the Rotation.
     *
     * @return 0, 90, 180 or 270
     */
    fun asInt(): Int {
        return when (this) {
            NORMAL -> 0
            ROTATION_90 -> 90
            ROTATION_180 -> 180
            ROTATION_270 -> 270
        }
    }

    companion object {
        /**
         * Create a Rotation from an integer. Needs to be either 0, 90, 180 or 270.
         *
         * @param rotation 0, 90, 180 or 270
         * @return Rotation object
         */
        fun fromInt(rotation: Int): Rotation {
            return when (rotation) {
                0 -> NORMAL
                90 -> ROTATION_90
                180 -> ROTATION_180
                270 -> ROTATION_270
                360 -> NORMAL
                else -> throw IllegalStateException("$rotation is an unknown rotation. Needs to be either 0, 90, 180 or 270!")
            }
        }

        val Context.currentRotation: Rotation
            get() {
                val displayManager = getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
                return when (displayManager.getDisplay(Display.DEFAULT_DISPLAY).rotation) {
                    Surface.ROTATION_90 -> ROTATION_90
                    Surface.ROTATION_270 -> ROTATION_270
                    Surface.ROTATION_180 -> ROTATION_180
                    Surface.ROTATION_0 -> NORMAL
                    else -> NORMAL
                }
            }
    }
}

data class ProgressResult<T>(val item: T, val progress: Float?)


class OutOfStorageException : IOException {
    constructor() : super()
    constructor(message: String?) : super(message)
    constructor(message: String?, cause: Throwable?) : super(message, cause)
    constructor(cause: Throwable?) : super(cause)
}