package com.example.popularmovies;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.squareup.picasso.Picasso;
import java.util.List;


public class CustomRecyclerView extends RecyclerView.Adapter<CustomRecyclerView.RecyclerViewHolder> {
    private final Context context;
    private List<Movie> movieList;
    private ItemOnClick itemOnClick;

    public interface ItemOnClick {
        void onClick(int i);

    }

    public CustomRecyclerView(Context context) {
        this.context = context;
    }


    @Override
    public RecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
        return new RecyclerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerViewHolder holder, int position) {
        Movie movie = movieList.get(position);
        Picasso.with(context).load(movie.imageUri).placeholder(R.drawable.ic_image_black_24dp).error(R.drawable.ic_image_black_24dp).into(holder.imageView);
    }


    @Override
    public int getItemCount() {
        if (null == this.movieList) {
            return 0;
        }
        return this.movieList.size();
    }

    public class RecyclerViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView imageView;

        public RecyclerViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.item_image);
            itemView.setOnClickListener(this);

        }

        @Override
        public void onClick(View view) {
            itemOnClick.onClick(getLayoutPosition());
        }
    }

    public void changeData(List<Movie> movieList, ItemOnClick itemOnClick) {
        this.movieList = movieList;
        this.itemOnClick = itemOnClick;
        notifyDataSetChanged();
    }

}
