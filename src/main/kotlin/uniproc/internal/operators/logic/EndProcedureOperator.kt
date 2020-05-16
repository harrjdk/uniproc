package uniproc.internal.operators.logic

import uniproc.ERROR_TOKEN
import uniproc.Vm
import uniproc.internal.Token
import uniproc.internal.operators.Operator


// Procedures must be ended with this op.
// procedures are really just token chains in a list of lists
// this tells the current vm to close the current list chain
class EndProcedureOperator: Operator("END") {
    override fun operation(vm: Vm, tokens: List<Token>, verbose: Boolean): List<Token> {
        return if (tokens.isNotEmpty()) {
            listOf(Token(ERROR_TOKEN, "Invalid line. END cannot be followed by additional data"))
        } else {
            vm.closeRoutine()
            emptyList()
        }
    }
}