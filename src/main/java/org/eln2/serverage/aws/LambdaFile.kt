package org.eln2.serverage.aws

data class LambdaFile(val filename: String, val fileContents: ByteArray) {

    /**
     * This function automatically generated by IDEA
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as LambdaFile

        if (filename != other.filename) return false
        if (!fileContents.contentEquals(other.fileContents)) return false

        return true
    }

    /**
     * This function automatically generated by IDEA
     */
    override fun hashCode(): Int {
        var result = filename.hashCode()
        result = 31 * result + fileContents.contentHashCode()
        return result
    }
}