package ir.romroid.secureboxrecorder.domain.model

import android.net.Uri
import android.os.Parcelable
import ir.romroid.secureboxrecorder.base.component.model.BaseResponseData
import kotlinx.parcelize.Parcelize

@Parcelize
data class AudioModel(
    val name: String,
    val uri: Uri
) : BaseResponseData(), Parcelable
