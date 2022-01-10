package org.eln2.serverage.shell

import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File

/*

ls
mv (move?)
edit - opens an editor
rm
du
run (starts the lambda)

Provided a filesystem... for now I guess just use File?

 */

open class Token
class Word(val word: String): Token() {
    override fun toString(): String = "Word(\"$word\")"
}

open class TokenizerException: RuntimeException()
class UnbalancedQuote: TokenizerException()
class Exhausted: TokenizerException()

class Tokenizer(private val input: Iterator<Char>): Iterator<Token> {
    companion object {
        fun isSeparator(c: Char) = c.isWhitespace()

        @JvmStatic
        fun main(args: Array<String>) {
            while(true) {
                print("> ")
                val line = readLine() ?: return
                try {
                    for(tok in Tokenizer(line.iterator())) {
                        println("Token: $tok")
                    }
                    println("End tokens.")
                } catch(_: UnbalancedQuote) {
                    println("UnbalancedQuote")
                } catch(_: Exhausted) {
                    println("Exhausted")
                }
            }
        }
    }

    private var buffer = ArrayList<Char>()
    private var nextToken: Token? = null

    private fun advance() {
        var c: Char? = null
        nextToken = null
        while(input.hasNext()) {
            c = input.next()
            if(!isSeparator(c)) break
        }
        if(c == null) return
        // Trampoline for multiple adjacent quoted strings, essentially
        while(true) {
            when (c) {
                '"', '\'', '“', '”', '‘', '’' -> {
                    val eq = c
                    var endQuote = false
                    while (input.hasNext()) {
                        c = input.next()
                        if (c == eq) {
                            endQuote = true
                            break
                        }
                        buffer.add(c)
                    }
                    if(!endQuote) throw UnbalancedQuote()
                    c = null
                    // Fall through to next iteration to find a separator
                }
                else -> {
                    // c can be null coming from a non-first iteration
                    if(c != null) buffer.add(c)
                    var seenQuote = false
                    while (input.hasNext()) {
                        c = input.next()
                        if (isSeparator(c)) break
                        if (c == '"' || c == '\'' || c == '“', c == '”', c == '‘', c == '’') {
                            seenQuote = true
                            break
                        }
                        buffer.add(c)
                    }
                    if(seenQuote) continue
                    nextToken = Word(buffer.joinToString(""))
                    buffer.clear()
                    return
                }
            }
        }
    }

    init { advance() }

    override fun hasNext(): Boolean = nextToken != null

    override fun next(): Token {
        val tok = nextToken ?: throw Exhausted()
        advance()
        return tok
    }
}


enum class ProgramState {
    SHELL,
    EDIT
}

enum class Commands {
    LS, MV, EDIT, DU, RUN, HELP
}

class Shell(inStream: BufferedInputStream, outStream: BufferedOutputStream, val path: File) {
    private val bufferedReader = inStream.bufferedReader(Charsets.UTF_8)
    val bufferedWriter = outStream.bufferedWriter(Charsets.UTF_8)
    var state = ProgramState.SHELL

    fun eval() {
        when (state) {
            ProgramState.SHELL -> {
                val bufferLine = bufferedReader.readLine().split(" ")
                if (bufferLine.isNotEmpty()) {
                    val cmd = bufferLine[0]
                    if (cmd in Commands.values().map {it.name}) {
                        when (Commands.valueOf(cmd)) {
                            Commands.LS -> {
                                val foldersAndFiles = path.listFiles()?.filterNotNull()?: listOf()
                                foldersAndFiles.forEach {
                                    val typeCode = if (it.isFile) { "f" } else if (it.isDirectory) {"d"} else {""}
                                    bufferedWriter.write("$typeCode\t${it.absolutePath}")
                                }
                            }
                            Commands.MV -> {
                                bufferedWriter.write("Sorry, mv is not implemented :/")
                            }
                            Commands.EDIT -> {
                                bufferedWriter.write("Sorry, edit is not implemented :/")
                            }
                            Commands.DU -> {
                                bufferedWriter.write("Sorry, du is not implemented :/")
                            }
                            Commands.RUN -> {
                                bufferedWriter.write("Sorry, run is not implemented :/")
                            }
                            Commands.HELP -> {
                                bufferedWriter.write("Sorry, help is not implemented :/")
                            }
                        }
                    } else {
                        bufferedWriter.write("Unknown command $cmd.")
                        return
                    }
                }
            }
            ProgramState.EDIT -> {
                bufferedWriter.write("Sorry, EDIT is not implemented.")
                state = ProgramState.EDIT
            }
        }
    }
}

fun main() {
    Shell(System.`in`.buffered(), System.out.buffered(), File("run"))
}
