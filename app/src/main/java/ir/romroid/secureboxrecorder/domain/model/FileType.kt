package ir.romroid.secureboxrecorder.domain.model

import androidx.annotation.DrawableRes
import ir.romroid.secureboxrecorder.R

enum class FileType(
    @DrawableRes val imageRes: Int,
    val suffixes: List<String>
) {
    Media(
        imageRes = R.drawable.ic_round_info,
        suffixes = listOf(
            //music
            "mp3",
            "aac",
            "3gp",
            //video
            "mp4",
        )
    ),
    Text(
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