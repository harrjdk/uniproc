package uniproc.internal.operators

import uniproc.Vm
import uniproc.internal.Token

class DocOperation: Operator("DOC"), JavaSupport {
    override fun operation(vm: Vm, tokens: List<Token>, verbose: Boolean): List<Token> {
        // this is no-op
        return emptyList()
    }

    override fun compileJava(vm: Vm, tokens: List<Token>, verbose: Boolean): List<Token> {
        vm.getMainExecution().append("/* ${tokens.joinToString("") { "${it.value}" }} */")
        return emptyList()
    }
}