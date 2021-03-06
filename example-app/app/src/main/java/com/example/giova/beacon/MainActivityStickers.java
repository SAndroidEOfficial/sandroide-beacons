package com.example.giova.beacon;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import it.unibs.sandroide.lib.activities.SandroideBaseActivity;
import it.unibs.sandroide.lib.beacon.BeaconTags;
import it.unibs.sandroide.lib.beacon.msg.BeaconMsgBase;
import it.unibs.sandroide.lib.beacon.msg.BeaconMsgNearable;
import it.unibs.sandroide.lib.beacon.notifier.TagMonitorNotifier;
import it.unibs.sandroide.lib.beacon.notifier.TagRangeNotifier;

/**
 * Created by giova on 23/02/2017.
 */


public class MainActivityStickers extends SandroideBaseActivity implements BeaconConsumer {
    protected static final String TAG = "MainActivityStickers";
    private BeaconManager beaconManager;

    private ArrayList<String> logLines = new ArrayList<String>();
    private ListView mLogList;
    private ArrayAdapter logLinesAdapter;
    private String scanId;
    private TextView tvX1,tvY1,tvZ1,tvX2,tvY2,tvZ2,tvTemp1,tvTemp2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stickers);

        tvX2 = (TextView) findViewById(R.id.tvX2);
        tvY2 = (TextView) findViewById(R.id.tvY2);
        tvZ2 = (TextView) findViewById(R.id.tvZ2);
        tvTemp2 = (TextView) findViewById(R.id.tvTemp2);

        mLogList=(ListView) findViewById(R.id.logList);
        logLinesAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, logLines);
        mLogList.setAdapter(logLinesAdapter);

        beaconManager = BeaconManager.getInstanceForApplication(this.getApplicationContext());
        try {
            // load tagged beacons from shared preferences
            BeaconTags.getInstance().load(this);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        BeaconTags.getInstance().initLayouts(beaconManager,"near");
        BeaconTags.getInstance().initLayouts(beaconManager,"sky");
        BeaconTags.getInstance().initLayouts(beaconManager,"ice");

    }

    @Override
    protected void onResume() {
        super.onResume();
        beaconManager.bind(this);
    }

    @Override
    protected void onPause() {
        if (beaconManager.isBound(this)) beaconManager.unbind(this);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void startTagConfiguration(View v) {
        BeaconTags.getInstance().startTaggingActivity(this);
    }

    private void addLogLine(final String s) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                logLines.add(0,String.format("%s %s",new Date().getSeconds(),s));
                if (logLines.size()>10) logLines.remove(logLines.size()-1);
                logLinesAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onBeaconServiceConnect() {

        BeaconTags.getInstance().clearNotifiers(beaconManager);

        BeaconTags.getInstance().addRangeNotifierForTag(beaconManager, "near", new TagRangeNotifier() {
            @Override
            public void onTaggedBeaconReceived(BeaconMsgBase b) {
                addLogLine(String.format("NEAR Beacon in range for tag:%s, key:%s, ids:%s","near", b.getParserSimpleClassname(), b.getIdentifiers().toString()));

                BeaconMsgBase beac = new BeaconMsgNearable(b).parse();
                if (beac!=null) {
                    BeaconMsgNearable near = (BeaconMsgNearable) beac;
                    addLogLine(String.format("Found my nearable: %s",beac.toString()));
                    Log.i("MainActivityBeacon",String.format("Found my nearable: %s",beac.toString()));

                    final Double x=((BeaconMsgNearable) beac).getAccellX(),y=((BeaconMsgNearable) beac).getAccellY(),z=((BeaconMsgNearable) beac).getAccellZ(),temp= Double.valueOf(Math.round(((BeaconMsgNearable) beac).getTemp()));
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tvX2.setText(x.toString());
                            tvY2.setText(y.toString());
                            tvZ2.setText(z.toString());
                            tvTemp2.setText(temp.toString());
                        }
                    });


                } else {
                    addLogLine(String.format("This is not a nearable message: %s",b.getKeyIdentifier()));
                    Log.e("MainActivityBeacon",String.format("This is not a nearable message: %s",b.getKeyIdentifier()));
                }
            }
        });

        BeaconTags.getInstance().addRangeNotifierForTag(beaconManager, "sky", new TagRangeNotifier() {
            @Override
            public void onTaggedBeaconReceived(BeaconMsgBase b) {
                addLogLine(String.format("SKY Beacon in range for tag:%s, key:%s, ids:%s","sky", b.getParserSimpleClassname(), b.getIdentifiers().toString()));
            }
        });
        BeaconTags.getInstance().addRangeNotifierForTag(beaconManager, "ice", new TagRangeNotifier() {
            @Override
            public void onTaggedBeaconReceived(BeaconMsgBase b) {
                addLogLine(String.format("ICE Beacon in range for tag:%s, key:%s, ids:%s","ice", b.getParserSimpleClassname(), b.getIdentifiers().toString()));
            }
        });

        BeaconTags.getInstance().addMonitorNotifier(beaconManager, "sky", new TagMonitorNotifier(){
            @Override
            public void didEnterTag(String tag) {
                addLogLine(String.format("ENTER tag %s",tag));
            }

            @Override
            public void didExitTag(String tag) {
                addLogLine(String.format("EXIT tag %s",tag));
            }

            @Override
            public void didDetermineStateForTag(int i, String tag) {
                addLogLine(String.format("didDetermineStateForTag tag %s, %d",tag,i));
            }
        });

        BeaconTags.getInstance().addMonitorNotifier(beaconManager, "ice", new TagMonitorNotifier(){
            @Override
            public void didEnterTag(String tag) {
                addLogLine(String.format("ENTER tag %s",tag));
            }

            @Override
            public void didExitTag(String tag) {
                addLogLine(String.format("EXIT tag %s",tag));
            }

            @Override
            public void didDetermineStateForTag(int i, String tag) {
                addLogLine(String.format("didDetermineStateForTag tag %s, %d",tag,i));
            }
        });

    }

}
