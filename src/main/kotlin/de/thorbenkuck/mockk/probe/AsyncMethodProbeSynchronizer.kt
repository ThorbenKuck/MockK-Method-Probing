package de.thorbenkuck.mockk.probe

import org.assertj.core.api.Assertions
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.write

class AsyncMethodProbeSynchronizer<T: Any?>(
    private val methodResultFuture: CompletableFuture<T> = CompletableFuture(),
    private val argumentsResultFuture: CompletableFuture<List<Any?>> = CompletableFuture()
) {

    init {
        methodResultFuture.whenComplete { t, throwable ->
            executionEnd = System.currentTimeMillis()
            methodException = throwable
            setMethodResult(t)
        }
        argumentsResultFuture.whenComplete { arguments, _ ->
            argumentsEnd = System.currentTimeMillis()
            setArguments(arguments)
        }
    }

    var executionEnd: Long? = null
        private set
    var argumentsEnd: Long? = null
        private set
    var executionStart: Long? = null
        private set

    private lateinit var arguments: List<Any?>
    private val argumentsLock = ReentrantReadWriteLock()
    private lateinit var methodResult: ResultHolder<T>
    private var methodException: Throwable? = null
    private val methodsLock = ReentrantReadWriteLock()

    fun receivedMethodResult(): Boolean {
        return ::methodResult.isInitialized
    }

    private fun setArguments(args: List<Any?>) {
        argumentsLock.write {
            if(!this::arguments.isInitialized) {
                this.arguments = args
            }
        }
    }

    private fun setMethodResult(t: T) {
        methodsLock.write {
            if(!this::methodResult.isInitialized) {
                this.methodResult = ResultHolder(t)
            }
        }
    }


    fun getArguments(
        timeoutInSeconds: Long,
        errorMessage: String
    ): List<Any?> {
        if(!::arguments.isInitialized) {
            Assertions.assertThatCode {
                val args = argumentsResultFuture.get(timeoutInSeconds, TimeUnit.SECONDS)
                setArguments(args)
            }.withFailMessage(errorMessage)
                .doesNotThrowAnyException()

        }
        return arguments
    }

    fun getMethodResult(
        timeoutInSeconds: Long,
        errorMessage: String,
        failOnException: Boolean
    ): T {
        executionStart = System.currentTimeMillis()
        if(!::methodResult.isInitialized) {
            Assertions.assertThatCode {
                try {
                    val result = methodResultFuture.get(timeoutInSeconds, TimeUnit.SECONDS)
                    setMethodResult(result)
                } catch (executionException: ExecutionException) {
                    methodException = executionException
                    if(failOnException) {
                        throw executionException
                    }
                }
            }.withFailMessage(errorMessage)
                .doesNotThrowAnyException()

        }
        return methodResult.content
    }

    internal class ResultHolder<T : Any?>(val content: T)
}
