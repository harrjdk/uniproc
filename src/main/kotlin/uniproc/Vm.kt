package uniproc

import uniproc.internal.ERROR_TYPE
import uniproc.internal.Token
import uniproc.internal.operators.*
import uniproc.internal.operators.fileio.FileReadOperator
import uniproc.internal.operators.fileio.FileWriteOperator
import uniproc.internal.operators.math.MathDivOperator
import uniproc.internal.operators.math.MathMulOperator
import uniproc.internal.operators.math.MathPlusOperator
import uniproc.internal.operators.math.MathSubOperator
import uniproc.types.Routine
import uniproc.types.Structure
import java.io.File
import java.nio.charset.Charset
import java.util.*
import kotlin.collections.HashMap

val OPERATIONS = listOf<Operator>(
        DocOperation(),
        RaiseOperator(),
        PrintOperator(),
        InputOperator(),
        AssignmentOperator(),
        HistoryOperator(),
        MathPlusOperator(),
        MathSubOperator(),
        MathMulOperator(),
        MathDivOperator(),
        FileReadOperator(),
        FileWriteOperator()
)

class Vm(val verbose: Boolean = false) {

    private var valueSnapShotThreshold = 2

    private val history = LinkedList<String>()

    // name of the var we're going to assign the next input buffer to
    private var inputBufferClaim = ""
    // value buffer for non-left-side operators (+, -, *, /, etc)
    private var valueBuffer:Token = NULL
    private val variables = HashMap<String, Structure>()
    private val routines = HashMap<String, Routine>()

    fun setVar(name: String, value: Structure) {
        // we need to push the old values backwards
        if (variables.containsKey(name)) {
            for (i in valueSnapShotThreshold downTo 1) {
                val currentName = if ((i-1) == 0) "$name" else "${name}.previous${i-1}"
                if (variables[currentName] != null) {
                    if (verbose) {
                        println("[DEBUG] Setting ${name}.previous$i = $currentName (${variables[currentName]})")
                    }
                    variables["${name}.previous$i"] = variables[currentName]!!
                }
            }
        }
        if (verbose) {
            println("[DEBUG] Setting $name = ${value.value}")
        }
        variables[name] = value
    }

    fun getVar(name: String): Structure {
        return variables[name] ?: Structure(ERROR_TYPE, name)
    }

    fun handleTokens(tokens: List<Token>, debug: Boolean=true): Boolean {
        var unhandledTokens = tokens
        while (unhandledTokens.isNotEmpty()) {
            val currentToken = unhandledTokens[0]
            if (currentToken.type == OPERATION_TOKEN) {
                if (verbose) {
                    println("[DEBUG] running token \"${currentToken.value}\"")
                }
                OPERATIONS.forEach operatorCheck@{ operation ->
                    if (operation.name == currentToken.value.toUpperCase().trim()) {
                        if (verbose) {
                            println("[DEBUG] Matched operation ${operation.name}")
                        }
                        unhandledTokens = operation.operation(this, unhandledTokens.drop(1), verbose)
                        return@operatorCheck
                    }
                }
            } else if (currentToken.type == ERROR_TOKEN) {
                display("ERROR! ${currentToken.value}")
                return false
            } else {
                // we shouldn't have a line like this?
                if (verbose) {
                    println("[DEBUG] Got value token ${currentToken.type}->${currentToken.value}")
                }
                setValueBuffer(currentToken)
                unhandledTokens = unhandledTokens.drop(1)
            }
        }
        // sanity checks
        if (inputBufferClaim.isNotEmpty()) {
            println("WARNING! Input Buffer was unclaimed. This suggests bad assignment!")
            inputBufferClaim = ""
        }
        if (valueBuffer != NULL) {
            println("WARNING! Value Buffer was unclaimed! This suggests a missing operation!")
            valueBuffer = NULL
        }
        return true
    }

    fun display(displayString: String) {
        println(displayString)
    }

    fun getInput(): String {
        return readLine()?: ""
    }

    fun populateInputBuffer(input: String, type: String): Boolean {
        if (inputBufferClaim.isEmpty()) {
            return false
        } else {
            setVar(inputBufferClaim.trim(), Structure(type, input))
            inputBufferClaim = ""
            return true
        }
    }

    fun claimInputBuffer(name: String) {
        inputBufferClaim = name
    }

    fun getHistoryChronologically(): List<String> {
        return history.reversed()
    }

    fun addHistory(line: String) {
        history.addFirst(line)
    }

    fun getLastHistoryEntry(item: Int = 0): String {
        return history[item]
    }

    fun getValueBuffer(): Token {
        if (verbose) {
            println("[DEBUG] Accessing value buffer ${valueBuffer.type}->${valueBuffer.value}")
        }
        return valueBuffer
    }

    fun setValueBuffer(token: Token) {
        if (verbose) {
            println("[DEBUG] Setting value buffer to ${token.type}->${token.value}")
        }
        valueBuffer = token
    }

    fun clearValueBuffer() {
        if (verbose) {
            println("[DEBUG] clearing the value buffer")
        }
        valueBuffer = NULL
    }

    fun getFileContents(path: String): String? {
        val file = File(path)
        return if (file.exists() && file.canRead()) {
            file.readLines().joinToString("\n")
        } else {
            null
        }
    }

    fun writeFile(path: String, text: String, append: Boolean = false): Boolean {
        val file = File(path)
        return if (file.canWrite()) {
            if (append) {
                file.appendText(text, Charset.defaultCharset())
            } else {
                file.writeText(text, Charset.defaultCharset())
            }
            true
        } else {
            false
        }
    }
}