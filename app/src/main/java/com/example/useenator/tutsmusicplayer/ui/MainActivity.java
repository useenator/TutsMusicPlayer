package com.example.useenator.tutsmusicplayer.ui;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.useenator.tutsmusicplayer.R;
import com.example.useenator.tutsmusicplayer.models.Song;
import com.example.useenator.tutsmusicplayer.service.MusicService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private ArrayList<Song> songList;
    private ListView songListView;
    //  2. Start the Service
    private MusicService musicService;
    private Intent playServiceIntent;
    private boolean musicBoundFlag = false;

    //private MusicController controller;
    private boolean paused = false, playbackPaused = false;

    private TrackChangeReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        songListView = (ListView) findViewById(R.id.song_list);
        songList = new ArrayList<Song>();

        getSongList();

        //sort the list
        Collections.sort(songList, new Comparator<Song>() {
            public int compare(Song a, Song b) {
                return a.getTitle().compareTo(b.getTitle());
            }
        });

        SongAdapter songAdapter = new SongAdapter(this, songList);
        songListView.setAdapter(songAdapter);

        /*
        Back in onCreate, call the method:*/
        setController();

        //miniPlayer
        initLayout();
    }
    //////////////////////////////////// Life cycle ////////////////////////////////////////////
    @Override
    protected void onStart() {
        super.onStart();
        if (playServiceIntent == null) {
            playServiceIntent = new Intent(this, MusicService.class);
            bindService(playServiceIntent, musicServiceConnection, Context.BIND_AUTO_CREATE);
            startService(playServiceIntent);
        }
    }

    @Override
    protected void onDestroy() {
        stopService(playServiceIntent);
        //musicService = null;
       // musicService.onDestroy();
        musicServiceConnection = null;
        if (musicServiceConnection != null) {
            unbindService(musicServiceConnection);
        }


        this.unregisterReceiver(receiver);
        super.onDestroy();
    }

    @Override
    protected void onPause() {

        if (musicServiceConnection != null) {
            unbindService(musicServiceConnection);
        }
        //Now set playbackPaused to true in the pause method:
        paused = true;
        super.onPause();
    }
    @Override
    protected void onResume() {
        super.onResume();
        if (paused) {
            setController();

            bindService(playServiceIntent, musicServiceConnection, Context.BIND_AUTO_CREATE);
            //Now set playbackPaused to true in the pause method:
            paused = false;
        }
    }

    @Override
    protected void onStop() {
        //controller.hide();
        if (musicServiceConnection == null) {
            unbindService(musicServiceConnection);
        }
        super.onStop();
    }




    ///////////////////////////////////////// END life cycle ////////////////////////////////////////////

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

    private void setController() {
        //set the controller up
        //controller = new MusicController(this);

//        controller.setPrevNextListeners(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                playNext();
//            }
//        }, new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                playPrev();
//            }
//        });


//        controller.setMediaPlayer(this);
//        controller.setAnchorView(findViewById(R.id.song_list));
//        controller.setEnabled(true);
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

    public void getSongList() {
        //retrieve song info
        ContentResolver musicResolver = getContentResolver();
        Uri musicUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor musicCursor = musicResolver.query(musicUri, null, null, null, null);

        if (musicCursor != null && musicCursor.moveToFirst()) {
            //get columns
            int titleColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.TITLE);
            int idColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media._ID);
            int artistColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.ARTIST);
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


//    public void songPicked(View view) {
//        musicService.setSong(Integer.parseInt(view.getTag().toString()));
//        musicService.playSong();
//        if (playbackPaused) {
//            setController();
//            playbackPaused = false;
//        }
//        //setController();
//        //controller.show(0);
//        startActivity(new Intent(this, FullActivity.class));
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

    /////////////////////////////////////// MiniPlayer ////////////////////////////////////////////////

    ///////////////////////////////////////// import //////////////////////////////////////////////
    // private Button jumpButton, pauseButton, playButton, jumpBack;

    private double songCurrentTime = 0;
    private double songDurationTime = 0;
    private Handler myHandler = new Handler();


    private SeekBar seekbar;
    private TextView songCurrentTimeTextView, songDurationTimeTextView, songTextView;
    private ImageView songImageViewMiniPlayer;

    public static int oneTimeOnly = 0;
