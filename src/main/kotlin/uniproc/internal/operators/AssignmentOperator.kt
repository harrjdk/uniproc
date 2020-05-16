package uniproc.internal.operators

import uniproc.ERROR_TOKEN
import uniproc.NULL
import uniproc.OPERATION_TOKEN
import uniproc.Vm
import uniproc.internal.Token
import uniproc.types.Structure

fun containsOperation(tokens: List<Token>): Boolean {
    return tokens.map { token -> token.type }.any { type -> type == OPERATION_TOKEN }
}

class AssignmentOperator: Operator("ASSIGN"), JavaSupport {
    override fun operation(vm: Vm, tokens: List<Token>, verbose: Boolean): List<Token> {
        // This one is more complex because you could assign from an
        // expression such as ASSIGN @NAME INPUT WHAT'S YOUR NAME?
        // so we need to get the name we're claiming and see if the next token
        // is an operation
        if (verbose) {
            println("[DEBUG] received ASSIGN operator with ${tokens.size} tokens")
        }
        if (tokens.size < 2) {
            // something is wrong. throw an error token on the chain
            return listOf(Token(ERROR_TOKEN, "ASSIGN missing value"))
        }
        val nextToken = tokens[0]
        if (!isVariable(nextToken.value)) {
            // another error.
            return listOf(Token(ERROR_TOKEN, "ASSIGN must receive a reference"))
        }
        // if any future tokens are operators, we need to check
        val possibleReturn = tokens.drop(1)
        return if (containsOperation(possibleReturn)) {
            // we need to reserve the input buffer
            vm.claimInputBuffer(nextToken.value.trim())
            possibleReturn
        } else {
            val chainStr = tokens.drop(1).joinToString("") { token ->
                token.value
            }
            if (isVariable(chainStr.trim())) {
                vm.setVar(nextToken.value.trim(), Structure("String", vm.getVar(chainStr.trim()).value))
            } else {
                vm.setVar(nextToken.value.trim(), Structure("String", tokens.drop(1).joinToString("") { token ->
                    token.value
                }))
            }
            emptyList()
        }
    }

    override fun compileJava(vm: Vm, tokens: List<Token>, verbose: Boolean): List<Token> {
        if (tokens.size < 2) {
            // something is wrong. throw an error token on the chain
            return listOf(Token(ERROR_TOKEN, "ASSIGN missing value"))
        }
        val nextToken = tokens[0]
        if (verbose) {
            println("[DEBUG] Got token for var name \"${nextToken.value}\"")
        }
        if (!isVariable(nextToken.value)) {
            // another error.
            return listOf(Token(ERROR_TOKEN, "ASSIGN must receive a reference"))
        }
        // if any future tokens are operators, we need to check
        val possibleReturn = tokens.drop(1)
        return if (containsOperation(possibleReturn)) {
            // we need to reserve the input buffer
            if (verbose) {
                println("[DEBUG] Token after reference name is likely a operation, claiming the input buffer.")
            }
            vm.claimInputBuffer(nextToken.value.trim())
            possibleReturn
        } else {
            // if the var exists, we need to reassign it
            val varName = nextToken.value.trim()
            if (verbose) {
                println("[DEBUG] Checking vm for reference \"$varName\"")
            }
            if (vm.hasVar(varName, "String")) {
                // we need to add to the current method context, reassignment plus back propagation
                if (verbose) {
                    println("[DEBUG] \"$varName\" found, back propagating")
                }
                for (i in vm.valueSnapShotThreshold downTo 1) {
                    val currentName = if ((i-1) == 0) "$varName" else "${varName}.previous${i-1}"
                    val histName = "$varName.previous$i"
                    if (verbose) {
                        println("[DEBUG] Checking for \"$histName\"")
                    }
                    if (!vm.hasVar(histName, "String")) {
                        if (verbose) {
                            println("[DEBUG] \"$histName\" not found, placing static init.")
                        }
                        // we need to add this to the static context
                        vm.supportMetaData.append("static String ${sanitizeVar(histName)}=null;")
                    }
                    if (verbose) {
                        println("[DEBUG] Setting \"$histName\" = \"$varName\" in current execution context")
                    }
                    vm.getMainExecution().append("${sanitizeVar(histName)}=${sanitizeVar(currentName)};")
                }
            } else {
                if (verbose) {
                    println("[DEBUG] Not found. Initializing \"$varName\" in static context")
                }
                vm.setVar(sanitizeVar(varName), Structure("String", ""))
                vm.supportMetaData.append("static String ${sanitizeVar(varName)}=null;")
            }
            if (verbose) {
                println("[DEBUG] Setting \"$varName\" in current execution context")
            }
            vm.getMainExecution().append("${sanitizeVar(varName)}=\"${
            possibleReturn.joinToString("") { token ->
                token.value
            }
            }\";")
            emptyList()
        }
    }
}