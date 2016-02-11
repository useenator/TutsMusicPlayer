package com.example.useenator.tutsmusicplayer.ui;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.useenator.tutsmusicplayer.R;
import com.example.useenator.tutsmusicplayer.models.Song;

import java.util.ArrayList;

/**
 * Created by hmed on 31/01/16.
 */
public class SongAdapter extends BaseAdapter {

    private ArrayList<Song> songs;
    private LayoutInflater songLayoutInflater;
    Context mContext;

    public SongAdapter(Context context, ArrayList<Song> theSongs) {
        songs = theSongs;
        mContext=context;
        songLayoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return songs.size();
    }


    @Override
    public Object getItem(int position) {
        return songs.get(position);
    }

    @Override
    public long getItemId(int position) {
        // TODOo Auto-generated method stub
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //map to song layout
        LinearLayout songLayout = (LinearLayout) songLayoutInflater.inflate
                (R.layout.song, parent, false);


        // we need to test if the view (convertView) is empty(new) or to be recycled
        ViewHolder holder;
        if (convertView == null) {
            //brand new
            convertView = songLayoutInflater.inflate(R.layout.song, null);
            holder = new ViewHolder();
            //get title and artist views
            holder.songView = (TextView) convertView.findViewById(R.id.song_title);
            holder.artistView = (TextView) convertView.findViewById(R.id.song_artist);
            holder.albumArtTextView = (TextView) convertView.findViewById(R.id.song_albumn_art);
            holder.albumArtView = (ImageView) convertView.findViewById(R.id.songImageView);
            convertView.setTag(holder);
            //convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        //get song using position
        Song currSong = songs.get(position);
        //get title and artist strings
        holder.songView.setText(currSong.getTitle());
        holder.artistView.setText(currSong.getArtist());
        holder.albumArtTextView.setText(currSong.getAlbumnArt());

        try {
            Drawable drawable = BitmapDrawable.createFromPath(currSong.getAlbumnArt());
            holder.albumArtView.setImageDrawable(drawable);
        } catch (Exception ignored) {
        }
        //set position as tag
        songLayout.setTag(position);
        return convertView;
    }

    /*
    * ViewHolder
    * */
    private static class ViewHolder {
        TextView songView;
        TextView artistView;
        TextView albumArtTextView;
        ImageView albumArtView;


    }
}