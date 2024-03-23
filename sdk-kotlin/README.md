# SDK Kotlin

This module provide a thin layer over the swagger generated Kotlin SDK for Cells that can be found
in `sdk-openapi` module.

It provides convenience methods to handle authentication and implements some of the boiler plate
code to provide easier to use methods for the implementing application.

> **Warning**: it is quite easy to update the Cells Server, so in order to reduce the amount of work
necessary to update and maintain the SDK we only support Cells Servers in v4.4 or superior.