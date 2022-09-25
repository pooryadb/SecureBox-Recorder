package ir.romroid.secureboxrecorder.domain.model

import android.os.Parcelable
import androidx.annotation.DrawableRes
import ir.romroid.secureboxrecorder.R
import kotlinx.parcelize.Parcelize

@Parcelize
enum class FileType(
    @DrawableRes val imageRes: Int,
    val suffixes: List<String>
) : Parcelable {
    Media(
        imageRes = R.drawable.ic_round_info,
        suffixes = listOf(
            //music
            "mp3",
            "aac",
            "3gp",
            "opus",
            "ogg",
            //video
            "mp4",
            "mkv",
            "avi",
        )
    ),
    Document(
        imageRes = R.drawable.ic_play,
        suffixes = listOf(
            "txt",
            "scv",
            "pdf",
            "html",
        )
    ),
    Image(
        imageRes = R.drawable.ic_pause,
        suffixes = listOf(
            "jpeg",
            "jpg",
            "png",
            "svg",
            "gif",
        )
    ),

    Other(
        imageRes = R.drawable.ic_close,
        suffixes = emptyList()
    )

    ;


    companion object {
        fun getType(extension: String): FileType? {
            values().forEach {
                it.suffixes.forEach { suffix ->
                    if (suffix == extension)
                        return it
                }
            }

            return null
        }
    }

}