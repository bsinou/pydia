package org.sinou.android.pydia.db.account

import androidx.lifecycle.LiveData
import androidx.room.*
import org.sinou.android.pydia.db.browse.RTreeNode

@Dao
interface WorkspaceDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(workspace: RWorkspace)

    @Update
    fun update(workspace: RWorkspace)

    @Query("DELETE FROM workspace_table WHERE encoded_state = :encodedState")
    fun forgetWorkspace(encodedState: String)

    @Query("SELECT * FROM workspace_table WHERE encoded_state = :encodedState")
    fun getWorkspace(encodedState: String): RWorkspace?

    @Query("SELECT * FROM workspace_table WHERE encoded_state like :accountId || '%' ORDER BY sort_name")
    fun getWorkspaces(accountId: String): List<RWorkspace>

    @Query("SELECT * FROM workspace_table WHERE encoded_state like :accountId || '%' ORDER BY sort_name")
    fun getLiveWorkspaces(accountId: String): LiveData<List<RWorkspace>>

    @Query("SELECT * FROM workspace_table WHERE encoded_state like :encodedParentStateID || '%' ORDER BY slug")
    fun getWsForDiff(encodedParentStateID: String): List<RWorkspace>

    @Query("DELETE FROM workspace_table WHERE encoded_state like :accountID || '%'")
    fun forgetAccount(accountID: String)
}
