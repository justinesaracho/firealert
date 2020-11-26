package com.example.raha_firealert.User.ui.announcement;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.raha_firealert.Admin.announcement.AddAnnouncement;
import com.example.raha_firealert.Login;
import com.example.raha_firealert.MyConfig;
import com.example.raha_firealert.R;
import com.google.firebase.messaging.FirebaseMessaging;

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

public class AnnouncementFragment extends Fragment {

    private SharedPreferences myprofile;
    TextView textView;
    RecyclerView rv_announcement;
    RecyclerView.Adapter adapter;
    Button add_announcement;
    SwipeRefreshLayout swipeRefreshLayout;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_announcement, container, false);

        textView = view.findViewById(R.id.text_announcement);
        textView.setText("Announcement");

        myprofile = getActivity().getSharedPreferences(Login.PROFILEPREF_NAME, Context.MODE_PRIVATE);
        String role = myprofile.getString("role","");
        add_announcement = view.findViewById(R.id.btn_AddAnnouncement_id);
        if (role.equals("user")){
            add_announcement.setVisibility(View.GONE);
        }
        else{
            add_announcement.setOnClickListener(v -> {
                Intent addAnnouncement = new Intent(getActivity(), AddAnnouncement.class);
                startActivity(addAnnouncement);
                getActivity().finish();
            });
        }

        rv_announcement = view.findViewById(R.id.rv_alerts_id);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        rv_announcement.setLayoutManager(layoutManager);
        rv_announcement.setHasFixedSize(true);
        rv_announcement.addItemDecoration(new DividerItemDecoration(getActivity(),
                DividerItemDecoration.VERTICAL));
        rv_announcement.setAdapter(adapter);
        swipeRefreshLayout = view.findViewById(R.id.srl_alerts_id);
        swipeRefreshLayout.setOnRefreshListener(() -> new getAnnouncement().execute());
        swipeRefreshLayout.setRefreshing(true);
        new getAnnouncement().execute();
        return view;
    }

    class getAnnouncement extends AsyncTask<String,Void,String>{
        ProgressDialog pd;

        @Override
        protected String doInBackground(String... strings) {

            OkHttpClient getstudents = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(MyConfig.base_url+"/announcements")
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
            swipeRefreshLayout.setRefreshing(true);
//            pd = new ProgressDialog(getContext());
//            pd.setMessage("Loading...");
//            pd.setIndeterminate(false);
//            pd.setCancelable(false);
//            pd.show();
        }

        @Override
        protected void onPostExecute(String s) {
            try {
                JSONObject jsonObject = new JSONObject(s);
                JSONArray announcementArray = jsonObject.getJSONArray("data");
                ArrayList<AnnouncementInfo> announcementArrayList = new ArrayList<>();
                AnnouncementInfo announcementInfo;

                for (int i = 0; i < announcementArray.length(); i++){
                    jsonObject = announcementArray.getJSONObject(i);
                    String title = jsonObject.getString("title");
                    String details = jsonObject.getString("details");
                    String image = jsonObject.getString("image");
                    String created_at = jsonObject.getString("created_at");

                    announcementInfo = new AnnouncementInfo(title,details,image,created_at);
                    announcementArrayList.add(announcementInfo);
                }
                adapter = new announcementAdapter(getContext(),announcementArrayList);
                rv_announcement.setAdapter(adapter);
                adapter.notifyDataSetChanged();
                swipeRefreshLayout.setRefreshing(false);


            } catch (JSONException e) {
                e.printStackTrace();
            }
//            pd.dismiss();
        }
    }

    private class announcementAdapter extends RecyclerView.Adapter<announcementAdapter.ViewHolder>{
        private ArrayList<AnnouncementInfo> announcementInfos;
        private LayoutInflater mInflater;

        public announcementAdapter(Context context,ArrayList<AnnouncementInfo> announcementInfos) {
            this.mInflater = LayoutInflater.from(context);
            this.announcementInfos = announcementInfos;
        }

        @NonNull
        @Override
        public announcementAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = mInflater.from(parent.getContext()).inflate(R.layout.activity_announcement_recyclerview,parent,false);
            int height = parent.getMeasuredHeight() / 4;
            view.setMinimumHeight(height);
            ViewHolder vh = new ViewHolder(view);
            return vh;
        }

        @Override
        public void onBindViewHolder(@NonNull announcementAdapter.ViewHolder holder, int position) {
            final String title = String.valueOf(announcementInfos.get(position).title);
            final String details = announcementInfos.get(position).detailes;
            String string_date = announcementInfos.get(position).created_at;
            final String image = announcementInfos.get(position).getImage();

            holder.tv_title.setText(title);
            holder.tv_details.setText(details);
            //2020-01-26 15:05:21

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
            holder.cl_row.setOnClickListener((View v) -> {
                Intent view_announcement = new Intent(getActivity(),ViewAnnouncement.class);
                view_announcement.putExtra("title",title);
                view_announcement.putExtra("details",details);
                view_announcement.putExtra("image",image);
                view_announcement.putExtra("date", finalDate);
                startActivity(view_announcement);
                getActivity().finish();
            });
        }

        @Override
        public int getItemCount() {
            return announcementInfos.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView tv_title,tv_details,tv_date;
            ConstraintLayout cl_row;
            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tv_title = itemView.findViewById(R.id.tv_subject_id);
                tv_details = itemView.findViewById(R.id.tv_content_id);
                tv_date = itemView.findViewById(R.id.tv_date_id);
                cl_row = itemView.findViewById(R.id.cl_row_id);
            }
        }
    }

}