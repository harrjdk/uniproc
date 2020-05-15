package uniproc.internal.operators

import uniproc.Vm
import uniproc.internal.Token

fun isVariable(value: String): Boolean {
    return value != null && value.trim().startsWith("@")
}

abstract class Operator (val name: String) {

    // operations act on a vm and return a list of unconsumed tokens
    abstract fun operation(vm: Vm, tokens: List<Token>, verbose: Boolean = false): List<Token>
}