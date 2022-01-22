package org.eln2.serverage.aws

import org.eln2.serverage.LOGGER
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.core.SdkBytes
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.lambda.LambdaClient
import software.amazon.awssdk.services.lambda.model.*
import java.util.*

const val SERVER_AGE_PREFIX = "ServerAge_"

/**
 * Lambda - a way to run AWS Lambda's.
 *
 * Security notes:
 * Since the lambda is not run on the server, there is no chance of code execution of the players code on the server.
 * To prevent the mod from doing malicious things in a person's AWS account (such as deleting all of their lambdas), we
 * have prefixed all the Lambda names with "ServerAge_". We present these names without that prefix in the game, but
 * if you actually log into your AWS console, you will see the names in that manner.
 *
 * The mod is intended to allow each user to bring their own AWS account, but it should be noted that a malicious server
 * administrator could take over all the accounts, given that the mod does need to also be able to operate in AWS
 * lambda - thus the limited control that the IAM roles should have. Examples are provided at the bottom of this file,
 * but use them at your own risk!
 *
 * We are also considering a mode where the server administrator can trust their users with their own account and
 * provide blanket AWS Lambda usage to any user, but do note that there's not much preventing users from running each
 * other's lambdas in that situation, since this code doesn't currently protect outside a UserParameters context. This
 * could probably become a configuration option in the future.
 *
 * @param userParams User parameters to get user's context.
 */
@Suppress("MemberVisibilityCanBePrivate", "unused")
class Lambda(private val userParams: UserParameters) {

    /**
     * isRunnable - determines if Lambda is ready to run the lambda
     *
     * @return True if the lambda can be run (is in Active state)
     */
    fun isRunnable(lambdaInstance: LambdaInstance): Boolean {
        return getLambdaState(lambdaInstance) == State.ACTIVE.name
    }

    /**
     * getLambdaState - gets the current state of the lambda
     *
     * @return One of Pending, Inactive, Active, or Failed - only Active lambdas can be run. Blank if error.
     */
    fun getLambdaState(lambdaInstance: LambdaInstance): String {
        val hasValidArn = lambdaInstance.functionArn.isNotBlank()
        // only actually query for the function if we think we have an ARN.
        if (!hasValidArn) return ""
        return try {
            val functionState = userParams.lambdaClient.getFunction(
                GetFunctionRequest.builder().functionName(lambdaInstance.functionArn).build()
            ).configuration().state()
            functionState.name
        } catch (e: Exception) {
            LOGGER.error("Unable to get lambda state in AWS account ${userParams.getAwsAccountNumber()}")
            e.printStackTrace()
            ""
        }
    }

    /**
     * createPythonLambda - Creates a basic Python AWS Lambda
     *
     * Note: Lambda takes ~1 second to actually prepare the lambda for run, so immediately calling invoke probably fails
     *
     * @param lambdaName Name of the lambda (note that "ServerAge_" is prepended to the actual lambda name)
     * @param code A string containing the Python code you want to put in the lambda (function "lambda_handler" is called with event string and Lambda context)
     */
    fun createPythonLambda(lambdaName: String, code: String): LambdaInstance? {
        return createLambda(lambdaName, listOf(LambdaFile("main.py", code.toByteArray())), "main.lambda_handler", Runtime.PYTHON3_9)
    }

    /**
     * createLambda - Creates an AWS Lambda
     *
     * Note: Lambda takes ~1 second to actually prepare the lambda for run, so immediately calling invoke probably fails
     *
     * TODO: Allow users to bring a VPC.
     *
     * @param lambdaName Name of the lambda (note that "ServerAge_" is prepended to the actual lambda name)
     * @param files A list of [LambdaFile]s that build up the filesystem.
     * @param handler Lambda Entry Point (try "main.lambda_handler" for a function "lambda_handler" in main.py)
     * @param language The runtime to use
     * @param memorySize Memory size of Lambda (in MiB) (default: 128MiB)
     * @param timeout How long to wait before timing out (default: 10s)
     */
    fun createLambda(lambdaName: String, files: List<LambdaFile>, handler: String, language: Runtime, memorySize: Int = 128, timeout: Int = 10): LambdaInstance? {
        val realFunctionName = "$SERVER_AGE_PREFIX$lambdaName"

        val allAccountLambdas = userParams.lambdaClient.listFunctions().functions()

        if (!allAccountLambdas.none { it.functionName() == realFunctionName }) {
            LOGGER.info("Already have function called $realFunctionName in AWS account ${userParams.getAwsAccountNumber()}")
            return LambdaInstance(lambdaName, allAccountLambdas.first { it.functionName() == realFunctionName }.functionArn())
        }

        return try {
            val lambdaArn = userParams.lambdaClient.createFunction(
                CreateFunctionRequest.builder()
                    .architectures(Architecture.X86_64)
                    .code(FunctionCode.builder()
                        .zipFile(SdkBytes.fromByteArray(buildFilesystem(files)))
                        .build())
                    .description("Automated Lambda from Server Age")
                    .functionName(realFunctionName)
                    .memorySize(memorySize)
                    .role(userParams.lambdaRoleArn)
                    .timeout(timeout)
                    .handler(handler)
                    .runtime(language)
                    .build()
            ).functionArn()
            LambdaInstance(realFunctionName, lambdaArn)
        } catch (e: Exception) {
            LOGGER.error("Unable to create Lambda $lambdaName in AWS account ${userParams.getAwsAccountNumber()}")
            e.printStackTrace()
            null
        }
    }

