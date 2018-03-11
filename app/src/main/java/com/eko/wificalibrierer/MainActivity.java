package com.eko.wificalibrierer;

import android.media.AudioManager;
import android.media.ToneGenerator;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;

public class MainActivity extends AppCompatActivity {

    private TextView trackerState;
    private TextView currentLimit;
    private TextView limitAdd;
    private Button trackerButton;
    private Button recordingButton;
    private ProgressBar functionActiveBar;
    private SeekBar limitAddBar;
    private ListView wifiSignalList;
    private ArrayList<Integer> signalArrayList;

    private boolean isRecording = false;
    private boolean isTracking = false;

    private int recordedLimit;

    private int limitManipulation = 0;

    private int setLimit = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        trackerState = (TextView) findViewById(R.id.tracker_state);
        trackerButton = (Button) findViewById(R.id.wifi_tracker_button);
        trackerButton.setText("Activate Tracking");


        currentLimit = (TextView) findViewById(R.id.signal_limit);
        limitAdd = (TextView) findViewById(R.id.add_limit);
        recordingButton = (Button) findViewById(R.id.wifi_recording_button);
        recordingButton.setText("Activate Recording");
        functionActiveBar = (ProgressBar) findViewById(R.id.progress_bar);
        functionActiveBar.setVisibility(View.GONE);
        limitAddBar = (SeekBar) findViewById(R.id.add_bar);
        limitAddBar.setMax(10);
        wifiSignalList = (ListView) findViewById(R.id.signal_list);


        recordingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (isRecording != true) {
                    isRecording = true;
                    recordingButton.setText("Deactivate Recording");
                    functionActiveBar.setVisibility(View.VISIBLE);
                    HandlerThread handlerThread = new HandlerThread("HandlerThread");
                    handlerThread.start();
                    Handler handler = new Handler(handlerThread.getLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            wifiRecording();
                        }
                    });


                } else if (isRecording != false) {
                    isRecording = false;
                    recordingButton.setText("Activate Recording");
                    functionActiveBar.setVisibility(View.GONE);
                }
            }
        });


        trackerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isTracking != true) {
                    isTracking = true;
                    trackerButton.setText("Deactivate Tracking");
                    functionActiveBar.setVisibility(View.VISIBLE);
                    HandlerThread handlerThread = new HandlerThread("HandlerThread");
                    handlerThread.start();
                    Handler handler = new Handler(handlerThread.getLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            wifiTracking();
                        }
                    });

                } else if (isTracking != false) {
                    isTracking = false;
                    trackerButton.setText("Activate Tracking");
                    functionActiveBar.setVisibility(View.GONE);
                }

            }
        });



        limitAddBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (setLimit != 0) {

                    limitManipulation = i - 5;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            limitAdd.setText("" + limitManipulation );
                            int limitDisplay = setLimit + limitManipulation;
                            currentLimit.setText("current Limit: " + limitDisplay);
                        }
                    });

                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }


        });


    }


    public void wifiRecording() {

        signalArrayList = new ArrayList<>();
        final WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(getApplicationContext().WIFI_SERVICE);
        recordedLimit = 0;


        while (isRecording) {

            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            int signal = wifiInfo.getRssi();


            if (signal < recordedLimit) {
                recordedLimit = signal;
            }

            signalArrayList.add(signal);
            try {

                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();

            }

        }


        Collections.sort(signalArrayList);


        final ArrayAdapter<Integer> arrayAdapter =
                new ArrayAdapter<Integer>(this, android.R.layout.simple_list_item_1, signalArrayList);


        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setLimit = recordedLimit;
                wifiSignalList.setAdapter(arrayAdapter);
                limitAdd.setText("" + limitManipulation );
                int limitDisplay = setLimit + limitManipulation;
                currentLimit.setText("current Limit: " + limitDisplay);
            }
        });


    }


    public void wifiTracking() {

        final WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(getApplicationContext().WIFI_SERVICE);
        recordedLimit = 0;



                while (isTracking) {

                    WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                    final int signal = wifiInfo.getRssi();

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            trackerState.setText("Current Signal: " + signal);
                        }
                        });

                    //if signal is below limit activate alarm
                    if (signal < setLimit + limitManipulation) {
                        ToneGenerator toneGenerator = new ToneGenerator(AudioManager.STREAM_ALARM, 10);
                        toneGenerator.startTone(ToneGenerator.TONE_CDMA_ABBR_ALERT, 200);
                        toneGenerator.release();
                    }

                    try {

                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();

                    }
                }
            }

    }

