package com.sourcey.materiallogindemo;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.internal.view.ContextThemeWrapper;
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

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;

import org.apache.http.client.methods.HttpGet;

import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.SocketTimeoutException;

import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeoutException;

import javax.xml.transform.sax.SAXSource;

import butterknife.OnLongClick;

import static com.sourcey.materiallogindemo.UPnPDiscovery.discoverDevices;
import static com.sourcey.materiallogindemo.UPnPDiscovery.stopDiscovery;


public class MainActivity extends AppCompatActivity {

    //name of the saved used id var
    private static final String PREFS_NAME = "USER_INFORMATION";

    public String LOGIN="";
    /*Used for filtering button presses*/
    /*Allow ON or OFF command only if we have received a response already*/
    private Boolean filterButton = Boolean.FALSE;

    Boolean fastScan = Boolean.FALSE;

    //Time out for the HTTP request
    //do not go under 500ms
    static final int  TIME_OUT =  1000;

    Context context;

    HashMap<String, HashMap> users = new HashMap<>();

    //Used for storing the devices which must get a Key

    List<String> devicesForKey = new ArrayList<>(5);


    List<String> devicesByIp = new ArrayList<>(5);
    static HashMap<String, String[]> devices = new HashMap<>();

    // name: NIckName , value = ip, key

    Integer numericDC;
    static  String deviceDutyCycle;
    static String deviceIp;
    static String deviceKey;
    static String deviceNickName;
    static String[] addresses= {"","",""};
    //ip,key,dc
    public static String UserId="";
    public static String NumericUserId="";// = "000002";


    public static String UserPassword="";
    //public final static String serverURL = "http://khansystems.com/clienteQuery/index.php";
    public final static String serverURL = "http://gruporyrintegradores.com/LeafLife/clienteQuery/index.php";

    Boolean serverConnection = Boolean.FALSE;


    List<AsyncTask> deviceScanner = new ArrayList<>(10);




    private ProgressDialog progress;

    //variable to store names
    private String m_Text = "";


    //@Bind(R.id.buttonScan) Button _scanButton;

    private NsdManager mNsdManager;
    private NsdManager.DiscoveryListener mDiscoveryListener;
    private NsdManager.ResolveListener mResolveListener;
    private NsdServiceInfo mServiceInfo;
    public String mRPiAddress;

    // The NSD service type that the RPi exposes.
    private static final String SERVICE_TYPE = "_http._tcp.";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = getApplicationContext();



//        NumericUserId =getIntent().getExtras().getString("NUMERIC_ID");

