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

    companion object {
        private const val TAG = "PlayerDialog"
        private val _TIME_FORMAT = "%02d:%02d"
    }

    private var player: MediaPlayer? = null
    private var repeatableJob: Job? = null
    private var isPlaying = false
        set(value) {
            field = value

            if (value) {
                binding?.fabPlay?.setImageResource(R.drawable.ic_pause)
                setupSeekBarJob()
            } else {
                binding?.fabPlay?.setImageResource(R.drawable.ic_play)
                repeatableJob?.cancel()
            }
        }

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

                    tvElapsed.text = _TIME_FORMAT.format(progress / 60, progress % 60)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }

        })

        fabPlay.setOnClickListener {
            togglePlay()
        }

    }

    private fun togglePlay() {
        player?.let {
            isPlaying = if (it.isPlaying) {
                it.pause()
                false
            } else {
                it.start()
                true
            }
        } ?: run {
            if (preparePlay()) {
                player?.start()
                this@AudioPlayerDialog.isPlaying = true
            }
        }
    }

    private fun preparePlay(): Boolean {

        player = MediaPlayer()
        return player?.run {
            binding?.prg?.toShow()
            try {
                setDataSource(args.audioModel.uri.path!!)
                prepare()
            } catch (e: IOException) {
                e.logE("$TAG preparePlay")
                return false
            }

            setOnPreparedListener {
                "media prepared".logI(TAG)
                binding?.apply {
                    prg.toHide()

                    val durationSec = ((player?.duration ?: 0).toDouble() / 1000).nextDown().toInt()
                    seekbar.max = durationSec
                    tvAll.text = _TIME_FORMAT.format(durationSec / 60, durationSec % 60)
                }
            }

            setOnCompletionListener {
                seekTo(0)
                binding?.seekbar?.progress = binding?.seekbar?.max ?: 0
                this@AudioPlayerDialog.isPlaying = false
            }

            return true
        } ?: false
    }

    override fun onPause() {
        isPlaying = false
        player?.release()

        super.onPause()
    }

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
                        tvElapsed.text = _TIME_FORMAT.format(currentPos / 60, currentPos % 60)
                    }
                }
            }
        }.apply {
            start()
            "job start".logD(TAG)
        }

    }

}