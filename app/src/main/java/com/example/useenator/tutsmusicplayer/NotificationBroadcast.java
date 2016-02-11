package com.example.useenator.tutsmusicplayer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

import com.example.useenator.tutsmusicplayer.service.MusicService;
//import com.tutorialsface.audioplayer.MainActivity;
//import com.tutorialsface.audioplayer.controls.Controls;
//import com.tutorialsface.audioplayer.service.SongService;
//import com.tutorialsface.audioplayer.util.PlayerConstants;

public class NotificationBroadcast extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals(Intent.ACTION_MEDIA_BUTTON)) {
            KeyEvent keyEvent = (KeyEvent) intent.getExtras().get(Intent.EXTRA_KEY_EVENT);
            if (keyEvent.getAction() != KeyEvent.ACTION_DOWN)
                return;
			//todo configure KEYCODE_MEDIA, KEYCODE_HEADSETHOOK
            switch (keyEvent.getKeyCode()) {
                case KeyEvent.KEYCODE_HEADSETHOOK:
                case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
//                	if(!PlayerConstants.SONG_PAUSED){
//    					Controls.pauseControl(context);
//                	}else{
//    					Controls.playControl(context);
//                	}
                	break;
                case KeyEvent.KEYCODE_MEDIA_PLAY:
                	break;
                case KeyEvent.KEYCODE_MEDIA_PAUSE:
                	break;
                case KeyEvent.KEYCODE_MEDIA_STOP:
                	break;
                case KeyEvent.KEYCODE_MEDIA_NEXT:
                	Log.d("TAG", "TAG: KEYCODE_MEDIA_NEXT");
                	//Controls.nextControl(context);
					Toast.makeText(context, "KEYCODE_MEDIA_NEXT", Toast.LENGTH_LONG).show();
                	break;
                case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                	Log.d("TAG", "TAG: KEYCODE_MEDIA_PREVIOUS");
                	//Controls.previousControl(context);
					Toast.makeText(context, "KEYCODE_MEDIA_PREVIOUS", Toast.LENGTH_LONG).show();
                	break;
            }
		}  else{
            	if (intent.getAction().equals(MusicService.NOTIFY_PLAY)) {
    				Controls.playControl(context);
					Toast.makeText(context, "playControl", Toast.LENGTH_LONG).show();
        		} else if (intent.getAction().equals(MusicService.NOTIFY_PAUSE)) {
    				Controls.pauseControl(context);
					Toast.makeText(context, "pauseControl", Toast.LENGTH_LONG).show();
        		} else if (intent.getAction().equals(MusicService.NOTIFY_NEXT)) {
        			Controls.nextControl(context);
					Toast.makeText(context, "nextControl", Toast.LENGTH_LONG).show();
        		} else if (intent.getAction().equals(MusicService.NOTIFY_DELETE)) {
					Intent i = new Intent(context, MusicService.class);
					context.stopService(i);
//					Intent in = new Intent(context, MainActivity.class);
//			        in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//			        context.startActivity(in);
        		}else if (intent.getAction().equals(MusicService.NOTIFY_PREVIOUS)) {
    				Controls.previousControl(context);
					Toast.makeText(context, "previousControl", Toast.LENGTH_LONG).show();
        		}
		}
	}

	public String ComponentName() {
		return this.getClass().getName();
	}
}
