package com.example.useenator.tutsmusicplayer.arch;


import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;


import android.media.MediaPlayer;

import android.os.Handler;

import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.useenator.tutsmusicplayer.service.MusicService;
import com.example.useenator.tutsmusicplayer.R;
import com.example.useenator.tutsmusicplayer.models.Song;
import com.example.useenator.tutsmusicplayer.ui.SongAdapter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;


public class PlayFullScreenActivity extends AppCompatActivity {
    private Button jumpButton, pauseButton, playButton, jumpBack;
    private ImageView iv;
    private MediaPlayer mediaPlayer;
    private double startTime = 0;
    private double finalTime = 0;
    private Handler myHandler = new Handler();

    private int forwardTime = 5000;
    private int backwardTime = 5000;
    private SeekBar seekbar;
    private TextView tx1, tx2, songTextView;

    public static int oneTimeOnly = 0;

    private ArrayList<Song> songList;
    private ListView songView;
    //  2. Start the Service
    private MusicService musicService;
    private Intent playServiceIntent;
    private boolean musicBoundFlag = false;

    //get the listview
    private ListView songsListView;


    private List<String> getSongsList() {
        //Some audio may be explicitly marked as not being music
        String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";

        String[] projection = {
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.DURATION
        };
//      Cursor cursor = contentResolver.query(uri, null, null, null, null);
        Cursor cursor = this.managedQuery(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                null,
                null);

        List<String> songs = new ArrayList<String>();
        List<String> songsIds = new ArrayList<String>();
        while (cursor.moveToNext()) {
            songs.add(cursor.getString(0) + "||"
                    + cursor.getString(1) + "||"
                    + cursor.getString(2) + "||"
                    + cursor.getString(3) + "||"
                    + cursor.getString(4) + "||"
                    + cursor.getString(5));

            songsIds.add(cursor.getString(0));
        }

        //Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

        //Uri.withAppendedPath(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, "" + id)

        return songsIds;
    }

    private void getTrackInfo(Uri audioFileUri) {
        Uri fileUri = Uri.fromFile(new File("/sdcard/cats.jpg"));
        //-----------------------------------------
        MediaMetadataRetriever metaRetriever = new MediaMetadataRetriever();
        metaRetriever.setDataSource(getRealPathFromURIHelper(audioFileUri));
        String artist = metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
        String title = metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);

    }

    private String getRealPathFromURIHelper(Uri uri) {
        File myFile = new File(uri.getPath().toString());
        String s = myFile.getAbsolutePath();
        return s;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_full_screen);
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
//
//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });

//fill songslistview
//        songsListView = (ListView) findViewById(R.id.musicListView);
//        ArrayList<String> songs = (ArrayList<String>) getSongsList();
//        ArrayAdapter adapter = new ArrayAdapter(this,
//                android.R.layout.simple_list_item_1,
//                android.R.id.text1,
//                songs);


//        songsListView.setAdapter(adapter);
//        songsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                final String item = (String) parent.getItemAtPosition(position);
//                Uri uri = Uri.withAppendedPath(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, "" + item);
//                PlayFullScreenActivity.this.play(uri);
//
//                if (mediaPlayer.isPlaying()) {
//                    //stopSong();
//                    startSong();
//                } else {
//                    startSong();
//                }
////               view.animate().setDuration(2000).alpha(0)
////                       .withEndAction(new Runnable() {
////                           @Override
////                           public void run() {
////                               list.remove(item);
////                               adapter.notifyDataSetChanged();
////                               view.setAlpha(1);
////                           }
////                       });
//            }
//        });
//----------------------------------------------------------------------------------------------
        jumpButton = (Button) findViewById(R.id.button);
        pauseButton = (Button) findViewById(R.id.button2);
        playButton = (Button) findViewById(R.id.button3);
        jumpBack = (Button) findViewById(R.id.button4);
        iv = (ImageView) findViewById(R.id.imageView);

        tx1 = (TextView) findViewById(R.id.textView2);
        tx2 = (TextView) findViewById(R.id.textView3);
