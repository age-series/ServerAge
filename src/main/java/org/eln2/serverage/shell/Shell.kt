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
