package org.eln2.serverage.aws

import org.eln2.serverage.LOGGER
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.core.SdkBytes
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.lambda.LambdaClient
import software.amazon.awssdk.services.lambda.model.*
import java.security.MessageDigest
import java.util.*
import kotlin.jvm.Throws

class LambdaComputer(private val userParams: UserParameters) {

    private var lambdaArn: String = ""

    /**
     * getFunctionArn - gets the ARN of the underlying AWS Lambda function
     *
     * @return Lambda ARN or empty string if none is known
     */
    fun getFunctionArn(): String = lambdaArn?: ""

    /**
     * isRunnable - determines if Lambda is ready to run the lambda
     *
     * @return True if the lambda can be run
     */
    fun isRunnable(): Boolean {
        val hasValidArn = lambdaArn.isNotBlank()
        // only actually query for the function if we think we have an ARN.
        if (!hasValidArn) return false
        val functionState = userParams.lambdaClient.getFunction(
            GetFunctionRequest.builder().functionName(lambdaArn).build()
        ).configuration().state()
        LOGGER.debug("$lambdaArn: $functionState")
        return functionState == State.ACTIVE
    }

    /**
     * getLambdaState - gets the current state of the lambda
     *
     * @return One of Pending, Inactive, Active, or Failed - only Active lambdas can be run.
     */
    fun getLambdaState(): String {
        val hasValidArn = lambdaArn.isNotBlank()
        // only actually query for the function if we think we have an ARN.
        if (!hasValidArn) return ""
        val functionState = userParams.lambdaClient.getFunction(
            GetFunctionRequest.builder().functionName(lambdaArn).build()
        ).configuration().state()
        return functionState.name
    }

    /**
     * createLambda - Creates an AWS Lambda
     *
     * Note: Lambda takes ~1 second to actually prepare the lambda for run, so immediately calling invoke probably fails
     *
     * @param code A string containing the code you want to put in the lambda
     * @param language The runtime to use (Python suggested based on current impl)
     */
    fun createLambda(code: String, language: Runtime) {

        // TODO: Consider making these longer, or is 12 characters of the sum good enough?
        val functionName = "ServerAge_${code.sha256().substring(0 until 12)}"

        val matchingFunctionNames = userParams.lambdaClient.listFunctions().functions()

        if (!matchingFunctionNames.none { it.functionName() == functionName }) {
            LOGGER.info("Already have function called $functionName in account, must already have been uploaded")
            lambdaArn = matchingFunctionNames.first { it.functionName() == functionName }.functionArn()
            return
        }

        lambdaArn = userParams.lambdaClient.createFunction(
            CreateFunctionRequest.builder()
                .architectures(Architecture.X86_64)
                .code(FunctionCode.builder()
                    // In the future, this could support having more than just one source file in it.
                    .zipFile(SdkBytes.fromByteArray(createZip(code, "main.py")))
                    .build())
                .description("Automated Lambda from Server Age")
                .functionName(functionName)
                .memorySize(128)
                .role("arn:aws:iam::464053648600:role/server_age_lambda_role")
                .timeout(10)
                .handler("main.lambda_handler")
                .runtime(language)
                .build()
        ).functionArn()
    }

    /**
     * deleteLambda - Deletes the lambda
     */
    fun deleteLambda() {
        userParams.lambdaClient.deleteFunction(
            DeleteFunctionRequest.builder().functionName(lambdaArn).build()
        )
    }

    /**
     * runLambda
     *
     * @param event A string with event data that is passed to the lambda (typically json)
     * @return LambdaOutput - some fields may be blank
     * @throws ResourceConflictException in some cases
     */
    @Throws(ResourceConflictException::class)
    fun runLambda(event: String): LambdaOutput {
        val resp = userParams.lambdaClient.invoke(
            InvokeRequest.builder()
                .functionName(lambdaArn)
                .payload(SdkBytes.fromUtf8String(event))
                .build()
        )
        return LambdaOutput(
            resp.statusCode(),
            resp.executedVersion()?: "",
            resp.functionError()?: "",
            resp.logResult()?: "",
            resp.payload().asUtf8String()?: ""
        )
    }

    /**
     * Simple function used to create the first part of the function names, so they are less likely to collide.
     */
    private fun String.sha256(): String {
        val bytes = this.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.fold("") { str, it -> str + "%02x".format(it) }
    }
}

