package com.example.raha_firealert.User.ui.reportproblem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.Toast;

import com.example.raha_firealert.Admin.announcement.AddAnnouncement;
import com.example.raha_firealert.Dashboard;
import com.example.raha_firealert.MyConfig;
import com.example.raha_firealert.R;
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

public class ReportProblem extends Fragment {

    TextInputEditText et_subject,et_body;
    TextInputLayout etl_subject,etl_body;

    Button btn_send;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_report_problem, container, false);


        et_subject = v.findViewById(R.id.et_subject_id);
        et_body = v.findViewById(R.id.et_problem_id);

        etl_subject = v.findViewById(R.id.etl_subject_id);
        etl_body = v.findViewById(R.id.etl_problem_id);

        btn_send = v.findViewById(R.id.btn_send_id);
        btn_send.setOnClickListener(v1 -> {
            new sendReport().execute();
            View view = getActivity().getCurrentFocus();
            if (view != null) {
                InputMethodManager inputManager = (InputMethodManager) getActivity().getSystemService(
                        Context.INPUT_METHOD_SERVICE);
                View focusedView = getActivity().getCurrentFocus();
                inputManager.hideSoftInputFromWindow(focusedView.getWindowToken(),
                        InputMethodManager.HIDE_NOT_ALWAYS);
            }
//            InputMethodManager inputMethodManager = (InputMethodManager)
//                    getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
//            inputMethodManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
        });



        return v;
    }


    class sendReport extends AsyncTask<String,Void,String>{
        ProgressDialog pd;
        String subject = et_subject.getText().toString();
        String body = et_body.getText().toString();
        @Override
        protected String doInBackground(String... strings) {
            OkHttpClient getstudents = new OkHttpClient();
            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("subject",subject)
                    .addFormDataPart("body",body)
                    .build();
            Request request = new Request.Builder()
                    .url(MyConfig.base_url+"/problem")
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
            super.onPreExecute();
            pd = new ProgressDialog(getActivity());
            pd.setMessage("Sending Report...");
            pd.setIndeterminate(false);
            pd.setCancelable(false);
            pd.show();
        }


        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            try {
                JSONObject jsonObject = new JSONObject(s);
                boolean response = jsonObject.getBoolean("success");
                etl_subject.setError(null);
                etl_body.setError(null);
                if (response){


                    et_subject.setText(null);
                    et_body.setText(null);
                    Toast.makeText(getActivity(),"Problem has been sent successfully.",Toast.LENGTH_LONG).show();
                }
                else{
                    JSONObject error = jsonObject.getJSONObject("response");
                    if (error.has("subject")){
                        JSONArray name = error.getJSONArray("subject");
                        etl_subject.setError(name.get(0).toString());
                    }

                    if (error.has("body")){
                        JSONArray email = error.getJSONArray("body");
                        etl_body.setError(email.get(0).toString());
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            pd.dismiss();
        }
    }
}
