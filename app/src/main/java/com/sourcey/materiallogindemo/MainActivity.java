package com.sourcey.materiallogindemo;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.Bind;


public class MainActivity extends ActionBarActivity {

    Context context;


    List<String> devicesByIp = new ArrayList<>();


    //@Bind(R.id.buttonScan) Button _scanButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = getApplicationContext();







        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    public void ScanDevices(View view){
        /*
        WifiManager wifiMan = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInf = wifiMan.getConnectionInfo();
        int ipAddress = wifiInf.getIpAddress();
        String ip = String.format("%d.%d.%d.%d", (ipAddress & 0xff),(ipAddress >> 8 & 0xff),(ipAddress >> 16 & 0xff),(ipAddress >> 24 & 0xff));
        toast.setText(ip);
        */

        // Only works when NOT tethering
        WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

        DhcpInfo dhcp = wifi.getDhcpInfo();

        Log.d("IP",intToIP(dhcp.ipAddress));

        //display network info
        //Toast.makeText(getBaseContext(), intToIP(dhcp.ipAddress) , Toast.LENGTH_SHORT).show();
        //Toast.makeText(getBaseContext(), intToIP(dhcp.netmask) , Toast.LENGTH_SHORT).show();
        //Toast.makeText(getBaseContext(), intToIP(dhcp.ipAddress & dhcp.netmask) , Toast.LENGTH_SHORT).show();
        //Toast.makeText(getBaseContext(), intToIP(~dhcp.netmask | (dhcp.ipAddress & dhcp.netmask)) , Toast.LENGTH_SHORT).show();


        HashMap hm = DeviceScan(dhcp.ipAddress & dhcp.netmask, ~dhcp.netmask | (dhcp.ipAddress & dhcp.netmask));
        // call AsynTask to perform network operation on separate thread
       // new HttpAsyncTask().execute("http://192.168.0.25/KHAN?");

        Log.d("Hashmap", hm.toString());


    }
    //Intended for debugging purposes only
    public void printDevices(View view){


        //select the linear layout defined in the xml
        final LinearLayout lm = (LinearLayout) findViewById(R.id.linearLayoutMain);



        for ( final String deviceIp : devicesByIp ){

            Log.d("Devices by IP", deviceIp);

            //Create the LL to add a text view and a button
            LinearLayout ll = new LinearLayout(this);
            ll.setOrientation(LinearLayout.HORIZONTAL);
            ll.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            ll.setGravity(Gravity.CENTER);


            TextView tvIp = new TextView(this);
            tvIp.setText(deviceIp);
            tvIp.setGravity(Gravity.CENTER);


            ll.addView(tvIp);


            Button btnOn = new Button(this);
            btnOn.setText("ON");


            btnOn.setOnClickListener(new View.OnClickListener() {
                                         @Override
                                         public void onClick(View v) {
                                             // put code on click operation
                                             Log.d("Button Pressed ON", deviceIp);
                                             turnON(deviceIp);
                                         }
                                     }

            );

            ll.addView(btnOn);

            Button btnOFF = new Button(this);
            btnOFF.setText("OFF");



            btnOFF.setOnClickListener(new View.OnClickListener() {
                                         @Override
                                         public void onClick(View v) {
                                             // put code on click operation
                                             Log.d("Button Pressed OFF", deviceIp);
                                             turnOFF(deviceIp);
                                         }
                                     }

            );

            ll.addView(btnOFF);

            lm.addView(ll);

        }




    }

