package org.sinou.pydia.sdk.api

/**
 * Centralize all string constants for the com.pydio.sdk API
 */
interface SdkNames {

    companion object {
        /* CONSTANTS */
        const val DEFAULT_CLIENT_ID = "cells-client"
        const val DEFAULT_CLIENT_SECRET = ""

        // Well known Pydio specific Mime Types
        const val NODE_MIME_WS_ROOT = "pydio/workspace-root"
        const val NODE_MIME_FOLDER = "pydio/nodes-list"
        const val NODE_MIME_RECYCLE = "pydio/recycle"
        const val NODE_MIME_DEFAULT = "application/octet-stream"

        // Workspaces types
        const val WS_TYPE_PERSONAL = "workspace-personal"
        const val WS_TYPE_CELL = "cell"
        const val WS_TYPE_DEFAULT = "workspace"
        const val RECYCLE_BIN_NAME = "recycle_bin"

        // Node (file and folder) properties
        const val NODE_PROPERTY_PATH = "path"
        const val NODE_PROPERTY_FILENAME = "filename"
        const val NODE_PROPERTY_BYTESIZE = "bytesize"
        const val NODE_PROPERTY_MIME = "mime"
        const val NODE_PROPERTY_MTIME = "ajxp_modiftime"
        const val NODE_PROPERTY_TEXT = "text"
        const val NODE_PROPERTY_IS_FILE = "is_file"
        const val NODE_PROPERTY_IS_IMAGE = "is_image"
        const val NODE_PROPERTY_HAS_THUMB = "has_thumb"
        const val NODE_PROPERTY_IS_PRE_VIEWABLE = "is_pre_viewable"

        //    String NODE_PROPERTY_REMOTE_THUMBS = "remote_thumbs";
        const val NODE_PROPERTY_IMG_EXIF_ORIENTATION = "image_exif_orientation"
        const val NODE_PROPERTY_FILE_PERMS = "file_perms"
        const val NODE_PROPERTY_IMAGE_HEIGHT = "image_height"
        const val NODE_PROPERTY_IMAGE_WIDTH = "image_width"
        const val NODE_PROPERTY_LABEL = "label"
        const val NODE_PROPERTY_ETAG = "etag"
        const val NODE_PROPERTY_WORKSPACE_SLUG = "workspace_slug"
        const val NODE_PROPERTY_BOOKMARK = "bookmark"
        const val NODE_PROPERTY_SHARED = "shared"
        const val NODE_PROPERTY_SHARE_LINK = "share_link"
        const val META_KEY_WS_SHARES = "workspaces_shares"
        const val NODE_PROPERTY_META_HASH = "meta_hash"
        const val META_KEY_IMG_THUMBS = "ImageThumbnails"

        const val NODE_PROPERTY_UID = "uuid"
        const val NODE_PROPERTY_SHARE_UUID = "share_uuid"


        // TODO these values are used to parse Cells Legacy Registry
        const val WORKSPACE_PROPERTY_ID = "id"
        const val WORKSPACE_DESCRIPTION = "description"
        const val WORKSPACE_PROPERTY_SLUG = "repositorySlug"
        const val WORKSPACE_PROPERTY_LABEL = "label"
        const val WORKSPACE_PROPERTY_TYPE = "repository_type"
        const val WORKSPACE_PROPERTY_ACCESS_TYPE = "access_type"
        const val WORKSPACE_PROPERTY_OWNER = "owner"
        const val WORKSPACE_PROPERTY_CROSS_COPY = "allowCrossRepositoryCopy"
        const val WORKSPACE_PROPERTY_META_SYNC = "meta_syncable_REPO_SYNCABLE"
        const val WORKSPACE_PROPERTY_ACL = "acl"
        const val WORKSPACE_IS_PUBLIC = "is_public" // Cells
        //    String WORKSPACE_ACCESS_TYPE_DIRECTORY = "directory";
    }
}