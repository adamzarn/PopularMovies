package com.example.android.popularmovies;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by adamzarn on 6/26/17.
 */

public class ReviewRecyclerViewAdapter extends RecyclerView.Adapter<ReviewRecyclerViewAdapter.ReviewViewHolder> {

    private static final String TAG = RecyclerView.class.getSimpleName();
    final private ReviewClickListener reviewOnClickListener;

    private ReviewObject[] myReviewData = null;

    public interface ReviewClickListener {
        void onListItemClick(View view, int clickedItemIndex);
    }

    public ReviewRecyclerViewAdapter(ReviewClickListener listener) {
        reviewOnClickListener = listener;
    }

    public ReviewViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        Context context = viewGroup.getContext();
        int layoutIdForListItem = R.layout.review_item;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(layoutIdForListItem, viewGroup, shouldAttachToParentImmediately);
        ReviewViewHolder viewHolder = new ReviewViewHolder(view);

        return viewHolder;
    }

    public void setData(ReviewObject[] Reviews) {
        myReviewData = Reviews;
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(ReviewViewHolder holder, int position) {
        Log.d(TAG, "#" + position);
        ReviewObject currentReview = myReviewData[position];
        holder.reviewAuthorView.setText(currentReview.getAuthor());
        holder.reviewContentView.setText(currentReview.getContent());
    }

    @Override
    public int getItemCount() {
        if (myReviewData == null) {
            return 0;
        }
        return myReviewData.length;
    }

    public ReviewObject getItemAtPosition(int clickedPosition) {
        return myReviewData[clickedPosition];
    }

    class ReviewViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        TextView reviewAuthorView;
        TextView reviewContentView;

        public ReviewViewHolder(View itemView) {
            super(itemView);

            reviewAuthorView = (TextView) itemView.findViewById(R.id.review_author);
            reviewContentView = (TextView) itemView.findViewById(R.id.review_content);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int clickedPosition = getAdapterPosition();
            reviewOnClickListener.onListItemClick(v, clickedPosition);
        }
    }
}
