package org.sinou.pydia.sdk.client.model

import org.sinou.pydia.sdk.api.Registry
import org.sinou.pydia.sdk.api.SdkNames
import org.sinou.pydia.sdk.api.ui.Plugin
import org.sinou.pydia.sdk.api.ui.WorkspaceNode
import org.sinou.pydia.sdk.utils.Log
import org.w3c.dom.Document
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import java.io.InputStream
import java.util.Properties
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.xpath.XPathConstants
import javax.xml.xpath.XPathExpressionException
import javax.xml.xpath.XPathFactory

class DocumentRegistry : Registry {

    private var prefix: String? = null
    private var userNode: Node? = null
    private val xmlDocument: Document?
    private var parsedWorkspaces: List<WorkspaceNode>? = null
    private var parsedPlugins: List<Plugin>? = null
//    private var parsedActions: List<Action>? = null

    private val ajxpRepositoriesXPath = "/user/repositories"

    //    private val ajxpActionsXPath = "/ajxp_registry/actions"
    private val ajxpPluginsXPath = "/ajxp_registry/plugins"
    private val pydioRepositoriesXPath = "/pydio_registry/user/repositories"


    constructor(xmlDocument: Document?) {
        this.xmlDocument = xmlDocument
        handleRoot()
    }

    constructor(input: InputStream?) {
        val factory = DocumentBuilderFactory.newInstance()
        val builder = factory.newDocumentBuilder()
        xmlDocument = builder.parse(input)
        // NodeList children = xmlDocument.getChildNodes();
        // for (int i = 0; i < children.getLength(); i++) {
        //     Log.i(logTag, i + ": " + children.item(i).toString());
        // }
        handleRoot()
    }

    private fun handleRoot() {

        xmlDocument?.let {
            if (it.hasChildNodes()) {
                val root = it.childNodes.item(0)

                prefix = if (root.nodeName == CELLS_PREFIX) {
                    "/$CELLS_PREFIX/"
                } else throw RuntimeException("Unexpected root: " + root.nodeName)

                if (root.hasChildNodes()) {
                    val children = root.childNodes
                    for (i in 0 until children.length) {
                        if (USER_NODE_NAME == children.item(i).nodeName) {
                            userNode = children.item(i)
                            break
                        }
                    }
                }
            }
        }
    }

    override fun isLoggedIn(): Boolean {
        return userNode != null
    }

    override fun getWorkspaces(): List<WorkspaceNode> {
        parsedWorkspaces?.let {
            return it
        }
        val tmp = parseWorkspaces()
        parsedWorkspaces = tmp
        return tmp
    }

    override fun getActions(): MutableList<Action> {
        TODO("Not yet implemented")
    }

    private fun parseWorkspaces(): List<WorkspaceNode> {
        val xPath = XPathFactory.newInstance().newXPath()
        try {
            val workspaceNodes: MutableList<WorkspaceNode> = ArrayList()
            var repositoriesNode = xPath.compile(ajxpRepositoriesXPath)
                .evaluate(xmlDocument, XPathConstants.NODESET) as NodeList
            if (repositoriesNode.length == 0) {
                repositoriesNode = xPath.compile(pydioRepositoriesXPath)
                    .evaluate(xmlDocument, XPathConstants.NODESET) as NodeList
            }
            if (repositoriesNode.length > 0) {
                val repositoriesChildNodes = repositoriesNode.item(0).childNodes
                for (i in 0 until repositoriesChildNodes.length) {
                    val node = repositoriesChildNodes.item(i)
                    val tag = node.nodeName
                    if ("repo" == tag) {
                        parseWorkspace(node)?.let {
                            workspaceNodes.add(it)
                        }
                    }
                }
            }
            return workspaceNodes
        } catch (e: XPathExpressionException) {
            e.printStackTrace()
        }
        return listOf()
    }