        //discoverDevices(context);
       // discoverDevices(this);





        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, 0);

        UserId = prefs.getString("USER_ID", "");
        UserPassword = prefs.getString("PASSWORD","");
        NumericUserId = prefs.getString("NUMERIC_ID", "");
        LOGIN = prefs.getString("LOGIN","");

        Log.d("user_id",UserId);
        Log.d("password",UserPassword);
        Log.d("numeric_id", NumericUserId);
        Log.i("LoginState", LOGIN);

        //loadHashMap();
        //THIS SHOULD BE DONE AFTER GETTING THE ID FROM THE LOGIN
        //if we skip the login then load them from here
        //loadUsersHashMap();
        //printDevices2();

        SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        boolean stayLoggedIn = SP.getBoolean("stayLoggedIn", false);


        if(stayLoggedIn == Boolean.TRUE)
        {

            /*Skip login activity*/
            /*Use last saved credentials*/


            createInitialUI();

        }else
        {
            /*We need a new authentication*/
            //startService(new Intent(this, TimeService.class));

            Intent intent = new Intent(this, LoginActivity.class);
            // startActivity(intent);
            startActivityForResult(intent, 0);
            Log.d("afterLogin", "activity");

        }









    }

    public static void SetRemoteDevices( String remote)
    {
        //TODO set remote state to every device on the network
        Log.d("remote", remote);
        //http://192.168.0.113/REMOTE?UserID=000002
        //http://192.168.0.113/NOREMOTE
        if(remote.equals("ON"))
        {
            //give credentials
            Log.d("device", "/REMOTE?UserID=" + NumericUserId);
            DeviceForEach("/REMOTE?UserID=" + NumericUserId);


        }else if(remote.equals("OFF"))
        {
            //remove credentials
            //give credentials
            Log.d("device", "/NOREMOTE");
            //DeviceForEach("/NOREMOTE");
            //new HttpCommand().execute(ip + urlCommand + NumericUserId + convertedKey);
        }else
        {

            /*MISRA*/
            //do nothing
        }

    }



    private static void DeviceForEach(String command)
    {
        for (Map.Entry<String, String[]> entry : devices.entrySet()) {
            deviceNickName = entry.getKey();
            String value[] = entry.getValue();
            deviceIp = value[0];
            deviceKey = value[1];
            deviceDutyCycle = value[2];

            Log.d("device", command);
            new HttpCommand().execute(deviceIp + command);

        }


    }

    //check for wifi connection
    private Boolean wifiConected()
    {
        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        return mWifi.isConnected();
    }


    public void ScanDevices(View view){
        //pressing the button toggles the text and the function
        final Button btn = (Button) findViewById(R.id.buttonScan);


        SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        fastScan = SP.getBoolean("fastScan",false);

        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        if (!mWifi.isConnected()) {
            // Do whatever
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Wifi Connection");
            builder.setMessage("You must be connected to a wifi network in order to scan devices");


// Set up the buttons
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    //nothing to do MISRA
                }
            });

            builder.setCancelable(false);


            //builder.show();
            AlertDialog dialog = builder.show();
            TextView messageText = (TextView)dialog.findViewById(android.R.id.message);
            messageText.setGravity(Gravity.CENTER);
            dialog.show();

        }else {

            if (btn.getText().equals("Stop Scan")) {
                //we must stop the async tasks
                btn.setText("Scan Devices");
                if(fastScan == false)
                {

                for (AsyncTask asyncTask : deviceScanner) {

                    asyncTask.cancel(true);


                  }
                }




            } else// we must scan the devices
            {

                if(fastScan == true)
                {
                    Runnable r = new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            // your code here
                            WifiManager wifi = (WifiManager)context.getSystemService( context.getApplicationContext().WIFI_SERVICE );

                            if(wifi != null) {

                                WifiManager.MulticastLock lock = wifi.createMulticastLock("The Lock");
                                lock.acquire();
                                Log.i("ssdp","here1");

                                DatagramSocket socket = null;

                                try {

                                    InetAddress group = InetAddress.getByName("239.255.255.250");
                                    int port = 1900;
                                    String query =
                                            "M-SEARCH * HTTP/1.1\r\n" +
                                                    "HOST: 239.255.255.250:1900\r\n"+
                                                    "MAN: \"ssdp:discover\"\r\n"+
                                                    "MX: 1\r\n"+
                                                    // "ST: urn:schemas-upnp-org:service:AVTransport:1\r\n"+  // Use for Sonos
                                                    "ST:LifLyfeCorp\r\n"+  // Use this for all UPnP Devices
                                                    "\r\n";

                                    socket = new DatagramSocket();
                                    socket.setReuseAddress(true);
                                    Log.i("ssdp", "here2");

                                    DatagramPacket dgram = new DatagramPacket(query.getBytes(), query.length(),
                                            group, port);
                                    socket.send(dgram);

                                    long time = System.currentTimeMillis();
                                    long curTime = time + 5000;

                                    Log.i("ssdp","here3");
                                    // Let's consider all the responses we can get in 10 seconds
                                    for(int i=0; i<50000;i++) {

// Do your stuff

                                        DatagramPacket p = new DatagramPacket(new byte[256],256);
                                        socket.receive(p);


                                        String s = new String(p.getData(), 0, p.getLength());
                                        Log.i("ssdp",s);
                                        if (s.contains("SERVER: KhanSystems")) {

                                            int first = s.indexOf("KhanSystems");
                                            int last = s.indexOf("USN:");
                                            String deviceInfo = s.substring(first, last);
                                            Log.i("ssdp", deviceInfo);
                                            String[] parts = deviceInfo.split("/");
                                            parseDevices(parts[2],parts[3]);
                                            // addresses.add(p.getAddress().getHostAddress());

                                        }

                                        time = System.currentTimeMillis();
                                        Log.i("ssdp","time");
                                    }
                                    Log.i("ssdp","finishednow");


                                } catch (UnknownHostException e) {
                                    e.printStackTrace();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                finally {
                                    if (socket != null) {
                                        socket.close();
                                        Log.i("ssdp", "herenotnull");
                                    }
                                }
                                lock.release();
                            }

                        }
                    };

                    Thread t = new Thread(r);
                    t.start();


                    Log.d("here", "herescanbutton");
                }else {


            /*
        WifiManager wifiMan = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInf = wifiMan.getConnectionInfo();
        int ipAddress = wifiInf.getIpAddress();
        String ip = String.format("%d.%d.%d.%d", (ipAddress & 0xff),(ipAddress >> 8 & 0xff),(ipAddress >> 16 & 0xff),(ipAddress >> 24 & 0xff));
        toast.setText(ip);
*/              btn.setText("Stop Scan");
                // Only works when NOT tethering
                WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);


                DhcpInfo dhcp = wifi.getDhcpInfo();

                Log.d("IP", intToIP(dhcp.ipAddress));


                    int netmask = logLocalIpAddresses();



                //display network info
               /*Toast.makeText(getBaseContext(), intToIP(dhcp.ipAddress) , Toast.LENGTH_SHORT).show();
                Toast.makeText(getBaseContext(), intToIP(dhcp.netmask) , Toast.LENGTH_SHORT).show();
                Toast.makeText(getBaseContext(), intToIP(dhcp.ipAddress & dhcp.netmask) , Toast.LENGTH_SHORT).show();
                Toast.makeText(getBaseContext(), intToIP(~dhcp.netmask | (dhcp.ipAddress & dhcp.netmask)) , Toast.LENGTH_SHORT).show();

*/
                    Toast.makeText(getBaseContext(), intToIP(dhcp.ipAddress) , Toast.LENGTH_SHORT).show();
                    Toast.makeText(getBaseContext(), intToIP(netmask) , Toast.LENGTH_SHORT).show();
                    Toast.makeText(getBaseContext(), intToIP(dhcp.ipAddress & netmask) , Toast.LENGTH_SHORT).show();
                    Toast.makeText(getBaseContext(), intToIP(~netmask | (dhcp.ipAddress & netmask)) , Toast.LENGTH_SHORT).show();

                    //TODO change the netmask calculation method, its bugged on android 4+

                DeviceScan(dhcp.ipAddress & netmask, ~netmask | (dhcp.ipAddress & netmask));
                // call AsynTask to perform network operation on separate thread
                // new HttpAsyncTask().execute("http://192.168.0.25/KHAN?");

                }
            }
        }

    }


    public int logLocalIpAddresses() {
        int mask = 0;
        Enumeration<NetworkInterface> nwis;
        try {
            nwis = NetworkInterface.getNetworkInterfaces();
            while (nwis.hasMoreElements()) {

                NetworkInterface ni = nwis.nextElement();
                for (InterfaceAddress ia : ni.getInterfaceAddresses())
                {

                    byte[] arr = ia.getAddress().getAddress();

                    if ( arr.length == 4 &&
                            ni.getDisplayName().equals("wlan0"))
                    {
                        //only handle IPV4
                        Log.i("ipAdd", String.format("%s: %s/%d",
                                ni.getDisplayName(), ia.getAddress(), ia.getNetworkPrefixLength()));
                         mask = (0xffffffff) >>> (32 - ia.getNetworkPrefixLength());
                        Log.i("ipAdd",intToIP(mask));



                    }


                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return mask;
    }

    /*Used for adding devices discovered with fast scan*/


    public void parseDevices(String name, String ip) {

        //remove newline
        ip = ip.replace("\n", "").replace("\r", "");
        ip = "http://"+ip;
        final String displayName = name;
        /*If the name and the ip are already in our hashmap, then dont initiate remote strategy*/
        if (hashmapContainsIP(ip) && devices.containsKey(name)) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getBaseContext(), "Device " + displayName +" already added", Toast.LENGTH_SHORT).show();
//stuff that updates ui

                }
            });


        } else {

            addresses[0] = ip;//ipaddress

            Random r = new Random();
            int i1 = r.nextInt(999 - 0) + 0;

            String convertedKey = intToKey(i1);

            while (devices.containsValue(convertedKey) == Boolean.TRUE) {

                i1 = r.nextInt(999 - 0) + 0;

                convertedKey = intToKey(i1);
            }
            addresses[1] = convertedKey;//devicekey assigned
            //Toast.makeText(getBaseContext(), ipDevice, Toast.LENGTH_SHORT).show();
            addresses[2] = "10"; //Init with 100
            Log.d("IP ACCEPTED", ip);
            devicesByIp.add(ip);

            devices.put(name, addresses);


            //Add the devices to a list
            //When the progress dialog gets cancelled or stops
            //We must send the keys
            devicesForKey.add(name);


            //http://192.168.100.17/SETKEY?key=444

            //http://192.168.0.113/SETKEY?key=999
            // String urlCommand = "/SETKEY?key=";

            //http://192.168.0.113/REMOTE?UserID=000002999
            String urlCommand = "/REMOTE?UserID=";

            //send the setkey command
            //might have to be done at the end of the async tasks
            //TODO test the new command and decide whether to relocate it or not to the end of the async tasks
            // new HttpAsyncTask().execute(ipAdd +  urlCommand + convertedKey);


            //If we are logged in, we must send the device parameters , i.e. numeric user id and key
            //this means we can only have 1 user for device
            //the app doesnt send the parameters if we are not logged in
            if (!LOGIN.equals("FALSE")) {
                new HttpCommand().execute(ip + urlCommand + NumericUserId + convertedKey);
            }

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getBaseContext(), "Device Added", Toast.LENGTH_SHORT).show();
                }
            });
            /* save the added devices */
            saveUsersHashMap();

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    printDevices2();

