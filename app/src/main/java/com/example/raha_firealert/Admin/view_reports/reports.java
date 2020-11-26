package com.example.raha_firealert.Admin.view_reports;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.anychart.AnyChart;
import com.anychart.AnyChartView;
import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.chart.common.dataentry.ValueDataEntry;
import com.anychart.chart.common.listener.Event;
import com.anychart.chart.common.listener.ListenersInterface;
import com.anychart.charts.Pie;
import com.anychart.enums.Align;
import com.anychart.enums.LegendLayout;
import com.example.raha_firealert.Admin.announcement.AddAnnouncement;
import com.example.raha_firealert.MyConfig;
import com.example.raha_firealert.R;
import com.example.raha_firealert.User.ui.announcement.AnnouncementInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class reports extends Fragment {
    Spinner spinner_year,spinner_month;
    Button btn_submit;
    List<DataEntry> data;
    AnyChartView anyChartView;
    Pie pie;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_reports, container, false);

        spinner_year = v.findViewById(R.id.spinner_year_id);
        new get_year().execute();



        List<String> month_array =  new ArrayList<String>();
        for (int i = 0; i < 12; i++) {
            Calendar cal = Calendar.getInstance();
            SimpleDateFormat month_date = new SimpleDateFormat("MMMM");
            cal.set(Calendar.MONTH, i);
            String month_name = month_date.format(cal.getTime());

            month_array.add(month_name);
        }



        spinner_month = v.findViewById(R.id.spinner_month_id);
        ArrayAdapter<String> month_adapter = new ArrayAdapter<String>(
                getActivity(), R.layout.spinner_month, month_array);
        month_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_month.setAdapter(month_adapter);


        anyChartView = v.findViewById(R.id.any_chart_view);
        anyChartView.setProgressBar(v.findViewById(R.id.progress_bar));

        pie = AnyChart.pie();

        pie.setOnClickListener(new ListenersInterface.OnClickListener(new String[]{"x", "value"}) {
            @Override
            public void onClick(Event event) {
                Toast.makeText(getActivity(), event.getData().get("x") + ":" + event.getData().get("value"), Toast.LENGTH_SHORT).show();
            }
        });

//        List<DataEntry> datas = new ArrayList<>();
//        datas.add(new ValueDataEntry("Apples", 6371664));
//        datas.add(new ValueDataEntry("Pears", 789622));
//        datas.add(new ValueDataEntry("Bananas", 7216301));

        pie.title("Alert");

        pie.labels().position("outside");

        pie.legend().title().enabled(true);
        pie.legend().title()
                .text("Reports")
                .padding(0d, 0d, 10d, 0d);

        pie.legend()
                .position("center-bottom")
                .itemsLayout(LegendLayout.HORIZONTAL)
                .align(Align.CENTER);

        anyChartView.setChart(pie);
//


        btn_submit = v.findViewById(R.id.btn_submit_id);
        btn_submit.setOnClickListener(v1 -> {
            new get_alerts().execute();
        });

        return v;
    }


    class get_year extends AsyncTask<String,Void,String>{
        ProgressDialog pd;

        @Override
        protected String doInBackground(String... strings) {
            OkHttpClient getstudents = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(MyConfig.base_url+"/alert_request/get_year")
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
            pd = new ProgressDialog(getActivity());
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
                JSONArray years = jsonObject.getJSONArray("year");
                ArrayList<String> yearArray = new ArrayList<>();

                for (int i = 0; i < years.length(); i++){
                    String year = years.get(i).toString();
                    yearArray.add(year);
                    Log.d("check", year);
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                        getActivity(), android.R.layout.simple_spinner_item, yearArray);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner_year.setAdapter(adapter);

                new get_alerts().execute();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            pd.dismiss();
        }
    }


    class get_alerts extends AsyncTask<String,Void,String>{
        ProgressDialog pd;
        String year = spinner_year.getSelectedItem().toString();
        String month = spinner_month.getSelectedItem().toString();
        @Override
        protected String doInBackground(String... strings) {
            OkHttpClient getstudents = new OkHttpClient();

            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("year",year)
                    .addFormDataPart("month",month)
                    .build();

            Request request = new Request.Builder()
                    .url(MyConfig.base_url+"/alert_request/get_alerts")
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
            } catch (
                    IOException e) {
                e.printStackTrace();
            }
            return null;
        }


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pd = new ProgressDialog(getActivity());
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
                JSONObject alerts = jsonObject.getJSONObject("alerts");
                int responded = alerts.getInt("responded");
                int not_responded = alerts.getInt("not_responded");

                Log.d("check", String.valueOf(responded));

                List<DataEntry> datas = new ArrayList<>();
                datas.add(new ValueDataEntry("Responded", responded));
                datas.add(new ValueDataEntry("Not Responded", not_responded));

                pie.data(datas);


            } catch (JSONException e) {
                e.printStackTrace();
            }
            pd.dismiss();
        }
    }
}
