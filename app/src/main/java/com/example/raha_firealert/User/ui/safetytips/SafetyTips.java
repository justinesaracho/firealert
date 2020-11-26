package com.example.raha_firealert.User.ui.safetytips;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.ProgressDialog;
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
import com.example.raha_firealert.User.ui.announcement.AnnouncementFragment;
import com.example.raha_firealert.User.ui.announcement.AnnouncementInfo;
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

public class SafetyTips extends Fragment {

    RecyclerView rv_tips;
    RecyclerView.Adapter adapter;
    SwipeRefreshLayout swipeRefreshLayout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_safety_tips, container, false);

        rv_tips = view.findViewById(R.id.rv_tips_id);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        rv_tips.setLayoutManager(layoutManager);
        rv_tips.setHasFixedSize(true);
        rv_tips.addItemDecoration(new DividerItemDecoration(getActivity(),
                DividerItemDecoration.VERTICAL));
        rv_tips.setAdapter(adapter);
        swipeRefreshLayout = view.findViewById(R.id.srl_tips_id);
        swipeRefreshLayout.setOnRefreshListener(() -> new getTips().execute());
        swipeRefreshLayout.setRefreshing(true);
        new getTips().execute();
        return view;
    }


    class getTips extends AsyncTask<String,Void,String>{
        @Override
        protected String doInBackground(String... strings) {
            OkHttpClient getstudents = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(MyConfig.base_url+"/tips")
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
                ArrayList<TipInfo> tipInfoArrayList = new ArrayList<>();
                TipInfo tipInfo;

                for (int i = 0; i < announcementArray.length(); i++){
                    jsonObject = announcementArray.getJSONObject(i);
                    String subject = jsonObject.getString("subject");
                    String content = jsonObject.getString("content");
                    String created_at = jsonObject.getString("created_at");

                    tipInfo = new TipInfo(subject,content,created_at);
                    tipInfoArrayList.add(tipInfo);
                }
                adapter = new tipAdapter(getContext(),tipInfoArrayList);
                rv_tips.setAdapter(adapter);
                adapter.notifyDataSetChanged();
                swipeRefreshLayout.setRefreshing(false);
            } catch (JSONException e) {
                e.printStackTrace();
            }


        }
    }


    private class tipAdapter extends RecyclerView.Adapter<tipAdapter.ViewHolder>{
        private ArrayList<TipInfo> tipInfos;
        private LayoutInflater mInflater;

        public tipAdapter(Context context, ArrayList<TipInfo> tipInfos) {
            this.mInflater = LayoutInflater.from(context);
            this.tipInfos = tipInfos;
        }

        @NonNull
        @Override
        public tipAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = mInflater.from(parent.getContext()).inflate(R.layout.activity_tip_recycler_view,parent,false);
            int height = parent.getMeasuredHeight() / 4;
            view.setMinimumHeight(height);
            tipAdapter.ViewHolder vh = new tipAdapter.ViewHolder(view);
            return vh;
        }

        @Override
        public void onBindViewHolder(@NonNull tipAdapter.ViewHolder holder, int position) {
            final String subject = String.valueOf(tipInfos.get(position).subject);
            final String content = tipInfos.get(position).content;
            String string_date = tipInfos.get(position).created_at;


            holder.tv_subject.setText(subject);
//            holder.tv_content.setText(content);

            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd,yyyy hh:mm a");
            dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            String date = null;
            try {
                date = dateFormat.format(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(string_date));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            holder.tv_date.setText(date);

            String finalDate = date;
            holder.cl_row.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent view_tips = new Intent(getActivity(), ViewTips.class);
                    view_tips.putExtra("subject",subject);
                    view_tips.putExtra("content",content);
                    view_tips.putExtra("date", finalDate);
                    startActivity(view_tips);
                    getActivity().finish();
                }
            });

        }

        @Override
        public int getItemCount() {
            return tipInfos.size();
        }


        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView tv_subject,tv_content,tv_date;
            ConstraintLayout cl_row;
            public ViewHolder(View view) {
                super(view);
                tv_subject = itemView.findViewById(R.id.tv_subject_id);
//                tv_content = itemView.findViewById(R.id.tv_content_id);
                tv_date = itemView.findViewById(R.id.tv_date_id);
                cl_row = itemView.findViewById(R.id.cl_tiprow_id);
            }
        }
    }
}
