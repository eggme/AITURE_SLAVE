package com.lsj.aiture_slave;

import android.bluetooth.BluetoothAdapter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;

import java.util.Set;

public class MainActivity extends AppCompatActivity {

    // Google Service API AIzaSyCbuC5dqlCLPnyMsKtABimfTMABD8jznak
    private final String KEY = "AIzaSyCbuC5dqlCLPnyMsKtABimfTMABD8jznak";
    YouTubePlayerView view;
    YouTubePlayer.OnInitializedListener listener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        view = (YouTubePlayerView)findViewById(R.id.view);
        view.initialize(KEY,listener);
        listener = new YouTubePlayer.OnInitializedListener() {
            @Override
            public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean b) {
                youTubePlayer.loadVideo("vewH-f3fAes&index=7&list=PLRx0vPvlEmdB6sCgj_jubp8KPb1ni0VOC");
            }

            @Override
            public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {

            }
        };

    }
}
