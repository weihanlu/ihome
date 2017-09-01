package com.qhiehome.ihome.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.qhiehome.ihome.R;

import java.util.List;

/**
 * Created by YueMa on 2017/9/1.
 */

public class SearchMapAdapter extends RecyclerView.Adapter<SearchMapAdapter.SearchViewHolder>{

    private Context mContext;
    private List<String> mSearchResults;

    public SearchMapAdapter(Context mContext, List<String> mSearchResults) {
        this.mContext = mContext;
        this.mSearchResults = mSearchResults;
    }

    public void setmSearchResults(List<String> mSearchResults) {
        this.mSearchResults = mSearchResults;
    }

    @Override
    public SearchViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new SearchViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_city_select, parent, false));
    }

    @Override
    public void onBindViewHolder(final SearchViewHolder holder, int position) {

        holder.tv_search.setText(mSearchResults.get(position));
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onClickListener != null) {
                    onClickListener.onClick(holder.itemView, holder.getLayoutPosition(), holder.tv_search.getText().toString());
                }
            }
        });
    }


    @Override
    public int getItemCount() {
        return mSearchResults.size();
    }

    class SearchViewHolder extends RecyclerView.ViewHolder {
        TextView tv_search;

        private SearchViewHolder(View view) {
            super(view);
            tv_search = (TextView) view.findViewById(R.id.tv_city);
        }

    }

    public void setOnItemClickListener(OnClickListener listener) {
        this.onClickListener = listener;
    }

    private OnClickListener onClickListener;

    public interface OnClickListener {
        void onClick(View view, int i, String name);
    }
}
