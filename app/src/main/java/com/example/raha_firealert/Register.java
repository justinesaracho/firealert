package com.example.raha_firealert;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.google.android.gms.safetynet.SafetyNet;
import com.google.android.gms.safetynet.SafetyNetApi;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Register extends AppCompatActivity {
    private TextInputEditText et_name,et_email,et_password,et_confirm_password,et_mobilenumber;
    private TextInputLayout etl_name,etl_email,etl_password,etl_confirm_password,etl_mobile_number;
    SharedPreferences loginlogout_pref,profileinfo_pref;
    private CheckBox cb_captcha;
    boolean is_recaptcha_checked;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        cb_captcha = findViewById(R.id.cb_captcha_id);

        et_name = findViewById(R.id.et_name_id);
        et_email = findViewById(R.id.et_email_id);
        et_mobilenumber = findViewById(R.id.et_mobilenumber_id);
        et_password = findViewById(R.id.et_password_id);
        et_confirm_password = findViewById(R.id.et_confirm_password_id);

        etl_name = findViewById(R.id.etl_name_id);
        etl_email = findViewById(R.id.etl_email_id);
        etl_mobile_number = findViewById(R.id.etl_mobilenumber_id);
        etl_password = findViewById(R.id.etl_password_id);
        etl_confirm_password = findViewById(R.id.etl_confirm_password_id);

        boolean with_google = getIntent().getBooleanExtra("with_google",false);
        if (with_google){
            String google_dpname = getIntent().getStringExtra("name");
            String google_email = getIntent().getStringExtra("email");
            et_name.setText(google_dpname);
            et_name.setEnabled(false);
            et_email.setText(google_email);
            et_email.setEnabled(false);
        }

        cb_captcha.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    reCaptcha();
                }
            }
        });

    }


    public void reCaptcha(){
        SafetyNet.getClient(this).verifyWithRecaptcha("6LcrKsgZAAAAAOwtbV3E-mXjJ_-tWI8T8d06mkRE")
                .addOnSuccessListener(this, new OnSuccessListener<SafetyNetApi.RecaptchaTokenResponse>() {
                    @Override
                    public void onSuccess(SafetyNetApi.RecaptchaTokenResponse response) {
                        // Indicates communication with reCAPTCHA service was
                        // successful.
                        String userResponseToken = response.getTokenResult();
                        if (!userResponseToken.isEmpty()) {
                            // Validate the user response token using the
                            // reCAPTCHA siteverify API.
                            Log.d("check", "reCaptcha: "+response.getTokenResult());
                            is_recaptcha_checked = true;
                            cb_captcha.setEnabled(false);
                        }
                        else{
                            is_recaptcha_checked = false;
                            cb_captcha.setChecked(false);
                        }
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("check","reCaptcha Error: "+e.getMessage());
                        cb_captcha.setChecked(false);
                    }
                });
    }

    public void register(View v){
        if (is_recaptcha_checked){
            new register().execute();
        }
        else{
            Toast.makeText(getApplicationContext(),"You should check the recaptcha",Toast.LENGTH_LONG).show();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!is_recaptcha_checked){
            cb_captcha.setChecked(false);
        }
    }

    class register extends AsyncTask<String,Void,String>{
        ProgressDialog pd;
        String name = et_name.getText().toString();
        String email = et_email.getText().toString();
        String mobilenumber = et_mobilenumber.getText().toString();
        String password = et_password.getText().toString();
        String confirm_password = et_confirm_password.getText().toString();
        @Override
        protected String doInBackground(String... strings) {
            OkHttpClient getstudents = new OkHttpClient();
            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("name",name)
                    .addFormDataPart("email",email)
                    .addFormDataPart("mobile_number",mobilenumber)
                    .addFormDataPart("password",password)
                    .addFormDataPart("password_confirmation",confirm_password)
                    .addFormDataPart("captcha", String.valueOf(is_recaptcha_checked))
                    .build();
            Request request = new Request.Builder()
                    .url(MyConfig.base_url+"/register")
                    .post(requestBody)
                    .build();
            try {
                Response response = getstudents.newCall(request).execute();
                Log.d("check", String.valueOf(response.code()));
                if (response.isSuccessful()){
//                    Toast.makeText(Register.this,response.body())
                    return response.body().string();
                }
                else{
                    return null;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            pd = new ProgressDialog(Register.this);
            pd.setMessage("Loading...");
            pd.setIndeterminate(false);
            pd.setCancelable(false);
            pd.show();
        }

        @Override
        protected void onPostExecute(String s) {
            pd.dismiss();
            try {
                etl_name.setError(null);
                etl_email.setError(null);
                etl_mobile_number.setError(null);
                etl_password.setError(null);
                JSONObject jsonObject = new JSONObject(s);
                Log.d("check", s);
                boolean response = jsonObject.getBoolean("success");

                if (response){
                    loginlogout_pref = getSharedPreferences(Login.LOGINLOGOUTPREF_NAME,Context.MODE_PRIVATE);
                    SharedPreferences.Editor loginlogout_pref_editor = loginlogout_pref.edit();
                    loginlogout_pref_editor.putBoolean("loggedin",true);
                    loginlogout_pref_editor.apply();

                    JSONObject user = jsonObject.getJSONObject("response");

                    String id = user.getString("id");
                    String str_name = user.getString("name");
                    String str_email = user.getString("email");
                    String str_mobilenumber = user.getString("mobile_number");
                    String role = jsonObject.getString("role");

                    profileinfo_pref = getSharedPreferences(Login.PROFILEPREF_NAME, Context.MODE_PRIVATE);
                    SharedPreferences.Editor profileinfo_pref_editor = profileinfo_pref.edit();
                    profileinfo_pref_editor.putString("id",id);
                    profileinfo_pref_editor.putString("name",str_name);
                    profileinfo_pref_editor.putString("email",str_email);
                    profileinfo_pref_editor.putString("mobile_number",str_mobilenumber);
                    profileinfo_pref_editor.putString("role",role);
                    profileinfo_pref_editor.apply();


                    Intent goDashboard = new Intent(Register.this, Dashboard.class);
                    startActivity(goDashboard);
                    finish();
                }
                else{
                    JSONObject error = jsonObject.getJSONObject("response");
                    if (error.has("name")){
                        JSONArray name = error.getJSONArray("name");
                        etl_name.setError(name.get(0).toString());
                    }

                    if (error.has("email")){
                        JSONArray email = error.getJSONArray("email");
                        etl_email.setError(email.get(0).toString());
                    }

                    if (error.has("mobile_number")){
                        JSONArray email = error.getJSONArray("mobile_number");
                        etl_mobile_number.setError(email.get(0).toString());
                    }

                    if (error.has("password")){
                        JSONArray password = error.getJSONArray("password");
                        etl_password.setError(password.get(0).toString());
                        et_confirm_password.getText().clear();
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }


    public void back(View v){

        Intent goback = new Intent(this,Login.class);
        startActivity(goback);
        finish();

    }

    @Override
    public void onBackPressed() {
        Intent goback = new Intent(this,Login.class);
        startActivity(goback);
        finish();
    }

    @Override
    public boolean onSupportNavigateUp() {
        Intent goback = new Intent(this,Login.class);
        startActivity(goback);
        finish();
        return true;
    }

    public void hideKeyboard(){
        InputMethodManager inputManager = (InputMethodManager)
                getSystemService(Context.INPUT_METHOD_SERVICE);

        inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                InputMethodManager.HIDE_NOT_ALWAYS);
    }

}