    //receives the network ip address in order to find the devices
    // returns a hashmap containing ip adress of the device and an ID
   // public  HashMap DeviceScan(int ipSubnetInteger, int ipBroadcastInteger){
        public  HashMap DeviceScan(int ipSubnet, int ipBroadcast){

        //long ipSubnet = ((long) ipSubnetInteger);
        //long ipBroadcast = ((long) ipBroadcastInteger);
        // Create a hash map


        HashMap hm = new HashMap();
        String urlDiscovery= "http://";
        String urlDevice = null;
        String urlCommand = "/KHAN?";

        Log.d("IP CHECKED", longToIP(ipSubnet));
        ipSubnet+=0x01000000;//endianess , first address
        ipBroadcast-=0x01000000;
        //Log.d("IP CHECKED", longToIP(ipSubnet));
        //Log.d("IP CHECKED", longToIP(ipBroadcast));

            Log.d("IP CHECKED", intToIP(ipSubnet));
            Log.d("IP CHECKED", intToIP(ipBroadcast));




            //we scan every ip address for the devices until the broadcast address is reached


        Log.d("IP CHECKED", intToIP(ipSubnet));
        for(int i=0; invertIP(ipSubnet) <= invertIP(ipBroadcast)   ;ipSubnet+=0x01000000){

            // call AsynTask to perform network operation on separate thread
            i++;

            urlDevice = longToIP(ipSubnet);
            Log.d("IP CHECKED", urlDevice);
            new HttpAsyncTask().execute(urlDiscovery+urlDevice+urlCommand);
        }




        return hm;


    }

    private static int invertIP( int ipAdd){

        byte[] hex = new byte[4];

        hex[0]= (byte)(ipAdd & 0xff);
        hex[1]= (byte)(ipAdd>>8 & 0xff);
        hex[2]= (byte)(ipAdd>>16 & 0xff);
        hex[3]= (byte)(ipAdd>>24 & 0xff);


        return ByteBuffer.wrap(hex).getInt();





    }

    public static String GET(String url){
        InputStream inputStream = null;
        String result = "";
        try {

            // set the connection timeout value to .1 seconds
            final HttpParams httpParams = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpParams, 500);

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
        }

        return result;
    }

    private static String convertInputStreamToString(InputStream inputStream) throws IOException{
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while((line = bufferedReader.readLine()) != null)
            result += line;

        inputStream.close();
        return result;

    }

    public boolean isConnected(){
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(this.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected())
            return true;
        else
            return false;
    }
    private class HttpAsyncTask extends AsyncTask<String, Void, String> {

        private String ipAdd;



        @Override
        protected String doInBackground(String... urls) {

            ipAdd = urls[0];
            return GET(urls[0]);
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            //Toast.makeText(getBaseContext(), "Received!", Toast.LENGTH_LONG).show();
            //Toast.makeText(getBaseContext(), result, Toast.LENGTH_LONG).show();

            if(result.equals("ACCEPTED")){


                //Toast.makeText(getBaseContext(), ipDevice, Toast.LENGTH_SHORT).show();
                Log.d("IP ACCEPTED", parseIP(ipAdd));
                devicesByIp.add(parseIP(ipAdd));

            }


        }
    }



    private class HttpTurnOnOff extends AsyncTask<String, Void, String> {

        private String ipAdd;



        @Override
        protected String doInBackground(String... urls) {

            ipAdd = urls[0];
            return GET(urls[0]);
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            //Toast.makeText(getBaseContext(), "Received!", Toast.LENGTH_LONG).show();
            //Toast.makeText(getBaseContext(), result, Toast.LENGTH_LONG).show();

            if(result.equals("Changed to OFF")|| result.equals("Changed to ON")) {
                Log.d("State", ipAdd + " " + result);
            }


        }
    }


    private static String intToIP(int ipAddress) {
        String ret = String.format("%d.%d.%d.%d", (ipAddress & 0xff),
                (ipAddress >> 8 & 0xff), (ipAddress >> 16 & 0xff),
                (ipAddress >> 24 & 0xff));

        return ret;
    }

    private static String longToIP(long ipAddress) {
        String ret = String.format("%d.%d.%d.%d", (ipAddress & 0xff),
                (ipAddress >> 8 & 0xff), (ipAddress >> 16 & 0xff),
                (ipAddress >> 24 & 0xff));

        return ret;
    }

    private static String parseIP( String ipAddress){

        char[] charArray = ipAddress.toCharArray();
        int i =0;
        for( int j=0; i< charArray.length;i++){

            if(charArray[i]=='/'){
                j++;
                if (j==3){
                    break;

                }
            }
        }

        return ipAddress.substring(0,i);

    }

    public void turnON( String ip){

        final String turnOnCommand = "/?State=ON";

        new HttpTurnOnOff().execute(ip+turnOnCommand);


    }


    public void turnOFF( String ip){

        final String turnOffCommand = "/?State=OFF";

        new HttpTurnOnOff().execute(ip+turnOffCommand);


    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
}
