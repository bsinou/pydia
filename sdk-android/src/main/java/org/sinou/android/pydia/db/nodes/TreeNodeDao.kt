package org.sinou.android.pydia.db.nodes

import androidx.lifecycle.LiveData
import androidx.room.*
import org.sinou.android.pydia.db.Converters

@Dao
@TypeConverters(Converters::class)
interface TreeNodeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(treeNode: RTreeNode)

    @Update
    fun update(treeNode: RTreeNode)

    @Query("DELETE FROM tree_nodes WHERE encoded_state = :stateId")
    fun delete(stateId: String)

    @Query("DELETE FROM tree_nodes WHERE encoded_state like :stateId || '%'")
    fun deleteUnder(stateId: String)

    @Query("SELECT * FROM tree_nodes WHERE encoded_state like :stateId || '%'")
    fun getUnder(stateId: String): List<RTreeNode>

    @Query("SELECT * FROM tree_nodes WHERE encoded_state = :encodedState LIMIT 1")
    fun getLiveNode(encodedState: String): LiveData<RTreeNode>

    @Query("SELECT * FROM tree_nodes WHERE encoded_state = :encodedState LIMIT 1")
    fun getNode(encodedState: String): RTreeNode?

    @Query("SELECT * FROM tree_nodes WHERE encoded_state like :encodedParentStateID || '%' AND parent_path = :parentPath ORDER BY sort_name")
    fun ls(encodedParentStateID: String, parentPath: String): LiveData<List<RTreeNode>>

    @Query("SELECT * FROM tree_nodes WHERE encoded_state like :encodedParentStateID || '%' AND parent_path = :parentPath ORDER BY name")
    fun getNodesForDiff(encodedParentStateID: String, parentPath: String): List<RTreeNode>

    @Query("SELECT * FROM tree_nodes WHERE name like '%' ||  :name || '%' LIMIT 100")
    fun query(name: String): List<RTreeNode>

    @Query("SELECT * FROM tree_nodes WHERE encoded_state like :encodedParentStateID || '%' AND parent_path = :parentPath AND mime = :mime ORDER BY sort_name")
    fun lsWithMime(
        encodedParentStateID: String,
        parentPath: String,
        mime: String
    ): LiveData<List<RTreeNode>>

    @Query("SELECT * FROM tree_nodes WHERE encoded_state like :accountID || '%' AND is_bookmarked = 1 ORDER BY name")
    fun getBookmarked(accountID: String): LiveData<List<RTreeNode>>
//
//    @Query("SELECT * FROM tree_node_table WHERE account_id = :accountID AND is_shared = 1")
//    fun getShared(accountID: String): LiveData<List<RTreeNode>>
//
//    @Query("SELECT * FROM tree_node_table WHERE account_id = :accountID AND is_offline = 1")
//    fun getOfflineRoots(accountID: String): LiveData<List<RTreeNode>>

}