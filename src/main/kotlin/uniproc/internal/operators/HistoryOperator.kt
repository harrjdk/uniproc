package uniproc.internal.operators

import uniproc.Vm
import uniproc.internal.Token

class HistoryOperator: Operator("HISTORY") {
    override fun operation(vm: Vm, tokens: List<Token>, verbose: Boolean): List<Token> {
        val history = vm.getHistoryChronologically().joinToString("\n")
        if (verbose) {
            println("[DEBUG] Pulled VM history records")
        }
        if (!vm.populateInputBuffer(history, "String")) {
            vm.display(history)
        }
        return emptyList()
    }
}