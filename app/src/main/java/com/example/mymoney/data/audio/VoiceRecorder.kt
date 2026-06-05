package com.example.mymoney.data.audio

import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.util.Log
import java.io.File

/**
 * Quản lý ghi âm bằng [MediaRecorder] và phát lại bằng [MediaPlayer].
 * Sử dụng applicationContext để tránh memory leak.
 */
class VoiceRecorder(private val context: Context) {

    private val TAG = "VoiceRecorder"
    private var recorder: MediaRecorder? = null
    private var player: MediaPlayer? = null

    /** File âm thanh hiện tại (null nếu chưa có) */
    var recordingFile: File? = null
        private set

    /** Bắt đầu ghi âm, tạo file m4a trong cacheDir */
    fun startRecording() {
        val file = File(context.cacheDir, "voice_${System.currentTimeMillis()}.m4a")
        recordingFile = file
        recorder = MediaRecorder(context).apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setAudioSamplingRate(16000)
            setAudioEncodingBitRate(64000)
            setOutputFile(file.absolutePath)
            prepare()
            start()
        }
        Log.d(TAG, "Recording started → ${file.name}")
    }

    /**
     * Dừng ghi âm.
     * @return File chứa âm thanh, hoặc null nếu thất bại.
     */
    fun stopRecording(): File? {
        return try {
            recorder?.stop()
            recorder?.release()
            recorder = null
            Log.d(TAG, "Recording stopped: ${recordingFile?.length()} bytes")
            recordingFile
        } catch (e: Exception) {
            Log.e(TAG, "stopRecording failed: ${e.message}")
            recorder?.release()
            recorder = null
            null
        }
    }

    /**
     * Phát lại file âm thanh đã ghi.
     * @param onComplete Callback khi phát xong.
     */
    fun startPlayback(onComplete: () -> Unit = {}) {
        val file = recordingFile ?: return
        if (!file.exists()) return
        try {
            player?.release()
            player = MediaPlayer().apply {
                setDataSource(file.absolutePath)
                prepare()
                setOnCompletionListener { onComplete() }
                start()
            }
            Log.d(TAG, "Playback started")
        } catch (e: Exception) {
            Log.e(TAG, "Playback failed: ${e.message}")
            player?.release()
            player = null
        }
    }

    /** Dừng phát lại */
    fun stopPlayback() {
        try { player?.stop() } catch (_: Exception) {}
        player?.release()
        player = null
    }

    /** Kiểm tra đang phát hay không */
    fun isPlaying(): Boolean = player?.isPlaying == true

    /** Xóa file ghi âm và dừng phát */
    fun deleteRecording() {
        stopPlayback()
        recordingFile?.delete()
        recordingFile = null
    }

    /** Giải phóng toàn bộ tài nguyên (gọi trong onCleared) */
    fun release() {
        stopPlayback()
        try { recorder?.stop() } catch (_: Exception) {}
        recorder?.release()
        recorder = null
    }
}

