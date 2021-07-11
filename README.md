# MockK Method Probing

> Testing asynchronous methods using modern and performant means

[![Build Status](https://travis-ci.org/ThorbenKuck/MockK-Method-Probing.svg?branch=master)](https://travis-ci.org/ThorbenKuck/MockK-Method-Probing)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.thorbenkuck/MockK-Method-Probing/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.thorbenkuck/MockK-Method-Probing)

Mockk provides a lot of functions for mocking and spying, even integration in Spring and stuff. The only thing really missing is a way to test asynchronous code, or probing of methods in general.

This library is a small extension that enables tests to do just that, without massive overhead or busy/active waiting.

## Motivation

This small library enables you to test asynchronous code (or synchronous code for that matter), without the need for busy or active waiting. It instead focuses on letting the test thread sleep until either the max wait time has elapsed, or the probed method was called.

With this library you can test asynchronous code more reliable and with better performance and without higher technical complexity in your tests.

A more detailed motivation as to why one would use this library and where the idea for this library stems from can be found [here](MOTIVATION.md).

## Getting started:

Add the following dependency to your build automation tool:

### Maven:

```xml
<dependency>
    <groupId>com.github.thorbenkuck</groupId>
    <artifactId>mockk-method-probe</artifactId>
    <version>0.1.1</version>
    <scope>test</scope>
</dependency>
```

### Gradle:

```groovy
testImplementation 'com.github.thorbenkuck:mockk-method-probe:0.1.1'
```

# Using the library

> Note: Even though most of the library is working best with spy classes, later down below there is an explanation for using custom mocks

Let us imagine we have the following class, which is called asynchronously in our test:

```kotlin

class ToTest {
    fun testMethod(any: Any?): Any? {
        return any
    }
    
    fun triggerError() {
        throw IllegalStateException()
    }
}
```

## MethodBarriers

Barriers are a way of waiting for methods to being called and finish.
They are simpler, more light weight and faster than probes, though provide a lower total amount of transported information.

They allow analyzing execution exceptions, which is not true for all probe operations, since they require the method to return something.

If you want to continue your test once a method has been called, you can utilize the barrier like this:

```kotlin
// Arrange
val toTest = spyk(ToTest())
val barrier = barrier { toTest.testMethod(any()) }

// Act
// Trigger the test

// Assert
barrier.tryToTraverse()
```

If the method is not called within 10 seconds or an exception is raised while executing the stub, the test will fail. Otherwise, the test will simply continue.

To also continue the test, even if an exception is thrown, you can pass the argument `failOnException` to the method tryToTraverse and continue your test. For example like this:

```kotlin
val toTest = spyk(ToTest())
val barrier = barrier { toTest.triggerError() }

// Act
thread {
    toTest.triggerError()
}

// Assert
barrier.tryToTraverse(
    failOnException = false
)
assertThat(barrier.raisedException()).isNotNull.isInstanceOf(IllegalStateException::class.java)
```

This check can also be performed reactive, like this:

```kotlin
val barrier = barrier { toTest.triggerError() }
barrier.onError {
    assertThat(it).isInstanceOf(IllegalStateException::class.java)
}
```

> Note: The onError will be called on the test thread, to not interact with the tested code.

### MethodBarriers with "strict" mocks

The previous examples require the tested class to either be a relaxed mock, or a spy. If you want to use a "normal" mock, you can use the `barrierFor` method instead:

```kotlin
// Arrange
val toTest = mockk<ToTest>()
val input = "Foo"
val methodProbe = barrierFor { toTest.testMethod(any()) } returns "Bar"

// Act
var result: Any? = null
thread {
    result = toTest.testMethod(input) // Ugly way of verifying that the answer is as mocked
}

// Assert
methodProbe.tryToTraverse() // Method has been called
assertThat(result).isNotNull
    .isEqualTo("Bar")
    .isNotEqualTo(input)
```

Using `probing` instead of `probe` will return a custom version of the `MockKStubScope` called `ProbeMockKStubScope`. So you can use the normal mockk toolset and receive a MethodProbe to analyze the results.


## MethodProbes

If you need more detailed information about the method (like return values or argument), you can utilize the method probe.

Other than the MethodBarrier, a MethodProbe holds more relevant information than. This introduces a little more computational complexity and operational overhead, but if you need more information you can use this function

```kotlin
// Arrange
val toTest = spyk(ToTest())
val methodProbe = probe { toTest.testMethod(any()) }

// Act
thread {
    toTest.testMethod("Foo")
}

// Assert
val firstArgument: String = methodProbe.getArgument(0)
val result = methodProbe.getResult()
assertThat(firstArgument).isEqualTo(result)
```

Calling any method on the method probe will wait until the respective information is present, meaning every method also allows you to define how long it will wait.

So, calling `methodProbe.getArgument(0)` waits until the spied method has been called, but not necessarily until it is finished.    
If the method is not called in the defined timeout (default 10 seconds), the test will fail

Calling `methodProbe.getResult()` on the other hand waits until the spied method finishes, which implies that no exception is raised while doing so.    
If the method is not called or did not finish in the defined timeout (default 10 seconds), the test will fail.    
The same is true, if the spied upon code throws any exception.

### MethodProbes with "strict" mocks

The previous examples require the tested class to either be a relaxed mock, or a spy. If you want to use a "normal" mock, you can use probing instead:

```kotlin
// Arrange
val toTest = mockk<ToTest>()
val input = "Foo"
val methodProbe = probing { toTest.testMethod(any()) } returns "Bar"

// Act
thread {
    toTest.testMethod(input)
}

// Assert
val probedResult = methodProbe.getResult()
assertThat(result).isEqualTo(probedResult)
    .isEqualTo("Bar")
    .isNotEqualTo(input)
```

Using `probing` instead of `probe` will return a custom version of the `MockKStubScope` called `ProbeMockKStubScope`. So you can use the normal mockk toolset and receive a MethodProbe to analyze the results.

### MethodProbes fluent asserts

When you are using MethodProbes, you can utilize AssertJ, to validate your result. For example like this:


```kotlin

 import java.util.concurrent.TimeUnit// Arrange
val toTest = mockk<ToTest>()
val methodProbe = probe { toTest.testMethod(any()) }

// Act
// perform the test

// Assert
methodProbe.assertThatResult()
    .isNotNull
methodProbe.assertThatExecutionTimeMillis()
    .isLessThen(TimeUnit.SECONDS.toMillis(1))
methodProbe.asserThatArguments()
    .hasSize(1)
```

# Spring Test Support

If you are using Spring and are writing integration tests utilizing mockk [(also using springmockk)](https://github.com/Ninja-Squad/springmockk), you can utilize this library to write more reliable and performant integration tests, for example like this.

Let's assume you want to test a create/update workflow, through Kafka and test that the result is correct. It can be done like this:

```kotlin
@SpringIntegrationTest // Setup Database, ApplicationContext, Kafka, whatever your heart desires
class ExampleIntegrationTest {
    @SpyKBean
    lateinit var kafkaListener: KafkaListener
    
    @Autowired
    lateinit var entityRepository: EntityRepository
    
    @Test
    fun validateCreateUpdate() {
        // Arrange
        val createMessage = CreateSomethingMessage(/* data */)
        val updateMessage = UpdateSomethingMessage(/* data */)
        val kafkaBarrier = barrier { kafkaConsumer.consume(any()) }

        // Act
        kafkaTemplate.sendDefault(createMessage).get()
        kafkaTemplate.sendDefault(updateMessage).get()

        // Assert 
        kafkaBarrier.tryToTraverse()

        assertThat(entityRepository.findById(/* id */).get().version)
            .withFailMessage("Entity was not updated")
            .isEqualTo(2)
    }
}
```

and you can insert as many probes, barriers and mocks as you want to:

```kotlin
@SpringIntegrationTest // Setup Database, ApplicationContext, Kafka, whatever your heart desires
class ExampleIntegrationTest {
    @SpyKBean
    lateinit var kafkaListener: KafkaListener
    
    @SpyKBean
    lateinit var entityService: EntityService
    
    @Test
    fun validateCreateUpdate() {
        // Arrange
        val createMessage = CreateSomethingMessage(/* data */)
        val updateMessage = UpdateSomethingMessage(/* data */)
        val kafkaBarrier = barrier { kafkaConsumer.consume(any()) }
        val serviceProbe = probe { entityService.handle(ofType<UpdateSomethingMessage>()) }

        // Act
        kafkaTemplate.sendDefault(createMessage).get()
        kafkaTemplate.sendDefault(updateMessage).get()

        // Assert
        kafkaBarrier.tryToTraverse()
        val entity = serviceProbe.getResult()

        assertThat(entity.version)
            .withFailMessage("Entity was not updated")
            .isEqualTo(2)
    }
}
```
