package com.seproject.comparedanceproject;

import android.arch.lifecycle.Observer;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.dinuscxj.progressbar.CircleProgressBar;

public class ProgressBar extends AppCompatActivity {

    CircleProgressBar circleProgressBar;
    int duration;
    String [] command;
    String path;

    ServiceConnection mConnection;
    FFMpegService ffMpegService;
    Integer res;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_progress_bar);
        //sets the progress bar and its max value to 100
        circleProgressBar = findViewById(R.id.circleProgressBar);
        circleProgressBar.setMax(100);

        final Intent i = getIntent();

        if(i != null){
            duration = i.getIntExtra("duration", 0);
            command = i.getStringArrayExtra("command");
            path = i.getStringExtra("destination");


            //sends the duration of the new video, command line for FFMpeg and file destination to the FFMpegService class
            final Intent intent = new Intent(ProgressBar.this, FFMpegService.class);
            intent.putExtra("duration", String.valueOf(duration));
            intent.putExtra("command", command);
            intent.putExtra("destination", path);

            startService(intent);

            //creates a new service connection and binds the service
            mConnection = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder iBinder) {
                    FFMpegService.LocalBinder binder = (FFMpegService.LocalBinder)iBinder;
                    ffMpegService = binder.getServiceInstance();
                    ffMpegService.registerClient(getParent());

                    //observes the progress of the FFMpeg service to set the progress bar percentage
                    final Observer<Integer> resultObserver = new Observer<Integer>() {
                        @Override
                        public void onChanged(@Nullable Integer integer) {
                            res = integer;

                            if(res < 100){
                                circleProgressBar.setProgress(res);
                            }

                            if(res == 100){
                                circleProgressBar.setProgress(res);
                                stopService(intent);

                                Toast.makeText(getApplicationContext(), "Video trimmed successfully and saved in folder 'TrimVideos'", Toast.LENGTH_LONG).show();

                                //sets the meta data of the new video so that it can instantly appear in the users gallery
                                ContentValues values = new ContentValues(3);
                                values.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4");
                                values.put(MediaStore.Video.Media.DATA, path);
                                values.put(MediaStore.Video.Media.DURATION, String.valueOf(duration));
                                getContentResolver().insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);



                            }
                        }
                    };

                    //retrieves the percentage calculated in the FFMpegService class
                    ffMpegService.getPercentage().observe(ProgressBar.this, resultObserver);
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {
                    finish();
                }
            };

            //binds the ffmpeg service
            bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        }
    }
}
