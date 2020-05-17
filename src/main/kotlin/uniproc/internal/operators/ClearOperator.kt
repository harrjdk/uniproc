package uniproc.internal.operators

import uniproc.ERROR_TOKEN
import uniproc.Vm
import uniproc.internal.Token

class ClearOperator: Operator("CLEAR") {
    override fun operation(vm: Vm, tokens: List<Token>, verbose: Boolean): List<Token> {
        return if (tokens.size != 1) {
            listOf(Token(ERROR_TOKEN, "Cannot clear non-reference!"))
        } else {
            vm.delVar(tokens[0].value)
            emptyList()
        }
    }

}