package uniproc.internal.operators

import uniproc.EXIT_TOKEN
import uniproc.Vm
import uniproc.internal.Token

class ExitOperator: Operator("EXIT") {
    override fun operation(vm: Vm, tokens: List<Token>, verbose: Boolean): List<Token> {
        return listOf(Token(EXIT_TOKEN, ""))
    }
}