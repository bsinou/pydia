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
 * Values: BaseObject,Activity,Link,Mention,Collection,OrderedCollection,CollectionPage,OrderedCollectionPage,Application,Group,Organization,Person,Service,Article,Audio,Document,Event,Image,Note,Page,Place,Profile,Relationship,Tombstone,Video,Accept,Add,Announce,Arrive,Block,Create,Delete,Dislike,Flag,Follow,Ignore,Invite,Join,Leave,Like,Listen,Move,Offer,Question,Reject,Read,Remove,TentativeReject,TentativeAccept,Travel,Undo,Update,UpdateComment,UpdateMeta,View,Workspace,Digest,Folder,Cell,Share
 */

@JsonClass(generateAdapter = false)
enum class ActivityObjectType(val value: kotlin.String) {

    @Json(name = "BaseObject")
    BaseObject("BaseObject"),

    @Json(name = "Activity")
    Activity("Activity"),

    @Json(name = "Link")
    Link("Link"),

    @Json(name = "Mention")
    Mention("Mention"),

    @Json(name = "Collection")
    Collection("Collection"),

    @Json(name = "OrderedCollection")
    OrderedCollection("OrderedCollection"),

    @Json(name = "CollectionPage")
    CollectionPage("CollectionPage"),

    @Json(name = "OrderedCollectionPage")
    OrderedCollectionPage("OrderedCollectionPage"),

    @Json(name = "Application")
    Application("Application"),

    @Json(name = "Group")
    Group("Group"),

    @Json(name = "Organization")
    Organization("Organization"),

    @Json(name = "Person")
    Person("Person"),

    @Json(name = "Service")
    Service("Service"),

    @Json(name = "Article")
    Article("Article"),

    @Json(name = "Audio")
    Audio("Audio"),

    @Json(name = "Document")
    Document("Document"),

    @Json(name = "Event")
    Event("Event"),

    @Json(name = "Image")
    Image("Image"),

    @Json(name = "Note")
    Note("Note"),

    @Json(name = "Page")
    Page("Page"),

    @Json(name = "Place")
    Place("Place"),

    @Json(name = "Profile")
    Profile("Profile"),

    @Json(name = "Relationship")
    Relationship("Relationship"),

    @Json(name = "Tombstone")
    Tombstone("Tombstone"),

    @Json(name = "Video")
    Video("Video"),

    @Json(name = "Accept")
    Accept("Accept"),

    @Json(name = "Add")
    Add("Add"),

    @Json(name = "Announce")
    Announce("Announce"),

    @Json(name = "Arrive")
    Arrive("Arrive"),

    @Json(name = "Block")
    Block("Block"),

    @Json(name = "Create")
    Create("Create"),

    @Json(name = "Delete")
    Delete("Delete"),

    @Json(name = "Dislike")
    Dislike("Dislike"),

    @Json(name = "Flag")
    Flag("Flag"),

    @Json(name = "Follow")
    Follow("Follow"),

    @Json(name = "Ignore")
    Ignore("Ignore"),

    @Json(name = "Invite")
    Invite("Invite"),

    @Json(name = "Join")
    Join("Join"),

    @Json(name = "Leave")
    Leave("Leave"),

    @Json(name = "Like")
    Like("Like"),

    @Json(name = "Listen")
    Listen("Listen"),

    @Json(name = "Move")
    Move("Move"),

    @Json(name = "Offer")
    Offer("Offer"),

    @Json(name = "Question")
    Question("Question"),

    @Json(name = "Reject")
    Reject("Reject"),

    @Json(name = "Read")
    Read("Read"),

    @Json(name = "Remove")
    Remove("Remove"),

    @Json(name = "TentativeReject")
    TentativeReject("TentativeReject"),

    @Json(name = "TentativeAccept")
    TentativeAccept("TentativeAccept"),

    @Json(name = "Travel")
    Travel("Travel"),

    @Json(name = "Undo")
    Undo("Undo"),

    @Json(name = "Update")
    Update("Update"),

    @Json(name = "UpdateComment")
    UpdateComment("UpdateComment"),

    @Json(name = "UpdateMeta")
    UpdateMeta("UpdateMeta"),

    @Json(name = "View")
    View("View"),

    @Json(name = "Workspace")
    Workspace("Workspace"),

    @Json(name = "Digest")
    Digest("Digest"),

    @Json(name = "Folder")
    Folder("Folder"),

    @Json(name = "Cell")
    Cell("Cell"),

    @Json(name = "Share")
    Share("Share");

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
