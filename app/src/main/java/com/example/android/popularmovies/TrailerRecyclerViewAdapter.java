package com.example.android.popularmovies;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by adamzarn on 6/25/17.
 */

public class TrailerRecyclerViewAdapter extends RecyclerView.Adapter<TrailerRecyclerViewAdapter.TrailerViewHolder> {

    private static final String TAG = RecyclerView.class.getSimpleName();
    final private TrailerClickListener trailerOnClickListener;

    private TrailerObject[] myTrailerData = null;

    public interface TrailerClickListener {
        void onListItemClick(View view, int clickedItemIndex);
    }

    public TrailerRecyclerViewAdapter(TrailerClickListener listener) {
        trailerOnClickListener = listener;
    }

    public TrailerViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        Context context = viewGroup.getContext();
        int layoutIdForListItem = R.layout.trailer_item;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(layoutIdForListItem, viewGroup, shouldAttachToParentImmediately);
        TrailerViewHolder viewHolder = new TrailerViewHolder(view);

        return viewHolder;
    }

    public void setData(TrailerObject[] trailers) {
        myTrailerData = trailers;
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(TrailerViewHolder holder, int position) {
        TrailerObject currentTrailer = myTrailerData[position];
        holder.trailerNameView.setText(currentTrailer.getName());
    }

    @Override
    public int getItemCount() {
        if (myTrailerData == null) {
            return 0;
        }
        return myTrailerData.length;
    }

    public TrailerObject getItemAtPosition(int clickedPosition) {
        return myTrailerData[clickedPosition];
    }

    class TrailerViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        TextView trailerNameView;

        public TrailerViewHolder(View itemView) {
            super(itemView);

            trailerNameView = (TextView) itemView.findViewById(R.id.trailer_name);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int clickedPosition = getAdapterPosition();
            trailerOnClickListener.onListItemClick(v, clickedPosition);
        }
    }
}
