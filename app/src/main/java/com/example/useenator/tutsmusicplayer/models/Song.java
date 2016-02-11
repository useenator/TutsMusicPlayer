package com.example.useenator.tutsmusicplayer.models;

/**
 * Created by hmed on 31/01/16.
 */
public class Song {
    private long id;
    private String title;
    private String artist;
    private String mAlbumnArt;

    public Song(long songID, String songTitle, String songArtist) {
        id=songID;
        title=songTitle;
        artist=songArtist;
    }

    public long getID(){return id;}
    public String getTitle(){return title;}
    public String getArtist(){return artist;}
    public String getAlbumnArt() {return mAlbumnArt;}

    public void setAlbumnArt(String albumnArt) {mAlbumnArt = albumnArt;}


}
