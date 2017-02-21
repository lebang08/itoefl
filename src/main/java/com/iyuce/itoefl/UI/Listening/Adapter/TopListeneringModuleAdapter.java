package com.iyuce.itoefl.UI.Listening.Adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.iyuce.itoefl.R;

import java.util.ArrayList;

/**
 * Created by LeBang on 2017/1/23
 */
public class TopListeneringModuleAdapter extends RecyclerView.Adapter<TopListeneringModuleAdapter.ItemViewHolder> {

    //private Context mContext;
    private ArrayList<String> mList;
    private LayoutInflater mLayoutInflater;

    public TopListeneringModuleAdapter(Context context, ArrayList<String> list) {
        //mContext = context;
        mLayoutInflater = LayoutInflater.from(context);
        mList = list;
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ItemViewHolder(mLayoutInflater
                .inflate(R.layout.recycler_item_top_listenering_module, parent, false));
    }

    @Override
    public void onBindViewHolder(ItemViewHolder holder, int position) {
        if (position % 5 == 0) {
            holder.mProgressBar.setVisibility(View.VISIBLE);
        }
        holder.mItemTxtTitle.setText(mList.get(position));
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    class ItemViewHolder extends RecyclerView.ViewHolder {

        ProgressBar mProgressBar;
        TextView mItemTxtTitle;

        public ItemViewHolder(View view) {
            super(view);
            mProgressBar = (ProgressBar) view.findViewById(R.id.progressbar_item_top_listenering_order);
            mItemTxtTitle = (TextView) view.findViewById(R.id.txt_item_top_listenering_order_title);
        }
    }
}