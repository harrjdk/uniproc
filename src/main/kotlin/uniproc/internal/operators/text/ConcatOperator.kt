package uniproc.internal.operators.text

import uniproc.Vm
import uniproc.internal.Token
import uniproc.internal.operators.Operator
import uniproc.internal.operators.isVariable

class ConcatOperator: Operator("CONCAT") {
    override fun operation(vm: Vm, tokens: List<Token>, verbose: Boolean): List<Token> {
        // very similar to PRINT except we strip beginning and trailing spaces
        val printStr = tokens.joinToString("") { token ->
            if (isVariable(token.value)) {
                vm.getVar(token.value.trim()).value
            } else {
                token.value.trim()
            }
        }
        if (!vm.populateInputBuffer(printStr, "String")) {
            vm.display(printStr)
        }
        return emptyList()
    }
}