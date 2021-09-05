package com.example.recordingaudio

import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import android.media.MediaRecorder
import android.os.Build
import android.os.Environment
import android.widget.Button
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File
import java.io.IOException
import java.lang.Exception
import java.lang.IllegalStateException
import android.media.AudioAttributes

import android.media.MediaPlayer
import java.lang.IllegalArgumentException


class MainActivity : AppCompatActivity() {
    private var TAG = "MainActivity"
    protected var recorder: MediaRecorder? = null
    protected var player: MediaPlayer? = null
    private lateinit var mRecordButton : Button
    private lateinit var mPlayButton : Button
    private val dir: File = File(Environment.getExternalStorageDirectory().absolutePath + "/recordAudio/")
    private var output: String? = null

    private fun checkDir () {
        try{
            // create a File object for the parent directory
            val recorderDirectory = File(Environment.getExternalStorageDirectory().absolutePath+"/recordAudio/")
            // have the object build the directory structure, if needed.
            recorderDirectory.mkdirs()
        }catch (e: IOException){
            e.printStackTrace()
        }
        if(dir.exists()){
            val count = dir.listFiles().size
            output = Environment.getExternalStorageDirectory().absolutePath + "/recordAudio/recording"+count+".mp3"
        }
        recorder = MediaRecorder()

        recorder?.setAudioSource(MediaRecorder.AudioSource.MIC)
        recorder?.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        recorder?.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT)
        recorder?.setOutputFile(output)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        checkPermission ()
        mRecordButton = findViewById<Button>(R.id.RecordButton)
        mRecordButton.setOnClickListener {
            val myObservable = getObservable()
            val myObserver = getObserverRecord()
            myObservable.subscribe(myObserver)
            startRecordingStream()
        }
        mPlayButton = findViewById<Button>(R.id.PlayButton)
        mPlayButton.setOnClickListener {
            val myObservable = getObservable()
            val myObserver = getObserverPlay()
            myObservable.subscribe(myObserver)
            startPlaying() }

    }
    private fun startRecordingStream() {
        try {
            recorder!!.prepare()
            recorder!!.start()
        } catch (e: IOException) {
            // handle error
        } catch (e: IllegalStateException) {
            // handle error
        }
    }
    private fun stopRecording() {
        // stop recording and free up resources
       try {
           if (recorder != null){
               recorder?.stop()
               recorder?.release()
               recorder = null
           }
           initRecorder()
       }catch (e:Exception){

       }
    }
    private fun startPlaying() {
        player = MediaPlayer()
        try {
            val count =  if(dir.exists()) {
               dir.listFiles().size
            }else {
                val recorderDirectory = File( externalCacheDir?.absolutePath+"/recordingAndroid/")
                recorderDirectory.mkdirs()
                dir.listFiles().size
            }
            player!!.setDataSource(externalCacheDir?.absolutePath+"/recordingAndroid/recording"+count+".mp3") // pass reference to file to be played
            player!!.setAudioAttributes(
                AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            ) // optional step
            player!!.prepare() // may take a while depending on the media, consider using .prepareAsync() for streaming
        } catch (e: IOException) { // we need to catch both errors in case of invalid or inaccessible resources
            // handle error
        } catch (e: IllegalArgumentException) {
            // handle error
        }
        player!!.start()
    }
    private fun stopPlaying() {
        try {
            player!!.stop()
            player!!.release() // free up resources
            player = null
        }catch (e:Exception){

        }
    }
    private fun initRecorder() {
        recorder = MediaRecorder()

        if(dir.exists()){
            val count = dir.listFiles().size
            output = externalCacheDir?.absolutePath+"/recordingAndroid/recording"+count+".mp3"
        }
        recorder?.setAudioSource(MediaRecorder.AudioSource.MIC)
        recorder?.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        recorder?.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
        recorder?.setOutputFile(output)
    }

    private fun getObserverRecord(): Observer<String> {
        return object : Observer<String> {
            override fun onSubscribe(d: Disposable) {
            }

//Every time onNext is called, print the value to Android Studio’s Logcat//

            override fun onNext(s: String) {
                Log.d(TAG, "onNext: $s")
                mRecordButton.text = "Stop"
            }

//Called if an exception is thrown//

            override fun onError(e: Throwable) {
                Log.e(TAG, "onError: " + e.message)
            }

//When onComplete is called, print the following to Logcat//

            override fun onComplete() {
                Log.d(TAG, "onComplete")
                stopRecording()
                mRecordButton.text = "Record"
            }
        }
    }
    private fun getObserverPlay(): Observer<String> {
        return object : Observer<String> {
            override fun onSubscribe(d: Disposable) {
            }

//Every time onNext is called, print the value to Android Studio’s Logcat//

            override fun onNext(s: String) {
                Log.d(TAG, "onNext: $s")
                mPlayButton.text = "Stop"
            }

//Called if an exception is thrown//

            override fun onError(e: Throwable) {
                Log.e(TAG, "onError: " + e.message)
            }

//When onComplete is called, print the following to Logcat//

            override fun onComplete() {
                Log.d(TAG, "onComplete")
                stopPlaying()
                mPlayButton.text = "Play Back"
            }
        }
    }

//Give myObservable some data to emit//

    private fun getObservable(): Observable<String> {
        return Observable.just("1", "2", "3", "4", "5")
    }

    private fun checkPermission () {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            val permissions = arrayOf(android.Manifest.permission.RECORD_AUDIO, android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE)
            ActivityCompat.requestPermissions(this, permissions,0)
        }
        checkDir()
    }
}