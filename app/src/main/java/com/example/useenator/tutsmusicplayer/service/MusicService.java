package com.example.useenator.tutsmusicplayer.service;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.RemoteControlClient;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import com.example.useenator.tutsmusicplayer.NotificationBroadcast;
import com.example.useenator.tutsmusicplayer.PlayerConstants;
import com.example.useenator.tutsmusicplayer.UtilFunctions;
import com.example.useenator.tutsmusicplayer.ui.FullActivity;
import com.example.useenator.tutsmusicplayer.ui.MainActivity;
import com.example.useenator.tutsmusicplayer.R;
import com.example.useenator.tutsmusicplayer.models.Song;

import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;

public class MusicService extends Service implements
        MediaPlayer.OnPreparedListener,
        MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener,
        AudioManager.OnAudioFocusChangeListener{

    private static boolean SONG_READY = false;
    //media player
    private MediaPlayer player;
    //song list
    private ArrayList<Song> songs;
    //current position
    private int songPosn;

    private Song mPlaySong;
/*
    Step 1
    Remember that we added a shuffle button,
    so let's implement that now. First add new instance variables to the Service class:
            */
    private boolean shuffle=false;
    private Random rand;


    public MusicService() {
    }

    public void onCreate() {
        //create the service
        super.onCreate();
        //initialize position
        songPosn = 0;
        //create player
        player = new MediaPlayer();

     /*
    Instantiate the random number generator in onCreate:
     */
        rand=new Random();

     /* Notice that these correspond to the interfaces we implemented. We will be adding code
        to the onPrepared, onCompletion, and onError methods to respond to these events.
        Back in onCreate, invoke initMusicPlayer:
      */
        initMusicPlayer();
        /**from mplayer*/
        initOnCreate();
    }

    public void initMusicPlayer() {
        //set player properties
        player.setWakeMode(getApplicationContext(),
                PowerManager.PARTIAL_WAKE_LOCK);
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);

     /* The wake lock will let playback continue when the device becomes idle and we set
        the stream type
        to music. Set the class as listener for (1) when the MediaPlayer instance is prepared,
        (2) when a song has completed playback, and when (3) an error is thrown:*/

        player.setOnPreparedListener(this);
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);
    }

    public void setList(ArrayList<Song> theSongs) {
        songs = theSongs;
    }

    /*
    2.step2
    Return to your Service class to complete this binding process.
    Add an instance variable representing the inner Binder class we added:
     */
    private final IBinder musicBind = new MusicBinder();

    @Override
    public IBinder onBind(Intent intent) {return musicBind;}


    @Override
    public boolean onUnbind(Intent intent) {
    /*
        Add the onUnbind method to release resources when the Service instance is unbound:
     */
//        player.stop();
//        player.release();
        //super.onUnbind(intent);
        return false;
    }

    /*
    3. Begin Playback

    In order for the user to select songs,
    we also need a method in the Service class to set the current song. Add it now:
    */
    public void setSong(int songIndex) {songPosn = songIndex;}

    /*
      Step 1
      Let's now set the app up to play a track. In your Service class, add the following method:
    */
    public void playSong() {
        //play a song
        //reset song
        player.reset();
  /*
     Next, get the song from the list, extract the ID for it using its Song object,
     and model this as a URI:
   */
        //get song
        Song playSong = songs.get(songPosn);
        //mPlaySong=playSong;
        songTitle=playSong.getTitle();
        //get id
        long currSong = playSong.getID();
        //set uri
        Uri trackUri = ContentUris.withAppendedId(
                android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                currSong);
   /*
    We can now try setting this URI as the data source for the MediaPlayer instance,
    but an exception may be thrown if an error pops up so we use a try/catch block:
   */
        try {
            player.setDataSource(getApplicationContext(), trackUri);
        } catch (Exception e) {
            Log.e("MUSIC SERVICE", "Error setting data source", e);
        }
  /*
    After the catch block, complete the playSong method by calling the asynchronous method of
    the MediaPlayer to prepare it:
  */
        player.prepareAsync();

        /**from mplayer*/
        if(currentVersionSupportLockScreenControls){
            UpdateMetadata(playSong);
            remoteControlClient.setPlaybackState(RemoteControlClient.PLAYSTATE_PLAYING);
        }
    }//END playSong

    public Song getCurrentSong(){
        return mPlaySong;
    }

    /*
        Step 2
        When the MediaPlayer is prepared, the onPrepared method will be executed.
        Eclipse should have inserted it in your Service class. Inside this method, start the playback:
     */
    private String songTitle="";
    private static final int NOTIFY_ID=1;

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onPrepared(MediaPlayer mp) {
        //start playback
        mp.start();

        /*
        Now move to the onPrepared method, in which we currently simply start the playback.
        After the call to player.start(), add the following code:
         */
//        Intent notIntent = new Intent(this, MainActivity.class);
//        notIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//        PendingIntent pendInt = PendingIntent.getActivity(this, 0,
//                notIntent, PendingIntent.FLAG_UPDATE_CURRENT);
//
//        Notification.Builder builder = new Notification.Builder(this);
//
//        builder.setContentIntent(pendInt)
//                .setSmallIcon(R.drawable.android_music_player_play)
//                .setTicker(songTitle)
//                .setOngoing(true)
//                .setContentTitle("Playing")
//                .setContentText(songTitle);
//        Notification notification = builder.build();
//
//        startForeground(NOTIFY_ID, notification);


        //get song
        Song playSong = songs.get(songPosn);
        mPlaySong=playSong;
        //show status bar pending notification
        //TODO notification
        //notification();
        //broadcastIntent
        sendBroadCast();

        /**FROM MPLAYER*/
        newNotification();
    }

    /**
     *     show status bar pending notification
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void notification(){
        //Notification
        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification.Builder builder = new Notification.Builder(this);

        builder.setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.android_music_player_play)
                .setTicker(songTitle)
                .setOngoing(true)
                .setContentTitle("Playing")
                .setContentText(songTitle);
        Notification notification = builder.build();

        startForeground(NOTIFY_ID, notification);


    }

    /**
     *     Send event that track has changed
     */
    public void sendBroadCast(){
        //BroadCast sender
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(FullActivity.ACTION_TRACK_CHANGED);
        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
//        broadcastIntent.putExtra("title", mPlaySong.getTitle());
//        broadcastIntent.putExtra("art_album", mPlaySong.getAlbumnArt());
//        broadcastIntent.putExtra("artist", mPlaySong.getArtist());
        sendBroadcast(broadcastIntent);
    }

    /**
    Since we have called setForeground on the notification, we need to make sure we stop it when
    the Service instance is destroyed. Override the following method:
     */
    @Override
    public void onDestroy() {
        stopForeground(true);
        super.onDestroy();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if(player.getCurrentPosition()>0){//try player.getCurrentPosition()>=0
            mp.reset();
            playNext();
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        mp.reset();
        return false;
    }


    /*
    2. Implement Playback Control

    Step 1
    Remember that the media playback is happening in the Service class, but that the user interface
    comes from the Activity class. In the previous tutorial, we bound the Activity instance to
    the Service instance, so that we could control playback from the user interface.
    The methods in our Activity class that we added to implement the MediaPlayerControl
    interface will be called when the user attempts to control playback.
    We will need the Service class to act on this control,
    so open your Service class now to add a few more methods to it:
     */
    public int getCurrentPosition() {return player.getCurrentPosition();}

    public int getDuration() {return player.getDuration();}

    public boolean isPlaying() {
        return player.isPlaying();
    }

    public void pausePlayer() {
        player.pause();
    }

    public void seekTo(int posn) {
        player.seekTo(posn);
    }

    public void startGo() {
        player.start();
    }

    /*
    Step 2
    Now let's add methods to the Service class for skipping to the next and previous tracks.
    Start with the previous function:
     */
    public void playPrev() {
        songPosn--;
        if (songPosn < 0) songPosn = songs.size() - 1;
        playSong();
    }

    //skip to next
//    public void playNext() {
//        songPosn++;
//        if (songPosn >= songs.size()) songPosn = 0;
//        playSong();
//    }

    /*
    4. Shuffle Playback

    Step 1
    Remember that we added a shuffle button,
    so let's implement that now. First add new instance variables to the Service class:
     */


    public void setShuffle(){
        if(shuffle) shuffle=false;
        else shuffle=true;
    }

    /*
    We will simply toggle the shuffle setting on and off. We will check this flag when
    the user either skips to the next track or when a track ends and the next one begins.
    Amend the playNext method as follows:
     */
    public void playNext(){
        if(shuffle){
            int newSong = songPosn;
            while(newSong==songPosn){
                newSong=rand.nextInt(songs.size());
            }
            songPosn=newSong;
        }
        else{
            songPosn++;
            if(songPosn>=songs.size()) songPosn=0;
        }
        SONG_READY=false;
        playSong();
    }



    //-------------------------------------------------------------------------------------------
    /*
        * We will call this method later in the tutorial. This will form part of the interaction
        * between the Activity and Service classes, for which we also need a Binder instance.
        * Add the following snippet to the Service class after the setList method:
    */
    public class MusicBinder extends android.os.Binder {
        public MusicService getService() {
            return MusicService.this;
        }
    }






    //////////////////////////////  lockScreen From MPLAYER///////////////////////////////////////:
    private ComponentName remoteComponentName;
    private RemoteControlClient remoteControlClient;
    AudioManager audioManager;
    Bitmap mDummyAlbumArt;
    private static Timer timer;
    private static boolean currentVersionSupportBigNotification = false;
    private static boolean currentVersionSupportLockScreenControls = false;
    //todo change package path
    public static final String NOTIFY_PREVIOUS = "com.tutorialsface.audioplayer.previous";
    public static final String NOTIFY_DELETE = "com.tutorialsface.audioplayer.delete";
    public static final String NOTIFY_PAUSE = "com.tutorialsface.audioplayer.pause";
    public static final String NOTIFY_PLAY = "com.tutorialsface.audioplayer.play";
    public static final String NOTIFY_NEXT = "com.tutorialsface.audioplayer.next";
    int NOTIFICATION_ID = 1111;

    void initOnCreate(){
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        currentVersionSupportBigNotification = UtilFunctions.currentVersionSupportBigNotification();
        currentVersionSupportLockScreenControls = UtilFunctions.currentVersionSupportLockScreenControls();

        if(currentVersionSupportLockScreenControls){
            RegisterRemoteClient();
        }
    }

    @SuppressLint("NewApi")
    private void RegisterRemoteClient(){
        remoteComponentName = new ComponentName(getApplicationContext(),
                                                new NotificationBroadcast().ComponentName());
        try {
            if(remoteControlClient == null) {
                audioManager.registerMediaButtonEventReceiver(remoteComponentName);
                Intent mediaButtonIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
                mediaButtonIntent.setComponent(remoteComponentName);
                PendingIntent mediaPendingIntent = PendingIntent.getBroadcast(this, 0, mediaButtonIntent, 0);
                remoteControlClient = new RemoteControlClient(mediaPendingIntent);
                audioManager.registerRemoteControlClient(remoteControlClient);
            }
            remoteControlClient.setTransportControlFlags(
                    RemoteControlClient.FLAG_KEY_MEDIA_PLAY |
                            RemoteControlClient.FLAG_KEY_MEDIA_PAUSE |
                            RemoteControlClient.FLAG_KEY_MEDIA_PLAY_PAUSE |
                            RemoteControlClient.FLAG_KEY_MEDIA_STOP |
                            RemoteControlClient.FLAG_KEY_MEDIA_PREVIOUS |
                            RemoteControlClient.FLAG_KEY_MEDIA_NEXT);
        }catch(Exception ex) {
        }
    }




    @SuppressLint("NewApi")
    private void UpdateMetadata(Song song){
        if (remoteControlClient == null)
            return;
        RemoteControlClient.MetadataEditor metadataEditor = remoteControlClient.editMetadata(true);
        //todo dummy album
        metadataEditor.putString(MediaMetadataRetriever.METADATA_KEY_ALBUM, "DUMMY ALBUM");
        metadataEditor.putString(MediaMetadataRetriever.METADATA_KEY_ARTIST, song.getArtist());
        metadataEditor.putString(MediaMetadataRetriever.METADATA_KEY_TITLE, song.getTitle());
        mDummyAlbumArt =UtilFunctions.getAlbumart(getApplicationContext(), song.getAlbumnArt());
        if(mDummyAlbumArt == null){
            mDummyAlbumArt = BitmapFactory.decodeResource(getResources(), R.drawable.default_album_art);
        }
        metadataEditor.putBitmap(RemoteControlClient.MetadataEditor.BITMAP_KEY_ARTWORK, mDummyAlbumArt);
        metadataEditor.apply();
        audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
    }

    /**
     * Notification
     * Custom Bignotification is available from API 16
     */
    @SuppressLint("NewApi")
    private void newNotification() {
        String songName = mPlaySong.getTitle();
        //todo change dummy albumArt
        String albumName = "DummyAlbum";//mPlaySong.getAlbum();
        RemoteViews simpleContentRemoteView = new RemoteViews(getApplicationContext().getPackageName(),R.layout.custom_notification);
        RemoteViews expandedContentRemoteView = new RemoteViews(getApplicationContext().getPackageName(), R.layout.big_notification);

        Notification notification = new NotificationCompat.Builder(getApplicationContext())
                .setSmallIcon(R.drawable.ic_music)
                .setContentTitle(songName).build();

        setListeners(simpleContentRemoteView);
        setListeners(expandedContentRemoteView);

        notification.contentView = simpleContentRemoteView;
        if(currentVersionSupportBigNotification){
            notification.bigContentView = expandedContentRemoteView;
        }

        try{
            //long albumId = PlayerConstants.SONGS_LIST.get(PlayerConstants.SONG_NUMBER).getAlbumId();
            Bitmap albumArt = UtilFunctions.getAlbumart(this,mPlaySong.getAlbumnArt());
            if(albumArt != null){
                notification.contentView.setImageViewBitmap(R.id.imageViewAlbumArt, albumArt);
                if(currentVersionSupportBigNotification){
                    notification.bigContentView.setImageViewBitmap(R.id.imageViewAlbumArt, albumArt);
                }
            }else{
                notification.contentView.setImageViewResource(R.id.imageViewAlbumArt, R.drawable.default_album_art);
                if(currentVersionSupportBigNotification){
                    notification.bigContentView.setImageViewResource(R.id.imageViewAlbumArt, R.drawable.default_album_art);
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }//todo notification controls visibility
        if(!isPlaying()){//PlayerConstants.SONG_PAUSED){
            notification.contentView.setViewVisibility(R.id.btnPause, View.GONE);
            notification.contentView.setViewVisibility(R.id.btnPlay, View.VISIBLE);

            if(currentVersionSupportBigNotification){
                notification.bigContentView.setViewVisibility(R.id.btnPause, View.GONE);
                notification.bigContentView.setViewVisibility(R.id.btnPlay, View.VISIBLE);
            }
        }else{
            notification.contentView.setViewVisibility(R.id.btnPause, View.VISIBLE);
            notification.contentView.setViewVisibility(R.id.btnPlay, View.GONE);

            if(currentVersionSupportBigNotification){
                notification.bigContentView.setViewVisibility(R.id.btnPause, View.VISIBLE);
                notification.bigContentView.setViewVisibility(R.id.btnPlay, View.GONE);
            }
        }

        notification.contentView.setTextViewText(R.id.textSongName, songName);
        notification.contentView.setTextViewText(R.id.textAlbumName, albumName);
        if(currentVersionSupportBigNotification){
            notification.bigContentView.setTextViewText(R.id.textSongName, songName);
            notification.bigContentView.setTextViewText(R.id.textAlbumName, albumName);
        }
        notification.flags |= Notification.FLAG_ONGOING_EVENT;
        startForeground(NOTIFICATION_ID, notification);
    }

    /**
     * Notification click listeners
     * @param view
     */
    public void setListeners(RemoteViews view) {
        Intent previous = new Intent(NOTIFY_PREVIOUS);
        Intent delete = new Intent(NOTIFY_DELETE);
        Intent pause = new Intent(NOTIFY_PAUSE);
        Intent next = new Intent(NOTIFY_NEXT);
        Intent play = new Intent(NOTIFY_PLAY);

        PendingIntent pPrevious = PendingIntent.getBroadcast(getApplicationContext(), 0, previous, PendingIntent.FLAG_UPDATE_CURRENT);
        view.setOnClickPendingIntent(R.id.btnPrevious, pPrevious);

        PendingIntent pDelete = PendingIntent.getBroadcast(getApplicationContext(), 0, delete, PendingIntent.FLAG_UPDATE_CURRENT);
        view.setOnClickPendingIntent(R.id.btnDelete, pDelete);

        PendingIntent pPause = PendingIntent.getBroadcast(getApplicationContext(), 0, pause, PendingIntent.FLAG_UPDATE_CURRENT);
        view.setOnClickPendingIntent(R.id.btnPause, pPause);

        PendingIntent pNext = PendingIntent.getBroadcast(getApplicationContext(), 0, next, PendingIntent.FLAG_UPDATE_CURRENT);
        view.setOnClickPendingIntent(R.id.btnNext, pNext);

        PendingIntent pPlay = PendingIntent.getBroadcast(getApplicationContext(), 0, play, PendingIntent.FLAG_UPDATE_CURRENT);
        view.setOnClickPendingIntent(R.id.btnPlay, pPlay);

    }

    /**
     *   onStartCommand
     *   initialization of the handlers meant to be triggered by the notification broadcaster
     *   and handled by the MusicService.
     */
    //todo: may be the handler need to be moved to onCreate !!
    @SuppressLint("NewApi")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
//            if(PlayerConstants.SONGS_LIST.size() <= 0){
//                PlayerConstants.SONGS_LIST = UtilFunctions.listOfSongs(getApplicationContext());
//            }
//            MediaItem data = PlayerConstants.SONGS_LIST.get(PlayerConstants.SONG_NUMBER);
//            if(currentVersionSupportLockScreenControls){
//                RegisterRemoteClient();
//            }
//            String songPath = data.getPath();
//            playSong(songPath, data);
//            newNotification();

//            PlayerConstants.SONG_CHANGE_HANDLER = new Handler(new Handler.Callback() {
//                @Override
//                public boolean handleMessage(Message msg) {
//                    MediaItem data = PlayerConstants.SONGS_LIST.get(PlayerConstants.SONG_NUMBER);
//                    String songPath = data.getPath();
//                    newNotification();
//                    try{
//                        playSong(songPath, data);
//                        MainActivity.changeUI();
//                        AudioPlayerActivity.changeUI();
//                    }catch(Exception e){
//                        e.printStackTrace();
//                    }
//                    return false;
//                }
//            });
            PlayerConstants.SONG_NEXT_HANDLER = new Handler(new Handler.Callback() {
                @Override
                public boolean handleMessage(Message msg) {
                    playNext();
                    newNotification();
                    return false;
                }
            });
            PlayerConstants.SONG_PREV_HANDLER = new Handler(new Handler.Callback() {
                @Override
                public boolean handleMessage(Message msg) {
                    playPrev();
                    newNotification();
                    return false;
                }
            });
            PlayerConstants.PLAY_PAUSE_HANDLER = new Handler(new Handler.Callback() {
                @Override
                public boolean handleMessage(Message msg) {
                    String message = (String)msg.obj;
//                    if(mp == null)
//                        return false;
                    if(message.equalsIgnoreCase(getResources().getString(R.string.play))){
                        PlayerConstants.SONG_PAUSED = false;
                        if(currentVersionSupportLockScreenControls){
                            remoteControlClient.setPlaybackState(RemoteControlClient.PLAYSTATE_PLAYING);
                        }
                        playSong(); //mp.start();
                    }else if(message.equalsIgnoreCase(getResources().getString(R.string.pause))){
                        PlayerConstants.SONG_PAUSED = true;
                        if(currentVersionSupportLockScreenControls){
                            remoteControlClient.setPlaybackState(RemoteControlClient.PLAYSTATE_PAUSED);
                        }
                        pausePlayer(); //mp.pause();
                    }
                    newNotification();
                    try{
                        //todo change buttons
//                        MainActivity.changeButton();
//                        AudioPlayerActivity.changeButton();
                    }catch(Exception e){}
                    Log.d("TAG", "TAG Pressed: " + message);
                    return false;
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
        return START_STICKY;
    }//END onStartCommand
    /**
     * for the case if audio focus changed
     */
    @Override
    public void onAudioFocusChange(int focusChange) {}


}//END service
/*
Tip: To ensure that your app does not interfere with other audio services on the user's device,
you should enhance it to handle audio focus gracefully. Make the Service class implement
the AudioManager.OnAudioFocusChangeListener interface. In the onCreate method,
create an instance of the AudioManager class and call requestAudioFocus on it. Finally,
implement the onAudioFocusChange method in your class to control what should happen when
the application gains or loses audio focus. See the Audio Focus section in
the Developer Guide for more details.
 */