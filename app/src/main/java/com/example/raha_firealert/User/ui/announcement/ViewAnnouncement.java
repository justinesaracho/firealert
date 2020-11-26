package com.example.raha_firealert.User.ui.announcement;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.raha_firealert.Dashboard;
import com.example.raha_firealert.MyConfig;
import com.example.raha_firealert.R;
import com.squareup.picasso.Picasso;

public class ViewAnnouncement extends AppCompatActivity {
    TextView tv_title,tv_details,tv_date;
    ImageView iv_image;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_announcement);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        tv_details = findViewById(R.id.tv_content_id);
        tv_details.setMovementMethod(new ScrollingMovementMethod());
        tv_title = findViewById(R.id.tv_subject_id);
        tv_date = findViewById(R.id.tv_date_id);
        iv_image = findViewById(R.id.iv_image_id);


        String image = getIntent().getStringExtra("image");
        Log.d("new_check", String.valueOf(!image.equals("null")));
        Log.d("new_check", image);
        if (!image.equals("null")){
            String url = MyConfig.image_url+image;
            Picasso.get()
                    .load(url)
                    .placeholder(R.drawable.image_placeholder)
                    .error(R.drawable.broken_image)
                    .into(iv_image);
        }
        else{
            iv_image.setImageResource(R.drawable.no_image);
        }



        String title = getIntent().getStringExtra("title");
        tv_title.setText(title);

        String details = getIntent().getStringExtra("details");
        tv_details.setText(details);

        String date = getIntent().getStringExtra("date");
        tv_date.setText(date);


    }

    @Override
    public void onBackPressed() {
        Intent goback = new Intent(this, Dashboard.class);
        goback.putExtra("from_activity","announcement");
        startActivity(goback);
        finish();
    }

    @Override
    public boolean onSupportNavigateUp() {
        Intent goback = new Intent(this, Dashboard.class);
        goback.putExtra("from_activity","announcement");
        startActivity(goback);
        finish();
        return true;
    }
}
