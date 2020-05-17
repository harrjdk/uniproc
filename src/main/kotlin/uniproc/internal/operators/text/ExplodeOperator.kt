package uniproc.internal.operators.text

import uniproc.ERROR_TOKEN
import uniproc.VALUE_TOKEN
import uniproc.Vm
import uniproc.internal.Token
import uniproc.internal.operators.Operator
import uniproc.internal.operators.isVariable

class ExplodeOperator: Operator("EXPLODE") {
    override fun operation(vm: Vm, tokens: List<Token>, verbose: Boolean): List<Token> {
        if (tokens.isEmpty()) {
            return listOf(Token(ERROR_TOKEN, "EXPLODE operator didn't receive value"))
        } else {
            val printStr = tokens.joinToString("") { token ->
                if (isVariable(token.value)) {
                    vm.getVar(token.value.trim()).value
                } else {
                    token.value
                }
            }
            val csvStr = printStr.split("").drop(1).dropLast(1).joinToString(",") { "\'$it\'" }
            if (!vm.populateInputBuffer(csvStr, "String")) {
                vm.display(csvStr)
            }
            return emptyList()
        }
    }
}