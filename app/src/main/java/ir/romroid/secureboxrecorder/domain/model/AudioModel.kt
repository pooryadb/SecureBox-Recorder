package ir.romroid.secureboxrecorder.domain.model

import android.net.Uri
import ir.romroid.secureboxrecorder.base.component.model.BaseResponseData

data class AudioModel(
    val name: String,
    val uri: Uri
) : BaseResponseData()
