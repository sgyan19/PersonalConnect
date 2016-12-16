package com.sun.conversation;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sun.personalconnect.Application;
import com.sun.personalconnect.R;

/**
 * Created by guoyao on 2016/12/16.
 */
public class CvsRecyclerAdapter extends RecyclerView.Adapter<CvsRecyclerAdapter.Holder> {

    private Context mContext;

    public CvsRecyclerAdapter(Context context){
        mContext = context;
    }

    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        return  new Holder(LayoutInflater.from(mContext).inflate(R.layout.holder_conversation_item, parent, false));
    }

    @Override
    public void onBindViewHolder(Holder holder, int position) {
        holder.cardView.update(Application.getInstance().getCvsHistoryManager().getCache(position));
    }

    @Override
    public int getItemCount() {
        return Application.getInstance().getCvsHistoryManager().getCacheCount();
    }

    public static class Holder extends RecyclerView.ViewHolder{
        public CvsCardView cardView;

        public Holder(View view) {
            super(view);
            cardView = (CvsCardView) view.findViewById(R.id.cvs_card);
        }
    }
}
