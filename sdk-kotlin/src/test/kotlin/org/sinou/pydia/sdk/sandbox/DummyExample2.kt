package org.sinou.pydia.sdk.sandbox

import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class DummyExample2 {
    private var result = 0
    @Before
    fun setupThis() {
        result++
    }

    @Test
    fun method() {
        println("### Testing method")
        Assert.assertEquals(result.toLong(), 1)
    }

    @After
    fun tearThis() {
        result++
    }
}