/**
 * Lambda Output - output from an AWS Lambda Invoke
 *
 * @param statusCode HTTP response code (typically 200)
 * @param executedVersion typically $LATEST
 * @param functionError If present, indicates an error and provides detail
 * @param logResult Last 4KB of execution log
 * @param payload The output from the function, if any.
 */
data class LambdaOutput(
    val statusCode: Int,
    val executedVersion: String,
    val functionError: String,
    val logResult: String,
    val payload: String
    )

/**
 * SERVER_REGION - sets the server's region. You can override this by setting SERVER_AWS_REGION as an env variable.
 *
 * By default, we use us-east-1
 */
val SERVER_REGION: String = run {
    val region = System.getenv("SERVER_AWS_REGION")
    if (region.isNullOrBlank()) {"us-east-1"} else {region}
}

/**
 * UserParameters - Stores data about a particular Minecraft user's account.
 *
 * @param minecraftPlayerUUID The UUID of the player
 * @param accessKeyId the AWS Access Key ID
 * @param secretKey the AWS Secret Key
 */
data class UserParameters(
    val minecraftPlayerUUID: UUID,
    val accessKeyId: String,
    val secretKey: String)
{

    /**
     * lambdaClient - the Lambda client that we can use for communicating with Lambda
     */
    val lambdaClient: LambdaClient = LambdaClient.builder()
        .region(Region.of(SERVER_REGION))
        .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKeyId, secretKey)))
        .build()
}

// Test function
fun main() {
    val pythonCode = """
    import json
    def lambda_handler(event, context):
      print("Hello from Lambda")
      print(json.dumps(event))
""".trimIndent()

    val accessKey = System.getenv("access_key")
    val secretKey = System.getenv("secret_key")

    val up = UserParameters(minecraftPlayerUUID = UUID.randomUUID(), accessKeyId = accessKey, secretKey = secretKey)
    val lc = LambdaComputer(up)

    lc.createLambda(pythonCode, Runtime.PYTHON3_9)

    var tries = 1
    var done = false
    while (tries <= 2 && !done) {
        if (lc.isRunnable()) {
            try {
                LOGGER.info(lc.isRunnable())
                val resp = lc.runLambda("")
                LOGGER.info(resp)
                done = true
            } catch (e: ResourceConflictException) {
                println("Could not run lambda: $e")
            }
        } else {
            LOGGER.warn("Lambda ${lc.getFunctionArn()} is not runnable")
        }
        tries += 1
        if (!done)
            Thread.sleep(1000)
    }





    lc.deleteLambda()
}

/*

Here's a sample IAM policy that I've been using. These could be cleaned up a bit more but generally work.
It shouldn't have excessive permissions (although I could be wrong).

In all policies below, I've replaced my AWS account number with an `x`.

CreateFunction - used to create the function
InvokeFunction - used to run the function
GetFunction - used to get list of functions (to prevent re-uploading)
DeleteFunction - used to delete a function
PassRole - permission required by CreateFunction to create the function

{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Sid": "VisualEditor0",
            "Effect": "Allow",
            "Action": [
                "lambda:CreateFunction",
                "iam:PassRole",
                "lambda:InvokeFunction",
                "lambda:GetFunction",
                "lambda:DeleteFunction"
            ],
            "Resource": [
                "arn:aws:iam::x:role/server_age_lambda_role",
                "arn:aws:lambda:*:x:function:ServerAge_*"
            ]
        },
        {
            "Sid": "VisualEditor1",
            "Effect": "Allow",
            "Action": "lambda:ListFunctions",
            "Resource": "*"
        }
    ]
}

Here's a sample IAM Role for the lambda role:
Ideally, it allows the lambda to log to CloudWatch

{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Effect": "Allow",
            "Action": "logs:CreateLogGroup",
            "Resource": "arn:aws:logs:us-east-1:x:*"
        },
        {
            "Effect": "Allow",
            "Action": [
                "logs:CreateLogStream",
                "logs:PutLogEvents"
            ],
            "Resource": [
                "arn:aws:logs:us-east-1:x:log-group:/aws/lambda/FunctionName:*"
            ]
        }
    ]
}

And here's the trust relationship for that role

{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Principal": {
        "Service": "lambda.amazonaws.com"
      },
      "Action": "sts:AssumeRole"
    }
  ]
}

 */
