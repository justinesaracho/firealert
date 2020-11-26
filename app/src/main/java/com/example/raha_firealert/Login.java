package com.example.raha_firealert;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.identity.BeginSignInRequest;
import com.google.android.gms.auth.api.identity.BeginSignInResult;
import com.google.android.gms.auth.api.identity.Identity;
import com.google.android.gms.auth.api.identity.SignInClient;
import com.google.android.gms.auth.api.identity.SignInCredential;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Login extends AppCompatActivity implements View.OnClickListener {
    String token;
    public static String LOGINLOGOUTPREF_NAME = "loginlogoutpref";
    public static String PROFILEPREF_NAME = "profilespref";
    public static String SIGNINWITHGOOGLEPREF_NAME = "signinwithgooglepref";
    private TextView tv_error;
    private TextInputLayout etl_email,etl_password;
    private TextInputEditText et_email,et_password;
    SharedPreferences loginlogout_pref,profileinfo_pref,signinwithgoogle_pref;
    private SignInClient oneTapClient;
    private BeginSignInRequest signInRequest;
    private static final int RC_SIGN_IN = 2;  // Can be any integer unique to the Activity.
    private boolean showOneTapUI = true;
    SignInButton btn_googlesignin;
    Context context;
    SharedPreferences.Editor signinwithgoogle_pref_editor;

    GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        context = this;
        tv_error = findViewById(R.id.tv_error_id);
        etl_email = findViewById(R.id.etl_email_id);
        etl_password = findViewById(R.id.etl_password_id);

        et_email = findViewById(R.id.et_email_id);
        et_password = findViewById(R.id.et_password_id);

        btn_googlesignin = findViewById(R.id.btn_googlesignin_id);
        btn_googlesignin.setOnClickListener(this);

        et_email.setText("admin@admin.com");
        et_password.setText("secret");

        loginlogout_pref = getSharedPreferences(LOGINLOGOUTPREF_NAME,Context.MODE_PRIVATE);
        profileinfo_pref = getSharedPreferences(PROFILEPREF_NAME,Context.MODE_PRIVATE);
        signinwithgoogle_pref = getSharedPreferences(SIGNINWITHGOOGLEPREF_NAME,Context.MODE_PRIVATE);
        signinwithgoogle_pref_editor = signinwithgoogle_pref.edit();


        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);


        boolean loggedin = loginlogout_pref.getBoolean("loggedin",false);
        Log.d("check", String.valueOf(loggedin));
        if (loggedin){
//            subscribeToTopic();
            Intent godashboard = new Intent(Login.this, Dashboard.class);
            startActivity(godashboard);
            finish();
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
//        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
//        updateUI(account);
        boolean with_google = signinwithgoogle_pref.getBoolean("signin_with_google",false);
        if (with_google){
            signOutGoogle();
        }
    }

    private void updateUI(GoogleSignInAccount account){
        if (account != null){
            Log.d("new_check", account.getEmail());
        }
    }

    public void subscribeToTopic(){
        if (profileinfo_pref.getString("role","").equals("administrator")){
//            PushNotifications.start(this, "7c8a90bf-9d1a-4d66-bdf1-a3cd437457bb");
//            PushNotifications.addDeviceInterest("alert");
            FirebaseMessaging.getInstance().subscribeToTopic("alert");
        }
        else{
            FirebaseMessaging.getInstance().subscribeToTopic("announcement");
        }
    }


    public void login(View v){
        new login().execute();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_googlesignin_id:
                signUp();
                break;
        }
    }

    private void signUp() {
        Log.d("new_check","signinwithgoogle clicked");
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void signOutGoogle(){
        mGoogleSignInClient.signOut()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Log.d("new_check","Account Signed out");
                    }
                });

        revokeAccess();
    }

    private void revokeAccess() {
        mGoogleSignInClient.revokeAccess()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Log.d("new_check","Access Revoked");
                    }
                });

        signinwithgoogle_pref_editor.clear();
        signinwithgoogle_pref_editor.commit();
        signinwithgoogle_pref_editor.apply();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            // Signed in successfully, show authenticated UI.
//            updateUI(account);
            String name = account.getDisplayName();
            String email = account.getEmail();
            new checkGoogleAccount(name,email).execute();

        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.d("new_check", "signInResult:failed code=" + e.getStatusCode());
