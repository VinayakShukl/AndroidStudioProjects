package android.wifind;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
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

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class AskEmailExist extends Activity {

    private String mac_address;
    private Regs rg1;
    private EditText email;
    private String email_address;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ask_email2);

        email = (EditText) findViewById(R.id.email);
        Button btn = (Button) findViewById(R.id.reg_btn);
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

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void setupActionBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    public class Regs extends AsyncTask<Void, Void, Boolean> {

        int code;
        ProgressDialog pd = null;

        @Override
        protected void onPreExecute() {
            pd = new ProgressDialog(AskEmailExist.this);
            pd.setMessage("Loading...");
            pd.show();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                HttpGet senddata = new HttpGet("http://192.168.52.112:8000/new_device/?mac=" + mac_address + "&email=" + email_address);
                HttpParams parameters = new BasicHttpParams();
                int timeout = 3*1000;
                HttpConnectionParams.setConnectionTimeout(parameters, timeout);
                HttpResponse response = new DefaultHttpClient(parameters).execute(senddata);
                code = response.getStatusLine().getStatusCode();

                if (code == 200) {
                    return true;
                } else if (code == 403) {
                    code = 123;
                }
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (ConnectTimeoutException e) {
                code = 420;
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                pd.dismiss();
            }
            return false;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            pd.dismiss();
            if (code == 420)
                showDialog(2);
            else if( code == 123)
                showDialog(3);
            else if (success) {
                showDialog(0);
            }
        }
    }

    public class Regs_code extends AsyncTask<String, Void, Boolean> {

        int code;

        @Override
        protected Boolean doInBackground(String... strings) {
            String request = null;
            try {

                URI uri = null;
                try {
                    uri = new URI(
                            "http",
                            "192.168.52.112:8000",
                            "/confirm_code_exist/",
                            "code=" + strings[0] + "&mac=" + mac_address + "&email=" + email_address + "&device=" + Build.MODEL + "&os=" + Build.VERSION.RELEASE,
                            null);
                    request = uri.toASCIIString();
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }

                HttpGet senddata = new HttpGet(request);
                HttpParams parameters = new BasicHttpParams();
                int timeout = 3*1000;
                HttpConnectionParams.setConnectionTimeout(parameters, timeout);
                HttpResponse response = new DefaultHttpClient(parameters).execute(senddata);
                code = response.getStatusLine().getStatusCode();


                if (code == 200) {
                    String[] args = {email_address, mac_address};
                    loginTasks.storeEmailMAC(AskEmailExist.this, args);
                    new loginTasks.loginTask(AskEmailExist.this).execute();
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
            } else {
                showDialog(4);
            }
        }
    }

    private AlertDialog createCodeDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
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

        builder.setMessage("Successful Registration");

        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent intent = new Intent(AskEmailExist.this, MainActivity.class);
                startActivity(intent);
            }
        });
        return builder.create();
    }

    private AlertDialog serverBusy() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage("Oops. Something went wrong");

        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //takes you to the homepage
            }
        });
        return builder.create();

    }

    private AlertDialog wrongInput() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage("Email Address doesn't exist. Please select correct option");

        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent intent = new Intent(AskEmailExist.this, UserCheck.class);
                startActivity(intent);
            }
        });
        return builder.create();

    }

    private AlertDialog incorrectCode() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage("Please enter correct code");

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
            AlertDialog alert = createCodeDialog();
            return alert;
        }
        if (id == 1) {
            AlertDialog alert = successDialog();
            return alert;
        }
        if (id == 2) {
            AlertDialog alert = serverBusy();
            return alert;
        }
        if (id == 3) {
            AlertDialog alert = wrongInput();
            return alert;
        }
        if (id == 4) {
            AlertDialog alert = incorrectCode();
            return alert;
        }
        return null;
    }

}
