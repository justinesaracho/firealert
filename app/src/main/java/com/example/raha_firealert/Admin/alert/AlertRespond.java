package com.example.raha_firealert.Admin.alert;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.raha_firealert.Dashboard;
import com.example.raha_firealert.Login;
import com.example.raha_firealert.MyConfig;
import com.example.raha_firealert.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.Charset;

import okhttp3.FormBody;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AlertRespond extends AppCompatActivity {
    TextView tv_AlertDetails_id;
    Button btn_respond_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alert_respond);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        tv_AlertDetails_id = findViewById(R.id.tv_AlertDetails_id);
        btn_respond_id = findViewById(R.id.btn_respond_id);

        new getSpecAlert().execute();

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent goback = new Intent(this, Dashboard.class);
        goback.putExtra("from_activity","alertrespond");
        startActivity(goback);
        finish();
    }

    @Override
    public boolean onSupportNavigateUp() {
        Intent goback = new Intent(this, Dashboard.class);
        goback.putExtra("from_activity","alertrespond");
        startActivity(goback);
        finish();
        return true;
    }

    class getSpecAlert extends AsyncTask<String,Void,String>{
        ProgressDialog pd;
        String id = getIntent().getStringExtra("alert_id");
        @Override
        protected String doInBackground(String... strings) {
            OkHttpClient getstudents = new OkHttpClient();

            Request request = new Request.Builder()
                    .url(MyConfig.base_url+"/alert/"+id)
                    .build();
            try {
                Response response = getstudents.newCall(request).execute();
                Log.d("check", String.valueOf(response.code()));
                if (response.isSuccessful()){
                    return response.body().string();
                }
                else{
                    return null;
                }
            } catch (
                    IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pd = new ProgressDialog(AlertRespond.this);
            pd.setMessage("Loading...");
            pd.setIndeterminate(false);
            pd.setCancelable(false);
            pd.show();
        }


        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            try {
                JSONObject jsonObject = new JSONObject(s);
                String name = jsonObject.getString("name");
                String mobile_number = jsonObject.getString("mobile_number");
                String address = jsonObject.getString("address");
                String date = jsonObject.getString("created_at");
                String status = jsonObject.getString("status");
                tv_AlertDetails_id.setText("From: " + name +"\n\n");
                tv_AlertDetails_id.setText(tv_AlertDetails_id.getText() + "Mobile Number: " + mobile_number +"\n\n");
                tv_AlertDetails_id.setText(tv_AlertDetails_id.getText() + "Date: " + date +"\n\n");
                tv_AlertDetails_id.setText(tv_AlertDetails_id.getText() + "Address: " + address +"\n");

                if (status.equals("1")){
                    btn_respond_id.setClickable(false);
                    btn_respond_id.setEnabled(false);
                    btn_respond_id.setText("RESPONDED");
                }


            } catch (JSONException e) {
                e.printStackTrace();
            }
            pd.dismiss();
        }
    }


    public void respond(View v){
        new respondAlert().execute();
    }


    class respondAlert extends AsyncTask<String,Void,String>{
        ProgressDialog pd;
        String id = getIntent().getStringExtra("alert_id");
        @Override
        protected String doInBackground(String... strings) {
            OkHttpClient getstudents = new OkHttpClient();

            FormBody formBody = new FormBody.Builder(Charset.forName("utf8"))
                    .add("status", "1")
                    .build();

            Request request = new Request.Builder()
                    .url(MyConfig.base_url+"/alert/"+id)
                    .header("Content-Type","application/x-www-form-urlencoded")
                    .patch(formBody)
                    .build();
            try {
                Response response = getstudents.newCall(request).execute();
                Log.d("check", String.valueOf(response.code()));
                if (response.isSuccessful()){
                    return response.body().string();
                }
                else{
                    return null;
                }
            } catch (
                    IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pd = new ProgressDialog(AlertRespond.this);
            pd.setMessage("Loading...");
            pd.setIndeterminate(false);
            pd.setCancelable(false);
            pd.show();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            try {
                JSONObject jsonObject = new JSONObject(s);
                boolean success = jsonObject.getBoolean("success");
                String response = jsonObject.getString("response");
                if (success){
                    Toast.makeText(AlertRespond.this,response,Toast.LENGTH_LONG).show();
                    Intent goback = new Intent(AlertRespond.this, Dashboard.class);
                    goback.putExtra("from_activity","alertrespond");
                    startActivity(goback);
                    finish();
                }
                else{
                    Toast.makeText(AlertRespond.this,response,Toast.LENGTH_LONG).show();
                    Intent goback = new Intent(AlertRespond.this, Dashboard.class);
                    goback.putExtra("from_activity","alertrespond");
                    startActivity(goback);
                    finish();
                }
                pd.dismiss();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
