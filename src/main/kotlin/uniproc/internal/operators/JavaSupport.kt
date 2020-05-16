package uniproc.internal.operators

import uniproc.Vm
import uniproc.internal.Token

interface JavaSupport {
    fun compileJava(vm: Vm, tokens: List<Token>, verbose: Boolean=false): List<Token>
    fun sanitizeVar(varName: String): String {
        return varName.trim().substring(1).replace(".","_")
    }
}