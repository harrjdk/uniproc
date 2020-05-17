package uniproc.internal.operators.math

import uniproc.ERROR_TOKEN
import uniproc.VALUE_TOKEN
import uniproc.Vm
import uniproc.internal.ERROR_TYPE
import uniproc.internal.Token
import uniproc.internal.operators.Operator
import uniproc.internal.operators.isVariable
import kotlin.math.acos

class MathArcCosOperator: Operator("ACOS") {
    override fun operation(vm: Vm, tokens: List<Token>, verbose: Boolean): List<Token> {
        if (verbose) {
            println("[DEBUG] entering mathematic arccosine operation")
        }
        if (tokens.isEmpty()) {
            return listOf(Token(ERROR_TYPE, "No right side to operation"))
        }
        val (_, rightSideValue) = tokens[0]
        var rightSideValueHolder = ""
        rightSideValueHolder = if (isVariable(rightSideValue.trim())) {
            vm.getVar(rightSideValue.trim()).value
        } else {
            rightSideValue
        }
        val rightSide = castMathType(rightSideValueHolder, verbose)
        if (rightSide.type== ERROR_TYPE) {
            return listOf(Token(ERROR_TOKEN, rightSide.value))
        }
        // our buffer
        var output = ""
        // now try and combine them, prefering the more precise type
        output = if (rightSide.type==MathTypes.FLOAT.string) {
            acos(rightSide.value.toDouble()).toString()
        } else {
            acos(rightSide.value.toDouble()).toInt().toString()
        }
        // check if we have more possible chain operations
        return if(tokens.size > 1) {
            // push this to the left side buffer
            vm.setValueBuffer(Token(VALUE_TOKEN, output))
            tokens.drop(1)
        } else if (!vm.populateInputBuffer(output, "String")) {
            vm.display(output)
            emptyList()
        } else {
            vm.clearValueBuffer() // shouldn't let that keep propagating
            emptyList()
        }
    }
}