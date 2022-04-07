package org.sinou.android.pydia.services

import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

// Given some classes
// class Controller(val service: BusinessService)
class BusinessService() {
    fun sayHello() {
        System.out.println("########### hello ################ ")
    }
}

// just declare it
val myModule = module {
    singleOf(::HelloController)
    singleOf(::BusinessService)
}

// Controller & BusinessService are declared in a module
class HelloController(private val service: BusinessService) {

    fun hello() {
        // service is ready to use
        service.sayHello()
    }
}
