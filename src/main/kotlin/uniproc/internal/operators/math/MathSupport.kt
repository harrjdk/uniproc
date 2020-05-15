package uniproc.internal.operators.math

import uniproc.internal.ERROR_TYPE
import uniproc.types.Structure

val alphaCheck = Regex("[^0-9.]")

enum class MathTypes(val string: String) {
    INT("Int"),
    FLOAT("Float")
}

fun castMathType(value: String, verbose: Boolean=false): Structure {
    if (verbose) {
        println("[DEBUG] trying to cast for math \"$value\"")
    }
    // first reject things with text
    val trimmed = value.trim()
    when {
        trimmed.contains(alphaCheck) -> {
            return Structure(ERROR_TYPE, "Invalid alphabetic characters in number type value")
        }
        trimmed.contains(".") -> {
            // probably a decimal
            return try {
                Structure(MathTypes.FLOAT.string, trimmed.toFloat().toString())
            } catch (e: NumberFormatException) {
                Structure(ERROR_TYPE, "Invalid decimal number \"$value\"")
            }
        }
        else -> {
            return try {
                Structure(MathTypes.INT.string, trimmed.toInt().toString())
            } catch (e: NumberFormatException) {
                Structure(ERROR_TYPE, "Invalid number \"$value\"")
            }
        }
    }
}