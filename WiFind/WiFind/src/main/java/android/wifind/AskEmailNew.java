package android.wifind;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class AskEmailNew extends Activity {

    private String mac_address;
    private Regs rg1;
    private EditText email;
    private String email_address;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ask_email);

        Button btn = (Button) findViewById(R.id.reg_btn);
        email = (EditText) findViewById(R.id.email);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                email_address = email.getText().toString();
                rg1 = new Regs();
                rg1.execute((Void) null);
            }
        });
        WifiManager manager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = manager.getConnectionInfo();
        mac_address = info.getMacAddress();
        // Show the Up button in the action bar.
        setupActionBar();
    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void setupActionBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    public class Regs extends AsyncTask<Void, Void, Boolean> {

        int code;
        ProgressDialog pd=null;

        @Override
        protected void onPreExecute(){
            pd=new ProgressDialog(AskEmailNew.this);
            pd.setMessage("Loading...");
            pd.show();
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            try {
                HttpGet senddata = new HttpGet("http://192.168.52.112:8000/new_user/?mac=" + mac_address + "&email=" + email_address);
                HttpParams parameters = new BasicHttpParams();
                int timeout = 100000;
                HttpConnectionParams.setConnectionTimeout(parameters,timeout);
                HttpResponse response = new DefaultHttpClient(parameters).execute(senddata);
                code = response.getStatusLine().getStatusCode();

                if (code == 200) {
                        return true;
                }


            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (ConnectTimeoutException e) {
            } catch (IOException e) {
                e.printStackTrace();
            }
            return false;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            pd.dismiss();
            if (success) {
                showDialog(0);
                //show dialog for confirmation code
            } else
                showDialog(2);
        }
    }

    public class Regs_code extends AsyncTask<String, Void, Boolean> {

        int code;

        @Override
        protected Boolean doInBackground(String... strings) {

            try {
                String request = null;
                try {

                    URI uri = new URI(
                            "http",
                            "192.168.52.112:8000",
                            "/confirm_code",
                            "?code=" + strings[0] + "&mac=" + mac_address + "&email=" + email_address + "&device=" + android.os.Build.MODEL + "&os=" + android.os.Build.VERSION.RELEASE,
                            null);
                    request = uri.toASCIIString();
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
                HttpGet senddata = new HttpGet(request);
                HttpParams parameters = new BasicHttpParams();
                int timeout = 15*1000;
                HttpConnectionParams.setConnectionTimeout(parameters, timeout);
                HttpResponse response = new DefaultHttpClient(parameters).execute(senddata);
                code = response.getStatusLine().getStatusCode();
          
                if (code == 200) {
                    String[] args = {email_address, mac_address};
                    loginTasks.storeEmailMAC(AskEmailNew.this, args);
                    new loginTasks.loginTask(AskEmailNew.this).execute();
                    return true;
                }


            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (HttpHostConnectException e) {
                code = 420;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return false;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            if (code == 420)
                showDialog(2);
            else if (success) {
                //alert dialog saying successful registration
                showDialog(1);
            } else
                showDialog(3);
        }
    }

    private AlertDialog createCodeDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirm");
        builder.setMessage("Enter the confirmation code sent to " + email_address + "@iiitd.ac.in : ");

        final EditText input_code = new EditText(this);
        input_code.setId(0);
        builder.setView(input_code);

        builder.setPositiveButton("Register", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String value = input_code.getText().toString();
                Regs_code rc = new Regs_code();
                rc.execute(value);
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        return builder.create();
    }

    private AlertDialog successDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Successful Registration");

        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //takes you to the homepage
                Toast.makeText(getApplicationContext(), "This takes you to the Home Activity", Toast.LENGTH_LONG).show();
            }
        });
        return builder.create();
    }

    private AlertDialog serverBusy() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Oops. Something went wrong");

        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //takes you to the homepage
            }
        });
        return builder.create();

    }

    private AlertDialog incorrectCode() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Please enter correct code");

        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                showDialog(0);
            }
        });
        return builder.create();

    }

    @Override
    protected Dialog onCreateDialog(int id) {
        if (id == 0) {
            return createCodeDialog();
        }
        if (id == 1) {
            return successDialog();
        }
        if (id == 2) {
            return serverBusy();
        }
        if (id == 3) {
            return incorrectCode();
        }
        return null;
    }

}
