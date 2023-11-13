package org.sinou.pydia.sdk.api.ui

import java.util.Properties

class Plugin(
    @JvmField val id: String,
    val name: String,
    val label: String,
    val description: String,
    val configs: Properties
)