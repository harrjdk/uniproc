package uniproc.internal.operators.text

import uniproc.ERROR_TOKEN
import uniproc.VALUE_TOKEN
import uniproc.Vm
import uniproc.internal.Token
import uniproc.internal.operators.Operator
import uniproc.internal.operators.isVariable

class SplitOperator: Operator("SPLIT") {
    override fun operation(vm: Vm, tokens: List<Token>, verbose: Boolean): List<Token> {
        if (tokens.size < 2) {
            return listOf(Token(ERROR_TOKEN, "SPLIT operator didn't receive value and deliminator"))
        } else {
            val printStr = tokens.dropLast(1).joinToString("") { token ->
                if (isVariable(token.value)) {
                    vm.getVar(token.value.trim()).value
                } else {
                    token.value
                }
            }
            val last = tokens.last()
            val delimStr = if (isVariable(last.value)) {
                    vm.getVar(last.value.trim()).value
                } else {
                last.value
            }
            if (verbose) {
                println("[DEBUG] SPLIT Deliminator received \"$delimStr\"")
            }
            val csvStr = printStr.split(delimStr).joinToString(",") { "\'${it.trim()}\'" }
            if (!vm.populateInputBuffer(csvStr, "String")) {
                vm.display(csvStr)
            }
            return emptyList()
        }
    }
}