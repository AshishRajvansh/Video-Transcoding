package com.example.videotranscoding

import android.content.Context
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.net.Uri
import com.linkedin.android.litr.MediaTransformer
import com.linkedin.android.litr.TransformationListener
import com.linkedin.android.litr.TransformationOptions


object Transcode {

    fun performTransform(
        context: Context,
        sourceVideoUri: Uri,
        targetVideoFilePath: String,
        videoTransformationListener: TransformationListener
    ): String {

        val requrestId = System.currentTimeMillis().toString()

        val mediaTransformer =
            MediaTransformer(context.applicationContext)

        mediaTransformer.transform(
            requrestId,
            sourceVideoUri,
            targetVideoFilePath,
            getTargetVideoFormat(),
            getTargetAudioFormat(),
            videoTransformationListener,
            getTransformationOptions()
        )

        return requrestId

    }

    private fun getTransformationOptions(): TransformationOptions? {
        return null
    }

    private fun getTargetAudioFormat(): MediaFormat {
        return MediaFormat().apply {
            setString(MediaFormat.KEY_MIME, MediaFormat.MIMETYPE_AUDIO_AAC)
            setInteger(MediaFormat.KEY_BIT_RATE, 128_000)
            setInteger(MediaFormat.KEY_SAMPLE_RATE, 44100)
            setInteger(MediaFormat.KEY_CHANNEL_COUNT, 2)
        }
    }

//    /storage/emulated/0/Video Transcoding/vid-landscape-720p-90.mp4

    private fun getTargetVideoFormat(): MediaFormat? {
        return MediaFormat().apply {
            setString(MediaFormat.KEY_MIME, MediaFormat.MIMETYPE_VIDEO_AVC)
//            setInteger(MediaFormat.KEY_WIDTH, 720)
//            setInteger(MediaFormat.KEY_HEIGHT, 1280)

            setInteger(MediaFormat.KEY_HEIGHT, 720)
            setInteger(MediaFormat.KEY_WIDTH, 1280)

            setInteger(MediaFormat.KEY_BIT_RATE, 2_500_500)
//            setInteger(MediaFormat.KEY_FRAME_RATE, 30)
            setInteger(MediaFormat.KEY_ROTATION, 90)

            setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 3)
            setInteger(
                MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface
            )
        }
    }
}