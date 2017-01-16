package com.sun.conversation;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sun.personalconnect.Application;
import com.sun.personalconnect.R;

import java.util.HashMap;

/**
 * Created by guoyao on 2016/12/16.
 */
public class CvsRecyclerAdapter extends RecyclerView.Adapter<CvsRecyclerAdapter.Holder> {

    private Context mContext;

    private HashMap<CvsNote, Integer> map = new HashMap<>();
    private HashMap<Integer, CvsNote> checkMap = new HashMap<>();

    public CvsRecyclerAdapter(Context context){
        mContext = context;
    }

    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        return  new Holder(LayoutInflater.from(mContext).inflate(R.layout.holder_conversation_item, parent, false));
    }

    @Override
    public void onBindViewHolder(Holder holder, int position) {
        CvsNote note = Application.App.getCvsHistoryManager().getCache(position);
        holder.cardView.update(note);
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

    public void notifyItemChanged(CvsNote note){
        Integer position = map.get(note);
        if(position != null) {
            notifyItemChanged(position);
        }
    }

    public int removeTooMoreCache(){
        return Application.App.getCvsHistoryManager().removeTooMoreCache();
    }

    public static class Holder extends RecyclerView.ViewHolder{
        public CvsCardView cardView;

        public Holder(View view) {
            super(view);
            cardView = (CvsCardView) view.findViewById(R.id.cvs_card);
        }
    }
}
