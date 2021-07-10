package de.thorbenkuck.mockk.probe

import org.assertj.core.api.Assertions.assertThat
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit
import kotlin.test.assertNotNull

class MethodProbe<T : Any?>(
    internal val methodResultFuture: CompletableFuture<T> = CompletableFuture(),
    internal val argumentsResultFuture: CompletableFuture<List<Any?>> = CompletableFuture()
) {

    private val synchronizer = AsyncMethodProbeSynchronizer(methodResultFuture, argumentsResultFuture)

    fun expectingTheExecutionTook(time: Long, timeUnit: TimeUnit) {
        getResult()
        val executionStart = assertNotNull(synchronizer.executionStart, "No execution start set")
        val executionEnd = assertNotNull(synchronizer.executionEnd, "No execution end set")
        val result = executionEnd - executionStart

        assertThat(timeUnit.toMillis(time))
            .withFailMessage("Expected the execution to be finished in $timeUnit $time, but it took $result ${TimeUnit.MILLISECONDS}")
            .isLessThanOrEqualTo(result)
    }

    fun waitTillCalled(
        timeoutInSeconds: Long = 10,
        errorMessage: String = "Method has not been called within $timeoutInSeconds seconds"
    ) {
        synchronizer.getArguments(timeoutInSeconds, errorMessage)
    }

    fun alreadyExecuted(): Boolean {
        return synchronizer.receivedMethodResult()
    }

    fun <S : Any?> getArgument(
        index: Int,
        timeoutInSeconds: Long = 10,
        errorMessage: String = "Method has not been called within $timeoutInSeconds seconds"
    ): Any? {
        val args = synchronizer.getArguments(timeoutInSeconds, errorMessage)
        assertThat(args.size).withFailMessage("No argument at index $index (max:${args.size})")
            .isGreaterThan(index)

        return args[index]
    }

    fun getResultNow(errorMessage: String = "Method has not been called") = getResult(0, errorMessage)

    fun getResult(
        timeoutInSeconds: Long = 10,
        errorMessage: String = "Method has not been called within $timeoutInSeconds seconds",
        failOnException: Boolean = true
    ): T {
        return synchronizer.getMethodResult(timeoutInSeconds, errorMessage, failOnException)
    }

}
