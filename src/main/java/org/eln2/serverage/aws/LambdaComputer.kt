package org.eln2.serverage.aws

import org.eln2.serverage.ServerAge.LOGGER
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.core.SdkBytes
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.lambda.LambdaClient
import software.amazon.awssdk.services.lambda.model.*
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import java.security.MessageDigest
import java.util.*

class LambdaComputer(private val userParams: UserParameters) {
    fun createLambda(sourceCode: SourceCode): String? {
        if (userParams.bucket != sourceCode.bucket) {
            LOGGER.info("User is not permitted to access bucket for source code!")
            return null
        }
        return userParams.lambdaClient.createFunction(
            CreateFunctionRequest.builder()
                .architectures(Architecture.X86_64)
                .code(FunctionCode.builder().s3Bucket(userParams.bucket).s3Key(sourceCode.key).build())
                .description("Automated Lambda from Server Age")
                .functionName("FunctionName")
                .memorySize(128)
                .role("")
                .timeout(10)
                .handler("")
                .runtime(Runtime.PYTHON3_9)
                .build()
        ).functionArn()
    }

    fun runLambda(arn: String, event: String): LambdaOutput {
        val resp = userParams.lambdaClient.invoke(
            InvokeRequest.builder()
                .functionName(arn)
                .payload(SdkBytes.fromUtf8String(event))
                .build()
        )
        return LambdaOutput(
            resp.statusCode(),
            resp.executedVersion(),
            resp.functionError(),
            resp.logResult(),
            resp.payload().asUtf8String()
        )
    }

    fun uploadSourceCode(code: String, language: Runtime): SourceCode {
        val sourceCode = SourceCode(userParams.bucket, code, code.sha256(), language)
        userParams.s3client.putObject(
            PutObjectRequest.builder()
                .bucket(userParams.bucket)
                .key(sourceCode.key)
                .build(),
            RequestBody.fromString(sourceCode.code)
        )
        return sourceCode
    }

    fun deleteSourceCode(codeLocation: SourceCode) {
        if (userParams.bucket != codeLocation.bucket) {
            LOGGER.info("User is not permitted to access bucket for source code!")
            return
        }
        userParams.s3client.deleteObject(
            DeleteObjectRequest.builder()
                .bucket(codeLocation.bucket)
                .key(codeLocation.key)
                .build()
        )
    }

    private fun String.sha256(): String {
        val bytes = this.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.fold("") { str, it -> str + "%02x".format(it) }
    }
}



data class SourceCode(val bucket: String, val code: String, val key: String, val language: Runtime)

data class LambdaOutput(
    val statusCode: Int,
    val executedVersion: String,
    val functionError: String,
    val logResult: String,
    val payload: String
    )

val SERVER_REGION: String = run {
    val region = System.getenv("SERVER_AWS_REGION")
    if (region.isNullOrBlank()) {"us-east-1"} else {region}
}

data class UserParameters(
    val minecraftPlayerUUID: UUID,
    val account: String,
    val bucket: String,
    val accessKeyId: String,
    val secretKey: String)
{
    val s3client: S3Client = S3Client.builder()
        .region(Region.of(SERVER_REGION))
        .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKeyId, secretKey)))
        .build()

    val lambdaClient: LambdaClient = LambdaClient.builder()
        .region(Region.of(SERVER_REGION))
        .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKeyId, secretKey)))
        .build()
}

fun main() {
    val pythonCode = """
    import json
    lambda_handler(event, context):
      print("Hello from Lambda")
      print(json.dumps(event))
""".trimIndent()

    val accessKey = ""
    val secretKey = ""

    val up = UserParameters(account = "", bucket="jrddunbr-test-mc-bucket", accessKeyId = accessKey, secretKey = secretKey, minecraftPlayerUUID = UUID.randomUUID())
    val lc = LambdaComputer(up)

    val source = lc.uploadSourceCode(pythonCode, Runtime.PYTHON3_9)
    val lambda = lc.createLambda(source)
    if (lambda != null) {
        val resp = lc.runLambda(lambda, "")
        LOGGER.info(resp)
    }
    lc.deleteSourceCode(source)
}
