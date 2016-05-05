package com.sourcey.materiallogindemo;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import android.content.Intent;
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

    ProgressDialog progressDialog;

    @Bind(R.id.input_email) EditText _emailText;
    @Bind(R.id.input_password) EditText _passwordText;
    @Bind(R.id.btn_login) Button _loginButton;
    @Bind(R.id.link_signup) TextView _signupLink;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        
        _loginButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                login();
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

        new HttpLogin().execute(email,password);






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

    public void onLoginSuccess() {
        _loginButton.setEnabled(true);
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




    private class HttpLogin extends AsyncTask<String, Void, String> {

        private String Username;
        private String Password;



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

            final Pattern p = Pattern.compile("(\\d{6})" );
            final Matcher m = p.matcher( result );
            if ( m.find() ) {
                extracted= m.group( 0 );
                //save the numeric id
                SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("NUMERIC_ID", extracted);
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
            Log.d("ConnectionTimeOut",e.getLocalizedMessage());
            Toast.makeText(getBaseContext(), "Connection Time out" , Toast.LENGTH_LONG).show();

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
