package uniproc.internal.operators.logic

import uniproc.ERROR_TOKEN
import uniproc.Vm
import uniproc.internal.Token
import uniproc.internal.operators.Operator
import uniproc.internal.operators.isVariable

class ExecuteOperator: Operator("EXECUTE") {
    override fun operation(vm: Vm, tokens: List<Token>, verbose: Boolean): List<Token> {
        return if (tokens.isEmpty()) {
            listOf(Token(ERROR_TOKEN, "No procedure name specified for execute!"))
        } else {
            val procName = tokens.joinToString("") { token ->
                if (isVariable(token.value)) {
                    vm.getVar(token.value.trim()).value
                } else {
                    token.value
                }
            }
            return if (vm.handleTokens(vm.getRoutine(procName), verbose)) {
                emptyList()
            } else {
                listOf(Token(ERROR_TOKEN, "Error handling procedure $procName"))
            }
        }
    }
}