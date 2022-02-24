package com.example.videotranscoding

import android.annotation.SuppressLint
import android.util.Log
import com.example.videotranscoding.temp.OutOfStorageException
import com.example.videotranscoding.temp.ProgressResult
import com.example.videotranscoding.temp.Rotation
import io.reactivex.Flowable.interval
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.mp4parser.IsoFile
import org.mp4parser.boxes.iso14496.part12.TrackBox
import org.mp4parser.muxer.FileDataSourceImpl
import org.mp4parser.muxer.FileRandomAccessSourceImpl
import org.mp4parser.muxer.Movie
import org.mp4parser.muxer.Mp4TrackImpl
import org.mp4parser.muxer.builder.DefaultMp4Builder
import org.mp4parser.support.Matrix
import java.io.File
import java.io.IOException
import java.io.RandomAccessFile
import java.util.concurrent.TimeUnit

object Transcode2 {

    val TAG = "testTranscoding"

    @SuppressLint("UsableSpace")
    fun rotateVideo(
        input: File,
        outputPath: String,
        rotation: Matrix
    ): Observable<ProgressResult<File>> {
        return Single.fromCallable { File(outputPath).apply { createNewFile() } }
            .flatMapObservable { output ->
//                val rotation =
//                    when (currentOrientation) {
//                        Rotation.ROTATION_90 -> Matrix.ROTATE_270
//                        Rotation.ROTATION_180 -> Matrix.ROTATE_180
//                        else -> Matrix.ROTATE_90
//                    }

                Log.d(TAG, "rotating video to: $rotation")

                val finalStream = RandomAccessFile(output.absolutePath, "rw").channel
                val heapFile = FileDataSourceImpl(input.absolutePath)
                val isoFile = IsoFile(input.absolutePath)
                val m = Movie()
                val trackBoxes = isoFile.movieBox.getBoxes(TrackBox::class.java)
                for (trackBox in trackBoxes) {
                    trackBox.trackHeaderBox.matrix = rotation
                    m.addTrack(
                        Mp4TrackImpl(
                            trackBox.trackHeaderBox.trackId,
                            isoFile,
                            FileRandomAccessSourceImpl(RandomAccessFile(input, "r")),
                            "output1"
                        )
                    )
                }
                val finalContainer = DefaultMp4Builder().build(m)

                var threadException: Throwable? = null

                // Make the call to write the file on a separate thread from the progress check
                val writeFileThread = Thread(
                    Runnable {
                        try {
                            finalContainer.writeContainer(finalStream)
                        } catch (e: InterruptedException) {
                            Log.d(TAG, "Rotate video interrupted")
                            threadException = e
                        } catch (e: IOException) {
                            Log.e(TAG, "IOException within video rotate write thread")
                            threadException = if (output.usableSpace == 0L) {
                                OutOfStorageException(
                                    "No storage remaining to rotate rotate video",
                                    e
                                )
                            } else {
                                e
                            }
                        }
                    }
                )
                writeFileThread.setUncaughtExceptionHandler { _, e -> threadException = e }

                val finalVideoSize = finalContainer.boxes.map { it.size }.sum()

                Observable.interval(200L, TimeUnit.MILLISECONDS)
                    .observeOn(Schedulers.computation())
                    .map {
                        threadException?.let { throw it }

                        val currentOutputSize = if (output.exists()) output.length() else 0
                        val progress = currentOutputSize / finalVideoSize.toFloat()

                        ProgressResult(output, progress)
                    }
                    .takeUntil {
                        val completionProgress = it.progress ?: 0.0f
                        completionProgress >= 1f || threadException != null
                    }
                    .observeOn(Schedulers.io())
                    .doOnSubscribe {
                        writeFileThread.start()
                    }
                    .doFinally {
                        writeFileThread.interrupt()
                        finalStream.close()
                        isoFile.close()
                        heapFile.close()
                    }
            }
            .subscribeOn(Schedulers.io())
            .onErrorResumeNext { error: Throwable ->
                val outputFile = File(outputPath)
                val mappedError = if (outputFile.usableSpace == 0L) {
                    OutOfStorageException(
                        "No storage remaining to rotate video",
                        error
                    )
                } else {
                    error
                }
                Observable.error<ProgressResult<File>>(mappedError)
            }
    }

}