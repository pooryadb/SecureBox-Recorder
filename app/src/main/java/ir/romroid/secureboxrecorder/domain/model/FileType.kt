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
    Music(
        imageRes = R.drawable.ic_music,
        suffixes = listOf(
            "mp3",
            "aac",
            "3gp",
            "opus",
            "ogg",
        )
    ),
    Video(
        imageRes = R.drawable.ic_video,
        suffixes = listOf(
            "mp4",
            "mkv",
            "avi",
        )
    ),
    Document(
        imageRes = R.drawable.ic_document,
        suffixes = listOf(
            "txt",
            "scv",
            "pdf",
            "html",
        )
    ),
    Picture(
        imageRes = R.drawable.ic_picture,
        suffixes = listOf(
            "jpeg",
            "jpg",
            "png",
            "svg",
            "gif",
        )
    ),

    Other(
        imageRes = R.drawable.ic_more,
        suffixes = emptyList()
    )

    ;


    companion object {
        fun getType(extension: String): FileType {
            values().forEach {
                it.suffixes.forEach { suffix ->
                    if (suffix == extension)
                        return it
                }
            }

            return Other
        }
    }

}