package ir.romroid.secureboxrecorder.presentation.recorder.dialog

import android.Manifest
import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.Context
import android.content.res.ColorStateList
import android.media.MediaRecorder
import android.os.Bundle
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.animation.doOnCancel
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import ir.romroid.secureboxrecorder.R
import ir.romroid.secureboxrecorder.base.component.BaseBottomSheetDialogFragment
import ir.romroid.secureboxrecorder.databinding.DialogRecorderBinding
import ir.romroid.secureboxrecorder.ext.*
import ir.romroid.secureboxrecorder.presentation.safe.SafeViewModel
import ir.romroid.secureboxrecorder.utils.*
import java.io.File
import java.io.IOException

@AndroidEntryPoint
class RecorderDialog : BaseBottomSheetDialogFragment<DialogRecorderBinding>() {

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> DialogRecorderBinding
        get() = DialogRecorderBinding::inflate

    override var hasCancelable = false

    private val safeVM by activityViewModels<SafeViewModel>()

    private companion object {
        const val TAG = "RecorderDialog"
        const val AUDIO_PERMISSION = Manifest.permission.RECORD_AUDIO
    }

    private var path = ""
    private var recorder: MediaRecorder? = null
    private var pendingPrepareRecord: (() -> Unit)? = null
    private var isRecording = false
    private var isPrepared = false

    override fun viewHandler(view: View, savedInstanceState: Bundle?) {
        binding?.apply {
            setupPath()

            fab.setOnClickListener {
                toggleRecord()
            }

            ibtnClose.setOnClickListener {
                dismiss()
            }

            btnSave.setOnClickListener {
                if (etName.text.toString().isEmpty()) {
                    etName.error = getString(R.string.please_enter_name)
                } else {
                    etName.error = null
                    saveVoice(etName.text.toString())
                }
            }


            val key = safeVM.getUserKey()
            val keyLength = key.length
            etName.afterTextChange {
                if (it.length == keyLength) {
                    if (it == key) {
                        val fTemp = File(path)
                        fTemp.delete()
                        dismiss(true)
                    }
                }
            }
        }
    }

    private fun setupPath() {// FIXME: use provider
        path = requireContext().cacheDir.path + "/" + VOICE_TEMP_FOLDER_NAME
        val f = File(path)
        if (f.exists().not()) {
            f.mkdirs()
        }
        path += "/tempRecord$VOICE_FORMAT"

        path.logD("$TAG path")
    }

    private fun saveVoice(name: String) {// FIXME: use provider
        val fTemp = File(path)

        val newPath =
            requireContext().getExternalFilesDir(null)?.path + "/" + VOICE_SAVED_FOLDER_NAME

        fTemp.copyTo(setupSaveFile(newPath, name + VOICE_FORMAT))
        fTemp.delete()
        dismiss(false)
    }

    private fun setupSaveFile(path: String, fileName: String): File {// FIXME: use provider
        val folderSave = File(path)
        if (folderSave.exists().not())
            folderSave.mkdirs()

        var fileSave = File("$path/$fileName")
        if (fileSave.exists()) {
            fileSave = File(path + "/" + fileName.replace(VOICE_FORMAT, "I$VOICE_FORMAT"))
        }

        return fileSave
    }

    private fun toggleRecord() {
        if (isPrepared) {
            if (isRecording) {
                stopRecord()
                setupPauseRecorder()
            } else {
                startRecord()
                setupResumeRecorder()
            }
        } else {
            pendingPrepareRecord = {
                startRecord()
                setupResumeRecorder()
                pendingPrepareRecord = null
            }
            prepareRecorder()
        }
    }

    private fun startRecord(): Boolean =
        try {
            recorder?.let {
                it.start()
                isRecording = true
                "startRecord".logD(TAG)
                true
            } ?: false
        } catch (e: Exception) {
            requireContext().toast(getString(R.string.error_record_stop))
            e.logE("$TAG startRecord")
            false
        }

    private fun setupResumeRecorder() = binding?.apply {
        fab.setImageResource(R.drawable.ic_pause)
        chronometer.base = SystemClock.elapsedRealtime()
        chronometer.start()
        animateWave.start()
    }

    private fun stopRecord(): Boolean =
        try {
            recorder?.stop()
            recorder?.release()
            isPrepared = false
            isRecording = false
            "stopRecord".logD(TAG)
            true
        } catch (e: Exception) {
            requireContext().toast(getString(R.string.error_record_stop))
            e.logE("$TAG stopRecord")
            false
        }

    private fun setupPauseRecorder() = binding?.apply {
        fab.toHide()
        chronometer.stop()
        animateWave.cancel()
        btnSave.toShow()
    }

    private fun prepareRecorder() {
        if (recordPermission()) {
            recorder = MediaRecorder().apply {
                try {
                    setAudioSource(MediaRecorder.AudioSource.MIC)
                    setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)//should equal to [Constants.VOICE_FORMAT]
                    setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                    setOutputFile(path)
                    prepare()

                    isPrepared = true
                } catch (e: IOException) {
                    isPrepared = false
                    requireContext().toast(getString(R.string.error_record_prepare))
                    e.logE("$TAG prepareRecorder")
                    return
                }

            }

            "success".logD("$TAG prepareRecorder")
            pendingPrepareRecord?.invoke()

        }

    }

    private fun recordPermission(): Boolean =
        when {
            (PermissionUtils.isGranted(requireContext(), AUDIO_PERMISSION).not()) -> {

                PermissionUtils.requestPermission(
                    requireContext(),
                    arrayOf(AUDIO_PERMISSION)
                )

                false
            }
            shouldShowRequestPermissionRationale(AUDIO_PERMISSION) -> {
                requireContext().toast(getString(R.string.cant_record_without_permission))
                false
            }
            else -> {
                true
            }
        }

    override fun onDestroy() {
        if (isRecording)
            stopRecord()
        super.onDestroy()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        PermissionUtils.register(
            this,
            object : PermissionUtils.PermissionListener {
                override fun observe(permissions: Map<String, Boolean>) {
                    permissions.forEach {
                        if (it.key == AUDIO_PERMISSION) {
                            if (it.value) {
                                prepareRecorder()
                            } else
                                requireContext().toast(
                                    getString(R.string.cant_record_without_permission)
                                )
                        }
                    }
                }
            })
    }

    private fun dismiss(gotoFileManager: Boolean) {
        findNavController().setBackStackLiveData(BACK_FROM_RECORDER, gotoFileManager)
    }

    private val animateWave: ValueAnimator by lazy {
        ValueAnimator().apply {
            setIntValues(
                requireContext().getAttrColor(android.R.attr.colorError),
                requireContext().getAttrColor(android.R.attr.colorControlNormal)
            )
            setEvaluator(ArgbEvaluator())
            addUpdateListener { valueAnimator ->
                binding?.imgWave?.imageTintList = ColorStateList.valueOf(
                    valueAnimator.animatedValue as Int
                )
            }
            doOnCancel {
                binding?.imgWave?.imageTintList = null
            }

            duration = 1000
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE
        }
    }

}