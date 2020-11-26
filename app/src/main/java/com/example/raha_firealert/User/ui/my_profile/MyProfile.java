package com.example.raha_firealert.User.ui.my_profile;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.Toast;

import com.example.raha_firealert.Dashboard;
import com.example.raha_firealert.Login;
import com.example.raha_firealert.MyConfig;
import com.example.raha_firealert.R;
import com.example.raha_firealert.Register;
import com.example.raha_firealert.User.ui.change_password.Change_password;
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


public class MyProfile extends Fragment {

    private SharedPreferences myprofile,profileinfo_pref;
    TextInputEditText et_name,et_email,et_mobilenumber;
    TextInputLayout etl_name,etl_email,etl_mobilenumber;
    Button btn_update_profile,btn_change_password;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_my_profile, container, false);

        et_name = view.findViewById(R.id.et_name_id);
        et_email = view.findViewById(R.id.et_email_id);
        et_mobilenumber = view.findViewById(R.id.et_mobilenumber_id);

        etl_name = view.findViewById(R.id.etl_name_id);
        etl_email = view.findViewById(R.id.etl_email_id);
        etl_mobilenumber = view.findViewById(R.id.etl_mobilenumber_id);


        myprofile = getActivity().getSharedPreferences(Login.PROFILEPREF_NAME, Context.MODE_PRIVATE);
        String name = myprofile.getString("name","");
        String email = myprofile.getString("email","");
        String mobile_number = myprofile.getString("mobile_number","");

        et_name.setText(name);
        et_email.setText(email);
        et_mobilenumber.setText(mobile_number);


        btn_update_profile = view.findViewById(R.id.btn_submit_update_id);
        btn_update_profile.setOnClickListener(v -> {
            new change_profile().execute();
        });


        btn_change_password = view.findViewById(R.id.btn_change_password_id);
        btn_change_password.setOnClickListener(v -> {
            Intent change_pass = new Intent(getActivity(), Change_password.class);
            startActivity(change_pass);
            getActivity().finish();
        });


        return view;
    }

    class change_profile extends AsyncTask<String,Void,String>{
        ProgressDialog pd;
        String id = myprofile.getString("id","");
        String email = et_email.getText().toString();
        String mobilenumber = et_mobilenumber.getText().toString();
        String name = et_name.getText().toString();
        @Override
        protected String doInBackground(String... strings) {
            OkHttpClient getstudents = new OkHttpClient();
            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("name",name)
                    .addFormDataPart("email",email)
                    .addFormDataPart("mobile_number",mobilenumber)
                    .build();
            Request request = new Request.Builder()
                    .url(MyConfig.base_url+"/update/edit/"+id)
                    .post(requestBody)
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
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }


        @Override
        protected void onPreExecute() {
//            super.onPreExecute();
            pd = new ProgressDialog(getActivity());
            pd.setMessage("Loading...");
            pd.setIndeterminate(false);
            pd.setCancelable(false);
            pd.show();
        }

        @Override
        protected void onPostExecute(String s) {
//            super.onPostExecute(s);
            Log.d("check", String.valueOf(s));
            etl_name.setError(null);
            etl_email.setError(null);
            etl_mobilenumber.setError(null);
            try {
                JSONObject jsonObject = new JSONObject(s);
                boolean response = jsonObject.getBoolean("success");
                if (response){

                    JSONObject user = jsonObject.getJSONObject("response");


                    String name = user.getString("name");
                    String email = user.getString("email");
                    String mobile_number = user.getString("mobile_number");

                    profileinfo_pref = getActivity().getSharedPreferences(Login.PROFILEPREF_NAME,Context.MODE_PRIVATE);
                    SharedPreferences.Editor profileinfo_pref_editor = profileinfo_pref.edit();
                    profileinfo_pref_editor.putString("name",name);
                    profileinfo_pref_editor.putString("email",email);
                    profileinfo_pref_editor.putString("mobile_number",mobile_number);
                    profileinfo_pref_editor.apply();


                    Intent success = new Intent(getActivity(),Dashboard.class);
                    success.putExtra("from_activity","myprofile");
                    startActivity(success);
                    getActivity().finish();



                    Toast.makeText(getActivity(),"Account updated successfully.",Toast.LENGTH_LONG).show();
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
                        etl_mobilenumber.setError(email.get(0).toString());
                    }


                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            pd.dismiss();

        }
    }




}