    private fun parseWorkspace(node: Node): WorkspaceNode? {
        val attrs = node.attributes

        // We currently only use  id, slug, label and description
        val id = attrs.getNamedItem(SdkNames.WS_XML_KEY_ID).nodeValue
        val slug = attrs.getNamedItem(SdkNames.WS_XML_KEY_SLUG).nodeValue
        val type = attrs.getNamedItem(SdkNames.WS_XML_KEY_TYPE).nodeValue

        val nodeProps = Properties()
        attrs.getNamedItem(SdkNames.WS_XML_KEY_ACL)?.let {
            nodeProps[SdkNames.WS_XML_KEY_ACL] = it.nodeValue
        }
        attrs.getNamedItem(SdkNames.WS_XML_KEY_ACCESS_TYPE)?.let {
            nodeProps[SdkNames.WS_XML_KEY_ACCESS_TYPE] = it.nodeValue
        }
        attrs.getNamedItem(SdkNames.WS_XML_KEY_OWNER)?.let {
            nodeProps[SdkNames.WS_XML_KEY_OWNER] = it.nodeValue
        }
        attrs.getNamedItem(SdkNames.WS_XML_KEY_CROSS_COPY)?.let {
            nodeProps[SdkNames.WS_XML_KEY_CROSS_COPY] = it.nodeValue
        }
        attrs.getNamedItem(SdkNames.WS_XML_KEY_META_SYNC)?.let {
            nodeProps[SdkNames.WS_XML_KEY_META_SYNC] = it.nodeValue
        }

        var label: String? = null
        var desc: String? = null

        val childProps = node.childNodes
        for (j in 0 until childProps.length) {
            val prop = childProps.item(j)
            when (prop.nodeName) {
                SdkNames.WS_XML_KEY_LABEL -> label = prop.firstChild.nodeValue

                SdkNames.WS_XML_KEY_DESC -> desc = prop.firstChild.nodeValue

            }
        }

        return if (SdkNames.hiddenWSLabels.contains(label)) {
            Log.d(logTag, "... Skipping technical WS: $label")
            null
        } else {
            WorkspaceNode(
                uuid = id,
                slug = slug,
                label = label ?: slug,
                description = desc,
                type = type,
                props = nodeProps
            )
        }

//        TODO we skip these properties since moving to kotlin only
//        val id = attrs.getNamedItem(SdkNames.WORKSPACE_PROPERTY_ID)
//        val acl = attrs.getNamedItem(SdkNames.WORKSPACE_PROPERTY_ACL)
//        val owner = attrs.getNamedItem(SdkNames.WORKSPACE_PROPERTY_OWNER)
//        val crossCopy = attrs.getNamedItem(SdkNames.WORKSPACE_PROPERTY_CROSS_COPY)
//        val accessType = attrs.getNamedItem(SdkNames.WORKSPACE_PROPERTY_ACCESS_TYPE)
//        val metaSync = attrs.getNamedItem(SdkNames.WORKSPACE_PROPERTY_META_SYNC)
//
    }

    override fun getPlugins(): List<Plugin> {
        return parsedPlugins ?: parsePlugins().also { parsedPlugins = it }!!
    }

    private fun parsePlugins(): List<Plugin>? {
        val xPath = XPathFactory.newInstance().newXPath()
        try {
            val plugins: MutableList<Plugin> = ArrayList()
            val pluginsNode =
                xPath.compile(ajxpPluginsXPath).evaluate(xmlDocument, XPathConstants.NODE) as Node
            val repositoriesChildNodes = pluginsNode.childNodes
            for (i in 0 until repositoriesChildNodes.length) {
                val node = repositoriesChildNodes.item(i)
                val tag = node.nodeName
                if ("ajxp_plugin" == tag || "plugin" == tag) {
                    val plugin = parsePlugin(node)
                    plugins.add(plugin)
                }
            }
            return plugins
        } catch (e: XPathExpressionException) {
            e.printStackTrace()
        }
        return null
    }

    private fun parsePlugin(node: Node): Plugin {
        var properties: Properties? = null
        val attrs = node.attributes
        val id = if (attrs.getNamedItem("id") != null) attrs.getNamedItem("id").nodeValue else ""
        val name =
            if (attrs.getNamedItem("name") != null) attrs.getNamedItem("name").nodeValue else ""
        val label =
            if (attrs.getNamedItem("label") != null) attrs.getNamedItem("label").nodeValue else ""
        val description =
            if (attrs.getNamedItem("description") != null) attrs.getNamedItem("description").nodeValue else ""
        val pluginChildNodes = node.childNodes
        for (i in 0 until pluginChildNodes.length) {
            val pluginChildNode = pluginChildNodes.item(i)
            val tag = pluginChildNode.nodeName
            if ("plugin_configs" == tag) {
                properties = parsePluginProperties(pluginChildNode)
                break
            }
        }
        return Plugin(id, name, label, description, properties ?: Properties())
    }

