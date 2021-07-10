package de.thorbenkuck.mockk.probe

import de.thorbenkuck.mockk.FutureValue
import java.lang.IllegalStateException
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit
import kotlin.test.fail

class AsyncMethodProbeSynchronizer<T : Any?>(
    methodResultFuture: CompletableFuture<T>,
    argumentsResultFuture: CompletableFuture<List<Any?>>,
    executionTimeFuture: CompletableFuture<Long>
) {

    private val methodResult: FutureValue<T> = FutureValue(methodResultFuture)
    private val argumentsResult: FutureValue<List<Any?>> = FutureValue(argumentsResultFuture)
    private val executionTime: FutureValue<Long> = FutureValue(executionTimeFuture)

    fun receivedMethodResult(): Boolean {
        return methodResult.hasData()
    }

    fun isFinished() = methodResult.isSet()

    fun getExecutionTime(timeoutInSeconds: Long, timeoutErrorMessage: String): Long {
        try {
            return executionTime.get(timeoutInSeconds, TimeUnit.SECONDS)
        } catch (timeoutException: Exception) {
            fail(timeoutErrorMessage, timeoutException)
        } catch (e: Exception) {
            throw IllegalStateException("There should not be any exception while getting the execution time, yet here we go... Please report this bug :)", e)
        }
    }

    fun getArguments(timeoutInSeconds: Long, timeoutErrorMessage: String): List<Any?> {
        try {
            return argumentsResult.get(timeoutInSeconds, TimeUnit.SECONDS)
        } catch (timeoutException: Exception) {
            fail(timeoutErrorMessage, timeoutException)
        } catch (e: Exception) {
            throw IllegalStateException("There should not be any exception while getting arguments, yet here we go... Please report this bug :)", e)
        }
    }

    fun getMethodResult(
        timeoutInSeconds: Long,
        errorMessage: String
    ): T {
        try {
            return methodResult.get(timeoutInSeconds, TimeUnit.SECONDS)
        } catch (timeoutException: Exception) {
            fail(errorMessage, timeoutException)
        } catch (e: Exception) {
            fail(errorMessage, e)
        }
    }
}
