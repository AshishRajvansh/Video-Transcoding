package com.example.videotranscoding

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.videotranscoding.temp.Rotation
import com.linkedin.android.litr.TransformationListener
import com.linkedin.android.litr.analytics.TrackTransformationInfo
import com.livinglifetechway.quickpermissions_kotlin.runWithPermissions
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import me.rosuh.filepicker.config.FilePickerManager
import org.mp4parser.support.Matrix
import java.io.File


class MainActivity : AppCompatActivity() {
    private val TAG = "testTranscoding"
    private val CUSTOM_REQUEST_CODE: Int = 200

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<Button>(R.id.btnTranscode).setOnClickListener {
//            startTranscoding()
            val filePath = "/storage/emulated/0/Video Transcoding/vid-landscape-720p-90.mp4"
            transcode(filePath)
        }

        findViewById<Button>(R.id.btnTranscode2).setOnClickListener {
//            startTranscoding()
            val filePath = "/storage/emulated/0/Video Transcoding/vid-landscape-720p-90.mp4"
            transcode2(filePath)
        }

    }

    private fun startTranscoding() = runWithPermissions(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    ) {
        FilePickerManager
            .from(this)
            .forResult(FilePickerManager.REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            FilePickerManager.REQUEST_CODE -> {
                if (resultCode == Activity.RESULT_OK) {
                    val list = FilePickerManager.obtainData()
                    list.forEach {
                        Log.d(TAG, "path ${it}")
                        Toast.makeText(this, "${it} file selected", Toast.LENGTH_SHORT).show()
                    }

                    if (list.isNotEmpty()) {
                        transcode(list[0])
                    }
                    // do your work
                } else {
                    Toast.makeText(
                        this,
                        "You didn't choose anything~",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun transcode2(filePath: String) {
        GlobalScope.launch(Dispatchers.IO) {

            Transcode2.rotateVideo(
                File(filePath),
                getTranscodedFilePath(filePath),
                Matrix.ROTATE_270
            ).observeOn(Schedulers.computation()).subscribe()
        }
    }


    private fun transcode(filePath: String) {
        GlobalScope.launch(Dispatchers.IO) {

            Transcode.performTransform(
                this@MainActivity,
                Uri.fromFile(File(filePath)),
                getTranscodedFilePath(filePath),
                transformationListener
            )
        }
    }

    private fun getTranscodedFilePath(filePath: String): String {
        val parent = File(getExternalFilesDir(null), "videoTrancodingRes")
        parent.mkdirs()

        val file = File(parent.absolutePath + "/" + "${System.currentTimeMillis()}.mp4")
        file.createNewFile()

        return file.absolutePath
    }

    private val transformationListener = object : TransformationListener {
        override fun onStarted(id: String) {
            Log.d(TAG, "onStarted")
        }

        override fun onProgress(id: String, progress: Float) {
            Log.d(TAG, "onProgress $progress")
        }

        override fun onCompleted(
            id: String,
            trackTransformationInfos: MutableList<TrackTransformationInfo>?
        ) {
            Log.d(TAG, "onCompleted")
        }

        override fun onCancelled(
            id: String,
            trackTransformationInfos: MutableList<TrackTransformationInfo>?
        ) {
            Log.d(TAG, "onCancelled")
        }

        override fun onError(
            id: String,
            cause: Throwable?,
            trackTransformationInfos: MutableList<TrackTransformationInfo>?
        ) {
            Log.d(TAG, "onError ${cause?.message}")
        }

    }


}