//            updateUI(null);
        }
    }

    class checkGoogleAccount extends AsyncTask<String,Void,String>{
        ProgressDialog pd;
        String name,email;

        public checkGoogleAccount(String name, String email) {
            this.name = name;
            this.email = email;
        }

        @Override
        protected String doInBackground(String... strings) {
            OkHttpClient getstudents = new OkHttpClient();
            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("email",email)
                    .build();
            Request request = new Request.Builder()
                    .url(MyConfig.base_url+"/login_request/google_account")
                    .post(requestBody)
                    .build();
            try {
                Response response = getstudents.newCall(request).execute();
                Log.d("check", String.valueOf(response.code()));
//                Log.d("check", response.body().string());
                if (response.isSuccessful()){
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
            super.onPreExecute();
            pd = new ProgressDialog(Login.this);
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
                Log.d("check", String.valueOf(jsonObject));
                boolean response = jsonObject.getBoolean("success");
                if (response){
                    JSONObject user = jsonObject.getJSONObject("response");

                    loginlogout_pref = getSharedPreferences(LOGINLOGOUTPREF_NAME,Context.MODE_PRIVATE);
                    SharedPreferences.Editor loginlogout_pref_editor = loginlogout_pref.edit();
                    loginlogout_pref_editor.putBoolean("loggedin",true);
                    loginlogout_pref_editor.apply();


                    String id = user.getString("id");
                    String name = user.getString("name");
                    String email = user.getString("email");
                    String mobilenumber = user.getString("mobile_number");
                    String role = jsonObject.getString("role");

                    profileinfo_pref = getSharedPreferences(PROFILEPREF_NAME,Context.MODE_PRIVATE);
                    SharedPreferences.Editor profileinfo_pref_editor = profileinfo_pref.edit();
                    profileinfo_pref_editor.putString("id",id);
                    profileinfo_pref_editor.putString("name",name);
                    profileinfo_pref_editor.putString("email",email);
                    profileinfo_pref_editor.putString("mobile_number",mobilenumber);
                    profileinfo_pref_editor.putString("role",role);
                    profileinfo_pref_editor.apply();
                    tv_error.setText("");

                    Intent goDashboard = new Intent(Login.this,Dashboard.class);
                    startActivity(goDashboard);
                    finish();
                }
                else{
//                    String error = jsonObject.getString("response");
//                    tv_error.setText(error);
                    signinwithgoogle_pref_editor.putBoolean("signin_with_google",true);
                    signinwithgoogle_pref_editor.commit();
                    signinwithgoogle_pref_editor.apply();
                    Intent intent = new Intent(Login.this,Register.class);
                    intent.putExtra("name",name);
                    intent.putExtra("email",email);
                    intent.putExtra("with_google",true);
                    startActivity(intent);
                    finish();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            pd.dismiss();
        }
    }


    class login extends AsyncTask<String,Void,String>{
        ProgressDialog pd;
        String email = et_email.getText().toString() ;
        String password = et_password.getText().toString();
        @Override
        protected String doInBackground(String... strings) {
            Log.d("check",email);
            Log.d("check",password);

            OkHttpClient getstudents = new OkHttpClient();
            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("email",email)
                    .addFormDataPart("password",password)
                    .build();
            Request request = new Request.Builder()
                    .url(MyConfig.base_url+"/login_request/login_submit")
                    .post(requestBody)
                    .build();
            try {
                Response response = getstudents.newCall(request).execute();
                Log.d("check", String.valueOf(response.code()));
//                Log.d("check", response.body().string());
                if (response.isSuccessful()){
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
            super.onPreExecute();
            pd = new ProgressDialog(Login.this);
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
                Log.d("check", String.valueOf(jsonObject));
                boolean response = jsonObject.getBoolean("success");
                if (response){
                    JSONObject user = jsonObject.getJSONObject("response");

                    loginlogout_pref = getSharedPreferences(LOGINLOGOUTPREF_NAME,Context.MODE_PRIVATE);
                    SharedPreferences.Editor loginlogout_pref_editor = loginlogout_pref.edit();
                    loginlogout_pref_editor.putBoolean("loggedin",true);
                    loginlogout_pref_editor.apply();


                    String id = user.getString("id");
                    String name = user.getString("name");
                    String email = user.getString("email");
                    String mobilenumber = user.getString("mobile_number");
                    String role = jsonObject.getString("role");

                    profileinfo_pref = getSharedPreferences(PROFILEPREF_NAME,Context.MODE_PRIVATE);
                    SharedPreferences.Editor profileinfo_pref_editor = profileinfo_pref.edit();
                    profileinfo_pref_editor.putString("id",id);
                    profileinfo_pref_editor.putString("name",name);
                    profileinfo_pref_editor.putString("email",email);
                    profileinfo_pref_editor.putString("mobile_number",mobilenumber);
                    profileinfo_pref_editor.putString("role",role);
                    profileinfo_pref_editor.apply();
                    tv_error.setText("");

                    Intent goDashboard = new Intent(Login.this,Dashboard.class);
                    startActivity(goDashboard);
                    finish();
                }
                else{
                    String error = jsonObject.getString("response");
                    tv_error.setText(error);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            pd.dismiss();
        }
    }


    public void register(View v){
        Intent register = new Intent(this,Register.class);
        startActivity(register);
        finish();


    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (getCurrentFocus() != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
        return super.dispatchTouchEvent(ev);
    }


}
