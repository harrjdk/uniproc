package uniproc.internal.operators.text

import uniproc.ERROR_TOKEN
import uniproc.NULL
import uniproc.VALUE_TOKEN
import uniproc.Vm
import uniproc.internal.ERROR_TYPE
import uniproc.internal.Token
import uniproc.internal.operators.Operator
import uniproc.internal.operators.isVariable
import uniproc.internal.operators.math.castMathType

class AtIndexOperator: Operator("AT") {
    override fun operation(vm: Vm, tokens: List<Token>, verbose: Boolean): List<Token> {
        if (verbose) {
            println("[DEBUG] entering text at index operation")
        }
        if (tokens.isEmpty()) {
            return listOf(Token(ERROR_TYPE, "No right side to operation"))
        }
        // we need to see if there's a value in the value buffer
        val valueBuffer = vm.getValueBuffer()
        if (valueBuffer!= NULL) {
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
            // we need to now assume this is an array. We hold arrays as strings in csv row format
            val leftSideList = parseCsvRow(leftSideValueHolder)
            // now we need to pull the element at the requested index
            val (_, rightSideValue) = tokens[0]
            var rightSideValueHolder = ""
            rightSideValueHolder = if (isVariable(rightSideValue.trim())) {
                vm.getVar(rightSideValue.trim()).value
            } else {
                rightSideValue
            }
            val rightSide = castMathType(rightSideValueHolder, verbose)
            // now if either is an error, abort
            if (rightSide.type== ERROR_TYPE) {
                return listOf(Token(ERROR_TOKEN, rightSide.value))
            }
            val index = rightSide.value.toInt()
            return if (index < 1 || leftSideList.size < (index -1)) {
                listOf(Token(ERROR_TOKEN, "Index $index out of bounds for ${leftSideValue.trim()}"))
            } else {
                val result = leftSideList[index - 1]
                if (!vm.populateInputBuffer(result, "String")) {
                    listOf(Token(VALUE_TOKEN, result))
                } else {
                    emptyList()
                }
            }
        } else {
            return listOf(Token(ERROR_TOKEN, "no value on the left side to check for index"))
        }
    }
}