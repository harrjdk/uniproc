package uniproc.internal.operators.logic

import uniproc.ERROR_TOKEN
import uniproc.Vm
import uniproc.internal.Token
import uniproc.internal.operators.Operator
import uniproc.internal.operators.isVariable
import uniproc.internal.operators.math.MathTypes
import uniproc.internal.operators.math.castMathType

class NotEqualsOperator: Operator("NEQ") {
    override fun operation(vm: Vm, tokens: List<Token>, verbose: Boolean): List<Token> {
        if (tokens.size < 3) {
            // something is wrong. throw an error token on the chain
            return listOf(Token(ERROR_TOKEN, "NEQ missing value"))
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
            val value1Structure = castMathType(value1, verbose)
            val value2Structure = castMathType(value2, verbose)
            return if (value1Structure.type==MathTypes.FLOAT.string || value2Structure.type==MathTypes.FLOAT.string) {
                if (value1Structure.value.toDouble() != value2Structure.value.toDouble()) {
                    possibleChain
                } else {
                    emptyList()
                }
            } else {
                if (value1Structure.value.toInt() != value2Structure.value.toInt()) {
                    possibleChain
                } else {
                    emptyList()
                }
            }
        }
    }

}