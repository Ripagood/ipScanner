package com.sourcey.materiallogindemo;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.InputFilter;
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
import android.widget.TableLayout;
import android.widget.TableRow;
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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import butterknife.Bind;


public class MainActivity extends AppCompatActivity {

    //name of the saved used id var
    private static final String PREFS_NAME = "USER_INFORMATION";

    //Time out for the HTTP request
    //do not go under 500ms
    static final int  TIME_OUT =  1000;

    Context context;


    List<String> devicesByIp = new ArrayList<>(5);
    HashMap<String, String[]> devices = new HashMap<>();

    // name: NIckName , value = ip, key

    Integer numericDC;
    String deviceDutyCycle;
    String deviceIp;
    String deviceKey;
    String deviceNickName;
    String[] addresses= {"","",""};
    //ip,key,dc
    String UserId="elias v";
    String NumericUserId;// = "000002";
    final String serverURL = "http://khansystems.com/clienteQuery/index.php";

    Boolean serverConnection = Boolean.FALSE;


    List<AsyncTask> deviceScanner = new ArrayList<>(10);




    private ProgressDialog progress;

    //variable to store names
    private String m_Text = "";


    //@Bind(R.id.buttonScan) Button _scanButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = getApplicationContext();



//        NumericUserId =getIntent().getExtras().getString("NUMERIC_ID");




        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, 0);
        NumericUserId = prefs.getString("NUMERIC_ID", "");
        Log.d("numericid",NumericUserId);

        loadHashMap();
        printDevices2();



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

        //final LinearLayout lm = (LinearLayout) findViewById(R.id.linearLayoutMain);
        //lm.removeAllViews();
        final TableLayout tl = (TableLayout) findViewById(R.id.TableLayout01);
        tl.removeAllViews();

        for (Map.Entry<String, String[]> entry : devices.entrySet()) {
            deviceNickName = entry.getKey();
            String value[] = entry.getValue();
            deviceIp = value[0];
            deviceKey = value[1];
            deviceDutyCycle = value[2];
            // deviceKey = "666";
            //Create the LL to add a text view and a button
            /*
            LinearLayout ll = new LinearLayout(this);
            ll.setOrientation(LinearLayout.HORIZONTAL);
            ll.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            ll.setGravity(Gravity.CENTER);*/
            TableRow tr = new TableRow(this);
            tr.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, TableRow.LayoutParams.WRAP_CONTENT));

            final Button btnChangeName = new Button(this);
            btnChangeName.setText(deviceNickName);



            btnChangeName.setOnClickListener(new View.OnClickListener() {
                                                 @Override
                                                 public void onClick(View v) {
                                                     // put code on click operation
                                                     Log.d("Button PressedName", btnChangeName.getText().toString());
                                                     ChangeNameAlert(btnChangeName.getText().toString());

                                                 }
                                             }

            );

            btnChangeName.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
            tr.addView(btnChangeName);

            //ll.addView(btnChangeName);


            Button btnOn = new Button(this);
            btnOn.setText("ON");


            btnOn.setOnClickListener(new View.OnClickListener() {
                                         @Override
                                         public void onClick(View v) {
                                             String[] arr = devices.get(btnChangeName.getText().toString());
                                             deviceKey = arr[1];
                                             // put code on click operation
                                             Log.d("Button Pressed ON", deviceKey);
                                             deviceIp = arr[0];

                                             if (serverConnection == Boolean.TRUE) {

                                                 new HttpPOST_TurnON_OFF().execute(NumericUserId, deviceKey, "ON");

                                             } else {

                                                 turnON(deviceIp);
                                             }

                                         }
                                     }

            );

            //ll.addView(btnOn);
            btnOn.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
            tr.addView(btnOn);

            Button btnOFF = new Button(this);
            btnOFF.setText("OFF");



            btnOFF.setOnClickListener(new View.OnClickListener() {
                                          @Override
                                          public void onClick(View v) {
                                              String[] arr = devices.get(btnChangeName.getText().toString());
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

            //ll.addView(btnOFF);
            btnOFF.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
            tr.addView(btnOFF);


            final Button btnSettings = new Button(this);
            btnSettings.setText(":");



            btnSettings.setOnClickListener(new View.OnClickListener() {
                                               @Override
                                               public void onClick(View v) {
                                                   String[] arr = devices.get(btnChangeName.getText().toString());
                                                   deviceKey = arr[1];
                                                   // put code on click operation
                                                   Log.d("Button Pressed OFF", deviceKey);
                                                   Log.d("dc", arr[2]);
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
                                                               case R.id.PopUpDimmer:
                                                                   //Toast.makeText(getApplicationContext(),"Item 1 Selected",Toast.LENGTH_LONG).show();
                                                                   //RegisterDevices();
                                                                   ShowDialog(btnChangeName.getText().toString());
                                                                   return true;
                                                               case R.id.PopUpOn:
                                                                   //Toast.makeText(getApplicationContext(),"Item 2 Selected",Toast.LENGTH_LONG).show();
                                                                   //new  HttpPOST_LoadDevices().execute(NumericUserId);
                                                                   PopUpOn(btnChangeName.getText().toString());
                                                                   return true;
                                                               case R.id.PopUpOff:
                                                                   //WipeDevices();
                                                                   PopUpOFF(btnChangeName.getText().toString());
                                                                   return true;
                                                               case R.id.PopUpInfo:
                                                                   //WipeDevices();
                                                                   ///PopUpOFF(btnChangeName.getText().toString());
                                                                   ShowDialogInfo(btnChangeName.getText().toString());
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

            //ll.addView(btnSettings);
            btnSettings.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
            tr.addView(btnSettings);

            tl.addView(tr);





            //ll.setGravity(Gravity.CENTER);


            //lm.addView(ll);
            //lm.setGravity(Gravity.CENTER);

        }

        //saveHashMap();


    }

    private void PopUpOn (String key){

        String[] arr = devices.get(key);
        deviceKey = arr[1];
        deviceIp = arr[0];
        // put code on click operation
        Log.d("Button Pressed ON", deviceKey);

        if (serverConnection == Boolean.TRUE ) {

            new HttpPOST_TurnON_OFF().execute(NumericUserId,deviceKey,"ON");

        } else {

            turnON(deviceIp);
        }
    }
    private void PopUpOFF(String key){


        String[] arr = devices.get(key);
        deviceKey = arr[1];
        deviceIp = arr[0];
        // put code on click operation
        Log.d("Button Pressed OFF", deviceKey);
        if (serverConnection == Boolean.TRUE) {

            new HttpPOST_TurnON_OFF().execute(NumericUserId, deviceKey, "OF");

        } else {

            turnOFF(deviceIp);
        }



    }

    private void RegisterDevicesConfirm(){

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Register Devices");
        builder.setMessage("You are about to sync your devices to the cloud. Continue?");


// Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                WipeDevices(); //Delete from server, then register
                RegisterDevices();
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

    private void WipeSettingsConfirm(){

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Settings");
        builder.setMessage("You are about to wipe your settings. This will delete your local devices and user. Continue?");



// Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                WipeSettings();
                deleteHashMap();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        AlertDialog dialog = builder.show();
        TextView messageText = (TextView)dialog.findViewById(android.R.id.message);
        messageText.setGravity(Gravity.CENTER);
        dialog.show();
       // builder.show();



    }

    private void WipeDevicesConfirm(){

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Devices");
        builder.setMessage("You are about to delete your registered devices from the cloud. Continue?");


// Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                WipeDevices();
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

    private void ShowDialog(String key)
    {
        final AlertDialog.Builder popDialog = new AlertDialog.Builder(this);
        final SeekBar seek = new SeekBar(this);
        seek.setMax(100);
        final String deviceK = key;

        //popDialog.setIcon(android.R.drawable.btn_star_big_on);
        popDialog.setTitle("Select Intensity");
        popDialog.setView(seek);

        addresses = devices.get(key);
        seek.incrementProgressBy(Integer.parseInt(addresses[2]));


        seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                //Do something here with new value
                //txtView.setText("Value of : " + progress);
                //we must save the value for the seekbar on the hashmap and reload it
                numericDC = progress;

            }

            public void onStartTrackingTouch(SeekBar arg0) {
                // TODO Auto-generated method stub

            }

            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
                addresses = devices.get(deviceK);
                addresses[2] = Integer.toString(numericDC);
                //Log.d("length",Integer.toString(addresses.length));

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

    private void ShowDialogInfo(String key)
    {
        final AlertDialog.Builder popDialog = new AlertDialog.Builder(this);

        final String deviceK = key;

        //popDialog.setIcon(android.R.drawable.btn_star_big_on);
        popDialog.setTitle("Device Information");


        addresses = devices.get(key);

        popDialog.setMessage("IP: "+ addresses[0]+ "\n"+
                             "Intensity: "+ addresses[2]+ "\n"+
                              "Key: "+ addresses[1]);
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

    private void ChangeNameAlert(String device){


        deviceNickName = device;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Change Name");

// Set up the input
        final EditText input = new EditText(this);
// Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        int maxLength = 12;
        input.setFilters(new InputFilter[]{new InputFilter.LengthFilter(maxLength)});
        builder.setView(input);

// Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                m_Text = input.getText().toString();
                if(devices.containsKey(m_Text))
                {
                    Toast.makeText(getBaseContext(),"Device Name already selected. Please introduce another one." , Toast.LENGTH_LONG).show();

                }else
                {
                    ChangeNameHashMap(m_Text, deviceNickName);
                    printDevices2();
                }





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

        devices.put(NewKey, devices.get(OldKey));
        devices.remove(OldKey);
        saveHashMap();
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

        int i;



        //we scan every ip address for the devices until the broadcast address is reached

        progress=new ProgressDialog(this);

        progress.setMessage("Scanning network for Devices");
        progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        //progress.setIndeterminate(true);
        progress.setProgress(0);
        progress.setCanceledOnTouchOutside(false);
        progress.setCancelable(true);
        progress.setButton(ProgressDialog.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Cancel download task
                ScanDevices(null);
                progress.dismiss();
            }
        });
        progress.show();


        Log.d("IP CHECKED", intToIP(ipSubnet));
        for(i=0; invertIP(ipSubnet) <= invertIP(ipBroadcast)   ;ipSubnet+=0x01000000){

            // call AsynTask to perform network operation on separate thread
            i++;

            urlDevice = longToIP(ipSubnet);
            Log.d("IP CHECKED", urlDevice);
            deviceScanner.add(new HttpAsyncTask().execute(urlDiscovery + urlDevice + urlCommand));
            //new HttpAsyncTask().execute(urlDiscovery + urlDevice + urlCommand);
            // MakeRequestGet(urlDiscovery+urlDevice+urlCommand);
        }



        progress.setMax(i);




    }



    private static int invertIP( int ipAdd){

        byte[] hex = new byte[4];

        hex[0]= (byte)(ipAdd & 0xff);
        hex[1]= (byte)(ipAdd>>8 & 0xff);
        hex[2]= (byte)(ipAdd>>16 & 0xff);
        hex[3]= (byte)(ipAdd>>24 & 0xff);


        return ByteBuffer.wrap(hex).getInt();

    }

    private String GET3(String url){

        String response="";
        try {
            URL oracle = new URL(url);
            URLConnection yc = oracle.openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    yc.getInputStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null)response += inputLine;
                //System.out.println(inputLine);

            in.close();
        }
        catch(MalformedURLException e){
            response="malformedurl";
            Log.d("MalformedURl", e.getLocalizedMessage());

        }
        catch (SocketTimeoutException e){


            response = "timeout";
            Log.d("Connection timed out", e.getLocalizedMessage());

        }
        catch(IOException e){


            response = "ioexception";
            Log.d("IOException",e.getLocalizedMessage());
        }
        return  response;

    }

    private String GET2(String url){

        String response="";

        try{
            //Consider next request:
            HttpRequest req=new HttpRequest(url);
            // prepare http get request,  send to "http://host:port/path" and read server's response as String
            response=  req.prepare().sendAndReadString();
        }
        catch(MalformedURLException e){
            response="malformedurl";
            Log.d("MalformedURl", e.getLocalizedMessage());

        }
        catch (SocketTimeoutException e){


            response = "timeout";
            Log.d("Connection timed out", e.getLocalizedMessage());

        }
        catch(IOException e){


            response = "ioexception";
            Log.d("IOException",e.getLocalizedMessage());
        }
        return  response;

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
            response="malformedurl";
            Log.d("MalformedURl",e.getLocalizedMessage());
        }
        catch (SocketTimeoutException e){
            response="timeout";
            Log.d("Connection timed out", e.getLocalizedMessage());
        }
        catch(IOException e){
            response = "ioexception";
            Log.d("IOException",e.getLocalizedMessage());
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
            return GET4(urls[0]);
        }



        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            //Toast.makeText(getBaseContext(), "Received!", Toast.LENGTH_LONG).show();
            //Toast.makeText(getBaseContext(), result, Toast.LENGTH_LONG).show();

            progress.incrementProgressBy(1);
            if(progress.getMax() == progress.getProgress()){
                progress.dismiss();
                ScanDevices(null);
            }

            if(result.equals("ACCEPTED")){



                ipAdd = parseIP(ipAdd);


                if( hashmapContainsIP(ipAdd))
                {
                    Toast.makeText(getBaseContext(),"Device with IP "+ ipAdd +" already added", Toast.LENGTH_LONG).show();
                }
                else {

                    addresses[0] = ipAdd;//ipaddress

                    Random r = new Random();
                    int i1 = r.nextInt(999 - 0) + 0;

                    String convertedKey = intToKey(i1);

                    while (devices.containsValue(convertedKey) == Boolean.TRUE) {

                        i1 = r.nextInt(999 - 0) + 0;

                        convertedKey = intToKey(i1);
                    }
                    addresses[1] = convertedKey;//devicekey assigned
                    //Toast.makeText(getBaseContext(), ipDevice, Toast.LENGTH_SHORT).show();
                    addresses[2] = "100"; //Init with 100
                    Log.d("IP ACCEPTED", ipAdd);
                    devicesByIp.add(ipAdd);
                    String intermediateNickName="";
                    if(ipAdd.length()>11)
                    {
                        intermediateNickName=ipAdd.substring(ipAdd.length()-11,ipAdd.length());
                    }else
                    {
                        intermediateNickName=ipAdd;
                    }
                    devices.put(intermediateNickName, addresses);

                    //http://192.168.0.113/SETKEY?key=999
                    String urlCommand = "/SETKEY?=key";

                    //send the setkey command
                    //might have to be done at the end of the async tasks
                    //TODO test the new command and decide wether to relocate it or not to the end of the async tasks
                    new HttpAsyncTask().execute(ipAdd +  urlCommand + addresses[1]);
                    printDevices2();

                }


            }


        }

        private Boolean hashmapContainsIP(String ipToCheck)
        {
            Boolean retVal = Boolean.FALSE;
            for (Map.Entry<String, String[]> entry : devices.entrySet())
            {
                deviceNickName = entry.getKey();
                String value[] = entry.getValue();
                deviceIp = value[0];
                deviceKey = value[1];
                deviceDutyCycle = value[2];
                if(deviceIp.equals(ipToCheck))
                {
                    retVal = Boolean.TRUE;
                }
            }
            return retVal;

        }
    }

    private void setKeysToDevices()
    {
        // TODO Send the SETKEY command to each device on the network

        for (Map.Entry<String, String[]> entry : devices.entrySet())
        {
            deviceNickName = entry.getKey();
            String value[] = entry.getValue();
            deviceIp = value[0];
            deviceKey = value[1];
            deviceDutyCycle = value[2];

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
            return GET4(urls[0]);
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
            new HttpPOSTREGISTER().execute(NickName, deviceKey);

        }


    }

    private void WipeDevices(){

        //Deletes the device from the server

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
            String dc = "100";
            String[] values = {ip,key,dc};
            devices.put(nickname, values);


        }
        //Log.d("hs",devices.toString());

        printDevices2();

        //Log.d("hs", devices.toString());
        saveHashMap();


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


    private String RegisterDevice(String NickName, String key){


        String result="";
        try {
            HttpRequest req = new HttpRequest(serverURL);
            HashMap<String, String> params = new HashMap<>();
            params.put("AddDevice", key);
            params.put("NickName", NickName);
            params.put("User", UserId);
            result=req.preparePost().withData(params).sendAndReadString();
        }
        catch( SocketTimeoutException e){
            Log.d("ConnectionTimeOut",e.getLocalizedMessage());

        }
        catch(MalformedURLException e){
            Log.d("MalformedURl",e.getLocalizedMessage());
        }
        catch(IOException e){
            Log.d("IO",e.getLocalizedMessage());
        }
        Log.d("Http Post Response:", result);
        return result;
    }

    /*
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
    */

    private String POST_ON_OFF_Device(String userId,String key, String command){

        //http://khansystems.com/clienteQuery/index.php?Update=000002999ON
        //http://khansystems.com/clienteQuery/index.php?Update=000002999OF


        String response="";

        HashMap<String, String>params=new HashMap<>();
        params.put("Update", userId+key+command);

        try {
            //Consider next request:
            HttpRequest req = new HttpRequest(serverURL);

            response= req.preparePost().withData(params).sendAndReadString();
        }
        catch(SocketTimeoutException e){
            //Toast.makeText(getBaseContext(), "Connection TimedOut", Toast.LENGTH_SHORT).show();
            Log.d("connection","timeout");
        }
        catch (MalformedURLException e){

            response=e.getMessage();
        }
        catch (IOException e){
            Toast.makeText(getBaseContext(), "Device Not Found", Toast.LENGTH_LONG).show();
            response=e.getMessage();
        }

        return response;

    }


/*
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
    */

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
            Log.d("d","https POST " +key);


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


    private void WipeSettings(){


        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.commit(); //important, otherwise it wouldn't save.

    }

    public void deleteHashMap(){

        deleteFile("hs.bin");

    }
    public void saveHashMap(){

        try {


            FileOutputStream fileOutputStream = openFileOutput("hs.bin", Context.MODE_PRIVATE);
            ObjectOutputStream objectOutputStream= new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(devices);
            objectOutputStream.close();
        }
        catch(IOException e){
            e.printStackTrace();
        }

    }

    public void loadHashMap(){


        try {
            FileInputStream fileInputStream = openFileInput("hs.bin");
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);

            devices = (HashMap) objectInputStream.readObject();
            objectInputStream.close();
        }
        catch( IOException e){
            e.printStackTrace();

        }
        catch (ClassNotFoundException e){
            e.printStackTrace();
        }
    }





    public void StartActivityConnectAP()
    {
        Intent intent = new Intent(this, ConnectAP.class);
        startActivity(intent);

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
                //Toast.makeText(getApplicationContext(),"Item 1 Selected",Toast.LENGTH_LONG).show();
                RegisterDevicesConfirm();
                return true;
            case R.id.LoadDevices:
                //Toast.makeText(getApplicationContext(),"Item 2 Selected",Toast.LENGTH_LONG).show();
                new  HttpPOST_LoadDevices().execute(NumericUserId);
                return true;
            case R.id.WipeDevices:

                WipeDevicesConfirm();
                //DELETE DEVICES FROM SERVER
                return true;

            case R.id.action_settings:
                //DELETES LOCAL SETTINGS
                //WipeSettings();
                //deleteHashMap();
                WipeSettingsConfirm();
                return true;
            case R.id.ConnectAP:
                //DELETES LOCAL SETTINGS
                //WipeSettings();
                //deleteHashMap();
               StartActivityConnectAP();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }
}


