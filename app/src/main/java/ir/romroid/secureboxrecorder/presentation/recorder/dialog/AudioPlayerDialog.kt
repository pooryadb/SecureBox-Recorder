package ir.romroid.secureboxrecorder.presentation.recorder.dialog

import android.media.MediaPlayer
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import ir.romroid.secureboxrecorder.R
import ir.romroid.secureboxrecorder.base.component.BaseBottomSheetDialogFragment
import ir.romroid.secureboxrecorder.databinding.DialogAudioPlayerBinding
import ir.romroid.secureboxrecorder.ext.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import java.io.IOException
import kotlin.math.nextDown

@AndroidEntryPoint
class AudioPlayerDialog : BaseBottomSheetDialogFragment<DialogAudioPlayerBinding>() {

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> DialogAudioPlayerBinding
        get() = DialogAudioPlayerBinding::inflate
    private val args by navArgs<AudioPlayerDialogArgs>()
    private var player: MediaPlayer? = null
    private var repeatableJob: Job? = null
    private val isPlaying: Boolean
        get() = if (isPrepared) player?.isPlaying == true else false
    private var isPrepared = false

    override fun viewHandler(view: View, savedInstanceState: Bundle?) {
        setupUi()
        preparePlay()
    }

    private fun setupUi() = binding?.apply {
        tvTitle.text = args.audioModel.name

        seekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (player != null && fromUser) {
                    player!!.seekTo(progress * 1000)

                    tvElapsed.text = TIME_FORMAT.format(progress / 60, progress % 60)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }

        })

        fabPlay.setOnClickListener {
            togglePlayer()
        }

    }

    override fun onPause() {
//        pausePlayer()

        super.onPause()
    }

    override fun onDestroy() {
        pausePlayer()
        player?.release()
        isPrepared = false

        super.onDestroy()
    }

    private fun togglePlayer() {
        if (isPrepared) {
            if (isPlaying) {
                pausePlayer()
            } else {
                resumePlayer()
            }
        } else {
            if (preparePlay()) {
                resumePlayer()
            }
        }
    }

    @Throws
    private fun resumePlayer() = player?.let {
        it.start()
        binding?.fabPlay?.setImageResource(R.drawable.ic_pause)
        setupSeekBarJob()
    }

    @Throws
    private fun pausePlayer() = player?.let {
        it.pause()
        binding?.fabPlay?.setImageResource(R.drawable.ic_play)
        repeatableJob?.cancel()
    }

    private fun preparePlay(): Boolean {
        player = MediaPlayer()
        return player?.run {
            binding?.prg?.toShow()
            try {
                setDataSource(getFilePath())
                prepare()
            } catch (e: IOException) {
                e.logE("$TAG preparePlay")
                requireContext().toast(getString(R.string.error_open_file))
                return false
            }

            setOnPreparedListener {
                "media prepared".logI(TAG)
                binding?.apply {
                    prg.toHide()

                    val durationSec = ((player?.duration ?: 0).toDouble() / 1000).nextDown().toInt()
                    seekbar.max = durationSec
                    tvAll.text = TIME_FORMAT.format(durationSec / 60, durationSec % 60)
                }

                isPrepared = true
                resumePlayer()
            }

            setOnCompletionListener {
                seekTo(0)
                binding?.seekbar?.progress = binding?.seekbar?.max ?: 0
                pausePlayer()
            }

            return true
        } ?: false
    }

    @Throws
    private fun getFilePath(): String =
        args.audioModel.uri.path ?: throw Exception(getString(R.string.error_open_file))

    private fun setupSeekBarJob() {
        repeatableJob?.cancel()
        repeatableJob = lifecycleScope.superlaunchIO {
            while (isActive) {
                "repeat".logI(TAG)
                delay(500)//update interval
                val currentPos =
                    ((player?.currentPosition ?: 0).toDouble() / 1000).nextDown().toInt()

                launchMain {
                    binding?.apply {
                        seekbar.progress = currentPos
                        tvElapsed.text = TIME_FORMAT.format(currentPos / 60, currentPos % 60)
                    }
                }
            }
        }.apply {
            start()
            "job start".logD(TAG)
        }

    }

    private companion object {
        const val TAG = "PlayerDialog"
        const val TIME_FORMAT = "%02d:%02d"
    }

}