package uniproc.internal.operators

import uniproc.Vm
import uniproc.internal.Token

class PrintOperator: Operator("PRINT"), JavaSupport {
    override fun operation(vm: Vm, tokens: List<Token>, verbose: Boolean): List<Token> {
        if (verbose) {
            println("[DEBUG] entering print operation")
        }
        val printStr = tokens.joinToString("") { token ->
            if (isVariable(token.value)) {
                vm.getVar(token.value.trim()).value
            } else {
                token.value
            }
        }
        if (!vm.populateInputBuffer(printStr, "String")) {
            vm.display(printStr)
        }
        return emptyList()
    }

    override fun compileJava(vm: Vm, tokens: List<Token>, verbose: Boolean): List<Token> {
        val methodContent = vm.getMainExecution()
        // mv is our method content
        // now export PRINT AS A SYSTEM OUT PRINTLN
        methodContent.append("System.out.println(String.format(\"")
        val variableList = ArrayList<String>()
        tokens.forEach{
            token ->
            if (isVariable(token.value)) {
                if (token.value.startsWith(" ")) {
                    methodContent.append(" ")
                }
                methodContent.append("%s")
                if (token.value.endsWith(" ")) {
                    methodContent.append(" ")
                }
                variableList.add(token.value)
            } else {
                methodContent.append(token.value.replace("\"", "\\\""))
            }
        }
        methodContent.append("\",")
        methodContent.append(variableList.joinToString(",") {variable ->
            sanitizeVar(variable)
        })
        methodContent.append("));")
        return emptyList()
    }
}