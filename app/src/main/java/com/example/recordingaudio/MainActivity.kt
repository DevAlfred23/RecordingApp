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
    private val dir: File = File(Environment.getExternalStorageDirectory().absolutePath + "/recordingAndroid/")
    private var output: String? = null
    var mMediaPlayer: MediaPlayer? = null


    private fun checkDir () {
        try{
            // create a File object for the parent directory
            val recorderDirectory = File(Environment.getExternalStorageDirectory().absolutePath+"/recordingAndroid/")
            // have the object build the directory structure, if needed.
            recorderDirectory.mkdirs()
        }catch (e: IOException){
            e.printStackTrace()
        }
        if(dir.exists()){
            val count = dir.listFiles().size
            output = Environment.getExternalStorageDirectory().absolutePath + "/recordingAndroid/recording"+count+".mp3"
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
            var count =  if(dir.exists()) {
               dir.listFiles().size
            }else {
                val recorderDirectory = File( Environment.getExternalStorageDirectory().absolutePath + "/recordingAndroid/")
                recorderDirectory.mkdirs()
                dir.listFiles().size
            }
            count--
            player!!.setDataSource(Environment.getExternalStorageDirectory().absolutePath + "/recordingAndroid/recording"+count+".mp3") // pass reference to file to be played
            player!!.setAudioAttributes(
                AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )
            player!!.prepare()
        } catch (e: IOException) {
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
            output = Environment.getExternalStorageDirectory().absolutePath + "/recordingAndroid/recording"+count+".mp3"
        }
        recorder?.setAudioSource(MediaRecorder.AudioSource.MIC)
        recorder?.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        recorder?.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
        recorder?.setOutputFile(output)
    }

    private fun getObserverRecord(): Observer<String> {
        return object : Observer<String> {
            override fun onSubscribe(d: Disposable) {
                mRecordButton.text = "Stop"
            }
            override fun onNext(s: String) {
                Log.d(TAG, "onNext: $s")
                mRecordButton.text = "Stop"
                playSound()
            }
            override fun onError(e: Throwable) {
                Log.e(TAG, "onError: " + e.message)
            }
            override fun onComplete() {
                Log.d(TAG, "onComplete")
//                stopRecording()
                mRecordButton.setOnClickListener {
                    stopRecording()
                    mRecordButton.text = "Record"
                }
            }
        }
    }
    private fun getObserverPlay(): Observer<String> {
        return object : Observer<String> {
            override fun onSubscribe(d: Disposable) {
                mPlayButton.text = "Stop"
            }
            override fun onNext(s: String) {
                Log.d(TAG, "onNext: $s")
                mPlayButton.text = "Stop"
            }
            override fun onError(e: Throwable) {
                Log.e(TAG, "onError: " + e.message)
            }
            override fun onComplete() {
                Log.d(TAG, "onComplete")
//                stopPlaying()
                mPlayButton.setOnClickListener {
                    stopPlaying()
                    mPlayButton.text = "Play Back"
                }

            }
        }
    }
    private fun getObservable(): Observable<String> {
        return Observable.just("1")
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

    fun playSound() {
        if (mMediaPlayer == null) {
            mMediaPlayer = MediaPlayer.create(this, R.raw.effect)
             mMediaPlayer!!.start()
        } else mMediaPlayer!!.start()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        stopPlaying()
        stopRecording()
         if (mMediaPlayer != null) {
            mMediaPlayer!!.release()
            mMediaPlayer = null
            }
    }

    override fun onPause() {
        super.onPause()
        stopPlaying()
        stopRecording()
    }
}
