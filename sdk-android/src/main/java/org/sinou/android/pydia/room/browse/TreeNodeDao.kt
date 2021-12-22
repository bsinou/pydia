package org.sinou.android.pydia.room.browse

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface TreeNodeDao {

    @Insert
    fun insert(treeNode: TreeNode)

    @Update
    fun update(treeNode: TreeNode)

    @Query("SELECT * FROM tree_node_table WHERE account_id = :accountID AND parent_path = :path")
    fun ls(accountID: String, path: String): LiveData<List<TreeNode>>

    @Query("SELECT * FROM tree_node_table WHERE account_id = :accountID AND is_bookmarked = 1")
    fun getBookmarked(accountID: String): LiveData<List<TreeNode>>

    @Query("SELECT * FROM tree_node_table WHERE account_id = :accountID AND is_shared = 1")
    fun getShared(accountID: String): LiveData<List<TreeNode>>

    @Query("SELECT * FROM tree_node_table WHERE account_id = :accountID AND is_offline = 1")
    fun getOfflineRoots(accountID: String): LiveData<List<TreeNode>>

}