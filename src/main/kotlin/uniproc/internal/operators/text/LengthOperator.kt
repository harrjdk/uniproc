package uniproc.internal.operators.text

import uniproc.ERROR_TOKEN
import uniproc.VALUE_TOKEN
import uniproc.Vm
import uniproc.internal.Token
import uniproc.internal.operators.Operator
import uniproc.internal.operators.isVariable

class LengthOperator: Operator("LENGTH") {
    override fun operation(vm: Vm, tokens: List<Token>, verbose: Boolean): List<Token> {
        return if (tokens.size != 1) {
            listOf(Token(ERROR_TOKEN, "LENGTH operator didn't receive a valid reference"))
        } else {
            val nextToken = tokens[0]
            if (!isVariable(nextToken.value)) {
                listOf(Token(ERROR_TOKEN, "LENGTH operator didn't receive a valid reference"))
            } else {
                val list = parseCsvRow(vm.getVar(nextToken.value).value)
                val size = list.size.toString()
                if (!vm.populateInputBuffer(size, "String")) {
                    vm.display(size)
                    emptyList()
                } else {
                    listOf(Token(VALUE_TOKEN, list.size.toString()))
                }
            }
        }
    }
}