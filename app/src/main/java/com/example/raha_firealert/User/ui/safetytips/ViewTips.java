package com.example.raha_firealert.User.ui.safetytips;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.widget.TextView;

import com.example.raha_firealert.Dashboard;
import com.example.raha_firealert.R;

public class ViewTips extends AppCompatActivity {
    TextView tv_subject,tv_content,tv_date;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_tips);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        tv_content = findViewById(R.id.tv_content_id);
        tv_content.setMovementMethod(new ScrollingMovementMethod());
        tv_subject = findViewById(R.id.tv_subject_id);
        tv_date = findViewById(R.id.tv_date_id);

        String subject = getIntent().getStringExtra("subject");
        tv_subject.setText(subject);

        String content = getIntent().getStringExtra("content");
        tv_content.setText(Html.fromHtml(content));

        String date = getIntent().getStringExtra("date");
        tv_date.setText(date);

    }


    @Override
    public void onBackPressed() {
        Intent goback = new Intent(this, Dashboard.class);
        goback.putExtra("from_activity","tips");
        startActivity(goback);
        finish();
    }

    @Override
    public boolean onSupportNavigateUp() {
        Intent goback = new Intent(this, Dashboard.class);
        goback.putExtra("from_activity","tips");
        startActivity(goback);
        finish();
        return true;

    }
}