    /**
     * deleteLambda - Deletes the lambda
     */
    fun deleteLambda(lambdaInstance: LambdaInstance) {
        try {
            userParams.lambdaClient.deleteFunction(
                DeleteFunctionRequest.builder().functionName(lambdaInstance.functionArn).build()
            )
        } catch (e: Exception) {
            LOGGER.error("Unable to delete Lambda $lambdaInstance in AWS account ${userParams.getAwsAccountNumber()}")
            e.printStackTrace()
        }
    }

    /**
     * runLambda
     *
     * @param event A string with event data that is passed to the lambda (typically json)
     * @return LambdaOutput - some fields may be blank (-1 for status code if exception caught)
     */
    fun runLambda(lambdaInstance: LambdaInstance, event: String): LambdaOutput {
        return try {
            val resp = userParams.lambdaClient.invoke(
                InvokeRequest.builder()
                    .functionName(lambdaInstance.functionArn)
                    .payload(SdkBytes.fromUtf8String(event))
                    .build()
            )
            LambdaOutput(
                resp.statusCode(),
                resp.executedVersion()?: "",
                resp.functionError()?: "",
                resp.logResult()?: "",
                resp.payload().asUtf8String()?: ""
            )
        } catch (e: Exception) {
            LOGGER.error("Unable to run lambda $lambdaInstance in AWS account ${userParams.getAwsAccountNumber()}")
            e.printStackTrace()
            LambdaOutput(-1, "", "", "", "")
        }
    }

    fun listLambdas(): List<LambdaInstance> {
        return try {
            userParams.lambdaClient
                .listFunctions()
                .functions()
                .filter { it.functionName().startsWith(SERVER_AGE_PREFIX) }
                .map { LambdaInstance(it.functionName().substring(SERVER_AGE_PREFIX.length), it.functionArn()) }
        } catch (e: Exception) {
            LOGGER.error("Unable to list Lambdas in AWS account ${userParams.getAwsAccountNumber()}")
            e.printStackTrace()
            listOf()
        }
    }
}

/**
 * LambdaInstance
 *
 * @param functionName Name of the function (not the real name, the user provided name in-game)
 * @param functionArn the function's ARN
 */
data class LambdaInstance(val functionName: String, val functionArn: String)

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
    val secretKey: String,
    val lambdaRoleArn: String
)
{

    /**
     * lambdaClient - the Lambda client that we can use for communicating with Lambda
     */
    val lambdaClient: LambdaClient = LambdaClient.builder()
        .region(Region.of(SERVER_REGION))
        .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKeyId, secretKey)))
        .build()

    fun getAwsAccountNumber(): String {
        return try {
            lambdaRoleArn.split(":")[5]
        } catch (e: Exception) {
            ""
        }
    }
}

// Test function
fun main() {
    val pythonCode = """
import json
def lambda_handler(event, context):
    print("Hello from Lambda")
    print(json.dumps(event))
"""

    val accessKey = System.getenv("access_key")
    val secretKey = System.getenv("secret_key")
    val lambdaRoleArn = System.getenv("lambda_role")

    val up = UserParameters(minecraftPlayerUUID = UUID.randomUUID(), accessKeyId = accessKey, secretKey = secretKey, lambdaRoleArn)
    val lp = Lambda(up)

    val lambda = lp.createPythonLambda("test_lambda", pythonCode) ?: return

    val startTime = System.nanoTime()
    var stopTime: Long = 0
    var tries = 1
    var done = false
    while (tries <= 10 && !done) {
        if (lp.isRunnable(lambda)) {
            stopTime = System.nanoTime()
            try {
                LOGGER.info(lp.isRunnable(lambda))
                val resp = lp.runLambda(lambda, "{\"Hello\": \"world\"}")
                LOGGER.info(resp)
                done = true
            } catch (e: ResourceConflictException) {
                println("Could not run lambda: $e")
            }
        } else {
            LOGGER.warn("Lambda ${lambda.functionName} is not runnable")
        }
        tries += 1
        if (!done)
            Thread.sleep(100)
    }

    val timeMs = (stopTime - startTime) / 1_000_000

    println("Time taken: $timeMs ms")

    val startTime2 = System.nanoTime()
    LOGGER.info(lp.runLambda(lambda, ""))
    val stopTime2 = System.nanoTime()

    val timeMs2 = (stopTime2 - startTime2) / 1_000_000

    println("Time taken to start/complete lambda: $timeMs2 ms")

    lp.deleteLambda(lambda)
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
                "arn:aws:logs:us-east-1:x:log-group:/aws/lambda/*"
            ]
        }
    ]
}
*/ // because the resource has a comment start in it... lol.

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
