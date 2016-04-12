package com.sourcey.materiallogindemo;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

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
import java.util.Map;
import java.util.Random;

import butterknife.Bind;


public class MainActivity extends AppCompatActivity {


    //Time out for the HTTP request
    //do not go under 500ms
    static final int  TIME_OUT =  1000;

    Context context;


    List<String> devicesByIp = new ArrayList<>(5);
    HashMap<String, String[]> devices = new HashMap<>();

    // name: NIckName , value = ip, key

    String deviceIp;
    String deviceKey;
    String deviceNickName;
    String[] addresses= {"",""};

    String UserId="elias v";
    String NumericUserId = "000002";
    String serverURL = "http://khansystems.com/clienteQuery/index.php";

    Boolean serverConnection = Boolean.FALSE;


    List<AsyncTask> deviceScanner = new ArrayList<>(10);





    //variable to store names
    private String m_Text = "";


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
        //pressing the button toggles the text and the function
        final Button btn = (Button) findViewById(R.id.buttonScan);

        if(btn.getText().equals("Stop Scan"))
        {
            //we must stop the async tasks
            for( AsyncTask asyncTask: deviceScanner){

                asyncTask.cancel(true);


            }
            btn.setText("Scan Devices");



        }else// we must scan the devices
        {
            btn.setText("Stop Scan");
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


            DeviceScan(dhcp.ipAddress & dhcp.netmask, ~dhcp.netmask | (dhcp.ipAddress & dhcp.netmask));
            // call AsynTask to perform network operation on separate thread
            // new HttpAsyncTask().execute("http://192.168.0.25/KHAN?");
        }









    }
    //Intended for debugging purposes only
    public void Connection(View view){

        //pressing the button toggles the text and the function
        final Button btn = (Button) findViewById(R.id.ButtonConnection);

        if(btn.getText().equals("Connect to Server"))
        {
            btn.setText("Disconnect");
            serverConnection = Boolean.TRUE;


        }else
        {
            btn.setText("Connect to Server");
            serverConnection = Boolean.FALSE;
        }

    }

    private void printDevices2(){

        //select the linear layout defined in the xml

        final LinearLayout lm = (LinearLayout) findViewById(R.id.linearLayoutMain);
        lm.removeAllViews();

        for (Map.Entry<String, String[]> entry : devices.entrySet()) {
            deviceNickName = entry.getKey();
            String value[] = entry.getValue();
            deviceIp = value[0];
            deviceKey = value[1];
            // deviceKey = "666";
            //Create the LL to add a text view and a button
            LinearLayout ll = new LinearLayout(this);
            ll.setOrientation(LinearLayout.HORIZONTAL);
            ll.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            ll.setGravity(Gravity.CENTER);


            final Button btnChangeName = new Button(this);
            btnChangeName.setText(deviceNickName);



            btnChangeName.setOnClickListener(new View.OnClickListener() {
                                                 @Override
                                                 public void onClick(View v) {
                                                     // put code on click operation
                                                     Log.d("Button PressedName", deviceNickName);
                                                     ChangeNameAlert(deviceNickName);

                                                 }
                                             }

            );

            ll.addView(btnChangeName);


            Button btnOn = new Button(this);
            btnOn.setText("ON");


            btnOn.setOnClickListener(new View.OnClickListener() {
                                         @Override
                                         public void onClick(View v) {
                                             String[] arr = devices.get(btnChangeName.getText());
                                             deviceKey = arr[1];
                                             // put code on click operation
                                             Log.d("Button Pressed ON", deviceKey);

                                             if (serverConnection == Boolean.TRUE ) {

                                                 new HttpPOST_TurnON_OFF().execute(NumericUserId,deviceKey,"ON");

                                             } else {

                                                 turnON(deviceIp);
                                             }

                                         }
                                     }

            );

            ll.addView(btnOn);

            Button btnOFF = new Button(this);
            btnOFF.setText("OFF");



            btnOFF.setOnClickListener(new View.OnClickListener() {
                                          @Override
                                          public void onClick(View v) {
                                              String[] arr = devices.get(btnChangeName.getText());
                                              deviceKey = arr[1];
                                              // put code on click operation
                                              Log.d("Button Pressed OFF", deviceKey);
                                              if (serverConnection == Boolean.TRUE) {

                                                  new HttpPOST_TurnON_OFF().execute(NumericUserId, deviceKey, "OF");

                                              } else {

                                                  turnOFF(deviceIp);
                                              }
                                          }
                                      }

            );

            ll.addView(btnOFF);


            final Button btnSettings = new Button(this);
            btnSettings.setText(":");



            btnSettings.setOnClickListener(new View.OnClickListener() {
                                               @Override
                                               public void onClick(View v) {
                                                   String[] arr = devices.get(btnChangeName.getText());
                                                   deviceKey = arr[1];
                                                   // put code on click operation
                                                   Log.d("Button Pressed OFF", deviceKey);
                                                   //Creating the instance of PopupMenu
                                                   PopupMenu popup = new PopupMenu(MainActivity.this, btnSettings);
                                                   //Inflating the Popup using xml file
                                                   popup.getMenuInflater().inflate(R.menu.popupmenu, popup.getMenu());

                                                   //registering popup with OnMenuItemClickListener
                                                   popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                                                       public boolean onMenuItemClick(MenuItem item) {
                                                           //Toast.makeText(MainActivity.this,"You Clicked : " + deviceKey,Toast.LENGTH_SHORT).show();
                                                           //return true;
                                                           switch (item.getItemId()) {
                                                               case R.id.Dimmer:
                                                                   //Toast.makeText(getApplicationContext(),"Item 1 Selected",Toast.LENGTH_LONG).show();
                                                                   //RegisterDevices();
                                                                   ShowDialog(deviceKey);
                                                                   return true;
                                                               case R.id.Save:
                                                                   //Toast.makeText(getApplicationContext(),"Item 2 Selected",Toast.LENGTH_LONG).show();
                                                                   //new  HttpPOST_LoadDevices().execute(NumericUserId);
                                                                   return true;
                                                               case R.id.Delete:
                                                                   //WipeDevices();
                                                                   return true;

                                                               case R.id.action_settings:
                                                                   return true;
                                                               default:
                                                                   return true;
                                                           }
                                                       }
                                                   });

                                                   popup.show();//showing popup menu
                                               }
                                           }

            );

            ll.addView(btnSettings);




            ll.setGravity(Gravity.CENTER);


            lm.addView(ll);
            lm.setGravity(Gravity.CENTER);

        }


    }


    private void ShowDialog(String key)
    {
        final AlertDialog.Builder popDialog = new AlertDialog.Builder(this);
        final SeekBar seek = new SeekBar(this);
        seek.setMax(100);

        popDialog.setIcon(android.R.drawable.btn_star_big_on);
        popDialog.setTitle("Please Select "+ key);
        popDialog.setView(seek);

        seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser){
                //Do something here with new value
                //txtView.setText("Value of : " + progress);
                //we must save the value for the seekbar on the hashmap and reload it

            }

            public void onStartTrackingTouch(SeekBar arg0) {
                // TODO Auto-generated method stub

            }

            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub

            }
        });


        // Button OK
        popDialog.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }

                });


        popDialog.create();
        popDialog.show();

    }

    private void ChangeNameAlert(final String device){



        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Change Name");

// Set up the input
        final EditText input = new EditText(this);
// Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

// Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                m_Text = input.getText().toString();
                ChangeNameHashMap(m_Text, device);
                printDevices2();




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

    private void ChangeNameHashMap(String NewKey, String OldKey){

        devices.put(NewKey,devices.get(OldKey));
        devices.remove(OldKey);
    }

    //receives the network ip address in order to find the devices
    // returns a hashmap containing ip adress of the device and an ID
    // public  HashMap DeviceScan(int ipSubnetInteger, int ipBroadcastInteger){


    private   void  DeviceScan(int ipSubnet, int ipBroadcast){

        //long ipSubnet = ((long) ipSubnetInteger);
        //long ipBroadcast = ((long) ipBroadcastInteger);
        //
        //

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
            deviceScanner.add(new HttpAsyncTask().execute(urlDiscovery + urlDevice + urlCommand));
            //new HttpAsyncTask().execute(urlDiscovery + urlDevice + urlCommand);
            // MakeRequestGet(urlDiscovery+urlDevice+urlCommand);
        }



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

    private static String convertInputStreamToString(InputStream inputStream) throws IOException{
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while((line = bufferedReader.readLine()) != null)
            result += line;

        inputStream.close();
        return result;

    }

    private class HttpAsyncTask extends AsyncTask<String, Void, String> {

        private String ipAdd;

        private String intToKey(int key){

            String res="";

            res= Integer.toString(key);

            if(res.length()==1){
                res= "00"+res;
            }else if(res.length()==2){
                res= "0"+res;
            }

            return res;

        }



        @Override
        protected void onCancelled(){

            //Do nothing?

        }
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

                ipAdd = parseIP(ipAdd);

                addresses[0]=ipAdd;//ipaddress

                Random r = new Random();
                int i1 = r.nextInt(999 - 0) + 0;

                String convertedKey= intToKey(i1);

                while(devices.containsValue(convertedKey)== Boolean.TRUE){

                    i1 = r.nextInt(999 - 0) + 0;

                    convertedKey= intToKey(i1);
                }
                addresses[1]=convertedKey;//devicekey assigned
                //Toast.makeText(getBaseContext(), ipDevice, Toast.LENGTH_SHORT).show();
                Log.d("IP ACCEPTED", ipAdd);
                devicesByIp.add(ipAdd);
                devices.put(ipAdd,addresses);
                printDevices2();

            }


        }
    }


    private class HttpWipe extends AsyncTask<String, Void, String> {





        @Override
        protected String doInBackground(String... urls) {


            return GET(serverURL+urls[0]+urls[1]);
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            //Toast.makeText(getBaseContext(), "Received!", Toast.LENGTH_LONG).show();
            //Toast.makeText(getBaseContext(), result, Toast.LENGTH_LONG).show();


            Log.d("State", result);



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

    public void RegisterDevices( ){
        String NickName;
        String deviceKey;

        for (Map.Entry<String, String[]> entry : devices.entrySet()) {
            NickName = entry.getKey();
            String value[] = entry.getValue();
            //deviceIp = value[0];
            deviceKey = value[1];
            new HttpPOSTREGISTER().execute(NickName,deviceKey);

        }


    }

    private void WipeDevices(){



        for (Map.Entry<String, String[]> entry : devices.entrySet()) {
            String NickName = entry.getKey();
            String value[] = entry.getValue();
            String key = value[1];
            WipeDevice(key);

        }

    }

    private void WipeDevice(String key){

        //http://khansystems.com/clienteQuery/index.php?DeleteDevice=000002999

        new HttpWipe().execute(NumericUserId,key);
    }

    private String LoadDevices (String NumUserId){

        // http://khansystems.com/clienteQuery/index.php?GetDevices=000002

        String url;
        String result="";

        url = serverURL + "?GetDevices="+ NumUserId;
        result = GET(url);



        return  result;

    }

    //parses and sets the Devices from the http request
    public void SetDevices( String UnparsedDevices){
        String[] Devices = UnparsedDevices.split(";");
        String[] params;

        devices.clear();


        for (int i= 0;i < Devices.length -1;i++){

            params = Devices[i].split(",");
            //Log.d("d", Integer.toString(params.length));

            String nickname = params[1];
            String key = params[0];
            String ip = params[0];
            String[] values = {ip,key};
            devices.put(nickname, values);


        }

        printDevices2();


    }


    private class HttpPOST_LoadDevices extends AsyncTask<String, Void, String> {

        private String NumUserId;

        @Override
        protected String doInBackground(String... urls) {

            NumUserId = urls[0];

            return LoadDevices(NumUserId);
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            Toast.makeText(getBaseContext(), "Received!", Toast.LENGTH_LONG).show();
            //Toast.makeText(getBaseContext(), result, Toast.LENGTH_LONG).show();
            Log.d("Http Post Response:", result);
            SetDevices(result);



        }
    }

    private String RegisterDevice( String NickName, String key){

        String result="";

        HttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(serverURL);

        List<NameValuePair> nameValuePair = new ArrayList<NameValuePair>(3);
        nameValuePair.add(new BasicNameValuePair("AddDevice", key));
        nameValuePair.add(new BasicNameValuePair("NickName", NickName));
        nameValuePair.add(new BasicNameValuePair("User", UserId));

        //Encoding POST data
        try {
            httpPost.setEntity(new UrlEncodedFormEntity(nameValuePair));

        } catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
        }

        try {
            HttpResponse response = httpClient.execute(httpPost);
            // write response to log
            result = response.toString();
            Log.d("Http Post Response:", result);
        } catch (ClientProtocolException e) {
            // Log exception
            e.printStackTrace();
        } catch (IOException e) {
            // Log exception
            e.printStackTrace();
        }

        return  result;
    }



    private String POST_ON_OFF_Device( String userId, String key, String command){



        //http://khansystems.com/clienteQuery/index.php?Update=000002999ON
        //http://khansystems.com/clienteQuery/index.php?Update=000002999OF
        String result="";

        HttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(serverURL);

        List<NameValuePair> nameValuePair = new ArrayList<NameValuePair>(1);
        nameValuePair.add(new BasicNameValuePair("Update", userId+key+command));


        //Encoding POST data
        try {
            httpPost.setEntity(new UrlEncodedFormEntity(nameValuePair));

        } catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
        }

        try {
            HttpResponse response = httpClient.execute(httpPost);
            // write response to log
            result = response.toString();
            Log.d("Http Post Response:", result);
        } catch (ClientProtocolException e) {
            // Log exception
            e.printStackTrace();
        } catch (IOException e) {
            // Log exception
            e.printStackTrace();
        }

        return  result;
    }

    private class HttpPOST_TurnON_OFF extends AsyncTask<String, Void, String> {

        private String UserId;
        private String key;
        private String command; //ON OF



        @Override
        protected String doInBackground(String... urls) {

            UserId = urls[0];
            key = urls[1];
            command = urls[2];
            return POST_ON_OFF_Device(UserId, key, command);
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            //Toast.makeText(getBaseContext(), "Received!", Toast.LENGTH_LONG).show();
            //Toast.makeText(getBaseContext(), result, Toast.LENGTH_LONG).show();
            Log.d("d",key);


        }
    }


    private class HttpPOSTREGISTER extends AsyncTask<String, Void, String> {

        private String Nickname;
        private String key;



        @Override
        protected String doInBackground(String... urls) {

            Nickname = urls[0];
            key = urls[1];
            return RegisterDevice(Nickname, key);
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            //Toast.makeText(getBaseContext(), "Received!", Toast.LENGTH_LONG).show();
            //Toast.makeText(getBaseContext(), result, Toast.LENGTH_LONG).show();


        }
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

        /*
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
*/
        switch (item.getItemId()) {
            case R.id.SaveDevices:
                Toast.makeText(getApplicationContext(),"Item 1 Selected",Toast.LENGTH_LONG).show();
                RegisterDevices();
                return true;
            case R.id.LoadDevices:
                Toast.makeText(getApplicationContext(),"Item 2 Selected",Toast.LENGTH_LONG).show();
                new  HttpPOST_LoadDevices().execute(NumericUserId);
                return true;
            case R.id.WipeDevices:
                WipeDevices();
                return true;

            case R.id.action_settings:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }
}


