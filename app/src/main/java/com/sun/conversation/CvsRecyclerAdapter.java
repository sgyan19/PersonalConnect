package com.sun.conversation;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sun.personalconnect.Application;
import com.sun.personalconnect.R;

import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by guoyao on 2016/12/16.
 */
public class CvsRecyclerAdapter extends RecyclerView.Adapter<CvsRecyclerAdapter.Holder> {
    private static final String TAG = "CvsRecyclerAdapter";
    private static boolean mAdapterLog = true;
    private Context mContext;

    private HashMap<CvsNote, Integer> map = new HashMap<>();
    private HashMap<Integer, CvsNote> checkMap = new HashMap<>();

    public CvsRecyclerAdapter(Context context){
        mContext = context;
    }

    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        return  new Holder(LayoutInflater.from(mContext).inflate(R.layout.holder_conversation_item, parent, false), viewType);
    }

    @Override
    public void onBindViewHolder(Holder holder, int position) {
        CvsNote note = Application.App.getCvsHistoryManager().getCache(position);
        if(mAdapterLog) {
            Log.d(TAG, String.format("onBindViewHolder key:%d code:%d size:%d position:%d,content:%s", holderCount.get(holder.hashCode()), holder.hashCode(), holderCount.size(), position, note.getContent()));
        }
        holder.cardView.update(position, note);
        CvsNote firstWife = checkMap.get(position);
        if(firstWife != null){
            map.remove(firstWife);
        }
        map.put(note, position);
        checkMap.put(position, note);
    }

    @Override
    public int getItemCount() {
        return Application.App.getCvsHistoryManager().getCacheCount();
    }

    @Override
    public int getItemViewType(int position) {
        CvsNote note = Application.App.getCvsHistoryManager().getCache(position);
        return note.getType();
    }

    @Override
    public void onViewRecycled(Holder holder) {
        super.onViewRecycled(holder);
    }


    public void notifyDataSetChangedLog(){
        if(mAdapterLog) {
            holderCount.put(hashCode(), holderCount.size());
            Log.d(TAG, "notifyDataSetChanged");
        }
        notifyDataSetChanged();
    }

    public void notifyItemChanged(CvsNote note){
        Integer position = map.get(note);
        if(position != null) {
            notifyItemChanged(position);
        }
    }

    public int removeTooMoreCache(){
//        return Application.App.getCvsHistoryManager().removeTooMoreCache();
        return 0;
    }
    static HashMap<Integer,Integer> holderCount = new HashMap<>();

    public static class Holder extends RecyclerView.ViewHolder{
        public CvsCardView cardView;

        public Holder(View view, int viewType) {
            super(view);
            cardView = (CvsCardView) view.findViewById(R.id.cvs_card);
            cardView.init(viewType);
            if(mAdapterLog) {
                holderCount.put(hashCode(), holderCount.size());
                Log.d(TAG, String.format("createHolder code:%d size:%d type:%d", hashCode(), holderCount.size(), viewType));
            }
        }

        @Override
        protected void finalize() throws Throwable {
            if(mAdapterLog) {
                Integer code = holderCount.remove(hashCode());
                if (code != null) {
                    Log.d(TAG, String.format("finalize code:%d size:%d", code, holderCount.size()));
                }
            }
            super.finalize();
        }
    }
}
