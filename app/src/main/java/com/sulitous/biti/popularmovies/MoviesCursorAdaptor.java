package com.sulitous.biti.popularmovies;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

class MoviesCursorAdaptor extends CursorAdapter{

    private Context mContext;
    private static int sLoaderID;

    MoviesCursorAdaptor(Context context, Cursor c, int flags, int loaderID) {
        super(context, c, flags);
        mContext = context;
        sLoaderID = loaderID;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.list_item_poster,viewGroup,false);
        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder viewHolder = (ViewHolder) view.getTag();
        Movie movie = new Movie(cursor);
        viewHolder.posterView.setContentDescription(movie.original_title);
        Picasso.get().load(movie.getPoster_path()).placeholder(R.drawable.ic_file_download_white_48dp).error(R.drawable.ic_cloud_off_black_36px).into(viewHolder.posterView);
    }

    private static class ViewHolder{
        final ImageView posterView;

        ViewHolder(View view){
            posterView = (ImageView) view.findViewById(R.id.list_item_thumb);
        }

    }
}
