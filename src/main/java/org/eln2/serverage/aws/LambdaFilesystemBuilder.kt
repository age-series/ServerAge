package org.eln2.serverage.aws

import java.io.ByteArrayOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

// Based on https://stackoverflow.com/a/23613145
fun buildFilesystem(files: List<LambdaFile>): ByteArray {
    val baos = ByteArrayOutputStream()
    val zos = ZipOutputStream(baos)

    files.forEach {
        val entry = ZipEntry(it.filename)
        zos.putNextEntry(entry)
        zos.write(it.fileContents)
    }

    zos.closeEntry()
    zos.close()
    baos.flush()

    return baos.toByteArray()
}