    private fun parsePluginProperties(node: Node): Properties {
        val properties = Properties()
        val propertyNodes = node.childNodes
        for (i in 0 until propertyNodes.length) {
            val propertyNode = propertyNodes.item(i)
            val tag = propertyNode.nodeName
            if ("property" == tag) {
                val propName = propertyNode.attributes.getNamedItem("name").nodeValue
                val value = propertyNode.firstChild.nodeValue
                properties.setProperty(propName, value)
            }
        }
        return properties
    }

    companion object {
        private const val logTag = "DocumentRegistry"
        private const val CELLS_PREFIX = "pydio_registry"
        private const val USER_NODE_NAME = "user"
    }

    //    override fun getActions(): List<Action> {
//
//        return parsedActions ?: run { parseActions().also { parsedActions = it }!! }
//    }

//    private fun parseActions(): List<Action>? {
//        val xPath = XPathFactory.newInstance().newXPath()
//        try {
//            val actions: MutableList<Action> = ArrayList()
//            val repositoriesNode =
//                xPath.compile(ajxpActionsXPath).evaluate(xmlDocument, XPathConstants.NODE) as Node
//            val repositoriesChildNodes = repositoriesNode.childNodes
//            for (i in 0 until repositoriesChildNodes.length) {
//                val node = repositoriesChildNodes.item(i)
//                val tag = node.nodeName
//                if ("action" == tag) {
//                    val action = parseAction(node)
//                    actions.add(action)
//                }
//            }
//            return actions
//        } catch (e: XPathExpressionException) {
//            e.printStackTrace()
//        }
//        return null
//    }

//    private fun parseAction(node: Node): Action {
//        var adminOnly: Boolean? = null
//        var noUser: Boolean? = null
//        val read: Boolean? = null
//        val write: Boolean? = null
//        val userLogged: Boolean? = null
//
//        // To do: add constant in SDKNames instead of hardcoded string
//        val attrs = node.attributes
//        var dirDefault = false
//        var fileDefault = false
//        val name = attrs.getNamedItem("name").nodeValue
//        val dirDefaultNode = attrs.getNamedItem("dirDefault")
//        val fileDefaultNode = attrs.getNamedItem("fileDefault")
//        if (dirDefaultNode != null) {
//            dirDefault = "true" == dirDefaultNode.nodeValue
//        }
//        if (fileDefaultNode != null) {
//            fileDefault = "true" == fileDefaultNode.nodeValue
//        }
//        val actionChildNodes = node.childNodes
//        for (i in 0 until actionChildNodes.length) {
//            val actionChildNode = node.firstChild
//            val tag = actionChildNode.nodeName
//            if ("rightsContext" == tag) {
//                val rightsContextAttrs = node.attributes
//                val attrAdminOnly = rightsContextAttrs.getNamedItem("adminOnly")
//                if (attrAdminOnly != null) {
//                    adminOnly = java.lang.Boolean.valueOf(attrAdminOnly.nodeValue)
//                }
//                val attrNoUser = rightsContextAttrs.getNamedItem("noUser")
//                if (attrNoUser != null) {
//                    noUser = java.lang.Boolean.valueOf(attrNoUser.nodeValue)
//                }
//                val attrRead = rightsContextAttrs.getNamedItem("read")
//                if (attrRead != null) {
//                    adminOnly = java.lang.Boolean.valueOf(attrRead.nodeValue)
//                }
//                val attrWrite = rightsContextAttrs.getNamedItem("write")
//                if (attrWrite != null) {
//                    adminOnly = java.lang.Boolean.valueOf(attrWrite.nodeValue)
//                }
//                val attrUserLogged = rightsContextAttrs.getNamedItem("userLogged")
//                if (attrUserLogged != null) {
//                    adminOnly = java.lang.Boolean.valueOf(attrUserLogged.nodeValue)
//                }
//            }
//        }
//        return Action(name, dirDefault, fileDefault, write, read, adminOnly, userLogged, noUser)
//    }

}
