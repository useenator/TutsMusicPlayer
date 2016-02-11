package com.example.useenator.tutsmusicplayer.arch;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.MediaController;

import com.example.useenator.tutsmusicplayer.ui.FullActivity;
import com.example.useenator.tutsmusicplayer.service.MusicService;
import com.example.useenator.tutsmusicplayer.R;
import com.example.useenator.tutsmusicplayer.models.Song;
import com.example.useenator.tutsmusicplayer.ui.SongAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class MainActivityOld extends AppCompatActivity implements MediaController.MediaPlayerControl {

    private ArrayList<Song> songList;
    private ListView songView;
    //  2. Start the Service
    private MusicService musicService;
    private Intent playServiceIntent;
    private boolean musicBoundFlag = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        songView = (ListView) findViewById(R.id.song_list);
        songList = new ArrayList<Song>();

        getSongList();

        //sort the list
        Collections.sort(songList, new Comparator<Song>() {
            public int compare(Song a, Song b) {
                return a.getTitle().compareTo(b.getTitle());
            }
        });

        SongAdapter songAdapter = new SongAdapter(this, songList);
        songView.setAdapter(songAdapter);

        /*
        Back in onCreate, call the method:*/
        setController();
    }

    /*
    2.Step 1
    Back in your app's main Activity class, you will need to add the following additional imports:

     We are going to play the music in the Service class, but control it from the Activity class,
     where the application's user interface operates. To accomplish this, we will have to bind
     to the Service class. The above instance variables represent the Service class and Intent,
     as well as a flag to keep track of whether the Activity class is bound to the Service
     class or not. Add the following to your Activity class, after the onCreate method:
     */
    //connect to the service
    protected  ServiceConnection musicServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder) service;
            //get service
            musicService = binder.getService();
            //pass list
            musicService.setList(songList);
            musicBoundFlag = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicBoundFlag = false;
        }
    };//END ServiceConnection

    /*
    2.Step 2
    We will want to start the Service instance when the Activity instance starts,
    so override the onStart method:
     */
    @Override
    protected void onStart() {
        super.onStart();
        if (playServiceIntent == null) {
            playServiceIntent = new Intent(this, MusicService.class);
            bindService(playServiceIntent, musicServiceConnection, Context.BIND_AUTO_CREATE);
            startService(playServiceIntent);
        }
    }

    public void getSongListOld() {
        //retrieve song info
        ContentResolver musicResolver = getContentResolver();
        Uri musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor musicCursor = musicResolver.query(musicUri, null, null, null, null);

        if (musicCursor != null && musicCursor.moveToFirst()) {
            //get columns
            int titleColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media.TITLE);
            int idColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media._ID);
            int artistColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media.ARTIST);


            //add songs to list
            do {
                long thisId = musicCursor.getLong(idColumn);
                String thisTitle = musicCursor.getString(titleColumn);
                String thisArtist = musicCursor.getString(artistColumn);
                songList.add(new Song(thisId, thisTitle, thisArtist));
            }
            while (musicCursor.moveToNext());
        }
    }

    public void getSongList() {
        //retrieve song info
        ContentResolver musicResolver = getContentResolver();
        Uri musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor musicCursor = musicResolver.query(musicUri, null, null, null, null);

        if (musicCursor != null && musicCursor.moveToFirst()) {
            //get columns
            int titleColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media.TITLE);
            int idColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media._ID);
            int artistColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media.ARTIST);
            int albumnIDColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media.ALBUM_ID);

            //add songs to list
            do {
                long thisId = musicCursor.getLong(idColumn);
                String thisTitle = musicCursor.getString(titleColumn);
                String thisArtist = musicCursor.getString(artistColumn);
                String thisAlbumnID = musicCursor.getString(albumnIDColumn);
                // get the album art
                //Uri musicAlbumnUri = MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI;
                Uri musicAlbumnUri = Uri.withAppendedPath(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, "" + thisAlbumnID);
                Cursor musicAlbumnCursor = musicResolver
                        .query(musicAlbumnUri,//SELECT album_art FROM album_info WHERE (album_id=?)
                                null,//new String[]{MediaStore.Audio.Albums.ALBUM_ID, MediaStore.Audio.Albums.ALBUM_ART},
                                null,//MediaStore.Audio.Albums.ALBUM_ID + "=?",
                                null,//new String[]{String.valueOf(albumnIDColumn)},
                                null);

                if (musicAlbumnCursor != null&& musicAlbumnCursor.moveToFirst()) {

                    String thisAlbumnArt = musicAlbumnCursor.getString(
                            musicAlbumnCursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART));


                    //fill the list
                    Song thisSong = new Song(thisId, thisTitle, thisArtist);
                    thisSong.setAlbumnArt(thisAlbumnArt);
                    songList.add(thisSong);
                }//END if
            }
            while (musicCursor.moveToNext());
        }
    }

    /*
    3. Step 3
    Remember that we added an onClick attribute to the layout for each item in the song list.
    Add that method to the main Activity class:
     */
