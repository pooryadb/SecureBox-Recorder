package ir.romroid.secureboxrecorder.utils

import android.util.Patterns
import ir.romroid.secureboxrecorder.R
import java.util.regex.Pattern

object MyValidator {

    fun isUserNameValid(username: String): Boolean {//no validation
        return username.isNotEmpty()
    }

    fun isPhoneNumberValid(number: String): Boolean {
        return Patterns.PHONE.matcher(number).matches()
                && number.length == 11
                && number.substring(0, 2) == "09"
    }

    fun isCodeValid(code: String): Boolean {// 5 digit
        return code.length == 5 && Pattern.compile("[0-9]").toRegex().containsMatchIn(code)
    }

    fun isPasswordValid(pass: String): Int? {
        val patternStringList = listOf(
            Pair("[~!@$^_|]", R.string.invalid_pass_special_letter),
            Pair("[A-Za-z]", R.string.invalid_pass_string),
            Pair("[0-9]", R.string.invalid_pass_numbers),
            Pair(".{6,}", R.string.invalid_pass_long),
        )

        patternStringList.forEach {
            val check = Pattern.compile(it.first).toRegex().containsMatchIn(pass)
            if (!check)
                return it.second
        }

        return null
    }

    fun isNameValid(name: String): Boolean {
        return name.length in 2..100
    }

    fun isNationalCodeValid(code: String): Boolean {// 10 digit
        return code.length == 10 && Pattern.compile("[0-9]").toRegex().containsMatchIn(code)
    }
}