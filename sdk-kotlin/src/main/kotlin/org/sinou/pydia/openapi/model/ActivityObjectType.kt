/**
 *
 * Please note:
 * This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * Do not edit this file manually.
 *
 */

@file:Suppress(
    "ArrayInDataClass",
    "EnumEntryName",
    "RemoveRedundantQualifierName",
    "UnusedImport"
)

package org.sinou.pydia.openapi.model


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * 
 *
 * Values: baseObject,activity,link,mention,collection,orderedCollection,collectionPage,orderedCollectionPage,application,group,organization,person,service,article,audio,document,event,image,note,page,place,profile,relationship,tombstone,video,accept,add,announce,arrive,block,create,delete,dislike,flag,follow,ignore,invite,join,leave,like,listen,move,offer,question,reject,read,remove,tentativeReject,tentativeAccept,travel,undo,update,updateComment,updateMeta,view,workspace,digest,folder,cell,share
 */

@JsonClass(generateAdapter = false)
enum class ActivityObjectType(val value: kotlin.String) {

    @Json(name = "BaseObject")
    baseObject("BaseObject"),

    @Json(name = "Activity")
    activity("Activity"),

    @Json(name = "Link")
    link("Link"),

    @Json(name = "Mention")
    mention("Mention"),

    @Json(name = "Collection")
    collection("Collection"),

    @Json(name = "OrderedCollection")
    orderedCollection("OrderedCollection"),

    @Json(name = "CollectionPage")
    collectionPage("CollectionPage"),

    @Json(name = "OrderedCollectionPage")
    orderedCollectionPage("OrderedCollectionPage"),

    @Json(name = "Application")
    application("Application"),

    @Json(name = "Group")
    group("Group"),

    @Json(name = "Organization")
    organization("Organization"),

    @Json(name = "Person")
    person("Person"),

    @Json(name = "Service")
    service("Service"),

    @Json(name = "Article")
    article("Article"),

    @Json(name = "Audio")
    audio("Audio"),

    @Json(name = "Document")
    document("Document"),

    @Json(name = "Event")
    event("Event"),

    @Json(name = "Image")
    image("Image"),

    @Json(name = "Note")
    note("Note"),

    @Json(name = "Page")
    page("Page"),

    @Json(name = "Place")
    place("Place"),

    @Json(name = "Profile")
    profile("Profile"),

    @Json(name = "Relationship")
    relationship("Relationship"),

    @Json(name = "Tombstone")
    tombstone("Tombstone"),

    @Json(name = "Video")
    video("Video"),

    @Json(name = "Accept")
    accept("Accept"),

    @Json(name = "Add")
    add("Add"),

    @Json(name = "Announce")
    announce("Announce"),

    @Json(name = "Arrive")
    arrive("Arrive"),

    @Json(name = "Block")
    block("Block"),

    @Json(name = "Create")
    create("Create"),

    @Json(name = "Delete")
    delete("Delete"),

    @Json(name = "Dislike")
    dislike("Dislike"),

    @Json(name = "Flag")
    flag("Flag"),

    @Json(name = "Follow")
    follow("Follow"),

    @Json(name = "Ignore")
    ignore("Ignore"),

    @Json(name = "Invite")
    invite("Invite"),

    @Json(name = "Join")
    join("Join"),

    @Json(name = "Leave")
    leave("Leave"),

    @Json(name = "Like")
    like("Like"),

    @Json(name = "Listen")
    listen("Listen"),

    @Json(name = "Move")
    move("Move"),

    @Json(name = "Offer")
    offer("Offer"),

    @Json(name = "Question")
    question("Question"),

    @Json(name = "Reject")
    reject("Reject"),

    @Json(name = "Read")
    read("Read"),

    @Json(name = "Remove")
    remove("Remove"),

    @Json(name = "TentativeReject")
    tentativeReject("TentativeReject"),

    @Json(name = "TentativeAccept")
    tentativeAccept("TentativeAccept"),

    @Json(name = "Travel")
    travel("Travel"),

    @Json(name = "Undo")
    undo("Undo"),

    @Json(name = "Update")
    update("Update"),

    @Json(name = "UpdateComment")
    updateComment("UpdateComment"),

    @Json(name = "UpdateMeta")
    updateMeta("UpdateMeta"),

    @Json(name = "View")
    view("View"),

    @Json(name = "Workspace")
    workspace("Workspace"),

    @Json(name = "Digest")
    digest("Digest"),

    @Json(name = "Folder")
    folder("Folder"),

    @Json(name = "Cell")
    cell("Cell"),

    @Json(name = "Share")
    share("Share");

    /**
     * Override [toString()] to avoid using the enum variable name as the value, and instead use
     * the actual value defined in the API spec file.
     *
     * This solves a problem when the variable name and its value are different, and ensures that
     * the client sends the correct enum values to the server always.
     */
    override fun toString(): kotlin.String = value

    companion object {
        /**
         * Converts the provided [data] to a [String] on success, null otherwise.
         */
        fun encode(data: kotlin.Any?): kotlin.String? = if (data is ActivityObjectType) "$data" else null

        /**
         * Returns a valid [ActivityObjectType] for [data], null otherwise.
         */
        fun decode(data: kotlin.Any?): ActivityObjectType? = data?.let {
          val normalizedData = "$it".lowercase()
          values().firstOrNull { value ->
            it == value || normalizedData == "$value".lowercase()
          }
        }
    }
}

