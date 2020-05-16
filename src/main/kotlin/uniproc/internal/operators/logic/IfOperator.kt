package uniproc.internal.operators.logic

import uniproc.ERROR_TOKEN
import uniproc.Vm
import uniproc.internal.Token
import uniproc.internal.operators.Operator
import uniproc.internal.operators.isVariable

class IfOperator: Operator("IF") {
    override fun operation(vm: Vm, tokens: List<Token>, verbose: Boolean): List<Token> {
        if (tokens.size < 2) {
            // something is wrong. throw an error token on the chain
            return listOf(Token(ERROR_TOKEN, "IF missing value"))
        } else {
            val nextToken = tokens[0]
            val possibleChain = tokens.drop(1)
            val value = if (isVariable(nextToken.value)) {
                vm.getVar(nextToken.value).value
            } else {
                nextToken.value
            }
            return if (value != null && value.trim() != "0") {
                possibleChain
            } else {
                emptyList()
            }
        }
    }

}