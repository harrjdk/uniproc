package uniproc

import uniproc.internal.ERROR_TYPE
import uniproc.internal.Token
import uniproc.internal.operators.*
import uniproc.internal.operators.fileio.FileAppendOperator
import uniproc.internal.operators.fileio.FileReadOperator
import uniproc.internal.operators.fileio.FileWriteOperator
import uniproc.internal.operators.logic.*
import uniproc.internal.operators.math.*
import uniproc.internal.operators.text.*
import uniproc.types.Structure
import java.io.File
import java.nio.charset.Charset
import java.util.*
import kotlin.collections.ArrayDeque
import kotlin.collections.HashMap

val OPERATIONS = listOf<Operator>(
        // core operations
        ExitOperator(), DocOperation(), RaiseOperator(), PrintOperator(), InputOperator(), AssignmentOperator(),
        ClearOperator(), HistoryOperator(), BlankOperator(), ArgOperator(), LoadOperator(),
        // math operations
        MathPlusOperator(), MathSubOperator(), MathMulOperator(), MathDivOperator(), MathSqrtOperator(),
        MathPowOperator(), MathLogOperator(), MathSinOperator(), MathCosOperator(), MathTanOperator(),
        MathArcSinOperator(), MathArcCosOperator(), MathArcTanOperator(),
        // file operations
        FileReadOperator(), FileWriteOperator(), FileAppendOperator(),
        // procedure operations
        EndProcedureOperator(), ProcedureOperator(), ExecuteOperator(),
        // logic control operations
        IfOperator(), EqualsOperator(), GreaterThanEqOperator(), GreaterThanOperator(), LessThanEqOperator(),
        LessThanOperator(), NotEqualsOperator(),
        // text manipulation operations
        ExplodeOperator(), AtIndexOperator(), LengthOperator(), ConcatOperator(), StringEqualsOperator(),
        StringNotEqualsOperator(), StringEqualsCaseInsensitiveOperator(), SplitOperator()
)

private const val LAST_SET_CHAR = "~"
private const val LAST_SET_VAR = "@$LAST_SET_CHAR"

class Vm(val verbose: Boolean = false) {

    val valueSnapShotThreshold = 2
    var exit = false

    private val history = LinkedList<String>()

    // name of the var we're going to assign the next input buffer to
    private var inputBufferClaim = ""
    // value buffer for non-left-side operators (+, -, *, /, etc)
    private var valueBuffer:Token = NULL
    private val variables = HashMap<String, Structure>()
    private val setterStack = ArrayDeque<Structure>()
    // routine support
    private val routines = HashMap<String, List<List<Token>>>()
    private var currentRoutineName = ""
    private val routineBuffer = LinkedList<List<Token>>()

    // needed for compilation
    // essentially turns operators into recording code
    var className: String = ""
    var mainMethod: StringBuffer? = null
    // For any procedures that cannot be unwrapped (FUTURE WORK TODO)
    var currentExecutionContext = ""
    var supportMethods: HashMap<String, StringBuffer> = HashMap()
    var supportMetaData: StringBuffer = StringBuffer()

    fun hasRoutine(name: String): Boolean {
        return routines.containsKey(name)
    }

    fun addRoutine(name: String, tokens: List<List<Token>>) {
        routines[name] = tokens
    }

    fun setRoutineName(name: String): List<Token> {
        if (currentRoutineName.isNotEmpty()) {
            return listOf(Token(ERROR_TOKEN, "Currently creating routine $currentRoutineName!"))
        } else {
            currentRoutineName = name
            return emptyList()
        }
    }

    fun getRoutine(name: String): List<List<Token>> {
        return routines[name]?: listOf(listOf(Token(ERROR_TOKEN, "No procedure $name found!")))
    }

    fun pushRoutineBuffer(tokens: List<Token>) {
        routineBuffer.addLast(tokens)
    }

    fun closeRoutine() {
        val routineTokens = ArrayList<List<Token>>()
        routineTokens.addAll(routineBuffer)
        addRoutine(currentRoutineName, routineTokens)
        routineBuffer.clear()
        currentRoutineName = ""
    }

    fun hasVar(name: String, type: String): Boolean {
        var nameRef = name.trim()
        if (!nameRef.startsWith("@")) {
            nameRef = "@$nameRef"
        }
        if (variables.containsKey(nameRef)) {
            if (variables[nameRef]!!.type==type) {
                return true
            }
        }
        return false
    }

    fun setArg(index: Int, value: String) {
        variables["%$index"] = Structure("String", value)
    }

    fun getArg(name: String, type: String="String"): Structure {
        // args are uniquely different from variables
        // args are incoming to an executing script
        // and are accessed via %1, %2, etc
        var nameRef = name.trim()
        if (!nameRef.startsWith("%")) {
            nameRef = "%$nameRef"
        }
        return variables[nameRef]?:Structure("String", "")
    }

    fun delVar(name: String) {
        var nameRef = name.trim()
        if (!nameRef.startsWith("@")) {
            nameRef = "@$nameRef"
        }
        if (variables.containsKey(nameRef)) {
            for (i in valueSnapShotThreshold downTo 1) {
                if (verbose) {
                    println("[DEBUG] Clearing ${nameRef}.previous$i")
                }
                variables.remove("${nameRef}.previous$i")
            }
        }
        if (verbose) {
            println("[DEBUG] Clearing $nameRef")
        }
        variables.remove(nameRef)
    }

