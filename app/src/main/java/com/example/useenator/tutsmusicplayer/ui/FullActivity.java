package com.example.useenator.tutsmusicplayer.ui;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.useenator.tutsmusicplayer.R;
import com.example.useenator.tutsmusicplayer.models.Song;
import com.example.useenator.tutsmusicplayer.service.MusicService;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class FullActivity extends AppCompatActivity {

    //  2. Start the Service
    private MusicService musicService;
    private Intent playServiceIntent;

    ///////////////////////////////////////// import //////////////////////////////////////////////
    // private Button jumpButton, pauseButton, playButton, jumpBack;

    private double songCurrentTime = 0;
    private double songDurationTime = 0;
    private Handler myHandler = new Handler();

    private int forwardTime = 5000;
    private int backwardTime = 5000;
    private SeekBar seekbar;
    private TextView songCurrentTimeTextView, songDurationTimeTextView, songTextView;
    private ImageView songFullPlayerImageView;

    public static int oneTimeOnly = 0;

    private ArrayList<Song> songList;
    private ListView songView;
    private boolean musicBoundFlag = false;
    private TrackChangeReceiver receiver;

    //get the listview
    //private ListView songsListView;

    //////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full);

        initLayout();
    }
    //////////////////////////////////// Life cycle ////////////////////////////////////////////
    @Override
    protected void onStart() {
        super.onStart();
        if (playServiceIntent == null) {
            playServiceIntent = new Intent(this, MusicService.class);
            bindService(playServiceIntent, musicServiceConnection, Context.BIND_AUTO_CREATE);
            //startService(playServiceIntent);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
//        if (playServiceIntent == null) {
//            playServiceIntent = new Intent(this, MusicService.class);
//            startService(playServiceIntent);
//        }
        bindService(playServiceIntent, musicServiceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        //stopService(playServiceIntent);
//        if (playServiceIntent == null) {
            unbindService(musicServiceConnection);
//        }
       // musicService = null;
        this.unregisterReceiver(receiver);
        super.onDestroy();
    }

    ////////////////////////////////////END Life cycle ////////////////////////////////////////////
    //connect to the service
    private ServiceConnection musicServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder) service;
            //get service
            musicService = binder.getService();
            ////pass list
            //musicService.setList(songList);
            musicBoundFlag = true;

            //init the music controls on the first start of the activity
            initMusicControls();
            setAlbumArtAndTitle();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicBoundFlag = false;
        }
    };//END ServiceConnection

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

    ////////////////////////////////////////// initMusicControls //////////////////////////////////////////////////
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

    }



    //paly uri

    private void initLayout() {

        //pauseButton.setEnabled(false);
        songDurationTimeTextView = (TextView) findViewById(R.id.songDurationTimeTextView);
        songCurrentTimeTextView = (TextView) findViewById(R.id.songCurrentTimeTextView);
        songTextView = (TextView) findViewById(R.id.songTextView);//songTextView
        songFullPlayerImageView = (ImageView) findViewById(R.id.songFullPlayerImageView);

        //registerReceiver
        registerBroadReceiverEvent();


        /////////////////////////////// seek bar ////////////////////////////////////////////////
        seekbar = (SeekBar) findViewById(R.id.fullPlayerSeekBar);
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
        ///////////////////////////////// END seek bar///////////////////////////////////////////
    }



    /***
     * extract song albumArt and title
     */
    private void setAlbumArtAndTitle() {
        //////////////////////// extract song albumArt and title ////////////////////////////////
        Song currentSong = musicService.getCurrentSong();
        if (currentSong != null) {
            Drawable songDrawable = Drawable.createFromPath(currentSong.getAlbumnArt());
            songFullPlayerImageView.setImageDrawable(songDrawable);
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
//    @Override
//    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

//    }

    /**
     *  Register event TrackChangeReceiver ;
     */
    private void registerBroadReceiverEvent() {
        // registerReceiver TrackChangeReceiver receiver;
        IntentFilter filter = new IntentFilter(ACTION_TRACK_CHANGED);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        receiver = new TrackChangeReceiver();
        this.registerReceiver(receiver, filter);
    }
//    private void setRefreshRunnable(){
//        updateSongTimeRunnable= new RefreshRunnable();
//        updateSongTimeRunnable.run();
//    }
    /**
     *     Recieve event that track has changed
     */
    public static final String ACTION_TRACK_CHANGED =
            "track_changed";

    public void replayButton(View view) {
    }

    public void shuffleButton(View view) {
        musicService.setShuffle();
    }

    public void openSongsList(View view) {
        //startActivity(new Intent(this,MainActivity.class));
        Intent intent = new Intent(this,MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    private class TrackChangeReceiver extends BroadcastReceiver {


        @Override
        public void onReceive(Context context, Intent intent) {
//            String title = intent.getExtras().getString("title");
//            Song currentSong = musicService.getCurrentSong();
            setAlbumArtAndTitle();
            initMusicControls();

        }

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

    ////////////////////////////////////////////////////////////////////////////////////////////////

}// End ACTIVITY
