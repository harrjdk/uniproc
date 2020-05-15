package uniproc

import uniproc.internal.Token
import uniproc.internal.operators.AssignmentOperator
import uniproc.internal.operators.InputOperator
import uniproc.internal.operators.Operator
import uniproc.internal.operators.PrintOperator
import uniproc.types.Routine
import uniproc.types.Structure

val OPERATIONS = listOf<Operator>(
        PrintOperator(),
        InputOperator(),
        AssignmentOperator()
)

class Vm(val verbose: Boolean = false) {

    private var valueSnapShotThreshold = 2

    // name of the var we're going to assign the next input buffer to
    private var inputBufferClaim = ""
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
        return variables[name] ?: Structure("Error", name)
    }

    fun handleTokens(tokens: List<Token>, debug: Boolean=true) {
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
                        unhandledTokens = operation.operation(this, unhandledTokens.drop(1))
                        return@operatorCheck
                    }
                }
            }
        }
    }

    fun display(displayString: String) {
        println(displayString)
    }

    fun getInput(): String {
        return readLine()?: ""
    }

    fun populateInputBuffer(input: String) {
        if (inputBufferClaim.isEmpty()) {
            return
        } else {
            setVar(inputBufferClaim.trim(), Structure("STRING", input))
        }
    }

    fun claimInputBuffer(name: String) {
        inputBufferClaim = name
    }


}