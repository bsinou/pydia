package org.sinou.android.pydia.db.account

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.net.URL

@Entity(tableName = "account_id_table")
data class RAccountId(
    @PrimaryKey
    @ColumnInfo(name = "account_id") val accountID: String,

    @ColumnInfo(name = "dir_name") val dirName: String,

    @ColumnInfo(name = "db_name") val dbName: String,
) {
    companion object {
        fun newInstance(account: RAccount, index: Int): RAccountId {

            var cleanUrl = URL(account.url).host
            //var cleanDbName = cleanUrl.replace(".", "_")
            var cleanDbName = "nodes.$cleanUrl"

            if (index > 0) {
                cleanUrl += "-$index"
                cleanDbName += "-$index"
            }

            return RAccountId(
                accountID = account.accountID,
                dirName = cleanUrl,
                dbName = cleanDbName,
            )
        }
    }
}
