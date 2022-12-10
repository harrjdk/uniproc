package uniproc

import uniproc.internal.Token
import java.util.*

// As functionality is added, we should add more reserved words
// These are more or less going to be translated into macros
// and idioms in the target language
val RESERVED_WORDS = OPERATIONS.map { it.name }
const val OPERATION_TOKEN = "OPERATOR"
const val VALUE_TOKEN = "VALUE"
const val ERROR_TOKEN = "ERROR"
const val EXIT_TOKEN = "EXIT"
const val NULL_TOKEN = "NULL"
val NULL = Token(NULL_TOKEN, "")

open class Parser(val verbose: Boolean = true, vm: Vm = Vm(verbose = verbose)) {
    var lineCount = 0
    val myVm = vm
    open var healthy = true

    fun parseLine(line: String): List<Token> {
        // push the line to the vm's history
        myVm.addHistory(line)

        // parsing uniproc is easy, we read left to right and pick up reserved words
        // since this is made for everyday users, concepts of quoted strings and such
        // are meaningless
        // Begin reading until we hit a space
        val tokens = ArrayList<Token>()
        var buffer = ""
        var currentChar = ' '
        var counter = 0
        while (counter < line.length) {
            currentChar = line[counter]
            buffer+=currentChar
            if (verbose) {
                println("[DEBUG] Current Buffer: $buffer")
            }
            if (currentChar == ' ' || counter == line.length-1) {
                // create a token from the item, this is not precise at this point.
                tokens.add(Token(
                        if (RESERVED_WORDS.contains(buffer.uppercase(Locale.getDefault()).trim())) OPERATION_TOKEN else VALUE_TOKEN,
                        buffer
                ))
                buffer = ""
            }
            counter+=1
        }
        return tokens
    }

    fun executeTokens(tokens: List<Token>, args: List<String> = emptyList()) {
        if (myVm.exit) {
            // we shouldn't be here anyway
            return
        }
        lineCount+=1
        if (verbose) {
            println("[DEBUG] current tokens: ${tokens.joinToString { token -> "${token.type}->${token.value}" }}")
        }
        args.forEachIndexed { index, it ->
            if (verbose) {
                println("[DEBUG] setting arg ${index+1} to $it")
            }
            myVm.setArg(index+1, it)
        }
        healthy = myVm.handleTokens(listOf(tokens))
    }

    fun compileTokens(tokens: List<Token>) {
        if (verbose) {
            println("[DEBUG] current tokens: ${tokens.joinToString { token -> "${token.type}->${token.value}" }}")
        }
        healthy = myVm.compileTokens(tokens)
    }

    fun getCompiledCode(className: String): String {
        return """
            public class $className {
            ${myVm.supportMetaData}
            ${myVm.supportMethods.map{
            entry ->
            """public static void ${entry.key} {
                |${entry.value}
                |}
            """.trimMargin()
                }.joinToString("\n")
            }
            public static void main(String[] args) {
                ${myVm.mainMethod.toString()}
            }
            }
        """.trimIndent()
    }
}