//
//    private ArrayList<Song> songList;
//    private ListView songListView;

    //public static int oneTimeOnly = 0;
    private void initMusicControls() {
        //musicService.startGo();
        //---------------------------------------------------------------------------
        Toast.makeText(getApplicationContext(), "Playing sound", Toast.LENGTH_SHORT).show();
        //musicService.startGo();//mediaPlayexr.start();

        songDurationTime = musicService.getDuration();
        songCurrentTime = musicService.getCurrentPosition();

        if (oneTimeOnly == 0) {
            seekbar.setMax((int) songDurationTime);
            oneTimeOnly = 1;
        }

        songDurationTime = musicService.getDuration();
        songCurrentTime = musicService.getCurrentPosition();

//        if (oneTimeOnly == 0) {
        seekbar.setMax((int) songDurationTime);
//            oneTimeOnly = 1;
//        }
        songDurationTimeTextView.setText(String.format("%d min, %d sec",
                        TimeUnit.MILLISECONDS.toMinutes((long) songDurationTime),
                        TimeUnit.MILLISECONDS.toSeconds((long) songDurationTime) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long) songDurationTime)))
        );


//        songCurrentTimeTextView.setText(String.format("%d min, %d sec",
//                        TimeUnit.MILLISECONDS.toMinutes((long) songCurrentTime),
//                        TimeUnit.MILLISECONDS.toSeconds((long) songCurrentTime) -
//                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long) songCurrentTime)))
//        );

        seekbar.setProgress((int) songCurrentTime);
        myHandler.postDelayed(UpdateSongTimeRunnable, 100);

        //pauseButton.setEnabled(true);
        //playButton.setEnabled(false);
        //setAlbumArtAndTitle();

        //wait for the song to be prepared
        if (!musicService.isPlaying()) {
            //setAlbumArtAndTitle();
        }
        //setAlbumArtAndTitle();

    }
    private void initLayout() {

        //pauseButton.setEnabled(false);
        songDurationTimeTextView = (TextView) findViewById(R.id.songDurationTimeTextViewMiniPlayer);
        songCurrentTimeTextView = (TextView) findViewById(R.id.songCurrentTimeTextViewMiniPlayer);
        songTextView = (TextView) findViewById(R.id.songTextViewMiniPlayer);//songTextView
        songImageViewMiniPlayer = (ImageView) findViewById(R.id.songImageViewMiniPlayer);

        //registerReceiver
        registerBroadReceiverEvent();


        /////////////////////////////// seek bar ////////////////////////////////////////////////
        seekbar = (SeekBar) findViewById(R.id.seekBarMiniPlayer);
        seekbar.setClickable(false);
        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    musicService.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        ///////////////////////////////// listView onClick ///////////////////////////////////////////
        songListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //parent.getItemIdAtPosition(position);
//                musicService.setSong(Integer.parseInt(view.getTag().toString()));

                musicService.setSong(position);
                musicService.playSong();
                if (playbackPaused) {
                    setController();
                    playbackPaused = false;
                }
                //setController();
                //controller.show(0);
                startActivity(new Intent(MainActivity.this, FullActivity.class));
            }
        });
    }

    private void setAlbumArtAndTitle() {
        //////////////////////// extract song albumArt and title ////////////////////////////////
        Song currentSong = musicService.getCurrentSong();
        if (currentSong != null) {
            Drawable songDrawable = Drawable.createFromPath(currentSong.getAlbumnArt());
            songImageViewMiniPlayer.setImageDrawable(songDrawable);
            songTextView.setText(String.format("%s : %s.",
                            currentSong.getTitle(),
                            currentSong.getArtist())
            );//
        }
        //////////////////////// END extract song albumArt and title //////////////////////////////

//        thread= new Thread(new Runnable() {
//            @Override
//            public void run() {
////            int currentPosition = 0;
//                while (!musicThreadFinished) {
//                    try {
//                        Thread.sleep(1000);
//                        currentPosition = getCurrentPosition();
//                    } catch (InterruptedException e) {
//                        return;
//                    } catch (Exception e) {
//                        return;
//                    }
//
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            if (isPlaying()) {
//                                if (!playPauseButton.isChecked()) {
//                                    playPauseButton.setChecked(true);
//                                }
//                            } else {
//                                if (playPauseButton.isChecked()) {
//                                    playPauseButton.setChecked(false);
//                                }
//                            }
//                            musicDuration.setText(totalTime);
//                            musicCurLoc.setText(curTime);
//                        }
//                    });
//                }
//            }
//        }).start();
    }
    ////////////////////////////////////////// runnable //////////////////////////////////////////////////
    private Runnable UpdateSongTimeRunnable = new Runnable() {
        public void run() {
            songCurrentTime = musicService.getCurrentPosition();//.getCurrentPosition();
            songCurrentTimeTextView.setText(String.format("%d min, %d sec",

                            TimeUnit.MILLISECONDS.toMinutes((long) songCurrentTime),
                            TimeUnit.MILLISECONDS.toSeconds((long) songCurrentTime) -
                                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.
                                            toMinutes((long) songCurrentTime)))
            );
//            songDurationTime = musicService.getDuration();
//            songDurationTimeTextView.setText(String.format("%d min, %d sec",
//                            TimeUnit.MILLISECONDS.toMinutes((long) songDurationTime),
//                            TimeUnit.MILLISECONDS.toSeconds((long) songDurationTime) -
//                                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long) songDurationTime)))
//            );
            seekbar.setProgress((int) songCurrentTime);
            myHandler.postDelayed(this, 100);
            //if (MusicService.SENSOR_SERVICE)
        }

    };

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //connect to the service
//    private ServiceConnection musicServiceConnection = new ServiceConnection() {
//
//        @Override
//        public void onServiceConnected(ComponentName name, IBinder service) {
//            MusicService.MusicBinder binder = (MusicService.MusicBinder) service;
//            //get service
//            musicService = binder.getService();
//            ////pass list
//            //musicService.setList(songList);
//            musicBoundFlag = true;
//
//            //init the music controls on the first start of the activity
//            initMusicControls();
//        }
//
//        @Override
//        public void onServiceDisconnected(ComponentName name) {
//            musicBoundFlag = false;
//        }
//    };//END ServiceConnection

    public void nextButton(View view) {
        musicService.playNext();
        initMusicControls();
    }

    public void prevButton(View view) {
        musicService.playPrev();
        initMusicControls();
    }

    public void playButton(View view) {
        musicService.startGo();
        initMusicControls();
    }

    public void pauseButton(View view) {
        musicService.pausePlayer();
    }


    /**
     *  Register event TrackChangeReceiver ;
     */
    private void registerBroadReceiverEvent() {
        // registerReceiver TrackChangeReceiver receiver;
        IntentFilter filter = new IntentFilter(TrackChangeReceiver.ACTION_TRACK_CHANGED);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
         receiver = new TrackChangeReceiver();
        this.registerReceiver(receiver, filter);

    }


    public void openFullPlayer(View view) {
        startActivity(new Intent(this,FullActivity.class));
    }

    /**
     *     Recieve event that track has changed
     */
    private class TrackChangeReceiver extends BroadcastReceiver {
        public static final String ACTION_TRACK_CHANGED =
        "track_changed";

        @Override
        public void onReceive(Context context, Intent intent) {
//            String title = intent.getExtras().getString("title");
//            Song currentSong = musicService.getCurrentSong();
            setAlbumArtAndTitle();
            initMusicControls();

        }

    }

}
