package org.sinou.pydia.sdk.sandbox

import org.junit.After
import org.junit.AfterClass
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test

class DummyExample {

    @Before
    fun setupThis() {
        println("## Non static Setup")
    }

    @Test
    fun method() {
        val order = "b".compareTo("c")
        val order2 = "d".compareTo("c")
        val order3 = "D".compareTo("c")
        println("Order: $order")
        println("Order2: $order2")
        println("Order3: $order3")
    }

    @After
    fun tearThis() {
        println("## Non static TearDown")
    }

    companion object {
        @JvmStatic
        @BeforeClass
        fun setup(){
            println("## Static setup")
        }

        @JvmStatic
        @AfterClass
        fun tear() {
            println("## Static teardown")
        }
    }
}