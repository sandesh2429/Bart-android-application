package com.myproject.joy.bartapplication;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class ScheduleActivity extends AppCompatActivity implements AsyncResponse{
    private String sourceStation;
    private String destinationStation;
    private TextView sourceStationTextView;
    private TextView destinationTextView;
    private TextView clipperTextView;
    private TextView youthClipperTextView;
    private TextView seniorClipperTextView;
    private ListView listView;
    private ImageButton swapButton;

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private String[] stationList;
    private String[] stationAbbrevatedList;

    public GetSchedule getSchedule;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.schedule_layout);
        sourceStation = getIntent().getExtras().getString("sourceStation");
        destinationStation = getIntent().getExtras().getString("destinationStation");

        mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);

        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Schedule Info");

        sourceStationTextView = (TextView) findViewById(R.id.sourceStation);
        destinationTextView = (TextView) findViewById(R.id.destinationStation);
        clipperTextView = (TextView) findViewById(R.id.clipperTextView);
        seniorClipperTextView = (TextView) findViewById(R.id.seniorClipperTextView);
        youthClipperTextView = (TextView) findViewById(R.id.youthClipperTextView);
        listView = (ListView) findViewById(R.id.listView);
        swapButton = (ImageButton) findViewById(R.id.swapButton);

        sourceStationTextView.setText(sourceStation);
        destinationTextView.setText(destinationStation);

        stationList = getResources().getStringArray(R.array.stationList);
        stationAbbrevatedList = getResources().getStringArray(R.array.stationAbbrevatedList);

        for(int i = 0; i < stationList.length; i++) {
            if(stationList[i].equals(sourceStation)) {
                sourceStation = stationAbbrevatedList[i];
                break;
            }
        }

        for (int i =0 ; i < stationList.length; i++) {
            if(stationList[i].equals(destinationStation)) {
                destinationStation = stationAbbrevatedList[i];
            }
        }

        getSchedule = new GetSchedule(sourceStation, destinationStation);
        getSchedule.delegate = this;
        getSchedule.execute();

        swapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // flipCard(v);

                String tempStation = destinationStation;
                destinationStation = sourceStation;
                sourceStation = tempStation;
                String DisplaySourceName = "";
                String DisplayDestName = "";

                GetSchedule getSchedule = new GetSchedule(sourceStation, destinationStation);
                getSchedule.execute();

                for(int i = 0; i < stationList.length; i++) {
                    if(stationAbbrevatedList[i].equals(sourceStation)) {
                        DisplaySourceName = stationList[i];
                        break;
                    }
                }

                for (int i =0 ; i < stationList.length; i++) {
                    if(stationAbbrevatedList[i].equals(destinationStation)) {
                        DisplayDestName = stationList[i];
                    }
                }

                sourceStationTextView.setText(DisplaySourceName);
                destinationTextView.setText(DisplayDestName);

                float deg = swapButton.getRotation() + 180F;
                swapButton.animate().rotation(deg).setInterpolator(new AccelerateDecelerateInterpolator());





            }
        });
    }

    @Override
    public void processFinish(String output){
        Log.i("ProcessFinish", output);
    }

    private class GetSchedule extends AsyncTask<Void, Void, Void> {
        private String message;
        private String fairDetails;
        private String sourceStation;
        private String destinationStation;
        public AsyncResponse delegate = null;
        private String clipperAmount;
        private String youthClipperAmount;
        private String seniorClipperAmount;
        private String[] sourceArrivalList;
        private String[] destinationArrivalList;
        private String[] sourceArrivalAtLegList;
        private String[] departureAtLegList;
        private String[] legStationNameList;

        public GetSchedule(String sourceStation, String destinationStation) {
            this.sourceStation = sourceStation;
            this.destinationStation = destinationStation;
            sourceArrivalList = new String[4];
            destinationArrivalList = new String[4];
            sourceArrivalAtLegList = new String[4];
            departureAtLegList = new String[4];
            legStationNameList = new String[4];
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            HttpHandler httpHandler = new HttpHandler();

            final String url = "https://api.bart.gov/api/sched.aspx?cmd=depart&orig="+sourceStation+"&dest=" +destinationStation +"&time=now&b=0&a=4&key=MW9S-E7SL-26DU-VV8V&json=y";
            final String json = httpHandler.makeServiceCall(url);
            Log.i("ScheduleActivity", json);
            if(json != null) {
                //delegate.processFinish(json);
                try {
                    final JSONObject jsonObject = new JSONObject(json);
                    final JSONObject rootObject = jsonObject.getJSONObject("root");
                    final JSONObject scheduleObject = rootObject.getJSONObject("schedule");
                    final JSONObject scheduleInfoObject = scheduleObject.getJSONObject("request");
                    final JSONArray tripArray = scheduleInfoObject.getJSONArray("trip");
                    message = "";
                    stationList = getResources().getStringArray(R.array.stationList);
                    for(int i = 0; i < tripArray.length(); i++) {
                        JSONObject tripObject = tripArray.getJSONObject(i);
                        String origin = tripObject.getString("@origin");
                        String destination = tripObject.getString("@destination");
                        String originTime = tripObject.getString("@origTimeMin");
                        String destTime = tripObject.getString("@destTimeMin");
                        String tripTime = tripObject.getString("@tripTime");
                        JSONObject faresObject = tripObject.getJSONObject("fares");
                        JSONArray faresArray = faresObject.getJSONArray("fare");

                        fairDetails = "";
                        sourceArrivalList[i] = Html.fromHtml("<b>Origin Time :</b> ") + originTime;
                        destinationArrivalList[i] = Html.fromHtml("<b>Destination Time :</b> ") + destTime;
                        for (int j = 0; j < faresArray.length(); j++) {
                            JSONObject fairObject = faresArray.getJSONObject(j);
                            String fairName = fairObject.getString("@name");
                            String amount = fairObject.getString("@amount");
                            fairDetails += "Name: " + fairName + "\n";
                            fairDetails += "Amount: " + amount + "\n" + "\n";

                            if(fairName.equals("Clipper")) {
                                clipperAmount = amount;
                            } else if(fairName.equals("Senior/Disabled Clipper")) {
                                seniorClipperAmount = amount;
                            } else if(fairName.equals("Youth Clipper")) {
                                youthClipperAmount = amount;
                            }
                        }

                        JSONArray legArray = tripObject.getJSONArray("leg");
                        if(legArray.length() == 1) {
                            message += "Direct Train" + "\n";

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    sourceStation = null;
                                    destinationStation = null;
                                    clipperTextView.setText(Html.fromHtml("<b>Clipper : </b> $"+clipperAmount));
                                    seniorClipperTextView.setText(Html.fromHtml("<b>Senior/Disabled Clipper : </b> $"+seniorClipperAmount));
                                    youthClipperTextView.setText(Html.fromHtml("<b>Youth Clipper : </b> $"+youthClipperAmount));

                                    mAdapter = new MyAdapter(sourceArrivalList, destinationArrivalList);
                                    mRecyclerView.setAdapter(mAdapter);
                                }
                            });
                        } else {
                            JSONObject legObject = legArray.getJSONObject(0);
                            sourceArrivalAtLegList[i] = legObject.getString("@destTimeMin");
                            departureAtLegList[i] = legArray.getJSONObject(1).getString("@origTimeMin");
                            String station = legObject.getString("@destination");

                            for(int k = 0; k < stationList.length; k++) {
                                if(stationAbbrevatedList[k].equalsIgnoreCase(station)) {
                                    station = stationList[k];
                                    legStationNameList[i] = stationList[k];
                                    break;
                                }
                            }

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    sourceStation = null;
                                    destinationStation = null;
                                    clipperTextView.setText(Html.fromHtml("<b>Clipper:</b> $"+clipperAmount));
                                    seniorClipperTextView.setText(Html.fromHtml("<b>Senior/Disabled Clipper:</b> $"+seniorClipperAmount));
                                    youthClipperTextView.setText(Html.fromHtml("<b>Youth Clipper:</b> $"+youthClipperAmount));

                                    mAdapter = new ScheduleAdapter(sourceArrivalList, destinationArrivalList, sourceArrivalAtLegList, departureAtLegList, legStationNameList);
                                    mRecyclerView.setAdapter(mAdapter);
                                }
                            });
                        }
                    }

                } catch (JSONException exception) {
                    exception.printStackTrace();
                }
            } else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.e("GetSchedule", "Couldn't get json from server.");
                        createAlertDialog("Error", "Couldn't get json from server.");
                    }
                });
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            Log.i("GetSchedule", "onPostExecute");
            if(result != null) {
                delegate.processFinish(result.toString());
            }
        }
    }

    public void createAlertDialog(String title, String message) {
        android.support.v7.app.AlertDialog.Builder alert= new android.support.v7.app.AlertDialog.Builder(ScheduleActivity.this);
        alert.setTitle(title);
        alert.setMessage(message);
        alert.create().show();
    }
}
