package org.sinou.pydia.sdk.client.model.parser

import org.sinou.pydia.sdk.api.ui.Plugin
import java.util.Properties

abstract class RegistryItemHandler {
    fun onPref(name: String, value: String?) {}
    fun onAction(action: String, read: String?, write: String?) {}
    fun onWorkspace(p: Properties) {}
    fun onPlugin(p: Plugin) {}
}