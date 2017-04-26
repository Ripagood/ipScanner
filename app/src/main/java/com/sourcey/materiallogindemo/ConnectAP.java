package com.sourcey.materiallogindemo;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PorterDuff;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Handler;
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.BroadcastReceiver;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

public class ConnectAP extends AppCompatActivity {

    Context context;
    String selectedSSID;
    String pass_Text;

    //Time out for the HTTP request
    //do not go under 500ms
    static final int  TIME_OUT =  1000;

    private ProgressDialog setupPD;

    //The SSID of the network we are connected to when we create the activity
    //this will be used to reconnect after sending the info to an AP
    String mainSSID="";

    private ProgressDialog pd;


    List<String> ssids = new ArrayList<>(5);



    WifiManager mainWifi;
    WifiReceiver receiverWifi;
    List<ScanResult> wifiList;
    StringBuilder sb = new StringBuilder();




    String[] list;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect_ap);
        context = getApplicationContext();

        WifiManager wifiManager = (WifiManager) getSystemService (Context.WIFI_SERVICE);
        WifiInfo info = wifiManager.getConnectionInfo ();
        mainSSID = info.getSSID();
        Log.d("SSID", info.getSSID());



        scanForAPs();


        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("AP Connection");
        builder.setMessage("Boot your device in AP mode and select it from the list here, if the " +
                "device is not visible it will not be shown. Then input your network's credentials");


// Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                //Nothing to do, continue with the activity and close the alert dialog
            }
        });

        builder.setCancelable(false);


        //builder.show();
        AlertDialog dialog = builder.show();
        TextView messageText = (TextView)dialog.findViewById(android.R.id.message);
        messageText.setGravity(Gravity.CENTER);
        dialog.show();




    }


    //Present alert dialog asking wether to go back to main after sending ssid and pass
    //Also used as delay for clearing the ssids list
    private void MainActivityAlertDialog()
    {


        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("AP Connection");
        builder.setMessage("The network has been sent to the device. In order to use the device+" +
                "go back to Main Activity and push Scan Devices");


// Set up the buttons
        builder.setPositiveButton("Go back", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                //
                finish();
            }
        });

        builder.setNegativeButton("Add other Device", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //Do something after 2000ms
                        ssids.clear();
                        printAPs();
                    }
                }, 500);
            }
        });

        builder.setCancelable(false);


        //builder.show();
        AlertDialog dialog = builder.show();
        TextView messageText = (TextView)dialog.findViewById(android.R.id.message);
        messageText.setGravity(Gravity.CENTER);
        dialog.show();

    }



    //Register a receiver for APs
    private void scanForAPs()
    {

        mainWifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        receiverWifi = new WifiReceiver();
        registerReceiver(receiverWifi, new IntentFilter(
                WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        mainWifi.startScan();

    }


    //Displays ssids received from device in order to ask for password and send it
    //to the ap
    private void selectedAP(String ssids){

        AlertDialog.Builder builder = new AlertDialog.Builder(this);


        if(ssids.equals(""))
        {
            //No actual input || malforemd || ioexception
            builder.setMessage("No visible networks, Retry");

        }else {


            builder.setTitle("Select your home network and input your password");
            //builder.setMessage("Select your home network and input your password");
            //IMPORTANT DONT USE SET MESSAGE OR ITEMS WONT DISPLAY
        }
        list = ssids.split(";");
        //retrieve ssids



        List<CharSequence> charSequences = new ArrayList<>();

        for(int i=0; i<list.length;i++) {

            charSequences.add(list[i]);
            Log.d("ssidFromDevice",list[i]);
        }


        builder.setItems(list, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                // Do something with the selection
                //mDoneButton.setText(charSequenceArray[item]);
                inputPassword(list[item].toString());
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

        builder.show();
        /*
        AlertDialog dialog = builder.show();
        TextView messageText = (TextView)dialog.findViewById(android.R.id.message);
        messageText.setGravity(Gravity.CENTER);
        dialog.show();*/



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
                sendPasswordToDevice(selectedSSID,pass_Text);





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

    private void ConnectToAP(String APssid)
    {
        String networkSSID = APssid;


        WifiConfiguration conf = new WifiConfiguration();
        conf.SSID = "\"" + networkSSID + "\"";   // Please note the quotes.

        //KHAN AP has no auth
        conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        //add to android wifi manager settings
        WifiManager wifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
        wifiManager.addNetwork(conf);


        //enable and connect
        List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
        for( WifiConfiguration i : list ) {
            if(i.SSID != null && i.SSID.equals("\"" + networkSSID + "\"")) {
                wifiManager.disconnect();
                wifiManager.enableNetwork(i.networkId, true);
                wifiManager.reconnect();

                break;
            }
        }

        waitForConnection();


    }



    public void waitForConnection() {

        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {

            @Override
            protected void onPreExecute() {
                pd = new ProgressDialog(ConnectAP.this);
                pd.setTitle("Processing...");
                pd.setMessage("Please wait.");
                pd.setCancelable(false);
                pd.setIndeterminate(true);
                pd.show();
            }

            @Override
            protected Void doInBackground(Void... arg0) {
                try {
                    //check if connected!
                    while (!isConnected(ConnectAP.this)) {
                        //Wait to connect


                    }

                    Log.d("wifi","wifi connected");

                } catch (Exception e) {
                }
                return null;


            }

            @Override
            protected void onPostExecute(Void result) {
                if (pd!=null) {
                    pd.dismiss();

                }
                getSSIDfromDevice();//continue with execution
                //get ssids visible from device, display them and connect to one
            }

        };
        task.execute();
    }

    public static boolean isConnected(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = null;
        if (connectivityManager != null) {
            networkInfo = connectivityManager.getActiveNetworkInfo();
        }

        return networkInfo != null && networkInfo.getState() == NetworkInfo.State.CONNECTED;
    }



    private void printAPs()
    {
        final TableLayout tl = (TableLayout) findViewById(R.id.TableLayoutAP);
        tl.removeAllViews();
        String ssid;



        for(int i=0; i<ssids.size();i++)
        {
            ssid=ssids.get(i);
            Log.d("ssid",ssid);

            TableRow tr = new TableRow(this);
            tr.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, TableRow.LayoutParams.WRAP_CONTENT));

            final Button btnAP = new Button(this);
            btnAP.getBackground().setColorFilter(getResources().getColor(R.color.primary_darker), PorterDuff.Mode.MULTIPLY);
            btnAP.setText(ssid);



            btnAP.setOnClickListener(new View.OnClickListener() {
                                         @Override
                                         public void onClick(View v) {
                                             // put code on click operation
                                             Log.d("Button PressedName", btnAP.getText().toString());
                                             ConnectToAP(btnAP.getText().toString());



                                         }
                                     }

            );

            btnAP.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
            tr.addView(btnAP);
            tl.addView(tr);


        }


    }


    private void getSSIDfromDevice( ){

        final String deviceIP ="http://192.168.4.1/networks";


        new HttpCommandGetSSIDsVisible().execute(deviceIP);


    }

    private void sendPasswordToDevice(String ssid,String password){
        final String deviceIP ="http://192.168.4.1/setting?";
        // 192.168.4.1/setting?ssid=xxxxx&pas=yyyyy


        new HttpCommandSendPassword().execute(deviceIP + "ssid=" + ssid + "&" + "pass=" +password);

    }

    private class HttpCommandSendPassword extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {
            return GET(urls[0]);
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            //Toast.makeText(getBaseContext(), "Received!", Toast.LENGTH_LONG).show();
            //Toast.makeText(getBaseContext(), result, Toast.LENGTH_LONG).show();
            Log.d("Received",result);
            //display results

            if(result.equals("saved"))
            {
                //correct delivery of the data
                WifiManager wifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
                wifiManager.disconnect();
                Toast.makeText(getBaseContext(), "Delivered! Scan for devices", Toast.LENGTH_LONG).show();

                //Reconnect to main network
                //i.e. the one saved when the users enters the activity
                //see on create method
                //WifiManager wifiManager = (WifiManager)context.getSystemService(WIFI_SERVICE);
                int netId = -1;
                for (WifiConfiguration tmp : wifiManager.getConfiguredNetworks())
                    if (tmp.SSID.equals( "\""+mainSSID+"\""))
                    {
                        netId = tmp.networkId;
                        wifiManager.enableNetwork(netId, true);
                    }

                MainActivityAlertDialog();



            }






        }
    }

    private class HttpCommandGetSSIDsVisible extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {
            return GET(urls[0]);
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            //Toast.makeText(getBaseContext(), "Received!", Toast.LENGTH_LONG).show();
            //Toast.makeText(getBaseContext(), result, Toast.LENGTH_LONG).show();
            Log.d("Received",result);
            //display results
            selectedAP(result);





        }
    }

    private String GET(String url){

        String response="";

        try{
            //Consider next request:
            HttpRequest req=new HttpRequest(url);
            // prepare http get request,  send to "http://host:port/path" and read server's response as String
            response=  req.prepare().sendAndReadString();
        }
        catch(MalformedURLException e){

            runOnUiThread(new Runnable()
            {
                public void run()
                {
                    Toast.makeText(getApplicationContext(), "malformedUrl", Toast.LENGTH_SHORT).show();
                }
            });
            Log.d("MalformedURl",e.getLocalizedMessage());
        }
        catch (SocketTimeoutException e){

            Log.d("Connection timed out", e.getLocalizedMessage());
            runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(getApplicationContext(), "Connection Time Out", Toast.LENGTH_SHORT).show();
                }
            });
        }
        catch(IOException e){

            Log.d("IOException",e.getLocalizedMessage());
            runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(getApplicationContext(), "ioException", Toast.LENGTH_SHORT).show();
                }
            });
        }
        return  response;

    }


    public static String GET4(String url){
        InputStream inputStream = null;
        String result = "";
        try {

            // set the connection timeout value to .1 seconds
            final HttpParams httpParams = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpParams, TIME_OUT);

            // create HttpClient
            HttpClient httpclient = new DefaultHttpClient(httpParams);

            // make GET request to the given URL
            HttpResponse httpResponse = httpclient.execute(new HttpGet(url));

            // receive response as inputStream
            inputStream = httpResponse.getEntity().getContent();

            // convert inputstream to string
            if(inputStream != null)
                result = convertInputStreamToString(inputStream);
            else
                result = "Did not work!";

        } catch (Exception e) {
            Log.d("InputStream", e.getLocalizedMessage());
            //Log.d("InputStream", "NO connection");
        }

        return result;
    }

    private static String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while((line = bufferedReader.readLine()) != null)
            result += line;

        inputStream.close();
        return result;

    }


    @Override
    public void onDestroy() {
        unregisterReceiver(receiverWifi);
        super.onDestroy();
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
                ssids.clear();
                for(ScanResult s : wifiList) {
                    //"CONNECTION_NAME" is the name of SSID you would like filter
                    if (s.SSID != null && s.SSID.toLowerCase().contains("khan")) {
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
