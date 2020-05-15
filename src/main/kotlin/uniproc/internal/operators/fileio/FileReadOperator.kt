package uniproc.internal.operators.fileio

import uniproc.ERROR_TOKEN
import uniproc.Vm
import uniproc.internal.Token
import uniproc.internal.operators.Operator

class FileReadOperator: Operator("READ") {
    override fun operation(vm: Vm, tokens: List<Token>, verbose: Boolean): List<Token> {
        if (verbose) {
            println("[DEBUG] Entering File Read Operation")
        }
        // this is very similar to input but we're reading from a file
        val filePath = tokens.joinToString("") { token ->
            token.value
        }
        val contents = vm.getFileContents(filePath)
        return if (contents != null) {
            if (!vm.populateInputBuffer(contents!!, "String")) {
                vm.display(contents)
            }
            emptyList()
        } else {
            listOf(Token(ERROR_TOKEN, "Cannot access file $filePath"))
        }
    }
}