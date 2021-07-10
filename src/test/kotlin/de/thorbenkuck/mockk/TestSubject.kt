package de.thorbenkuck.mockk

class TestSubject {
    fun passThrough(input: Any?): Any? {
        return input
    }

    fun timeoutThenPassThrough(input: Any?, timeoutInMillis: Long = 1000): Any? {
        Thread.sleep(timeoutInMillis)
        return passThrough(input)
    }
}
