package com.android.insecurebankv2;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

/*
The page that allows gives the user below functionalities
Transfer: Module that allows transfer of amount between two accounts
View Statement: Module that allows the user to view transaction history for the logged in user
Change Password:  Module that allows the logged in user to change the password
@author Dinesh Shetty
*/
public class PostLogin extends Activity {
	//	The Button that handles the transfer activity
	Button transfer_button;
    //  The Textview that handles the root status display
	TextView root_status;
	//	The Button that handles the view transaction history activity
	Button statement_button;
	//	The Button that handles the change password activity
	Button changepasswd_button;
    Button nearbyatm_button;
	String uname;
    String imei = "";
    Boolean doParticipate;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_post_login);
		Intent intent = getIntent();
		uname = intent.getStringExtra("uname");

        root_status =(TextView) findViewById(R.id.rootStatus);
        //   Display root status
        showRootStatus();
		transfer_button = (Button) findViewById(R.id.trf_button);
		transfer_button.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				/*
				The class that allows allows transfer of amount between two accounts
				*/
				Intent dT = new Intent(getApplicationContext(), DoTransfer.class);
				startActivity(dT);
			}
		});
		statement_button = (Button) findViewById(R.id.viewStatement_button);
		statement_button.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				viewStatment();
			}
		});
		changepasswd_button = (Button) findViewById(R.id.button_ChangePasswd);
		changepasswd_button.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				changePasswd();
			}
		});
        nearbyatm_button = (Button) findViewById(R.id.button_NearbyATM);
        nearbyatm_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent dT = new Intent(getApplicationContext(), NearbyATM.class);
                startActivity(dT);
            }
        });

        //checkbox = (CheckBox)findViewById(R.id.checkbox_ppip);
        doParticipate = PreferenceManager.getDefaultSharedPreferences(this).
                getBoolean("participate", false);
        sendUserIMEIIfAllowed();
	}

    private void sendUserIMEIIfAllowed(){
        try {
            if (doParticipate) {
                System.out.println("NULIST: user doesn't allow to send messages");
                return;
            }

            TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            imei = telephonyManager.getDeviceId();
            new PostDataTask().execute("");
        }
        catch(Exception e){
            System.err.println("NULIST: error: sendUserIMEIIfAllowed: "+e.toString());
            e.printStackTrace();
        }
    }

    class PostDataTask extends AsyncTask< String, String, String > {

        @Override
        protected String doInBackground(String...params) {
            try {
                postData(params[0]);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(Double result) {}
        protected void onProgressUpdate(Integer...progress) {}

        public void postData(String valueIWantToSend) throws ClientProtocolException, IOException, JSONException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
            try {
                HttpClient httpclient = new DefaultHttpClient();
                HttpPost httppost = new HttpPost("http://165.124.182.177:8888/postdata");
                // Add your data
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
                nameValuePairs.add(new BasicNameValuePair("username", uname));
                nameValuePairs.add(new BasicNameValuePair("imei", imei));
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                // Execute HTTP Post Request
                HttpResponse responseBody = httpclient.execute(httppost);

                InputStream in = responseBody.getEntity().getContent();
                String result = convertStreamToString(in);
                System.out.println("NULIST: PostData RS: " + result);
            }
            catch(Exception e){
                System.err.println("NULIST: error postdata: "+e.toString());
            }
        }
    }

    private String convertStreamToString(InputStream in ) throws IOException {
        // TODO Auto-generated method stub
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader( in , "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        StringBuilder sb = new StringBuilder();
        String line = null;
        while ((line = reader.readLine()) != null) {
            sb.append(line + "\n");
        } in .close();
        return sb.toString();
    }

    void showRootStatus() {
        boolean isrooted = doesSuperuserApkExist("/system/app/Superuser.apk")||
                doesSUexist();
        if(isrooted==true)
        {
            root_status.setText("Rooted Device!!");
        }
        else
        {
            root_status.setText("Device not Rooted!!");
        }
    }

    private boolean doesSUexist() {
        Process process = null;
        try {
            process = Runtime.getRuntime().exec(new String[] { "/system/xbin/which", "su" });
            BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
            if (in.readLine() != null) return true;
            return false;
        } catch (Throwable t) {
            return false;
        } finally {
            if (process != null) process.destroy();
        }

    }

    private boolean doesSuperuserApkExist(String s) {

        File rootFile = new File("/system/app/Superuser.apk");
        Boolean doesexist = rootFile.exists();
        if(doesexist == true)
        {
            return(true);
        }
        else
        {
            return(false);
        }
    }

    /*
    The page that allows the user to allow password change for the logged in user
    */
	protected void changePasswd() {
		// TODO Auto-generated method stub
		Intent cP = new Intent(getApplicationContext(), ChangePassword.class);
		cP.putExtra("uname", uname);
		startActivity(cP);
	}

	/*
	The function that allows the user to view transaction history for the logged in user
	*/
	protected void viewStatment() {
		// TODO Auto-generated method stub
		Intent vS = new Intent(getApplicationContext(), ViewStatement.class);
		vS.putExtra("uname", uname);
		startActivity(vS);
	}
	// Added for handling menu operations
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	// Added for handling menu operations
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar wil
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			callPreferences();
			return true;
		} else if (id == R.id.action_exit) {
			Intent i = new Intent(getBaseContext(), LoginActivity.class);
			i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(i);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public void callPreferences() {
		// TODO Auto-generated method stub
		Intent i = new Intent(this, FilePrefActivity.class);
		startActivity(i);
	}
}