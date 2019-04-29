package com.seproject.comparedanceproject;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    CheckBox originalC, rehearsalC;
    Uri selectedUri1, selectedUri2;
    Button compareVideos;

    private int STORAGE_PERMISSION_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        originalC = findViewById(R.id.originalCheck);
        rehearsalC = findViewById(R.id.rehearsalCheck);
        compareVideos = findViewById(R.id.compareVideos);

        originalC.setChecked(false);
        rehearsalC.setChecked(false);
        compareVideos.setClickable(false);
    }

    //Allows the user to select a video from their gallery
    public void TrimVideo(View vid) {
        //checks if the permission for reading external storage has been set
        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
            //send the selected video as an intent
            Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            i.setType("video/*");
            startActivityForResult(i, 100);
        }
        else{
            //calls the method for enabling permissions
            requestReadPermission();
        }

    }

    //asks the user to enable permissions to access their storage
    private void requestReadPermission(){
        if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)){
            new AlertDialog.Builder(this)
                    .setTitle("Permission needed")
                    .setMessage("This permission is needed to select a video from your external storage")
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(MainActivity.this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
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
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
        }
    }

    //sends a message according to what the user has permitted
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == STORAGE_PERMISSION_CODE) {
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this, "Permission GRANTED", Toast.LENGTH_SHORT).show();
            }
            else{
                Toast.makeText(this, "Permission DENIED", Toast.LENGTH_SHORT).show();
            }
        }
    }

    //sends the users chosen original video as an intent
    public void UploadOriginal(View vid) {
        Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        i.setType("video/*");
        startActivityForResult(i, 200);
    }

    //send the users chosen rehearsal video as an intent
    public void UploadRehearsal(View vid) {
        Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        i.setType("video/*");
        startActivityForResult(i, 300);
    }

    //sends the uri of both chosen videos as an intent to the CompareVideos class
    public void CompareVideos(View vid) {
        Intent i = new Intent(MainActivity.this, CompareVideos.class);
        i.putExtra("originalURI", selectedUri1.toString());
        i.putExtra("rehearsalURI", selectedUri2.toString());

        startActivity(i);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK) {
            //Converts the URI of the selected video and converts it into a stream activity to be able to be sent to TrimVideo activity
            Uri selectedUri = data.getData();

            Intent i = new Intent(MainActivity.this, TrimVideo.class);
            i.putExtra("uri", selectedUri.toString());

            startActivity(i);
        }

        //retrieves the uri of the selected video and sets the path to a file, the checkbox is ticked and the file path is displayed
        if (requestCode == 200 && resultCode == RESULT_OK) {
            selectedUri1 = data.getData();
            originalC.setChecked(true);

            File file = new File(selectedUri1.getPath());
            Toast.makeText(MainActivity.this, file.toString(), Toast.LENGTH_LONG).show();
        }

        //retrieves the uri of the selected video and sets the path to a file, the checkbox is ticked and the file path is displayed
        if (requestCode == 300 && resultCode == RESULT_OK) {
            selectedUri2 = data.getData();
            rehearsalC.setChecked(true);

            File file = new File(selectedUri2.getPath());
            Toast.makeText(MainActivity.this, file.toString(), Toast.LENGTH_LONG).show();
        }

        //sets the button to compare videos to be clickable only if two videos have been selected as notified by the checkboxes
        if (selectedUri1 != null && selectedUri2 != null) {
            compareVideos.setClickable(true);
        }

    }
}
