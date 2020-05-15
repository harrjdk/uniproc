package uniproc

import uniproc.internal.Token

// As functionality is added, we should add more reserved words
// These are more or less going to be translated into macros
// and idioms in the target language
val RESERVED_WORDS = listOf(
        "PRINT", "READ", "INPUT", "WRITE", "APPEND",
        "+", "-", "/", "*", "ERROR", "ASSIGN"
)
val OPERATION_TOKEN = "OPERATOR"
val VALUE_TOKEN = "VALUE"
val ERROR_TOKEN = "ERROR"

open class Parser(val verbose: Boolean = true, vm: Vm = Vm(verbose = verbose)) {
    private val myVm = vm

    fun parseLine(line: String): List<Token> {
        // parsing uniproc is easy, we read left to right and pick up reserved words
        // since this is made to every day users, concepts of quoted strings and such
        // are meaningless
        // Begin reading until we hit a space
        val tokens = ArrayList<Token>()
        var buffer = ""
        var currentChar = ' '
        var counter = 0
        while (counter < line.length) {
            currentChar = line[counter]
            buffer+=currentChar
            if (currentChar == ' ' || counter == line.length-1) {
                // create a token from the item, this is not precise at this point.
                tokens.add(Token(
                        if (RESERVED_WORDS.contains(buffer.toUpperCase().trim())) OPERATION_TOKEN else VALUE_TOKEN,
                        buffer
                ))
                buffer = ""
            }
            counter+=1
        }
        return tokens
    }

    fun executeTokens(tokens: List<Token>) {
        myVm.handleTokens(tokens)
    }
}