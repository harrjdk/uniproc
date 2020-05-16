package uniproc.internal.operators.fileio

import uniproc.ERROR_TOKEN
import uniproc.Vm
import uniproc.internal.ERROR_TYPE
import uniproc.internal.Token
import uniproc.internal.operators.Operator

class FileAppendOperator: Operator("APPEND") {
    override fun operation(vm: Vm, tokens: List<Token>, verbose: Boolean): List<Token> {
        if (verbose) {
            println("[DEBUG] Entering File APPEND Operation")
        }
        // this is very similar to input but we're reading from a file
        if (tokens.size < 2) {
            return listOf(Token(ERROR_TOKEN, "No data holder token present for write operation"))
        }
        // Any data that is written should come from a reference. This is an opinion enforced by the interpreter.
        val dataHolder = vm.getVar(tokens[0].value.trim())
        if (dataHolder.type== ERROR_TYPE) {
            return listOf(Token(ERROR_TOKEN, "Reference \"${tokens[0].value.trim()}\" is invalid."))
        }
        val filePath = tokens.drop(1).joinToString("") { token ->
            token.value
        }
        return if (vm.writeFile(filePath, dataHolder.value, true)) {
            emptyList()
        } else {
            listOf(Token(ERROR_TYPE, "An error occurred writing to $filePath"))
        }
    }
}