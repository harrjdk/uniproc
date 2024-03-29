package uniproc.internal.operators.text

import uniproc.ERROR_TOKEN
import uniproc.Vm
import uniproc.internal.Token
import uniproc.internal.operators.Operator
import uniproc.internal.operators.isVariable
import uniproc.internal.operators.math.MathTypes
import uniproc.internal.operators.math.castMathType

class StringEqualsCaseInsensitiveOperator: Operator("STREQCI") {
    override fun operation(vm: Vm, tokens: List<Token>, verbose: Boolean): List<Token> {
        if (tokens.size < 3) {
            // something is wrong. throw an error token on the chain
            return listOf(Token(ERROR_TOKEN, "STREQCI missing value"))
        } else {
            val nextToken = tokens[0]
            val comparingToken = tokens[1]
            val possibleChain = tokens.drop(2)
            val value1 = if (isVariable(nextToken.value)) {
                vm.getVar(nextToken.value).value
            } else {
                nextToken.value
            }
            val value2 = if (isVariable(comparingToken.value)) {
                vm.getVar(comparingToken.value).value
            } else {
                comparingToken.value
            }
            return if (value1.equals(value2, true)) {
                possibleChain
            } else {
                emptyList()
            }
        }
    }
}