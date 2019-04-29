package com.seproject.comparedanceproject;

import android.Manifest;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.devbrackets.android.exomedia.ui.widget.VideoView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;


public class CompareVideos extends AppCompatActivity {

    private int STORAGE_PERMISSION_CODE = 1;

    Uri originalUri, rehearsalUri;
    VideoView originalView, rehearsalView;
    Bitmap originalBmp, rehearsalBmp;

    boolean originalPlaying = false, rehearsalPlaying = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compare_videos);

        originalView = findViewById(R.id.originalView);
        rehearsalView = findViewById(R.id.rehearsalView);

        Intent i = getIntent();

        if(i != null){
            //sets the Uri as a string for both selected videos
            String oriPath = i.getStringExtra("originalURI");
            String rehPath = i.getStringExtra("rehearsalURI");

            //sets the Uri of both videos
            originalUri = Uri.parse(oriPath);
            rehearsalUri = Uri.parse(rehPath);

            //sets the video uri to each video view
            originalView.setVideoURI(originalUri);
            rehearsalView.setVideoURI(rehearsalUri);

            //plays both videos
            originalView.start();
            rehearsalView.start();

            //sets the variable for both videos to be playing to true
            originalPlaying = true;
            rehearsalPlaying = true;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(ContextCompat.checkSelfPermission(CompareVideos.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
            if(item.getItemId() == R.id.compareFrames) {
            //if the compare frames button is clicked, the videos are paused
            if (rehearsalPlaying || originalPlaying) {
                rehearsalView.pause();
                rehearsalPlaying = false;

                originalView.pause();
                originalPlaying = false;
            }

            //create an alert for the user to confirm the selected frames from the videos
            final AlertDialog.Builder alert = new AlertDialog.Builder(CompareVideos.this);

            //sets alert layout
            LinearLayout linearLayout = new LinearLayout(CompareVideos.this);
            linearLayout.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMargins(50, 0, 50, 100);

            //sets the alert message
            alert.setMessage("Are you sure you want to compare these two frames?");
            alert.setTitle("Compare Frames");
            alert.setView(linearLayout);

            alert.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            //if the user selects yes, the selected frames are saved to the users storage and its file paths are sent to the CompareFrames class
            alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (originalView.getBitmap() == null || rehearsalView.getBitmap() == null)
                        Toast.makeText(CompareVideos.this, "There are no frames", Toast.LENGTH_LONG).show();
                    else {
                        //both frames from the videos are set to bitmaps
                        originalBmp = originalView.getBitmap();
                        originalBmp.setDensity(Bitmap.DENSITY_NONE);
                        rehearsalBmp = rehearsalView.getBitmap();
                        rehearsalBmp.setDensity(Bitmap.DENSITY_NONE);

                        //the frames are saved to user storage and set to file variables
                        File originalFrame = saveFrame(originalBmp);
                        File rehearsalFrame = saveFrame(rehearsalBmp);

                        //both files are sent to CompareFrames class
                        Intent i = new Intent(CompareVideos.this, CompareFrames.class);
                        i.putExtra("originalFrame", originalFrame.getAbsolutePath());
                        i.putExtra("rehearsalFrame", rehearsalFrame.getAbsolutePath());
                        startActivity(i);

                    }
                }
            });

            alert.show();
             }
        }
        else{
            //asks the user to enable write to storage permissions
            requestWritePermission();
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
                            ActivityCompat.requestPermissions(CompareVideos.this, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
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

    //method for saving the frame to user storage
    private File saveFrame(Bitmap frame) {
        //creates a new folder 'Frames' within the 'TrimVideos' folder if not already created
        File folder = new File(Environment.getExternalStorageDirectory() + "/TrimVideos/Frames");
        if(!folder.exists()){
            folder.mkdir();
        }

        //saves the image names at 'frame' and adds an increasing number to the end if already exists
        int num = 0;
        String fileName = "frame" + ".jpg";
        File file = new File(folder, fileName);
        while(file.exists()) {
            fileName = "frame" + (num++) +".jpg";
            file = new File(folder, fileName);
        }

        //compresses the bitmap into a PNG file. The density is set to 'None' so its doesn't scale
        try (FileOutputStream out = new FileOutputStream(file)) {
            frame.setDensity(Bitmap.DENSITY_NONE);
            // PNG is a lossless format, the compression factor (100) is ignored
            frame.compress(Bitmap.CompressFormat.PNG, 100, out);
        } catch (IOException e) {
            e.printStackTrace();
        }


        //sets the metadata for the new image so that it appears in the users gallery
        ContentValues values = new ContentValues(2);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/PNG");
        values.put(MediaStore.Images.Media.DATA, file.getAbsolutePath());
        getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        return file;
    }

    //loads the menu item into the class
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menucompare,menu);
        return true;
    }

}
