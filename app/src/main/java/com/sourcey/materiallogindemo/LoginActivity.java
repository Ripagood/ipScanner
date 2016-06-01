package com.sourcey.materiallogindemo;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import android.content.Intent;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.ButterKnife;
import butterknife.Bind;

public class LoginActivity extends AppCompatActivity {

    final String serverURL = "http://khansystems.com/clienteQuery/index.php";


    private static final String TAG = "LoginActivity";
    private static final int REQUEST_SIGNUP = 0;
    private static final String PREFS_NAME = "USER_INFORMATION";
    private int loginSuccess =0;

    public AsyncTask loginTask;

    public static String NumericUserId="";
    ProgressDialog progressDialog;

    @Bind(R.id.input_email) EditText _emailText;
    @Bind(R.id.input_password) EditText _passwordText;
    @Bind(R.id.btn_login) Button _loginButton;
    @Bind(R.id.btn_SkipLogin) Button _SkiplLoginButton;
    @Bind(R.id.link_signup) TextView _signupLink;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        
        _loginButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if(isOnline())
                {
                    login();
                }else
                {
                    Toast.makeText(getBaseContext(), "Not online!", Toast.LENGTH_LONG).show();
                }

            }
        });

        _SkiplLoginButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {

                if(wifiConected())
                {
                SkipLogin();
                }else
                {
                    Toast.makeText(getBaseContext(), "Wifi must be active!", Toast.LENGTH_LONG).show();
                }
            }

        });

        _signupLink.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // Start the Signup activity
                Intent intent = new Intent(getApplicationContext(), SignupActivity.class);
                startActivityForResult(intent, REQUEST_SIGNUP);
            }
        });

        //restore previously used email and password
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, 0);
        String email = prefs.getString("USER_ID", "");
        String password = prefs.getString("PASSWORD","");


        _emailText.setText(email);
        _passwordText.setText(password);



    }

    private boolean isOnline() {
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }


    public void login() {
        Log.d(TAG, "Login");

        if (!validate()) {
            onLoginFailed();
            return;
        }

        _loginButton.setEnabled(false);


        progressDialog = new ProgressDialog(LoginActivity.this,
                R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Authenticating...");
        progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                _loginButton.setEnabled(true);
                loginTask.cancel(true);
                //kill the async task in case it is running

            }
        });

        progressDialog.show();

        String email = _emailText.getText().toString();
        String password = _passwordText.getText().toString();

        // TODO: Implement your own authentication logic here.
        // Need to add the remote server check

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("USER_ID", email);
        editor.putString("PASSWORD", password);
        editor.commit(); //important, otherwise it wouldn't save.

        loginTask = new HttpLogin().execute(email, password);






        /*
        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        // On complete call either onLoginSuccess or onLoginFailed
                        onLoginSuccess();
                        // onLoginFailed();
                        progressDialog.dismiss();
                    }
                }, 3000);*/
    }

    public void SkipLogin(){

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Skip Login");
        builder.setMessage("Are you sure you want to skip login? You won't be able to synchronize to" +
                " the cloud");


// Set up the buttons
        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

               //Login and clear important variables
                MainActivity.NumericUserId="";
                MainActivity.UserId="";
                MainActivity.UserPassword="";

                SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("LOGIN", "FALSE");
                editor.commit(); //important, otherwise it wouldn't save.

                Log.d("LAN","nouser_pass_id");
                // correct authentication
                finish();

            }
        });
        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
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


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_SIGNUP) {
            if (resultCode == RESULT_OK) {

                // TODO: Implement successful signup logic here
                // By default we just finish the Activity and log them in automatically
                this.finish();
            }
        }
    }

    @Override
    public void onBackPressed() {
        // Disable going back to the MainActivity
        moveTaskToBack(true);
    }

    @Override
    protected void onPause() {
        super.onPause();

        if(progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(progressDialog != null) {
            progressDialog.dismiss();
        }
    }


    public void onLoginSuccess() {
        _loginButton.setEnabled(true);
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("LOGIN","TRUE");
        editor.commit(); //important, otherwise it wouldn't save.
        finish();
    }

    public void onLoginFailed() {
        Toast.makeText(getBaseContext(), "Login failed", Toast.LENGTH_LONG).show();

        _loginButton.setEnabled(true);
    }

    public boolean validate() {
        boolean valid = true;

        String email = _emailText.getText().toString();
        String password = _passwordText.getText().toString();

        if (email.isEmpty() ) {
            _emailText.setError("enter a valid email address");
            valid = false;
        } else {
            _emailText.setError(null);
        }

        if (password.isEmpty() || password.length() < 4 || password.length() > 10) {
            _passwordText.setError("between 4 and 10 alphanumeric characters");
            valid = false;
        } else {
            _passwordText.setError(null);
        }

        return valid;
    }

    //check for wifi connection
    private Boolean wifiConected()
    {
        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        return mWifi.isConnected();
    }




    private class HttpLogin extends AsyncTask<String, Void, String> {

        private String Username;
        private String Password;


        @Override
        protected void onCancelled(){

            //Do nothing?

        }

        @Override
        protected String doInBackground(String... urls) {

            Username = urls[0];
            Password = urls[1];
            return confirmLogin(Username, Password);
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            //Toast.makeText(getBaseContext(), "Received!", Toast.LENGTH_LONG).show();
            //Toast.makeText(getBaseContext(), result, Toast.LENGTH_LONG).show();

            String extracted;

            final Pattern p = Pattern.compile("(\\d{6,})" );
            final Matcher m = p.matcher( result );
            if ( m.find() ) {
                extracted= m.group( 0 );
                //save the numeric id
                SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("NUMERIC_ID", extracted);
                MainActivity.NumericUserId = extracted;
                editor.commit(); //important, otherwise it wouldn't save.
                Log.d("postexec","correct");
                // correct authentication
                onLoginSuccess();
                progressDialog.dismiss();
            }
            else{
                extracted="";
                Log.d("postexec","failed");
                onLoginFailed();
                progressDialog.dismiss();
            }



        }
    }


    private String confirmLogin(String username, String password){

        //http://khansystems.com/clienteQuery/index.php?username=jo.echeagaray@gmail.com&password=admin

        String result="";
        try {
            HttpRequest req = new HttpRequest(serverURL);
            HashMap<String, String> params = new HashMap<>();
            params.put("username", username);
            params.put("password", password);
            result=req.preparePost().withData(params).sendAndReadString();
        }
        catch( SocketTimeoutException e){
            Log.d("ConnectionTimeOut", e.getLocalizedMessage());
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
            Log.d("MalformedURl",e.getLocalizedMessage());
        }
        catch(IOException e){
            Log.d("IO",e.getLocalizedMessage());
        }
        Log.d("Http Login Response:", result);
        return result;
    }

}
