package org.eln2.serverage.aws

import java.io.ByteArrayOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

// https://stackoverflow.com/a/23613145
fun createZip(fileContents: String, fileName: String): ByteArray {
    val baos = ByteArrayOutputStream()
    val zos = ZipOutputStream(baos)

    val entry = ZipEntry(fileName)
    zos.putNextEntry(entry)
    zos.write(fileContents.toByteArray())
    zos.closeEntry()
    zos.close()
    baos.flush()

    return baos.toByteArray()
}