//        songTextView = (TextView) findViewById(R.id.textView4);
//        songTextView.setText("Song.mp3");
        //play
        //play();

        /////////////////////////////plaFullScreen/////////////////////////////////////:
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
//        songView.setAdapter(songAdapter);

        /*
        Back in onCreate, call the method:*/
        //setController();

    }




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
    //connect to the service
    private ServiceConnection musicServiceConnection = new ServiceConnection() {

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

    //paly uri
    private void splay(Uri uri) {
        //song
        //mediaPlayer = MediaPlayer.create(this, uri);

        //       mediaplay
        try {
            if (mediaPlayer != null) {
                mediaPlayer.release();
                mediaPlayer = null;
            }
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setDataSource(getApplicationContext(), uri);
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }


        seekbar = (SeekBar) findViewById(R.id.fullPlayerSeekBar);
        seekbar.setClickable(false);
        pauseButton.setEnabled(false);

        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startSong();
            }
        });

        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pauseSong();
            }
        });

        jumpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                jumpForward();
            }
        });

        jumpBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                jumpBackward();
            }
        });
    }

    private void startSong() {
        Toast.makeText(getApplicationContext(), "Playing sound", Toast.LENGTH_SHORT).show();
        //mediaPlayexr.start();
        musicService.playSong();

        finalTime = musicService.getDuration();
        startTime = musicService.getCurrentPosition();
        //MediaPlayer.TrackInfo[] trackInfo = mediaPlayer.getTrackInfo();


        songTextView.setText("Song.mp3");

        if (oneTimeOnly == 0) {
            seekbar.setMax((int) finalTime);
            oneTimeOnly = 1;
        }
        tx2.setText(String.format("%d min, %d sec",
                        TimeUnit.MILLISECONDS.toMinutes((long) finalTime),
                        TimeUnit.MILLISECONDS.toSeconds((long) finalTime) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long) finalTime)))
        );

        tx1.setText(String.format("%d min, %d sec",
                        TimeUnit.MILLISECONDS.toMinutes((long) startTime),
                        TimeUnit.MILLISECONDS.toSeconds((long) startTime) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long) startTime)))
        );

        seekbar.setProgress((int) startTime);
        myHandler.postDelayed(UpdateSongTime, 100);
        pauseButton.setEnabled(true);
        playButton.setEnabled(false);
    }

    private void jumpBackward() {
        int temp = (int) startTime;

        if ((temp - backwardTime) > 0) {
            startTime = startTime - backwardTime;
            musicService.seekTo((int) startTime);
            Toast.makeText(getApplicationContext(), "You have Jumped backward 5 seconds", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getApplicationContext(), "Cannot jump backward 5 seconds", Toast.LENGTH_SHORT).show();
        }
        playPrev();
    }

    private void jumpForward() {
        int temp = (int) startTime;

        if ((temp + forwardTime) <= finalTime) {
            startTime = startTime + forwardTime;
            musicService.seekTo((int) startTime);
            Toast.makeText(getApplicationContext(), "You have Jumped forward 5 seconds", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getApplicationContext(), "Cannot jump forward 5 seconds", Toast.LENGTH_SHORT).show();
        }
        playNext();
    }

    private void pauseSong() {
        Toast.makeText(getApplicationContext(), "Pausing sound", Toast.LENGTH_SHORT).show();


        pauseButton.setEnabled(false);
        playButton.setEnabled(true);

        musicService.pausePlayer();
    }





    private Runnable UpdateSongTime = new Runnable() {
        public void run() {
            startTime = musicService.getCurrentPosition();//.getCurrentPosition();
            tx1.setText(String.format("%d min, %d sec",

                            TimeUnit.MILLISECONDS.toMinutes((long) startTime),
                            TimeUnit.MILLISECONDS.toSeconds((long) startTime) -
                                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.
                                            toMinutes((long) startTime)))
            );
            seekbar.setProgress((int) startTime);
            myHandler.postDelayed(this, 100);
        }
    };


////////////////////////////////////////////////////////////////////////////////////////////////////





    /*
2.Step 2
We will want to start the Service instance when the Activity instance starts,
so override the onStart method:
 */
    @Override
    protected void onStart() {
        super.onStart();
//        if (playServiceIntent == null) {
//            playServiceIntent = new Intent(this, MusicService.class);
//            bindService(playServiceIntent, musicServiceConnection, Context.BIND_AUTO_CREATE);
//
//           startService(playServiceIntent);
//        }
    }
    @Override
    protected void onPause() {

//        if (musicServiceConnection != null) {
//            unbindService(musicServiceConnection);
//        }
        super.onPause();
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





    private void playNext() {
        musicService.playNext();
        if (playbackPaused) {
            //setController();
            playbackPaused = false;
        }
        //controller.show(0);
    }

    /*
    Step 2
    If the user interacts with the controls while playback is paused, the MediaPlayer object may behave unpredictably. To cope with this, we will set and use the playbackPaused flag. First amend the playNext and playPrev methods:
     */
    private void playPrev() {
        musicService.playPrev();
        if (playbackPaused) {
            //setController();
            playbackPaused = false;
        }
        //controller.show(0);
    }

    /*
    We reset the controller and update the playbackPaused flag when playback has been paused. Now make similar changes to the playSong method:
     */
    public void songPicked(View view) {
        musicService.setSong(Integer.parseInt(view.getTag().toString()));
        musicService.playSong();
        if (playbackPaused) {
            //setController();
            playbackPaused = false;
        }
        //setController();
        //controller.show(0);
    }
    /*
    As you work with the MediaPlayer and MediaController classes, you will find that this type of processing is a necessary requirement to avoid errors. For example, you will sometimes find that the controller's seekTo bar does not update until the user interacts with it. These resources behave differently on different API levels, so thorough testing and tweaking is essential if you plan on releasing your app to the public. The app we are creating in this series is really only a foundation.
     */






    //////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        int id = item.getItemId();

        //noinspection SimplifiableIfStatement

        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}