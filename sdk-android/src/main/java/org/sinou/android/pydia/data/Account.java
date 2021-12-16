package org.sinou.android.pydia.data;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import org.jetbrains.annotations.Contract;

@Entity(tableName = "account_records")
public class Account {


    @PrimaryKey(autoGenerate = true)
    private int uid;

    @NonNull
    @ColumnInfo(name = "login")
    private String login;

    @NonNull
    @ColumnInfo(name = "url")
    private String url;

    @NonNull
    @ColumnInfo(name = "skip_verify")
    private Boolean skipVerify;

    @Contract(pure = true)
    public Account(@NonNull String login, @NonNull String url) {
        this.login = login;
        this.url = url;
        skipVerify = false;
    }

    public Account(@NonNull String login, @NonNull String url, @NonNull boolean skipVerify) {
        this.login = login;
        this.url = url;
        this.skipVerify = skipVerify;
    }

    @NonNull
    public String getLogin() {
        return login;
    }

    @NonNull
    public String getUrl() {
        return url;
    }
}
