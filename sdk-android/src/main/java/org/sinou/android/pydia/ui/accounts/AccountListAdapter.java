package org.sinou.android.pydia.ui.accounts;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import org.sinou.android.pydia.R;
import org.sinou.android.pydia.data.Account;

import java.util.List;

public class AccountListAdapter extends RecyclerView.Adapter<AccountListAdapter.AccountViewHolder> {

    private final LayoutInflater mInflater;
    private List<Account> accounts; // Cached copy

    AccountListAdapter(Context context) {
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public AccountViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = mInflater.inflate(R.layout.list_item_account, parent, false);
        return new AccountViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(AccountViewHolder holder, int position) {
        if (accounts != null) {
            Account current = accounts.get(position);
            holder.accountItemView.setText(current.getLogin() +"@"+current.getUrl());
        } else {
            // Covers the case of data not being ready yet.
            // TODO plug i18n
            holder.accountItemView.setText("No accounts");
        }
    }

    void setAccounts(List<Account> accounts) {
        this.accounts = accounts;
        notifyDataSetChanged();
    }

    // getItemCount() is called many times, and when it is first called accounts is null
    @Override
    public int getItemCount() {
        if (accounts != null)
            return accounts.size();
        else return 0;
    }

    class AccountViewHolder extends RecyclerView.ViewHolder {
        private final TextView accountItemView;

        private AccountViewHolder(View itemView) {
            super(itemView);
            accountItemView = itemView.findViewById(R.id.textView);
        }
    }
}
