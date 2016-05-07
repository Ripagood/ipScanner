package com.sourcey.materiallogindemo;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by elias on 5/05/16.
 */
public class TimeService extends Service {
    // constant
    public static final long NOTIFY_INTERVAL = 10 * 1000 * 6; // 60 seconds

    //name of the saved used id var
    private static final String PREFS_NAME = "USER_INFORMATION";

    //by default we are OFF
    private String oldRemote="OFF";
    private String newRemote="";
    final String serverURL = MainActivity.serverURL;


    // run on another Thread to avoid crash
    private Handler mHandler = new Handler();
    // timer handling
    private Timer mTimer = null;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        // cancel if already existed
        if (mTimer != null) {
            mTimer.cancel();
        } else {
            // recreate new
            mTimer = new Timer();
        }
        // schedule task
        mTimer.scheduleAtFixedRate(new TimeDisplayTimerTask(), 0, NOTIFY_INTERVAL);
    }

    class TimeDisplayTimerTask extends TimerTask {

        @Override
        public void run() {
            // run on another thread
            mHandler.post(new Runnable() {

                @Override
                public void run() {
                    // display toast

                    //restore previously used email and password
                    SharedPreferences prefs = getSharedPreferences(PREFS_NAME, 0);
                    String user = prefs.getString("USER_ID", "");
                    String pass = prefs.getString("PASSWORD","");
                    if( user.equals("") || pass.equals(""))
                    {
                        //Do nothing, no saved data
                    }else
                    {
                        new HttpPOST_Get_Remote().execute(user, pass);
                    }

                }

            });
        }


    }

    private class HttpPOST_Get_Remote extends AsyncTask<String, Void, String> {

        private String user;
        private String pass;

        @Override
        protected String doInBackground(String... urls) {

            user = urls[0];
            pass = urls[1];

            return POST_Get_Remote(user, pass);
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            Toast.makeText(getBaseContext(), "Received!", Toast.LENGTH_SHORT).show();
            //Toast.makeText(getBaseContext(), result, Toast.LENGTH_SHORT).show();
            if (result == null || result.length() == 0) {
                result="no response";
            }
            Log.d("Http Post Response:", result);
            //SetDevices(result);
            //TODO set remote ON to devices on the network
            final Pattern p = Pattern.compile("ON|OFF" );
            final Matcher m = p.matcher( result );
            if ( m.find() ) {
                newRemote = m.group(0);
                Log.d("match",newRemote);
                if(!newRemote.equals(oldRemote))
                {
                 //we have a different value, must update

                    MainActivity.SetRemoteDevices(newRemote);
                }


            }




        }
    }

    private String POST_Get_Remote(String username, String password) {


        //http://khansystems.com/clienteQuery/index.php?username_version=jo.echeagaray@gmail.com&password=admin
        String response = "";

        HashMap<String, String> params = new HashMap<>();
        params.put("username_version", username);
        params.put("password", password);

        try {
            //Consider next request:
            HttpRequest req = new HttpRequest(serverURL);

            response = req.preparePost().withData(params).sendAndReadString();
        } catch (SocketTimeoutException e) {
            //Toast.makeText(getBaseContext(), "Connection TimedOut", Toast.LENGTH_SHORT).show();
            Log.d("connection", "timeout");
        } catch (MalformedURLException e) {

            response = e.getMessage();
        } catch (IOException e) {
         //   Toast.makeText(getBaseContext(), "Device Not Found", Toast.LENGTH_SHORT).show();
            response = e.getMessage();
        }

        return response;

    }
}