//stuff that updates ui

                }
            });


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

    private void SetConnectionToServer()
    {
        //pressing the button toggles the text and the function
        final Button btn = (Button) findViewById(R.id.ButtonConnection);

        btn.setText("Disconnect");
        serverConnection = Boolean.TRUE;

    }

    private boolean isOnline() {
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }



    private void printDevices2(){

       // ContextThemeWrapper newContext = new ContextThemeWrapper(context, R.style.AppTheme_Button);
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

            //ContextThemeWrapper newContext = new ContextThemeWrapper(getBaseContext(),R.style.ButtonTheme );
            final Button btnChangeName = new Button(this);
            btnChangeName.getBackground().setColorFilter(getResources().getColor(R.color.primary_darker), PorterDuff.Mode.MULTIPLY);
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

            btnChangeName.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    // TODO Auto-generated method stub
                    Log.d("Button Long press", btnChangeName.getText().toString());
                    deleteDeviceShowDialog(btnChangeName.getText().toString());
                    return true;
                }
            });

            btnChangeName.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
            tr.addView(btnChangeName);

            //ll.addView(btnChangeName);


            Button btnOn = new Button(this);
            btnOn.getBackground().setColorFilter(getResources().getColor(R.color.primary_darker), PorterDuff.Mode.MULTIPLY);
            btnOn.setText("ON");


            btnOn.setOnClickListener(new View.OnClickListener() {
                                         @Override
                                         public void onClick(View v) {


                                             PopUpOn(btnChangeName.getText().toString());
                                             /*
                                             String[] arr = devices.get(btnChangeName.getText().toString());
                                             deviceKey = arr[1];
                                             // put code on click operation
                                             Log.d("Button Pressed ON", deviceKey);
                                             deviceIp = arr[0];

                                             if (serverConnection == Boolean.TRUE) {

                                                 new HttpPOST_TurnON_OFF().execute(NumericUserId, deviceKey, "ON");

                                             } else {

                                                 turnON(deviceIp);
                                             }*/

                                         }
                                     }

            );

            //ll.addView(btnOn);
            btnOn.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
            tr.addView(btnOn);

            Button btnOFF = new Button(MainActivity.this);
            btnOFF.getBackground().setColorFilter(getResources().getColor(R.color.primary_darker), PorterDuff.Mode.MULTIPLY);
            btnOFF.setText("OFF");



            btnOFF.setOnClickListener(new View.OnClickListener() {
                                          @Override
                                          public void onClick(View v) {


                                              PopUpOFF(btnChangeName.getText().toString());
                                              /*
                                              String[] arr = devices.get(btnChangeName.getText().toString());
                                              deviceKey = arr[1];
                                              // put code on click operation
                                              Log.d("Button Pressed OFF", deviceKey);
                                              if (serverConnection == Boolean.TRUE) {

                                                  new HttpPOST_TurnON_OFF().execute(NumericUserId, deviceKey, "OF");

                                              } else {

                                                  turnOFF(deviceIp);
                                              }
                                              */
                                          }
                                      }

            );

            //ll.addView(btnOFF);
            btnOFF.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
            tr.addView(btnOFF);


            final Button btnSettings = new Button(this);
            btnSettings.getBackground().setColorFilter(getResources().getColor(R.color.primary_darker), PorterDuff.Mode.MULTIPLY);
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
                                                                   //Toast.makeText(getApplicationContext(),"Item 1 Selected",Toast.LENGTH_SHORT).show();
                                                                   //RegisterDevices();
                                                                   ShowDialog(btnChangeName.getText().toString());
                                                                   return true;
                                                               case R.id.PopUpOn:
                                                                   //Toast.makeText(getApplicationContext(),"Item 2 Selected",Toast.LENGTH_SHORT).show();
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

                                                               case R.id.PopUpRemote:
                                                                   //WipeDevices();
                                                                   ///PopUpOFF(btnChangeName.getText().toString());
                                                                   ShowDialogRemote(btnChangeName.getText().toString());
                                                                   return true;

                                                               case R.id.PopUpRemove:
                                                                   //WipeDevices();
                                                                   ///PopUpOFF(btnChangeName.getText().toString());
                                                                   ShowDialogRemove(btnChangeName.getText().toString());
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

            btnSettings.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    // Secret method
                    // will reset the device
                    Log.d("Settings Long press", btnChangeName.getText().toString());

                   resetDeviceDialog(btnChangeName.getText().toString());

                    return true;
                }
            });

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

    /*  Used for the dimmer function */

    private void sendIntensity(String ip, String intensity)
    {
        String uri;
        uri = ip + "/INTENSITY?pwm=" + intensity;
        /*Copied from PopUPon, may need check*/
        if(!wifiConected())
        {
            SetConnectionToServer();
            //will affect serverConnection variable

        }

        if((isOnline() && serverConnection) || wifiConected())
        {

            /*
            String[] arr = devices.get(key);
            deviceKey = arr[1];
            deviceIp = arr[0];
            // put code on click operation
            */
            Log.d("Set intensity", intensity);

            if (serverConnection == Boolean.TRUE ) {

                if(filterButton == Boolean.FALSE)
                {
                    filterButton = Boolean.TRUE;
                    //If we decide to send intensity by server we must add it here
                    //new HttpPOST_TurnON_OFF().execute(NumericUserId,deviceKey,"ON");
                }


            } else {
                Log.d("Intenstiy", uri);
                new HttpIntensity().execute(uri);
            }

        }else
        {

            if(!wifiConected())
            {
                Toast.makeText(getApplicationContext(),"Wifi must be active!",Toast.LENGTH_SHORT).show();
            }

            if(!isOnline())
            {
                Toast.makeText(getApplicationContext(),"Not online!",Toast.LENGTH_SHORT).show();
            }


        }

    }

    private void PopUpRemote (String ip, String command,String key){

        Log.i("Pressed Remote 0 ", deviceKey);
        if(isOnline() &&  wifiConected()) {

            // Not required to know IP here in a ForEachRemote operation
           /* String[] arr = devices.get(key);
            deviceKey = arr[1];
            deviceIp = arr[0];*/
            // put code on click operation
            Log.i("Pressed Remote ", deviceKey);
            if (!LOGIN.equals("FALSE")) {


                    //http://192.168.0.113/REMOTE?UserID=000002999
                    Log.i("Pressed Remote 1 ", deviceKey +" "+ command);

                    if(command.equals("ON"))
                    {
                        //http://192.168.0.12/REMOTE?UserID=000001430
                        Log.i("Pressed Remote 1 ",ip + "/REMOTE?UserID=" + NumericUserId + key);
                        new HttpCommandRemote().execute(ip + "/REMOTE?UserID=" + NumericUserId + key);
                    }else
                    {
                        Log.i("Pressed Remote 1 ",ip +"/NOREMOTE" );
                        new HttpCommandRemote().execute(ip + "/NOREMOTE");
                    }




            } else {

                Toast.makeText(getApplicationContext(),"Must be logged in!",Toast.LENGTH_SHORT).show();
            }
        }else
        {

            if(wifiConected() && command.equals("OFF"))
            {
                new HttpCommand().execute(ip + "/NOREMOTE");
            }

            if(!wifiConected())
            {
                Toast.makeText(getApplicationContext(),"Wifi must be active!",Toast.LENGTH_SHORT).show();
            }

            if(!isOnline())
            {
                Toast.makeText(getApplicationContext(),"Not online!",Toast.LENGTH_SHORT).show();
            }

        }


    }

    private void PopUpOn (String key){

        //In order to send an ON command, the device must be
        //check for wifi, if not on wifi, call connect to server button

        if(!wifiConected())
        {
            SetConnectionToServer();
            //will affect serverConnection variable

        }

        if((isOnline() && serverConnection) || wifiConected())
        {

            String[] arr = devices.get(key);
            deviceKey = arr[1];
            deviceIp = arr[0];
            // put code on click operation
            Log.d("Button Pressed ON", deviceKey);

            if (serverConnection == Boolean.TRUE ) {

                if(filterButton == Boolean.FALSE)
                {
                    filterButton = Boolean.TRUE;
                    new HttpPOST_TurnON_OFF().execute(NumericUserId,deviceKey,"ON");
                }


            } else {

                turnON(deviceIp);
            }

        }else
        {

            if(!wifiConected())
            {
                Toast.makeText(getApplicationContext(),"Wifi must be active!",Toast.LENGTH_SHORT).show();
            }

            if(!isOnline())
            {
                Toast.makeText(getApplicationContext(),"Not online!",Toast.LENGTH_SHORT).show();
            }


        }


    }
    private void PopUpOFF(String key){

        if(!wifiConected())
        {
            SetConnectionToServer();
            //will affect serverConnection variable

        }
        if((isOnline() && serverConnection) || wifiConected()) {

            String[] arr = devices.get(key);
            deviceKey = arr[1];
            deviceIp = arr[0];
            // put code on click operation
            Log.d("Button Pressed OFF", deviceKey);
            if (serverConnection == Boolean.TRUE) {

                if(filterButton == Boolean.FALSE)
                {
                    filterButton = Boolean.TRUE;
                    new HttpPOST_TurnON_OFF().execute(NumericUserId, deviceKey, "OF");
                }


            } else {

                turnOFF(deviceIp);
            }
        }else
        {
            if(!wifiConected())
            {
                Toast.makeText(getApplicationContext(),"Wifi must be active!",Toast.LENGTH_SHORT).show();
            }

            if(!isOnline())
            {
                Toast.makeText(getApplicationContext(),"Not online!",Toast.LENGTH_SHORT).show();
            }

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
    private void secondResetDeviceDialog(String key)
    {
        final AlertDialog.Builder popDialog = new AlertDialog.Builder(this);

        final String deviceK = key;

        //popDialog.setIcon(android.R.drawable.btn_star_big_on);
        popDialog.setTitle("Reset Device");
        popDialog.setMessage("Are you sure? Your configuration will be deleted");
        // Button OK
        popDialog.setPositiveButton("Yes",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        String[] info= devices.get(deviceK);
                        String urlCommand = info[0]+ "/cleareeprom";
                        Log.d("urlCommand",urlCommand);

                        new HttpCommand().execute(urlCommand);

                        DeleteDeviceFromHashMap(deviceK);

                        dialog.dismiss();
                    }

                });
        popDialog.setNegativeButton("No",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(getBaseContext(),"Yeah though so" , Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }


                });




        popDialog.create();
        popDialog.show();

    }



    private void resetDeviceDialog(String key)
    {
        final AlertDialog.Builder popDialog = new AlertDialog.Builder(this);

        final String deviceK = key;

        //popDialog.setIcon(android.R.drawable.btn_star_big_on);
        popDialog.setTitle("Reset Device");
        popDialog.setMessage("You are about to reset this device. Continue?");
        // Button OK
        popDialog.setPositiveButton("Yes",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        secondResetDeviceDialog(deviceK);
                        dialog.dismiss();
                    }

                });
        popDialog.setNegativeButton("No",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(getBaseContext(),"Wise choice" , Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }


                });




        popDialog.create();
        popDialog.show();

    }

    private void deleteDeviceShowDialog(String key)
    {
        final AlertDialog.Builder popDialog = new AlertDialog.Builder(this);

        final String deviceK = key;

        //popDialog.setIcon(android.R.drawable.btn_star_big_on);
        popDialog.setTitle("Delete Device");
        popDialog.setMessage("You are about to delete this device. Continue?");
        // Button OK
        popDialog.setPositiveButton("Yes",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        DeleteDeviceFromHashMap(deviceK);
                        printDevices2();

                        dialog.dismiss();
                    }

                });
        popDialog.setNegativeButton("No",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(getBaseContext(),"Wise choice" , Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }


                });




        popDialog.create();
        popDialog.show();
    }

    /*Show DIalog for INTENSITY*/
    /* el ESP cuenta un 1% cada 75 microsegundos */
    /* su valor maximo de espera es 111          */
    /* !/60/2 = 0.00833333
    0.00833333333 / 0.000075
     */

    private void ShowDialog(String key)
    {
        final int max = 120;
        int initalSeekBar = 0;
        final AlertDialog.Builder popDialog = new AlertDialog.Builder(this);
        final SeekBar seek = new SeekBar(this);
        seek.setMax(max);
        final String deviceK = key;

        if(serverConnection == Boolean.FALSE) {
            //popDialog.setIcon(android.R.drawable.btn_star_big_on);
            popDialog.setTitle("Select Intensity");
            popDialog.setView(seek);

            addresses = devices.get(key);
            initalSeekBar = max - Integer.parseInt(addresses[2]);

            seek.setProgress(initalSeekBar);


            seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    //Do something here with new value
                    //txtView.setText("Value of : " + progress);
                    //we must save the value for the seekbar on the hashmap and reload it
                    numericDC = max - progress;


                    // numericDC = map(progress,1,100,111,1);


                }

                public void onStartTrackingTouch(SeekBar arg0) {
                    // TODO Auto-generated method stub

                }

                public void onStopTrackingTouch(SeekBar seekBar) {
                    // TODO Auto-generated method stub
                    addresses = devices.get(deviceK);
                    addresses[2] = Integer.toString(numericDC);
                    if (numericDC <= 7) {
                        //avoid the lower bound
                        numericDC = 5;
                    }

                    if (numericDC >= 96) {
                        //avoid the upper bound
                        numericDC = max;
                        Toast.makeText(getBaseContext(),"Minimum Intensity, turned Off" , Toast.LENGTH_SHORT).show();
                    }
                    //Log.d("length",Integer.toString(addresses.length));
                    //sendIntensity(addresses[0], addresses[2]);
                    sendIntensity(addresses[0], Integer.toString(numericDC));

                }
            });


            // Button OK
            popDialog.setPositiveButton("OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }

                    });

        }else
        {
            popDialog.setTitle("Intensity Level Not Available Online, touch on DISCONNECT");
            // Button OK
            popDialog.setPositiveButton("OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }

                    });
        }


        popDialog.create();
        popDialog.show();

    }

    private int map(int x, int in_min, int in_max, int out_min, int out_max)
    {
        return (x - in_min) * (out_max - out_min) / (in_max - in_min) + out_min;
    }


    /* Remove Device from the hashmap in app */
    private void ShowDialogRemove(String key)
    {
        final AlertDialog.Builder popDialog = new AlertDialog.Builder(this);

        final String deviceK = key;

        //popDialog.setIcon(android.R.drawable.btn_star_big_on);
        popDialog.setTitle("Remove Device?");

        // Button OK
        popDialog.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        devices.remove(deviceK);
                        saveUsersHashMap();
                        printDevices2();
                        dialog.dismiss();
                    }

                });

        // Button NOK
        popDialog.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }

                });


        popDialog.create();
        popDialog.show();

    }

    /*Dialog to manage set Remote Devices */
    private void ShowDialogRemote(String key)
    {
        final AlertDialog.Builder popDialog = new AlertDialog.Builder(this);

        String[] arr = devices.get(key);
        deviceKey = arr[1];
        deviceIp = arr[0];

        //popDialog.setIcon(android.R.drawable.btn_star_big_on);
        popDialog.setTitle("Force Remote Devices?");

        // Button OK
        popDialog.setPositiveButton("Remote",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        PopUpRemote(deviceIp,"ON",deviceKey);
                        dialog.dismiss();
                    }

                });

        // Button NOK
        popDialog.setNeutralButton("No Remote",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        PopUpRemote(deviceIp,"OFF",deviceKey);
                        dialog.dismiss();
                    }

                });

        // Button NOK
        popDialog.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }

                });


        popDialog.create();
        popDialog.show();

    }


    /* Show device information on the APP */
    private void ShowDialogInfo(String key)
    {
        final AlertDialog.Builder popDialog = new AlertDialog.Builder(this);

        final String deviceK = key;

        //popDialog.setIcon(android.R.drawable.btn_star_big_on);
        popDialog.setTitle("Device Information");


        addresses = devices.get(key);

        popDialog.setMessage("IP: " + addresses[0] + "\n" +
                "Intensity: " + addresses[2] + "\n" +
                "Key: " + addresses[1]);
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
                    Toast.makeText(getBaseContext(),"Device Name already selected. Please introduce another one." , Toast.LENGTH_SHORT).show();

                }else if
                (m_Text.equals("")){
                    Toast.makeText(getBaseContext(),"Please input a valid name." , Toast.LENGTH_SHORT).show();

                }else
                {
                    /*change name on device*/
                    String urlCommand = "/host?name=";
                    String[] info = devices.get(deviceNickName);
                    //ip,key,dc

                    new HttpCommand().execute(info[0] + urlCommand + m_Text);
                    /*change name on hashmap*/
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

    private void DeleteDeviceFromHashMap(String oldName){
        devices.remove(oldName);
        saveUsersHashMap();
    }

    private void ChangeNameHashMap(String NewKey, String OldKey){

        devices.put(NewKey, devices.get(OldKey));
        devices.remove(OldKey);
       // saveHashMap();
        saveUsersHashMap();
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

        Log.i("IP CHECKED", longToIP(ipSubnet));
        ipSubnet+=0x01000000;//endianess , first address
        ipBroadcast-=0x01000000;
        Log.i("IP CHECKED", longToIP(ipSubnet));
        Log.i("IP CHECKED", longToIP(ipBroadcast));

        Log.i("IP CHECKED", intToIP(ipSubnet));
        Log.i("IP CHECKED", intToIP(ipBroadcast));

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
            Log.i("IP CHECKED", urlDiscovery + urlDevice + urlCommand);
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



    /* Used for the SCAN DEVICES METHOD*/
    private static String GET2(String url){

        final int SCAN_TIME_OUT = 1000;

        String response="";

        try{
            //Consider next request:
            HttpRequest req=new HttpRequest(url);
            // prepare http get request,  send to "http://host:port/path" and read server's response as String
            response=  req.prepareTimeOut(SCAN_TIME_OUT).sendAndReadString();
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

            runOnUiThread(new Runnable()
            {
                public void run()
                {
                    Toast.makeText(getApplicationContext(), "malformedUrl", Toast.LENGTH_SHORT).show();
                }
            });
            Log.d("MalformedURl","url");
        }
        catch (SocketTimeoutException e){

            Log.d("Connection timed out", "url");
            runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(getApplicationContext(), "Connection Time Out", Toast.LENGTH_SHORT).show();
                }
            });
        }
        catch(IOException e){

            Log.d("IOException","url");
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

            // set the connection timeout value to 2 seconds (2000 milliseconds)
            final HttpParams httpParams = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpParams, 2000);
            HttpConnectionParams.setSoTimeout(httpParams, 2000);
            HttpClient httpclient = new DefaultHttpClient(httpParams);


            // create HttpClient
           // HttpClient httpclient = new DefaultHttpClient();

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

    private class HttpAsyncTask extends AsyncTask<String, Void, String> {

        private String ipAdd;





        @Override
        protected void onCancelled(){

            //Do nothing?

        }
        @Override
        protected String doInBackground(String... urls) {

            ipAdd = urls[0];
            /* this one works OK currently */
            return GET4(urls[0]);
        }



        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            //Toast.makeText(getBaseContext(), "Received!", Toast.LENGTH_SHORT).show();
            //Toast.makeText(getBaseContext(), result, Toast.LENGTH_SHORT).show();

            progress.incrementProgressBy(1);
            if(progress.getMax() == progress.getProgress()){
                progress.dismiss();
                ScanDevices(null);
            }

            if(result.equals("ACCEPTED")){



                ipAdd = parseIP(ipAdd);


                if( hashmapContainsIP(ipAdd))
                {
                    Toast.makeText(getBaseContext(),"Device with IP "+ ipAdd +" already added", Toast.LENGTH_SHORT).show();
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


                    //Add the devices to a list
                    //When the progress dialog gets cancelled or stops
                    //We must send the keys
                    devicesForKey.add(intermediateNickName);


                    //http://192.168.100.17/SETKEY?key=444

                    //http://192.168.0.113/SETKEY?key=999
                   // String urlCommand = "/SETKEY?key=";

                    //http://192.168.0.113/REMOTE?UserID=000002999
                    String urlCommand = "/REMOTE?UserID=";

                    //send the setkey command
                    //might have to be done at the end of the async tasks
                    //TODO test the new command and decide whether to relocate it or not to the end of the async tasks
                   // new HttpAsyncTask().execute(ipAdd +  urlCommand + convertedKey);


                    //If we are logged in, we must send the device parameters , i.e. numeric user id and key
                    //this means we can only have 1 user for device
                    //the app doesnt send the parameters if we are not logged in
                    if(!LOGIN.equals("FALSE"))
                    {
                        new HttpCommand().execute(ipAdd + urlCommand + NumericUserId + convertedKey);
                    }

                    Toast.makeText(getBaseContext(),"Device Added", Toast.LENGTH_SHORT).show();




                    printDevices2();




                }


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

    private String WipeDevicePost(String key)
    {

         //http://khansystems.com/clienteQuery/index.php?DeleteDevice=000002999

        String result="";

        String url = serverURL + "/?DeleteDevice="+ NumericUserId+key;
        Log.d("url",url);
        try {
            HttpRequest req = new HttpRequest(serverURL);
            HashMap<String, String> params = new HashMap<>();
            params.put("DeleteDevice", NumericUserId+key);
            result=req.preparePost().withData(params).sendAndReadString();
        }
        catch( SocketTimeoutException e){
            Log.d("ConnectionTimeOut", "time out");
            //Toast.makeText(LoginActivity.this, "Connection Time out" , Toast.LENGTH_LONG).show();

            runOnUiThread(new Runnable()
            {
                public void run()
                {
                    Toast.makeText(getApplicationContext(), "Connection Time out", Toast.LENGTH_SHORT).show();
                }
            });
        }
        catch(MalformedURLException e){
            Log.d("MalformedURl","malformed url");
        }
        catch(IOException e){
            Log.d("IO","io exception");
        }
        Log.d("Http Login Response:", result);
        return result;
    }



    private class HttpWipe extends AsyncTask<String, Void, String> {



        private String key="";


        @Override
        protected String doInBackground(String... urls) {


            key = urls[1];
            Log.d("keyAsyncTask",key);
            return WipeDevicePost(key);
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            //Toast.makeText(getBaseContext(), "Received!", Toast.LENGTH_SHORT).show();


            Toast.makeText(getBaseContext(), result, Toast.LENGTH_SHORT).show();


            Log.d("State", result);



        }
    }

    private class HttpIntensity extends AsyncTask<String, Void, String> {

        private String ipAdd;



        @Override
        protected String doInBackground(String... urls) {

            ipAdd = urls[0];
            return GET4(urls[0]);
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            //Toast.makeText(getBaseContext(), "Received!", Toast.LENGTH_SHORT).show();
            //Toast.makeText(getBaseContext(), result, Toast.LENGTH_SHORT).show();

            Log.d("IntensityPre", ipAdd + " " + result);
            if(result.equals("Received Intensity")) {
                Log.d("Intensity", ipAdd + " " + result);
                Toast.makeText(getBaseContext(), result, Toast.LENGTH_SHORT).show();
            }else
            {
                Log.d("Intensity", ipAdd + " " + "No response");
                Toast.makeText(getBaseContext(), "No response"+ result, Toast.LENGTH_SHORT).show();
            }

            filterButton = Boolean.FALSE;



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
            //Toast.makeText(getBaseContext(), "Received!", Toast.LENGTH_SHORT).show();
            //Toast.makeText(getBaseContext(), result, Toast.LENGTH_SHORT).show();

            if(result.equals("Changed to OFF")|| result.equals("Changed to ON")) {
                Log.d("State", ipAdd + " " + result);
                Toast.makeText(getBaseContext(), result, Toast.LENGTH_SHORT).show();
            }else
            {
                Log.d("State", ipAdd + " " + "No response");
                Toast.makeText(getBaseContext(), "No response", Toast.LENGTH_SHORT).show();
            }

            filterButton = Boolean.FALSE;



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

        if(filterButton == Boolean.FALSE)
        {
            filterButton = Boolean.TRUE;
            new HttpTurnOnOff().execute(ip+turnOnCommand);
        }



    }



    public void turnOFF( String ip){

        final String turnOffCommand = "/?State=OFF";

        if(filterButton == Boolean.FALSE)
        {
            filterButton = Boolean.TRUE;
            new HttpTurnOnOff().execute(ip+turnOffCommand);

        }




    }

    public void RegisterDevices( ){
        String NickName;
        String deviceKey;
        String theip;

        for (Map.Entry<String, String[]> entry : devices.entrySet()) {
            NickName = entry.getKey();
            String value[] = entry.getValue();
            theip = value[0];
            deviceKey = value[1];
            new HttpPOSTREGISTER().execute(NickName, deviceKey, theip);

        }


    }

    private void WipeDevices(){

        //Deletes the device from the server

        for (Map.Entry<String, String[]> entry : devices.entrySet()) {
            String NickName = entry.getKey();
            String value[] = entry.getValue();
            String key = value[1];
            Log.d("WipeDevices",key);
            WipeDevice(key);

        }

    }

    private void WipeDevice(String key){

        //http://khansystems.com/clienteQuery/index.php?DeleteDevice=000002999

        Log.d("WipeDevice",key);

        new HttpWipe().execute(NumericUserId,key);
    }

    private String LoadDevices (String NumUserId){

        // http://khansystems.com/clienteQuery/index.php?GetDevices=000002

        String url;
        String result="";

        url = serverURL + "/?GetDevices="+ NumUserId;
        Log.d("LoadDevices",url);
       // result = GET4(url);



        String response = "";

        HashMap<String, String> params = new HashMap<>();
        params.put("GetDevices", NumUserId);

        try {
            //Consider next request:
            HttpRequest req = new HttpRequest(serverURL);

            response = req.preparePost().withData(params).sendAndReadString();
        } catch (SocketTimeoutException e) {
            //Toast.makeText(getBaseContext(), "Connection TimedOut", Toast.LENGTH_SHORT).show();
            Log.d("connection", "timeout");
        } catch (MalformedURLException e) {

            Log.d("loadDevices","malformedurl");
            response = e.getMessage();
        } catch (IOException e) {
            //   Toast.makeText(getBaseContext(), "Device Not Found", Toast.LENGTH_SHORT).show();
            Log.d("loadDevices","ioexception");
            response = e.getMessage();
        }

        return response;


       // return result;
    }

    //parses and sets the Devices from the http request
    public void SetDevices( String UnparsedDevices){
        String[] Devices = UnparsedDevices.split(";");
        String[] params;

        devices.clear();


        for (int i= 0;i < Devices.length -1;i++){

            params = Devices[i].split(",");
            //Log.d("d", Integer.toString(params.length));

            String nickname = params[2];
            String key = params[0];
            String ip = params[1];
            String dc = "100";
            String[] values = {ip,key,dc};
            devices.put(nickname, values);


        }
        //Log.d("hs",devices.toString());

        printDevices2();

        //Log.d("hs", devices.toString());
       // saveHashMap();
        saveUsersHashMap();


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
            Toast.makeText(getBaseContext(), "Received!", Toast.LENGTH_SHORT).show();
            //Toast.makeText(getBaseContext(), result, Toast.LENGTH_SHORT).show();
            Log.d("Http Post Response:", result);
            SetDevices(result);



        }
    }


    private String RegisterDevice(String NickName, String key, String ip){

        Log.d("ip",ip);
        Log.d("NickName",NickName);
        Log.d("key",key);
        Log.d("User",UserId);

        String result="";
        try {
            HttpRequest req = new HttpRequest(serverURL);
            HashMap<String, String> params = new HashMap<>();
            params.put("AddDevice", key);
            params.put("ip",ip);
            params.put("NickName", NickName);
            params.put("User", UserId);
            result=req.preparePost().withData(params).sendAndReadString();
        }
        catch( SocketTimeoutException e){
            runOnUiThread(new Runnable(){

                @Override
                public void run(){
                    //update ui here
                    // display toast here
                    Toast.makeText(MainActivity.this, "Connection TimedOut", Toast.LENGTH_SHORT).show();
                }
            });
            //
            Log.d("connection","timeout");
           // Log.d("ConnectionTimeOut",e.getLocalizedMessage());

        }
        catch(MalformedURLException e){
            Log.d("MalformedURl","malformedUrl");
        }
        catch(IOException e){
            Log.d("IO","ioexception");
        }
        Log.d("Http Post Response:", result);
        return result;
    }


    private String POST_ON_OFF_Device(String userId,String key, String command){

        //http://khansystems.com/clienteQuery/index.php?Update=000002999ON
        //http://khansystems.com/clienteQuery/index.php?Update=000002999OF


        String response="";

        Log.d("userid",userId);
        Log.d("key",key);
        Log.d("command",command);

        HashMap<String, String>params=new HashMap<>();
        params.put("Update", userId+key+command);

        try {
            //Consider next request:
            HttpRequest req = new HttpRequest(serverURL);

            response= req.preparePostTimeOut(10000).withData(params).sendAndReadString();
        }
        catch(SocketTimeoutException e){
            runOnUiThread(new Runnable(){

                @Override
                public void run(){
                    //update ui here
                    // display toast here
                    Toast.makeText(MainActivity.this, "Connection TimedOut", Toast.LENGTH_SHORT).show();
                }
            });
            //
            Log.d("connection","timeout");
        }
        catch (MalformedURLException e){

            runOnUiThread(new Runnable(){

                @Override
                public void run(){
                    //update ui here
                    // display toast here
                    Toast.makeText(MainActivity.this, "Malformed URL", Toast.LENGTH_SHORT).show();
                }
            });

            response=e.getMessage();
        }
        catch (IOException e){
            //
            runOnUiThread(new Runnable(){

                @Override
                public void run(){
                    //update ui here
                    // display toast here
                    Toast.makeText(getBaseContext(), "Device Not Found", Toast.LENGTH_SHORT).show();
                }
            });
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
            //Toast.makeText(getBaseContext(), "Received!", Toast.LENGTH_SHORT).show();
            //Toast.makeText(getBaseContext(), result, Toast.LENGTH_SHORT).show();
            Log.d("d", "https POST " + key);


            if(result == null)
            {

            }else
            {
                Log.d("d","https POST "+ result);
                Toast.makeText(getBaseContext(), "Changed to "+result.replace(" ",""), Toast.LENGTH_SHORT).show();
            }
            filterButton = Boolean.FALSE;




        }
    }


    private class HttpPOSTREGISTER extends AsyncTask<String, Void, String> {

        private String Nickname;
        private String key;
        private String ip;



        @Override
        protected String doInBackground(String... urls) {


            Nickname = urls[0];
            key = urls[1];
            ip = urls[2];
            return RegisterDevice(Nickname, key,ip);
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            //Toast.makeText(getBaseContext(), "Received!", Toast.LENGTH_SHORT).show();
            Toast.makeText(getBaseContext(), result, Toast.LENGTH_SHORT).show();


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

    public void saveUsersHashMap(){

        //delete last hashmap
        users.remove(UserId);
        //add the new hashmap
        users.put(UserId,devices);

        try {


            FileOutputStream fileOutputStream = openFileOutput("hsUsers.bin", Context.MODE_PRIVATE);
            ObjectOutputStream objectOutputStream= new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(users);
            objectOutputStream.close();
        }
        catch(IOException e){
            e.printStackTrace();
        }

    }

    public void loadUsersHashMap(){


        try {
            FileInputStream fileInputStream = openFileInput("hsUsers.bin");
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);

            users = (HashMap) objectInputStream.readObject();
            if(users.containsKey(UserId))
            {
                devices = users.get(UserId);
            }

            objectInputStream.close();
        }
        catch( IOException e){
            e.printStackTrace();

        }
        catch (ClassNotFoundException e){
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

    /*Used for the initial UI*
    Must load the userss devices
     */
    private void createInitialUI()
    {
        //change menu
        invalidateOptionsMenu();
        //disable connect to server button
        //pressing the button toggles the text and the function
        final Button btn = (Button) findViewById(R.id.ButtonConnection);





        Log.d("login","result");
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, 0);

        UserId = prefs.getString("USER_ID", "");
        UserPassword = prefs.getString("PASSWORD","");
        NumericUserId = prefs.getString("NUMERIC_ID", "");
        LOGIN = prefs.getString("LOGIN","");

        if(LOGIN.equals("TRUE"))
        {
            btn.setEnabled(true);

        }else if (LOGIN.equals("FALSE"))
        {
            btn.setEnabled(false);
        }else
        {

        }

        Log.d("user_id",UserId);
        Log.d("password",UserPassword);
        Log.d("numeric_id", NumericUserId);
        Log.d("LoginState", LOGIN);



        /*Preferences for settings */

        SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        // The SharedPreferences editor - must use commit() to submit changes
        SharedPreferences.Editor editor = SP.edit();
        editor.putString("username",UserId);
        editor.commit();


        boolean usePreviousDevices = SP.getBoolean("usePreviousDevices",false);

        /*We are skipping login and the settings dont allow to keep the last devices*/
        if(usePreviousDevices == Boolean.FALSE && LOGIN.equals("FALSE")) {
            UserId="";
            editor.putString("username","");
            editor.commit();
        }

        loadUsersHashMap();
        printDevices2();
    }


    private static class HttpCommand extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {


            return GET4(urls[0]);
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            //Toast.makeText(getBaseContext(), "Received!", Toast.LENGTH_SHORT).show();
            //Toast.makeText(getBaseContext(), result, Toast.LENGTH_SHORT).show();


            Log.i("State", result);




        }
    }

    private  class HttpCommandRemote extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {


            return GET4(urls[0]);
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            //Toast.makeText(getBaseContext(), "Received!", Toast.LENGTH_SHORT).show();
            Toast.makeText(getBaseContext(), result, Toast.LENGTH_SHORT).show();
            Log.i("State", result);


        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);


        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu (Menu menu) {

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, 0);
        LOGIN = prefs.getString("LOGIN","");


        //These items should not be usable if the user logins without
        //authentication
        MenuItem SaveDev = menu.findItem(R.id.SaveDevices);
        MenuItem LoadDev = menu.findItem(R.id.LoadDevices);
        MenuItem WipeDev = menu.findItem(R.id.WipeDevices);

        if ( LOGIN.equals("FALSE"))
        {
            SaveDev.setEnabled(false);
            LoadDev.setEnabled(false);
            WipeDev.setEnabled(false);

        }else if( LOGIN.equals("TRUE"))
        {
            SaveDev.setEnabled(true);
            LoadDev.setEnabled(true);
            WipeDev.setEnabled(true);
        }else
        {
            /*Nothing to do*/
            /*     MISRA   */

        }

        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        createInitialUI();
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
                //Toast.makeText(getApplicationContext(),"Item 1 Selected",Toast.LENGTH_SHORT).show();
                if(isOnline()) {
                    RegisterDevicesConfirm();
                }else
                {
                    Toast.makeText(getApplicationContext(),"Not Online!",Toast.LENGTH_SHORT).show();
                }
                return true;
            case R.id.LoadDevices:
                //Toast.makeText(getApplicationContext(),"Item 2 Selected",Toast.LENGTH_SHORT).show();


                if(isOnline())
                {
                    new  HttpPOST_LoadDevices().execute(NumericUserId);
                }else
                {
                    Toast.makeText(getApplicationContext(),"Not Online!",Toast.LENGTH_SHORT).show();
                }


                return true;
            case R.id.WipeDevices:
                if(isOnline())
                {
                    WipeDevicesConfirm();
                    //DELETE DEVICES FROM SERVER
                }else
                {
                    Toast.makeText(getApplicationContext(),"Not Online!",Toast.LENGTH_SHORT).show();
                }


                return true;

            case R.id.action_settings:
                //DELETES LOCAL SETTINGS
                //WipeSettings();
                //deleteHashMap();
                //WipeSettingsConfirm();
                Intent i = new Intent(this, MyPreferencesActivity.class);
                startActivity(i);
                return true;
            case R.id.ConnectAP:
                //DELETES LOCAL SETTINGS
                //WipeSettings();
                //deleteHashMap();
                if(wifiConected())
                {
                    StartActivityConnectAP();
                }else
                {
                    Toast.makeText(getApplicationContext(),"Wifi must be active!",Toast.LENGTH_LONG).show();

                }

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }
}


