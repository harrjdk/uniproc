package uniproc.internal.operators

import uniproc.ERROR_TOKEN
import uniproc.OPERATION_TOKEN
import uniproc.Vm
import uniproc.internal.Token
import uniproc.types.Structure

fun containsOperation(tokens: List<Token>): Boolean {
    return tokens.map { token -> token.type }.any { type -> type == OPERATION_TOKEN }
}

class AssignmentOperator: Operator("ASSIGN") {
    override fun operation(vm: Vm, tokens: List<Token>, verbose: Boolean): List<Token> {
        // This one is more complex because you could assign from an
        // expression such as ASSIGN @NAME INPUT WHAT'S YOUR NAME?
        // so we need to get the name we're claiming and see if the next token
        // is an operation

        if (tokens.size < 2) {
            // something is wrong. throw an error token on the chain
            return listOf(Token(ERROR_TOKEN, "ASSIGN missing value"))
        }
        val nextToken = tokens[0]
        if (!isVariable(nextToken.value)) {
            // another error.
            return listOf(Token(ERROR_TOKEN, "ASSIGN must receive a reference"))
        }
        // if any future tokens are operators, we need to check
        val possibleReturn = tokens.drop(1)
        return if (containsOperation(possibleReturn)) {
            // we need to reserve the input buffer
            vm.claimInputBuffer(nextToken.value.trim())
            possibleReturn
        } else {
            vm.setVar(nextToken.value.trim(), Structure("String", tokens.drop(1).joinToString("") { token ->
                token.value
            }))
            emptyList()
        }
    }
}