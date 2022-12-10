package uniproc.internal.operators

import uniproc.ERROR_TOKEN
import uniproc.Parser
import uniproc.Vm
import uniproc.internal.Token
import uniproc.internal.operators.Operator
import kotlin.system.exitProcess

/** LoadOperator
 * Loads another uniproc file into the current VM context
 */
class LoadOperator: Operator("LOAD") {
    override fun operation(vm: Vm, tokens: List<Token>, verbose: Boolean): List<Token> {
        if (verbose) {
            println("[DEBUG] Entering Load Operation")
        }
        // this is very similar to input, but we're reading from a file
        // based on the READ operator but then executes the contents via the parser and the current vm
        // context
        val filePath = tokens.joinToString("") { token ->
            token.value
        }
        val contents = vm.getFileContents(filePath)
        return if (contents != null) {
            // TODO should we grab the potentially interactive parser here?
            val parser = Parser(verbose, vm)
            contents.lines().forEach {
                if (parser.healthy) {
                    parser.executeTokens(parser.parseLine(it))
                    if (parser.myVm.exit) {
                        if (verbose) {
                            println("[DEBUG] LOAD operation attempted to signal shutdown!")
                        }
                        parser.executeTokens(listOf(Token(ERROR_TOKEN, "LOAD operation attempted EXIT")))
                    }
                } else {
                    println("Terminated on execution line ${parser.lineCount} during LOAD $filePath")
                    exitProcess(1)
                }
            }
            emptyList()
        } else {
            listOf(Token(ERROR_TOKEN, "Cannot access file $filePath"))
        }
    }
}