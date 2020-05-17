package uniproc.internal.operators

import uniproc.ERROR_TOKEN
import uniproc.VALUE_TOKEN
import uniproc.Vm
import uniproc.internal.Token

class ArgOperator: Operator("ARG") {
    override fun operation(vm: Vm, tokens: List<Token>, verbose: Boolean): List<Token> {
        if (tokens.isEmpty()) {
            return listOf(Token(ERROR_TOKEN, "Missing argument index!"))
        }
        val argStr = tokens.joinToString("") { token ->
            if (isVariable(token.value)) {
                vm.getVar(token.value.trim()).value
            } else {
                token.value
            }
        }
        val retVal = vm.getArg(argStr).value
        return if (!vm.populateInputBuffer(retVal, "String")) {
            vm.display(retVal)
            listOf(Token(VALUE_TOKEN, retVal))
        } else {
            emptyList()
        }
    }
}