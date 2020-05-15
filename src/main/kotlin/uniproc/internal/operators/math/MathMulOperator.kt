package uniproc.internal.operators.math

import uniproc.ERROR_TOKEN
import uniproc.NULL
import uniproc.VALUE_TOKEN
import uniproc.Vm
import uniproc.internal.ERROR_TYPE
import uniproc.internal.Token
import uniproc.internal.operators.Operator
import uniproc.internal.operators.isVariable

class MathMulOperator: Operator("*") {
    override fun operation(vm: Vm, tokens: List<Token>, verbose: Boolean): List<Token> {
        if (verbose) {
            println("[DEBUG] entering mathematic mul operation")
        }
        if (tokens.isEmpty()) {
            return listOf(Token(ERROR_TYPE, "No right side to operation"))
        }
        // we need to see if there's a value in the value buffer
        val valueBuffer = vm.getValueBuffer()
        if (valueBuffer!=NULL) {
            var leftSideValueHolder = ""
            val (_, leftSideValue) = valueBuffer
            // by default everything is a string. This is important for a few reasons:
            // 1. we don't gain or lose data from casting to lossy formats
            // 2. Constants could be interned
            // check if the left side is a variable
            if (isVariable(leftSideValue.trim())) {
                // follow the reference
                leftSideValueHolder = vm.getVar(leftSideValue.trim()).value
            } else {
                leftSideValueHolder = leftSideValue
            }
            // try to convert the left side to
            val leftSide = castMathType(leftSideValueHolder, verbose)
            // there's a performance cost here, but we ensure we're able to use common casting for all math ops
            // now repeat for right side
            val (_, rightSideValue) = tokens[0]
            var rightSideValueHolder = ""
            if (isVariable(rightSideValue.trim())) {
                rightSideValueHolder = vm.getVar(rightSideValue.trim()).value
            } else {
                rightSideValueHolder = rightSideValue
            }
            val rightSide = castMathType(rightSideValueHolder, verbose)
            // now if either is an error, abort
            if (leftSide.type== ERROR_TYPE) {
                return listOf(Token(ERROR_TOKEN, leftSide.value))
            }
            if (rightSide.type== ERROR_TYPE) {
                return listOf(Token(ERROR_TOKEN, rightSide.value))
            }
            // our buffer
            var output = ""
            // now try and combine them, prefering the more precise type
            if (leftSide.type==MathTypes.FLOAT.string || rightSide.type==MathTypes.FLOAT.string) {
                output = (leftSide.value.toFloat() * rightSide.value.toFloat()).toString()
            } else {
                output = (leftSide.value.toInt() * rightSide.value.toInt()).toString()
            }
            // check if we have more possible chain operations
            if(tokens.size > 1) {
                // push this to the left side buffer
                vm.setValueBuffer(Token(VALUE_TOKEN, output))
                return tokens.drop(1)
            } else if (!vm.populateInputBuffer(output, "String")) {
                vm.display(output)
                return emptyList()
            } else {
                vm.clearValueBuffer() // shouldn't let that keep propagating
                return emptyList()
            }
        } else {
            return listOf(Token(ERROR_TOKEN, "no value on the left side to multiply"))
        }
    }
}