package uniproc.internal.operators

import uniproc.Vm
import uniproc.internal.Token

class InputOperator: Operator("INPUT") {
    override fun operation(vm: Vm, tokens: List<Token>, verbose: Boolean): List<Token> {
        vm.display(tokens.map {
            token ->
            token.value
        }.joinToString(""))

        // get input and assign to the standard input buffer
        val userInput = vm.getInput()
        if (verbose) {
            println("[DEBUG] INPUT received \"$userInput\"")
        }
        vm.populateInputBuffer(userInput)
        return emptyList()
    }
}