package uniproc.internal.operators

import uniproc.ERROR_TOKEN
import uniproc.VALUE_TOKEN
import uniproc.Vm
import uniproc.internal.Token

class BlankOperator: Operator("BLANK") {
    override fun operation(vm: Vm, tokens: List<Token>, verbose: Boolean): List<Token> {
        return if (tokens.isNotEmpty()) {
            listOf(Token(ERROR_TOKEN, "BLANK found with trailing operations"))
        } else {
            if (!vm.populateInputBuffer("", "String")) {
                listOf(Token(VALUE_TOKEN, ""))
            } else {
                emptyList()
            }
        }
    }
}