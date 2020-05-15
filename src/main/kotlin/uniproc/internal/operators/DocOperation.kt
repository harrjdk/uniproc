package uniproc.internal.operators

import uniproc.Vm
import uniproc.internal.Token

class DocOperation: Operator("DOC") {
    override fun operation(vm: Vm, tokens: List<Token>, verbose: Boolean): List<Token> {
        // this is no-op
        return emptyList()
    }
}