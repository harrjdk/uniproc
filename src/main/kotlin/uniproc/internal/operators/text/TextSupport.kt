package uniproc.internal.operators.text

fun parseCsvRow(row: String): List<String> {
    val result = ArrayList<String>()
    // assume strict csv with quotes
    var inElement = false
    var escapeQuote = false
    var buffer = ""
    row.toCharArray().forEach {
        char ->
        if (char == '\'') {
            if (inElement && !escapeQuote) {
                // we need to close the element
                result.add(buffer)
                buffer = ""
                inElement = false
            } else if (inElement && escapeQuote) {
                buffer+=char
            } else {
                inElement = true
            }
        } else if (char == '\\') {
            escapeQuote = true
        } else if (!inElement){
            // no-op
        } else {
            buffer+=char
        }
    }
    if (inElement || escapeQuote) {
        return emptyList()
    }
    return result
}