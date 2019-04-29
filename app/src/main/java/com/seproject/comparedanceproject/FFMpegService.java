package com.seproject.comparedanceproject;

import android.app.Activity;
import android.app.Service;
import android.arch.lifecycle.MutableLiveData;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;

public class FFMpegService extends Service {

    FFmpeg fFmpeg;
    int duration;

    String[] command;
    Callbacks activity;

    public MutableLiveData<Integer> percentage;
    IBinder myBinder = new LocalBinder();

    @Override
    public void onStart(Intent intent, int startId){
        super.onStart(intent, startId);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent != null){
            duration = Integer.parseInt(intent.getStringExtra("duration"));
            command = intent.getStringArrayExtra("command");

            //calls the method to load the FFMpeg binary and then to execute the FFMpeg command
            try{
                loadFFMpegBinary();
                execFFMpegCommand();
            } catch (FFmpegNotSupportedException e){
                e.printStackTrace();
            } catch (FFmpegCommandAlreadyRunningException e) {
                e.printStackTrace();
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    //executes the FFMpeg command
    private void execFFMpegCommand() throws FFmpegCommandAlreadyRunningException {
        fFmpeg.execute(command, new ExecuteBinaryResponseHandler() {
            @Override
            public void onSuccess(String message) {
            }

            @Override
            public void onProgress(String message) {
                //calculates a percentage according to the duration of the new video being compressed during progress of command execution
                String arr[];
                if(message.contains("time=")){
                    arr = message.split("time=");
                    String time = arr[1];

                    String minutes[] = time.split(":");
                    String[] space = minutes[2].split(" ");
                    String seconds = space[0];

                    int hours = Integer.parseInt(minutes[0]);
                    hours = hours * 3600;
                    int min = Integer.parseInt(minutes[1]);
                    min = min * 60;
                    float sec = Float.valueOf(seconds);

                    float timeInSec = hours + min + sec;

                    percentage.setValue((int)((timeInSec/duration) * 100));
                }
            }

            @Override
            public void onFailure(String message) {

            }

            @Override
            public void onStart() {
            }

            @Override
            public void onFinish() {
                //sets the percentage to be 100 on completion
                percentage.setValue(100);
            }
        });
    }

    @Override
    public void onCreate(){
        super.onCreate();
        //loads the FFMpeg binary
        try {
            loadFFMpegBinary();
        } catch(FFmpegNotSupportedException e){
            e.printStackTrace();
        }
        //sets percentage variable to be live data
        percentage = new MutableLiveData<>();
    }

    //method for loading the FFMpeg binary
    private void loadFFMpegBinary() throws FFmpegNotSupportedException {
        if(fFmpeg == null){
            fFmpeg = FFmpeg.getInstance(this);
        }

        fFmpeg.loadBinary(new LoadBinaryResponseHandler(){
            @Override
            public void onFailure(){
                super.onFailure();
            }

            @Override
            public void onSuccess() {
                super.onSuccess();
            }
        });
    }


    public FFMpegService(){
        super();
    }

    //returns a binder
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return myBinder;
    }

    //binds the FFMpegs service
    public class LocalBinder extends Binder{
        public FFMpegService getServiceInstance(){
            return FFMpegService.this;
        }
    }

    //registers activity as a client
    public void registerClient(Activity activity){
        this.activity = (Callbacks)activity;
    }

    public interface Callbacks{
        void updateClient(float data);
    }

    //returns the liver percentage of progress
    public MutableLiveData<Integer> getPercentage(){
        return percentage;
    }

}