//    public void songPicked(View view){
//        musicService.setSong(Integer.parseInt(view.getTag().toString()));
//        musicService.playSong();
//    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //menu item
        switch (item.getItemId()) {
            case R.id.action_end:
                stopService(playServiceIntent);
                musicService = null;
                System.exit(0);
                break;
            case R.id.action_shuffle:
                musicService.setShuffle();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /*
    Pressing the back button will not exit the app,
    since we will assume that the user wants playback to continue unless they select
    the end button. Use the same process if the the app is destroyed,
    overriding the activity's onDestroy method:
     */
    @Override
    protected void onDestroy() {
        stopService(playServiceIntent);
        musicService = null;
        super.onDestroy();
    }

    /*
    Step 3
    Back in your main Activity class, add a new instance variable:
     */
    private MusicController controller;

    /*
    We will be setting the controller up more than once in the life cycle of the app,
    so let's do it in a helper method. Add the following code snippet to your Activity class
     */
    private void setController() {
        //set the controller up
        controller = new MusicController(this);

        /*
        You can configure various aspects of the MediaController instance. For example,
        we will need to determine what will happen when the user presses the previous/next buttons. After instantiating the controller set these click listeners:
         */
        controller.setPrevNextListeners(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playNext();
            }
        }, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playPrev();
            }
        });

        /*
        We will implement playNext and playPrev a bit later, so just ignore the errors for now.
        Still inside the setController method, set the controller to work on media
        playback in the app, with its anchor view referring to the list we included in the layout:
         */
        controller.setMediaPlayer(this);
        controller.setAnchorView(findViewById(R.id.song_list));
        controller.setEnabled(true);
    }
    //play next
//    private void playNext(){
//        musicService.playNext();
//        controller.show(0);
//    }

    //play previous
//    private void playPrev(){
//        musicService.playPrev();
//        controller.show(0);
//    }

    /*
    We call the methods we added to the Service class. We will be adding more code to these later
    to take care of particular situations. Now let's turn to the MediaPlayerControl interface
    methods, which will be called by the system during playback and when the user interacts with
    the controls. These methods should already be in your Activity class, so we will just be
    altering their implementation.
     */
    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public boolean canSeekBackward() {
        return true;
    }

    @Override
    public boolean canSeekForward() {
        return true;
    }

    @Override
    public int getCurrentPosition() {
        if (musicService != null && musicBoundFlag && musicService.isPlaying())
            return musicService.getCurrentPosition();
        else return 0;
    }

    @Override
    public int getDuration() {
        if (musicService != null && musicBoundFlag && musicService.isPlaying())
            return musicService.getDuration();
        else return 0;
    }

    @Override
    public boolean isPlaying() {
        if (musicService != null && musicBoundFlag)
            return musicService.isPlaying();
        return false;
    }
//    @Override
//    public void pause() {
//        musicService.pausePlayer();
//    }

    @Override
    public void seekTo(int pos) {
        musicService.seekTo(pos);
    }

    @Override
    public void start() {
        musicService.startGo();
    }


    @Override
    public int getBufferPercentage() {
        return 0;
    }


    @Override
    public int getAudioSessionId() {
        return 0;
    }

    /*
    5. Tidy Up

    Step 1
    We are almost done, but still need to add a few bits of processing to take care
    of certain changes,
    such as the user leaving the app or pausing playback. In your Activity class,
    add a couple more instance variables:
     */
    private boolean paused = false, playbackPaused = false;

    /*
    We will use these to cope with the user returning to the app after leaving it and
    interacting with the controls when playback itself is paused. Override onPause
    to set one of these flags:
     */
    @Override
    public void pause() {
        playbackPaused = true;
        musicService.pausePlayer();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (paused) {
            setController();
            //Now set playbackPaused to true in the pause method:
            paused = false;
        }
    }

    @Override
    protected void onStop() {
        controller.hide();
        super.onStop();
    }

    private void playNext() {
        musicService.playNext();
        if (playbackPaused) {
            setController();
            playbackPaused = false;
        }
        controller.show(0);
    }

    /*
    Step 2
    If the user interacts with the controls while playback is paused, the MediaPlayer object may behave unpredictably. To cope with this, we will set and use the playbackPaused flag. First amend the playNext and playPrev methods:
     */
    private void playPrev() {
        musicService.playPrev();
        if (playbackPaused) {
            setController();
            playbackPaused = false;
        }
        controller.show(0);
    }

    /*
    We reset the controller and update the playbackPaused flag when playback has been paused. Now make similar changes to the playSong method:
     */
    public void songPicked(View view) {
        musicService.setSong(Integer.parseInt(view.getTag().toString()));
        musicService.playSong();
        if (playbackPaused) {
            setController();
            playbackPaused = false;
        }
        //setController();
        controller.show(0);
        startActivity(new Intent(this, FullActivity.class));
    }
    /*
    As you work with the MediaPlayer and MediaController classes, you will find that this type of processing is a necessary requirement to avoid errors. For example, you will sometimes find that the controller's seekTo bar does not update until the user interacts with it. These resources behave differently on different API levels, so thorough testing and tweaking is essential if you plan on releasing your app to the public. The app we are creating in this series is really only a foundation.
     */

    @Override
    protected void onPause() {

//        if (musicServiceConnection != null) {
//            unbindService(musicServiceConnection);
//        }
        super.onPause();
    }
}
