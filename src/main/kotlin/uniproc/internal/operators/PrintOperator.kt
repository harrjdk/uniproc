package uniproc.internal.operators

import uniproc.Vm
import uniproc.internal.Token

class PrintOperator: Operator("PRINT") {
    override fun operation(vm: Vm, tokens: List<Token>, verbose: Boolean): List<Token> {
        vm.display(tokens.joinToString("") { token ->
            if (isVariable(token.value)) {
                vm.getVar(token.value.trim()).value
            } else {
                token.value
            }
        })
        return emptyList()
    }
}