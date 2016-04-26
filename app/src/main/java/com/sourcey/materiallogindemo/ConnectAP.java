package com.sourcey.materiallogindemo;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.sourcey.materiallogindemo.R;

import java.util.ArrayList;
import java.util.List;

public class ConnectAP extends AppCompatActivity {

    Context context;
    String selectedSSID;
    String pass_Text;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect_ap);
        context = getApplicationContext();
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
}
