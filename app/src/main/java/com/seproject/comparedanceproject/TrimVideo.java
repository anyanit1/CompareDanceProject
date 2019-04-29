package com.seproject.comparedanceproject;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.VideoView;

import org.florescu.android.rangeseekbar.RangeSeekBar;

import java.io.File;

public class TrimVideo extends AppCompatActivity {

    Uri uri;
    ImageView imageView;
    VideoView videoView;
    TextView textViewLeft, textViewRight;
    RangeSeekBar rangeSeekBar;

    boolean isPlaying = false;
    int duration;
    String filePrefix;
    String[] command;
    File dest;
    String original_path;

    private int STORAGE_PERMISSION_CODE = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trim_video);

        //Sets all images, videos and texts
        imageView = findViewById(R.id.pause);
        videoView = findViewById(R.id.videoView);
        textViewRight = findViewById(R.id.tvvRight);
        textViewLeft = findViewById(R.id.tvvLeft);
        rangeSeekBar =  findViewById(R.id.seekbar);

        Intent i = getIntent();

        //If there is a video, it is played in the video view
        if(i != null){
            String imgPath = i.getStringExtra("uri");
            uri = Uri.parse(imgPath);
            isPlaying = true;
            videoView.setVideoURI(uri);
            videoView.start();
        }

        //calls the method for setting the listeners
        setListeners();
    }

    private void setListeners(){
        //settings for when the image view (pause icon/play icon) is clicked
        imageView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                //Icon is changed to play when video is paused
                if(isPlaying){
                    imageView.setImageResource(R.drawable.ic_play);
                    videoView.pause();
                    isPlaying = false;
                }
                //Icon is changed to pause when video is playing
                else{
                    imageView.setImageResource(R.drawable.ic_pause);
                    videoView.start();
                    isPlaying = true;
                }
            }
        });
        //settings for when the video view is clicked
        videoView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                //Icon is changed to play when video is paused
                if(isPlaying){
                    imageView.setImageResource(R.drawable.ic_play);
                    videoView.pause();
                    isPlaying = false;
                }
                //Icon is changed to pause when video is playing
                else{
                    imageView.setImageResource(R.drawable.ic_pause);
                    videoView.start();
                    isPlaying = true;
                }
            }
        });

        //Sets the minimum and maximum values of the seekbar according to the video currently playing
        //Sets the values of the seekbar if the were to be changed by the user
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                videoView.start();
                duration = mp.getDuration()/1000;

                textViewLeft.setText("00:00:00");
                textViewRight.setText(getTime(mp.getDuration()/1000));

                //sets the range values for seekbar of the video
                mp.setLooping(true);
                rangeSeekBar.setRangeValues(0, duration);
                rangeSeekBar.setSelectedMaxValue(duration);
                rangeSeekBar.setSelectedMinValue(0);
                rangeSeekBar.setEnabled(true);

                //when the range of the seekbar is changed by the user, the video starts playing from the minimum value of the seekbar
                rangeSeekBar.setOnRangeSeekBarChangeListener(new RangeSeekBar.OnRangeSeekBarChangeListener() {
                    @Override
                    public void onRangeSeekBarValuesChanged(RangeSeekBar bar, Object minValue, Object maxValue) {
                        videoView.seekTo((int)minValue * 1000);

                        //sets the text/time according to the changed values of the seekbar
                        textViewLeft.setText(getTime((int)bar.getSelectedMinValue()));
                        textViewRight.setText(getTime((int)bar.getSelectedMaxValue()));
                    }
                });

                //if the video ends, it replays form the selected minimum value of the seekbar
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if(videoView.getCurrentPosition() >= rangeSeekBar.getSelectedMaxValue().intValue() * 1000){
                            videoView.seekTo(rangeSeekBar.getSelectedMinValue().intValue() * 1000);
                        }
                    }
                },1000);


            }
        });
    }

    //method for calculating the time for the videos
    private String getTime(int seconds) {
        int hr = seconds/3600;
        int rem = seconds % 3600;
        int mn = rem/60;
        int sec = rem % 60;
        return String.format("%02d",hr) + ":" + String.format("%02d",mn) + ":" + String.format("%02d",sec);
    }

    //When the trim button is selected the user is asked to rename the new video before trimming
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.trim){
            //checks if permissions for writing to external storage has been set, if not then the user is asked to set permission
            if(ContextCompat.checkSelfPermission(TrimVideo.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                final AlertDialog.Builder alert = new AlertDialog.Builder(TrimVideo.this);


                //creates the alert layout for a the user to enter the new video title of the video being trimmed
                LinearLayout linearLayout = new LinearLayout(TrimVideo.this);
                linearLayout.setOrientation(LinearLayout.VERTICAL);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
                lp.setMargins(50,0,50,100);

                //sets the properties for user input
                final EditText input = new EditText(TrimVideo.this);
                input.setLayoutParams(lp);
                input.setGravity(Gravity.TOP|Gravity.START);
                input.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
                linearLayout.addView(input,lp);

                //sets the alert messsage and title
                alert.setMessage("Set video name?");
                alert.setTitle("Change video name");
                alert.setView(linearLayout);

                alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                //if the user submits, the new file name is stored, the video is trimmed according to selected values and details are sent to the ProgressBar class
                alert.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        filePrefix = input.getText().toString();

                        //the video is trimmed according to selected range values
                        trimVid(rangeSeekBar.getSelectedMinValue().intValue() * 1000,
                                rangeSeekBar.getSelectedMaxValue().intValue() * 1000, filePrefix);


                        //duration of video, command for FFMpeg and file destination is passed to ProgressBar class
                        Intent intent = new Intent(TrimVideo.this, ProgressBar.class);
                        intent.putExtra("duration", duration);
                        intent.putExtra("command", command);
                        intent.putExtra("destination", dest.getAbsolutePath());
                        startActivity(intent);

                        finish();
                        dialog.dismiss();

                    }
                });

                alert.show();
            }
            else{
                //asks the user to enable write to storage permissions
                requestWritePermission();
            }

        }
        return super.onOptionsItemSelected(item);
    }

    //method for asking user to activate permissions for writing to external storage
    private void requestWritePermission(){
        if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)){
            new AlertDialog.Builder(this)
                    .setTitle("Permission needed")
                    .setMessage("This permission is needed to save a video to your external storage")
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(TrimVideo.this, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .create().show();
        }
        else{
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
        }
    }

    //method for trimming the selected video according to chosen ranges
    private void trimVid(int startMs, int endMs, String fileName) {
        //creates a new file directory 'TrimVideos' if it doesn't exist
        File folder = new File(Environment.getExternalStorageDirectory() + "/TrimVideos");
        if(!folder.exists()){
            folder.mkdir();
        }

        //video file name
        filePrefix = fileName;

        //file extention
        String fileExt = ".mp4";

        //sets the new file as a file variable
        dest = new File(folder, filePrefix + fileExt);

        //gets the path of the original
        original_path = getRealPathFromUri(getApplicationContext(),uri);

        //sets the duration of the new fil as a variable
        duration = (endMs - startMs) / 1000;

        //the command line for FFMpeg to be able to create and compress a new video file
        //-ss (position time of where to start cutting the original video)
        //-y (overwrites the output files without asking)
        //-i (the input file to be read by FFMpeg)
        //-t (duration of cut video from starting cut to ending cut)
        //-vcodec (sets the video codec)
        //-b:v (sets the video bitrate)
        //-b:a (sets the audio bitrate)
        //-ac (sets the number of audio channels)
        //-ar (sets the sampling rate for audio streams)
        command = new String[]{"-ss", "" + startMs/1000, "-y", "-i", original_path, "-t", "" + (endMs - startMs)/1000, "-vcodec", "mpeg4", "-b:v", "2097152", "-b:a", "48000", "-ac", "2", "-ar", "22050", dest.getAbsolutePath()};

    }

    //method for retrieving the path of video to be be trimmed
    private String getRealPathFromUri(Context context, Uri contentUri) {
        Cursor cursor = null;

        try {

            String[] proj = {MediaStore.Images.Media.DATA};
            cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);

            cursor.moveToFirst();
            return cursor.getString(column_index);
        }catch (Exception e){
            e.printStackTrace();
            return "";
        }finally {
             if(cursor!=null){
                 cursor.close();
             }
        }
    }

    //loads a menu item to the class
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menutrim,menu);
        return true;
    }
}
