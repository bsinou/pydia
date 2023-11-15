package org.sinou.pydia.sdk.api.ui

import org.sinou.pydia.sdk.api.SdkNames.Companion.NODE_PROPERTY_MTIME
import org.sinou.pydia.sdk.api.SdkNames.Companion.NODE_PROPERTY_SHARED
import org.sinou.pydia.sdk.api.SdkNames.Companion.WORKSPACE_IS_PUBLIC
import org.sinou.pydia.sdk.api.SdkNames.Companion.WORKSPACE_PROPERTY_ACCESS_TYPE
import org.sinou.pydia.sdk.api.SdkNames.Companion.WORKSPACE_PROPERTY_ACL
import org.sinou.pydia.sdk.api.SdkNames.Companion.WORKSPACE_PROPERTY_OWNER
import org.sinou.pydia.sdk.api.SdkNames.Companion.WORKSPACE_PROPERTY_TYPE
import java.util.Properties

/**
 * Holds information about a distant workspace, in both Cells and legacy P8.
 *
 *
 * Warning: for comparison and equality, we assume that the workspaces are **in the same** server:
 * the workspaces don't hold a reference to their hosting server.
 */
class WorkspaceNode(
    val slug: String,
    var label: String?,
    var description: String?,
    val type: String,
) : Comparable<WorkspaceNode> {

    var properties: Properties? = null
    fun getProperty(key: String?): String {
        return properties!!.getProperty(key, "")
    }

    fun setProperty(key: String?, value: String?) {
        if (properties == null) {
            properties = Properties()
        }
        properties!!.setProperty(key, value)
    }

    val path: String
        get() = "/"

    /* WORKSPACES SPECIFIC PROPERTIES */
    private var plugins: List<Plugin>? = null

    //     private List<Action> availableActions;
    fun setPlugins(plugins: List<Plugin>?) {
        this.plugins = plugins
    }

    fun getPlugin(id: String): Plugin? {
        if (plugins == null) {
            return null
        }
        for (p in plugins!!) {
            if (id == p.id) {
                return p
            }
        }
        return null
    }

    val accessType: String
        get() = properties!!.getProperty(WORKSPACE_PROPERTY_ACCESS_TYPE)
    val workspaceType: String
        get() = properties!!.getProperty(WORKSPACE_PROPERTY_TYPE)
    val aCL: String
        get() = properties!!.getProperty(WORKSPACE_PROPERTY_ACL)
    val owner: String
        get() = properties!!.getProperty(WORKSPACE_PROPERTY_OWNER)
    val isPublic: Boolean
        get() = "true" == properties!!.getProperty(WORKSPACE_IS_PUBLIC)
    val isReadable: Boolean
        get() = "r" == aCL || "rw" == aCL
    val isWriteable: Boolean
        get() = "w" == aCL || "rw" == aCL
    val isShared: Boolean
        get() = "true" == properties!!.getProperty(NODE_PROPERTY_SHARED) || "shared" == properties!!.getProperty(
            WORKSPACE_PROPERTY_OWNER
        )
    val lastModified: Long
        get() = try {
            properties!!.getProperty(NODE_PROPERTY_MTIME).toLong()
        } catch (e: Exception) {
            0
        }

    override fun equals(obj: Any?): Boolean {
        if (this === obj) return true
        return if (obj !is WorkspaceNode) false else slug == obj.slug
        // we rely on the workspace slug that is unique by construction
    }


    /**
     * This is only used for diffs, for UI sorting we rather rely on the sort_name that is added
     * to the RWorkspace object upon creation ... bla bla TODO double check
     */
    override operator fun compareTo(other: WorkspaceNode): Int {
        return label?.compareTo(other.label ?: "") ?: -1
    }

}