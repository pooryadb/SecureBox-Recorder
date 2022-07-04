package ir.romroid.secureboxrecorder.presentation.recorder.dialog

import android.Manifest
import android.content.Context
import android.media.MediaRecorder
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import ir.romroid.secureboxrecorder.R
import ir.romroid.secureboxrecorder.base.component.BaseBottomSheetDialogFragment
import ir.romroid.secureboxrecorder.databinding.DialogRecorderBinding
import ir.romroid.secureboxrecorder.ext.*
import ir.romroid.secureboxrecorder.utils.*
import java.io.File
import java.io.IOException

@AndroidEntryPoint
class RecorderDialog : BaseBottomSheetDialogFragment<DialogRecorderBinding>() {

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> DialogRecorderBinding
        get() = DialogRecorderBinding::inflate

    companion object {
        private const val TAG = "RecorderDialog"
        private const val AUDIO_PERMISSION = Manifest.permission.RECORD_AUDIO
    }

    private var path = ""
    private var recorder: MediaRecorder? = null
    private var pendingPrepareRecord: (() -> Unit)? = null
    private var isRecording = false
        set(value) {
            if (value)
                binding?.fab?.setImageResource(R.drawable.ic_pause)
            else if (field)
                binding?.fab?.toHide()

            field = value
        }

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
                    return@setOnClickListener
                } else {
                    etName.error = null
                    saveVoice(etName.text.toString())
                }
            }
        }
    }

    private fun setupPath() {
        path = requireContext().cacheDir.path + "/" + VOICE_TEMP_FOLDER_NAME
        val f = File(path)
        if (f.exists().not()) {
            f.mkdirs()
        }
        path += "/tempRecord$VOICE_FORMAT"

        path.logD("$TAG path")
    }

    private fun saveVoice(name: String) {
        stopRecord()// to ensure

        val fTemp = File(path)

        if (name == DbHelper.getUserKey(requireContext())) {
            fTemp.delete()
            dismiss(true)
            return
        }

        val newPath = requireContext().cacheDir.path + "/" + VOICE_SAVED_FOLDER_NAME

        fTemp.copyTo(setupSaveFile(newPath, name + VOICE_FORMAT))
        fTemp.delete()
        dismiss(false)
    }

    private fun setupSaveFile(path: String, fileName: String): File {
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
        recorder?.let {
            this.isRecording =
                if (isRecording.not()) {
                    startRecord()
                } else {
                    binding?.btnSave?.toShow()
                    stopRecord().not()
                }
        } ?: run {
            pendingPrepareRecord = {
                this.isRecording = startRecord()
            }
            prepareRecorder()
        }
    }

    private fun startRecord(): Boolean =
        try {
            recorder?.let {
                it.start()
                "startRecord".logD(TAG)
                true
            } ?: false
        } catch (e: Exception) {
            e.logE("$TAG startRecord")
            false
        }


    private fun prepareRecorder() {
        stopRecord()// to ensure
//        setupFolder()

        if (recordPermission()) {
            recorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)//should equal to [Constants.VOICE_FORMAT]
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                setOutputFile(path)
                try {
                    prepare()
                } catch (e: IOException) {
                    e.logE("$TAG prepareRecorder")
                    return
                }
            }

            "success".logD("$TAG prepareRecorder")
            pendingPrepareRecord?.invoke()
            pendingPrepareRecord = null

        }

    }

    private fun stopRecord(): Boolean =
        try {
            recorder?.stop()
            recorder?.release()

            "stopRecord".logD(TAG)
            true
        } catch (e: Exception) {
            e.logE("$TAG stopRecord")
            false
        }

    private fun recordPermission(): Boolean = when {
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

    override fun onPause() {
        stopRecord()
        super.onPause()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        PermissionUtils.register(this,
            object : PermissionUtils.PermissionListener {
                override fun observe(permissions: Map<String, Boolean>) {
                    permissions.forEach {
                        when (it.key) {
                            AUDIO_PERMISSION -> {
                                if (it.value) {
                                    prepareRecorder()
                                } else
                                    requireContext().toast(
                                        getString(R.string.cant_record_without_permission)
                                    )
                            }
                        }
                    }
                }
            })
    }

    private fun dismiss(gotoFileManager: Boolean) {
        findNavController().setBackStackLiveData(BACK_FROM_RECORDER, gotoFileManager)
    }

}