package uniproc.internal.operators

import uniproc.ERROR_TOKEN
import uniproc.Vm
import uniproc.internal.Token

class RaiseOperator: Operator("RAISE") {
    override fun operation(vm: Vm, tokens: List<Token>, verbose: Boolean): List<Token> {
        if (verbose) {
            println("[DEBUG] Entering Raise Error operation")
        }
        val errorMessage = tokens.joinToString("") { token ->
            token.value
        }
        return listOf(Token(ERROR_TOKEN, errorMessage))
    }
}