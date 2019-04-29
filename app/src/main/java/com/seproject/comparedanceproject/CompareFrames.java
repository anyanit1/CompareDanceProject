package com.seproject.comparedanceproject;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class CompareFrames extends AppCompatActivity {

    private static final String TAG = "OCVSample::Activity";

    ImageView originalImageView, rehearsalImageView;
    Uri originalFrame, rehearsalFrame;
    Bitmap oFrame, rFrame;

    private File mCascadeFile;
    CascadeClassifier bodyCascade;

    //base loader callback for loading OpenCv to this activity
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i(TAG, "OpenCV Loaded Successfully");

                    try {

                        //accesses the file for haar cascade  upper body which is stores in a raw folder in the resources
                        InputStream is = getResources().openRawResource(R.raw.haarcascade_fullbody);
                        File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
                        mCascadeFile = new File(cascadeDir, "haarcascade_fullbody.xml");
                        FileOutputStream os = new FileOutputStream(mCascadeFile);

                        //creates a memory buffer for the output stream
                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = is.read(buffer)) != -1) {
                            os.write(buffer, 0, bytesRead);
                        }
                        is.close();
                        os.close();

                        //attempts to load the full body cascade classifier
                        bodyCascade = new CascadeClassifier(mCascadeFile.getAbsolutePath());
                        if (bodyCascade.empty()) {
                            Log.e(TAG, "Failed to load full body classifier");
                            bodyCascade = null;
                        } else
                            Log.i(TAG, "Loaded full body classifier from " + mCascadeFile.getAbsolutePath());

                        cascadeDir.delete();
                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.e(TAG, "Failed to load full body Exception thrown: " + e);
                    }
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compare_frames);

        originalImageView = findViewById(R.id.originalImageView);
        rehearsalImageView = findViewById(R.id.rehearsalImageView);

        Toast.makeText(getApplicationContext(), "Both frames saved in folder 'TrimVideos/Frames'", Toast.LENGTH_LONG).show();

        Intent i = getIntent();

        if(i != null){
            //gets the uri of both selected frames and displays them on an image view
            originalFrame = Uri.parse(i.getStringExtra("originalFrame"));
            rehearsalFrame = Uri.parse(i.getStringExtra("rehearsalFrame"));

            originalImageView.setImageURI(originalFrame);
            rehearsalImageView.setImageURI(rehearsalFrame);

            //sets each image as a bitmap
            oFrame = ((BitmapDrawable)originalImageView.getDrawable()).getBitmap();
            rFrame = ((BitmapDrawable)rehearsalImageView.getDrawable()).getBitmap();

        }

    }

    //if OpenCV library is not found, asks the user to install it
    @Override
    public void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_4_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //sets the image view as the two frames selected
        if(item.getItemId() == R.id.refreshFrames) {
            originalImageView.setImageURI(originalFrame);
            rehearsalImageView.setImageURI(rehearsalFrame);
        }
        return super.onOptionsItemSelected(item);
    }

    //loads the menu item into the class
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menurefresh,menu);
        return true;
    }

    //method for viewing body cascade for the original frame
    public void ViewBodyCascadeOriginal(View view){
        //attempts to load the cascade file
        if (!bodyCascade.load(mCascadeFile.getAbsolutePath())) {
            System.err.println("--(!)Error loading cascade: " + mCascadeFile.getAbsolutePath());
        } else {
                //creates a new Mat according to bitmap dimensions
                Mat originalMat = new Mat(oFrame.getHeight(), oFrame.getWidth(), CvType.CV_8U, new Scalar(4));
                Bitmap originalBmp = oFrame.copy(Bitmap.Config.ARGB_8888, true);

                //converts the bitmap into a Mat
                Utils.bitmapToMat(originalBmp, originalMat);

                //detects the bodies in the bitmap according to the haar cascade full body file
                originalBmp = detectAndDisplay(originalMat, bodyCascade);

                //views the detected bodies onto the image view
                originalImageView.setImageBitmap(originalBmp);
        }
    }

    //method for viewing body cascade for the original frame
    public void ViewBodyCascadeRehearsal(View view){
        //attempts to load the cascade file
        if (!bodyCascade.load(mCascadeFile.getAbsolutePath())) {
            System.err.println("--(!)Error loading cascade: " + mCascadeFile.getAbsolutePath());
        } else {
            //creates a new Mat according to bitmap dimensions
            Mat rehearsalMat = new Mat(rFrame.getHeight(), rFrame.getWidth(), CvType.CV_8U, new Scalar(4));
            Bitmap rehearsalBmp = rFrame.copy(Bitmap.Config.ARGB_8888, true);

            //converts the bitmap into a Mat
            Utils.bitmapToMat(rehearsalBmp, rehearsalMat);

            //detects the bodies in the bitmap according to the haar cascade full body file
            rehearsalBmp = detectAndDisplay(rehearsalMat, bodyCascade);

            //views the detected bodies onto the image view
            rehearsalImageView.setImageBitmap(rehearsalBmp);


        }
    }

    //method for detecting bodies in and image then created a square to be displayed around them
    public Bitmap detectAndDisplay(Mat frame, CascadeClassifier bodyCascade) {
        //Creats a Mat in grayscale
        Mat frameGray = new Mat();
        Imgproc.cvtColor(frame, frameGray, Imgproc.COLOR_BGR2GRAY);
        Imgproc.equalizeHist(frameGray, frameGray);

        //uses the cascade file to detect bodies from within the image
        MatOfRect bodies = new MatOfRect();
        bodyCascade.detectMultiScale(frameGray, bodies);

        //creates and array for each body that is detected in the image
        List<Rect> listOfBodies = bodies.toList();
        for (Rect body : listOfBodies) {

            Imgproc.rectangle(frame, body.tl(), body.br(), new Scalar(0, 255, 0, 255), 3);

        }
        //creates a bitmap and converts the mat file containing the detected bodies and squares into the bitmap to be returned and displayed
        Bitmap scale = Bitmap.createBitmap(frame.width(), frame.height(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(frame, scale);

        return scale;
    }
}
