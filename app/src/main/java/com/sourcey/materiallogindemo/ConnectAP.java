package com.sourcey.materiallogindemo;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.sourcey.materiallogindemo.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.BroadcastReceiver;

public class ConnectAP extends AppCompatActivity {

    Context context;
    String selectedSSID;
    String pass_Text;


    List<String> ssids = new ArrayList<>(5);



    WifiManager mainWifi;
    WifiReceiver receiverWifi;
    List<ScanResult> wifiList;
    StringBuilder sb = new StringBuilder();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect_ap);
        context = getApplicationContext();




        mainWifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        receiverWifi = new WifiReceiver();
        registerReceiver(receiverWifi, new IntentFilter(
                WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        mainWifi.startScan();



    }




    public void ConnectToAP(View view)
    {
        final TableLayout tl = (TableLayout) findViewById(R.id.TableLayoutAP);
        tl.removeAllViews();
        //We must connect to the KHAN AP and then send the ssid and pass for our home network

        //first we scan for wifi networks

        WifiManager wifiMgr = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        List<WifiConfiguration> list = wifiMgr.getConfiguredNetworks();
        String ssid;



        for(int i=0; i<list.size();i++){

            ssid=list.get(i).SSID.toString();

            TableRow tr = new TableRow(this);
            tr.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, TableRow.LayoutParams.WRAP_CONTENT));

            final Button btnAP = new Button(this);
            btnAP.setText(ssid);



            btnAP.setOnClickListener(new View.OnClickListener() {
                                                 @Override
                                                 public void onClick(View v) {
                                                     // put code on click operation
                                                     Log.d("Button PressedName", btnAP.getText().toString());


                                                 }
                                             }

            );

            btnAP.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
            tr.addView(btnAP);
            tl.addView(tr);


        }







    }


    private void selectedAP(){

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Network Selection");
        builder.setMessage("Select your home network and input your password");

        WifiManager wifiMgr = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        List<WifiConfiguration> list = wifiMgr.getConfiguredNetworks();
        String ssid;

        //retrieve ssids
        List<CharSequence> charSequences = new ArrayList<>();

        for(int i=0; i<list.size();i++) {

            charSequences.add(list.get(i).SSID.toString());
        }

        final CharSequence[] charSequenceArray = charSequences.toArray(new
                CharSequence[charSequences.size()]);
        builder.setItems(charSequenceArray, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                // Do something with the selection
                //mDoneButton.setText(charSequenceArray[item]);
                inputPassword(charSequenceArray[item].toString());
            }
        });

// Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {


            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        //builder.show();
        AlertDialog dialog = builder.show();
        TextView messageText = (TextView)dialog.findViewById(android.R.id.message);
        messageText.setGravity(Gravity.CENTER);
        dialog.show();



    }

    private void inputPassword(String ssid){


        selectedSSID = ssid;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Input password for "+ selectedSSID);

// Set up the input
        final EditText input = new EditText(this);
// Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
        int maxLength = 12;
        input.setFilters(new InputFilter[]{new InputFilter.LengthFilter(maxLength)});
        builder.setView(input);

// Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                pass_Text = input.getText().toString();
                //TODO connect to the AP and send the ssid and pass of our home network





            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();



    }



    private void printAPs()
    {
        final TableLayout tl = (TableLayout) findViewById(R.id.TableLayoutAP);
        tl.removeAllViews();
        String ssid;
        for(int i=0; i<ssids.size();i++)
        {
            ssid=ssids.get(i);

            TableRow tr = new TableRow(this);
            tr.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, TableRow.LayoutParams.WRAP_CONTENT));

            final Button btnAP = new Button(this);
            btnAP.setText(ssid);



            btnAP.setOnClickListener(new View.OnClickListener() {
                                         @Override
                                         public void onClick(View v) {
                                             // put code on click operation
                                             Log.d("Button PressedName", btnAP.getText().toString());


                                         }
                                     }

            );

            btnAP.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
            tr.addView(btnAP);
            tl.addView(tr);


        }


    }




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_connect_a, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    class WifiReceiver extends BroadcastReceiver {
        public void onReceive(Context c, Intent intent) {
            /*
            if(ssids.size()>0) {
                ssids.clear(); //clean the old results
                ssids.
            }*/
            String pattern1 = "SSID: ";
            String pattern2 = ",";

            sb = new StringBuilder();
            wifiList = mainWifi.getScanResults();


            if (wifiList != null) {
                for(ScanResult s : wifiList) {
                    //"CONNECTION_NAME" is the name of SSID you would like filter
                    if (s.SSID != null ) {
                        ssids.add(s.SSID.toString());
                        Log.d("ssids", s.SSID.toString());
                    }
                }
            }





/*
            for(int i = 0; i < wifiList.size(); i++){
               // sb.append(new Integer(i+1).toString() + ".");
              //  sb.append((wifiList.get(i)).toString());
               // sb.append("\n");



                Pattern p = Pattern.compile(Pattern.quote(pattern1) + "(.*?)" + Pattern.quote(pattern2));

                Matcher m = p.matcher(wifiList.get(i).toString());

                while (m.find()) {
                    //System.out.println(m.group(1));
                    Log.d("ssids", m.group(1));

                        ssids.add(m.group(1));

                }

            }
            */
            printAPs();
           // mainText.setText(sb);
           // Log.d("Button PressedName", sb.toString());
        }
    }
}
