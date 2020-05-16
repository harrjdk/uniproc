package uniproc.internal.operators.logic

import uniproc.ERROR_TOKEN
import uniproc.Vm
import uniproc.internal.Token
import uniproc.internal.operators.Operator
import uniproc.internal.operators.isVariable

class ProcedureOperator: Operator("PROCEDURE") {
    override fun operation(vm: Vm, tokens: List<Token>, verbose: Boolean): List<Token> {
        if (tokens.isEmpty()) {
            return listOf(Token(ERROR_TOKEN, "No procedure name specified!"))
        } else {
            val procName = tokens.joinToString("") { token ->
                if (isVariable(token.value)) {
                    vm.getVar(token.value.trim()).value
                } else {
                    token.value
                }
            }
            val possibleErrors = vm.setRoutineName(procName)
            return if (possibleErrors.isNotEmpty()) {
                possibleErrors
            } else {
                emptyList()
            }
        }
    }
}