    fun setVar(name: String, value: Structure) {
        var nameRef = name.trim()
        if (!nameRef.startsWith("@")) {
            nameRef = "@$nameRef"
        }
        // we need to push the old values backwards
        if (variables.containsKey(nameRef)) {
            for (i in valueSnapShotThreshold downTo 1) {
                val currentName = if ((i-1) == 0) "$nameRef" else "${nameRef}.previous${i-1}"
                if (variables[currentName] != null) {
                    if (verbose) {
                        println("[DEBUG] Setting ${nameRef}.previous$i = $currentName (${variables[currentName]})")
                    }
                    variables["${nameRef}.previous$i"] = variables[currentName]!!
                }
            }
        }
        if (verbose) {
            println("[DEBUG] Setting $nameRef = ${value.value}")
        }
        variables[nameRef] = value
        // push value into the front of the convenience value stack
        // not really a var but eh
        setterStack.addFirst(value)
    }

    fun getVar(name: String): Structure {
        var nameRef = name.trim()
        if (!nameRef.startsWith("@")) {
            nameRef = "@$nameRef"
        }
        if (nameRef == LAST_SET_VAR) {
            // pop the list
            return setterStack.removeFirst()
        }
        return variables[nameRef] ?: Structure(ERROR_TYPE, nameRef)
    }

    fun handleTokens(tokens: List<List<Token>>, debug: Boolean=true): Boolean {
        tokens.forEach {
            var unhandledTokens = it
            while (unhandledTokens.isNotEmpty() && !exit) {
                val currentToken = unhandledTokens[0]
                if (currentToken.type == OPERATION_TOKEN) {
                    if (verbose) {
                        println("[DEBUG] running token \"${currentToken.value}\"")
                    }
                    // TODO I don't like this hard code here
                    if (currentRoutineName.isNotEmpty() && currentToken.value.uppercase(Locale.getDefault()).trim() != "END") {
                        pushRoutineBuffer(unhandledTokens)
                        unhandledTokens = emptyList()
                        continue
                    }
                    OPERATIONS.forEach operatorCheck@{ operation ->
                        if (operation.name == currentToken.value.uppercase(Locale.getDefault()).trim()) {
                            if (verbose) {
                                println("[DEBUG] Matched operation ${operation.name}")
                            }
                            unhandledTokens = operation.operation(this, unhandledTokens.drop(1), verbose)
                            return@operatorCheck
                        }
                    }
                } else if (currentToken.type == ERROR_TOKEN) {
                    display("ERROR! ${currentToken.value}")
                    exit = true
                    return false
                } else if (currentToken.type == EXIT_TOKEN) {
                    exit = true
                    unhandledTokens = emptyList()
                    if (verbose) {
                        println("[DEBUG] exiting due to exit token")
                    }
                    return true
                } else {
                    // we shouldn't have a line like this?
                    if (verbose) {
                        println("[DEBUG] Got value token ${currentToken.type}->${currentToken.value}")
                    }
                    setValueBuffer(currentToken)
                    unhandledTokens = unhandledTokens.drop(1)
                }
            }
        }
        // sanity checks
        if (verbose && inputBufferClaim.isNotEmpty()) {
            println("WARNING! Input Buffer was unclaimed. This suggests bad assignment!")
            inputBufferClaim = ""
        }
        if (verbose && valueBuffer != NULL) {
            println("WARNING! Value Buffer was unclaimed! This suggests a missing operation!")
            valueBuffer = NULL
        }
        return true
    }

    fun display(displayString: String) {
        println(displayString)
    }

    fun getInput(): String {
        return readlnOrNull() ?: ""
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

    fun populateInputBufferForCompile(input: String, type: String): Boolean {
        val res = inputBufferClaim.isEmpty()
        inputBufferClaim = ""
        return res

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

    fun compileTokens(tokens: List<Token>): Boolean {
        if (mainMethod == null) {
            mainMethod = StringBuffer()
        }
        var unhandledTokens = tokens
        while (unhandledTokens.isNotEmpty()) {
            val currentToken = unhandledTokens[0]
            when (currentToken.type) {
                OPERATION_TOKEN -> {
                    if (verbose) {
                        println("[DEBUG] compiling token \"${currentToken.value}\"")
                    }
                    OPERATIONS.forEach operatorCheck@{ operation ->
                        if (operation.name == currentToken.value.uppercase(Locale.getDefault()).trim()) {
                            if (verbose) {
                                println("[DEBUG] Matched operation ${operation.name}")
                            }
                            if (operation is JavaSupport) {
                                unhandledTokens = (operation as JavaSupport).compileJava(this, unhandledTokens.drop(1), verbose)
                            } else {
                                System.err.println("Target Token does not support Java compilation. This may be addressed by additional targets in the future")
                            }
                            return@operatorCheck
                        }
                    }
                }
                ERROR_TOKEN -> {
                    display("ERROR! ${currentToken.value}")
                    return false
                }
                else -> {
                    // we shouldn't have a line like this?
                    if (verbose) {
                        println("[DEBUG] Got value token ${currentToken.type}->${currentToken.value}")
                    }
                    setValueBuffer(currentToken)
                    unhandledTokens = unhandledTokens.drop(1)
                }
            }
        }
        return true
    }

    fun getMainExecution(): StringBuffer {
        return if (currentExecutionContext.isEmpty()) {
            mainMethod!!
        } else {
            supportMethods.getOrDefault(currentExecutionContext, StringBuffer())
        }
    }
}