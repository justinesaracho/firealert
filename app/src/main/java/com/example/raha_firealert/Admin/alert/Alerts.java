package com.example.raha_firealert.Admin.alert;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.raha_firealert.MyConfig;
import com.example.raha_firealert.R;
import com.example.raha_firealert.User.ui.announcement.ViewAnnouncement;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.TimeZone;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Alerts extends Fragment {

    RecyclerView rv_alerts;
    RecyclerView.Adapter adapter;
    SwipeRefreshLayout swipeRefreshLayout;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_alerts, container, false);


        rv_alerts = view.findViewById(R.id.rv_alerts_id);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        rv_alerts.setLayoutManager(layoutManager);
        rv_alerts.setHasFixedSize(true);
        rv_alerts.addItemDecoration(new DividerItemDecoration(getActivity(),
                DividerItemDecoration.VERTICAL));
        rv_alerts.setAdapter(adapter);
        swipeRefreshLayout = view.findViewById(R.id.srl_alerts_id);
        swipeRefreshLayout.setOnRefreshListener(() -> new getAlerts().execute());
        swipeRefreshLayout.setRefreshing(true);
        new getAlerts().execute();

        return view;
    }


    class getAlerts extends AsyncTask<String,Void,String>{

        @Override
        protected String doInBackground(String... strings) {
            OkHttpClient getstudents = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(MyConfig.base_url+"/alert")
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
            swipeRefreshLayout.setRefreshing(true);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            try {
                JSONObject jsonObject = new JSONObject(s);
                JSONArray announcementArray = jsonObject.getJSONArray("data");
                ArrayList<AlertsInfo> alertsInfoArrayList = new ArrayList<>();
                AlertsInfo alertsInfo;

                for (int i = 0; i < announcementArray.length(); i++){
                    jsonObject = announcementArray.getJSONObject(i);
                    String id = jsonObject.getString("alert_id");
                    String title = jsonObject.getString("name");
                    String details = jsonObject.getString("address");
                    String created_at = jsonObject.getString("created_at");

                    alertsInfo = new AlertsInfo(id,title,details,created_at);
                    alertsInfoArrayList.add(alertsInfo);
                }
                adapter = new alertsAdapter(getContext(),alertsInfoArrayList);
                rv_alerts.setAdapter(adapter);
                adapter.notifyDataSetChanged();
                swipeRefreshLayout.setRefreshing(false);


            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }


    private class alertsAdapter extends RecyclerView.Adapter<alertsAdapter.ViewHolder>{
        private ArrayList<AlertsInfo> alertsInfos;
        private LayoutInflater mInflater;

        public alertsAdapter(Context context, ArrayList<AlertsInfo> alertsInfos) {
            this.mInflater = LayoutInflater.from(context);
            this.alertsInfos = alertsInfos;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = mInflater.from(parent.getContext()).inflate(R.layout.alerts_recyclerview,parent,false);
            int height = parent.getMeasuredHeight() / 4;
            view.setMinimumHeight(height);
            ViewHolder vh = new ViewHolder(view);
            return vh;
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            String alert_id = alertsInfos.get(position).getId();
            final String title = String.valueOf(alertsInfos.get(position).getName());
            final String details = alertsInfos.get(position).getAddress();
            String string_date = alertsInfos.get(position).getDate();

            holder.tv_name.setText(title);
            holder.tv_details.setText(details);
            holder.tv_date.setText(string_date);


            holder.cl_alerts_id.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent view_alert = new Intent(getActivity(), AlertRespond.class);
                    view_alert.putExtra("alert_id",alert_id);
                    startActivity(view_alert);
                    getActivity().finish();
                }
            });
        }

        @Override
        public int getItemCount() {
            return alertsInfos.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView tv_name,tv_details,tv_date;
            ConstraintLayout cl_alerts_id;
            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tv_name = itemView.findViewById(R.id.tv_subject_id);
                tv_details = itemView.findViewById(R.id.tv_content_id);
                tv_date = itemView.findViewById(R.id.tv_date_id);
                cl_alerts_id = itemView.findViewById(R.id.cl_alerts_id);
            }
        }
    }
}
