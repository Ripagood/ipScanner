package com.sourcey.materiallogindemo;
import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashSet;

/**
 * @author Bernd Verst(@berndverst)
 */
public class UPnPDiscovery extends AsyncTask
{
    HashSet<String> addresses = new HashSet<>();
    Context ctx;
    static UPnPDiscovery my_task;
    static boolean running= false;

    public UPnPDiscovery(Context context) {
        ctx = context;
    }


    @Override
    protected Object doInBackground(Object[] params) {

        Log.d("here","herewifi");
        WifiManager wifi = (WifiManager)ctx.getSystemService( ctx.getApplicationContext().WIFI_SERVICE );

        if(wifi != null) {

            WifiManager.MulticastLock lock = wifi.createMulticastLock("The Lock");
            lock.acquire();
            Log.d("here","here1");

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
                                "ST: ssdp:all\r\n"+  // Use this for all UPnP Devices
                                "\r\n";

                socket = new DatagramSocket();
                socket.setReuseAddress(true);
                Log.d("here", "here2");

                DatagramPacket dgram = new DatagramPacket(query.getBytes(), query.length(),
                        group, port);
                socket.send(dgram);

                long time = System.currentTimeMillis();
                long curTime = System.currentTimeMillis();

                Log.d("here","here3");
                // Let's consider all the responses we can get in 10 seconds
                while (curTime - time < 1000) {
                    if (!isCancelled()) {
// Do your stuff

                        DatagramPacket p = new DatagramPacket(new byte[2048], 2048);
                        socket.receive(p);


                        String s = new String(p.getData(), 0, p.getLength());
                        //Log.d("ssdp",s);
                        if (s.contains("SERVER: KhanSystems")) {

                            int first = s.indexOf("KhanSystems");
                            int last = s.indexOf("USN:");
                            String deviceInfo = s.substring(first, last);
                            Log.d("parsing", deviceInfo);
                            addresses.add(p.getAddress().getHostAddress());

                        }

                        curTime = System.currentTimeMillis();
                    }else
                    {
                        Log.d("here","break");
                        break;
                    }
                }

            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            finally {
                if (socket != null) {
                    socket.close();
                    Log.d("here", "herenotnull");
                }
            }
            lock.release();
        }
        return null;
    }


    @Override
    protected void onPostExecute(Object param) {

        Log.d("can1","finished");



    }


    @Override
    protected void onCancelled()
    {
        Log.d("can","cancelled");
        running=false;
    }
    public static void stopDiscovery()
    {
        my_task.cancel(true);

    }

    public static void discoverDevices(Context ctx) {
        Log.d("here","dd");


        if(running == true){

            my_task.cancel(true);

        }else
        {}

        my_task = new UPnPDiscovery(ctx);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            running=true;
            my_task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[]) null);
        }
        else {
            running=true;
            my_task.execute((Void[]) null);
        }
        //UPnPDiscovery discover = new UPnPDiscovery(ctx);
        //discover.execute();
        Log.d("here", "dd2");
        /*
        try {
            Thread.sleep(5000);
            for(String add: discover.addresses)
            {
                Log.d("upnp",add);
            }
            return discover.addresses.toArray(new String[discover.addresses.size()]);
        } catch (InterruptedException e) {
            Log.d("interrupted","int");
            return null;
        }*/

    }


}