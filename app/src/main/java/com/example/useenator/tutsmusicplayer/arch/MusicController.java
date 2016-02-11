package com.example.useenator.tutsmusicplayer.arch;

import android.content.Context;
import android.widget.MediaController;

/**
 * Created by hmed on 31/01/16.
 */
public class MusicController extends MediaController {

    public MusicController(Context c){
        super(c);
    }
/*
You can tailor the MediaController class in various ways.
 All we want to do is stop it from automatically hiding after three seconds by overriding the hide method.

Tip: You may need to tweak the theme your app uses in order to ensure that the media controller
text is clearly visible.
 */
    public void hide(){}

}