package com.example.raha_firealert.Admin.announcement;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.raha_firealert.Dashboard;
import com.example.raha_firealert.Login;
import com.example.raha_firealert.MyConfig;
import com.example.raha_firealert.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AddAnnouncement extends AppCompatActivity {

    TextInputLayout etl_title,etl_details;
    TextInputEditText et_title,et_details;
    private static final int RESULT_LOAD_IMAGE = 1;
    Uri selectedImage;
    String path;
    String type = null;
    TextView tv_imagename;
    Button btn_image;
    File file;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_announcement);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        tv_imagename = findViewById(R.id.tv_imagename_id);
        etl_title = findViewById(R.id.etl_title_id);
        etl_details = findViewById(R.id.etl_details_id);
        btn_image = findViewById(R.id.btn_image_id);

        et_title = findViewById(R.id.et_title_id);
        et_details = findViewById(R.id.et_details_id);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= 23){
            if (!checkPermission()){
                requestPermission();
            }
        }
    }

    private boolean checkPermission(){
        int result = ContextCompat.checkSelfPermission(AddAnnouncement.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (result == PackageManager.PERMISSION_GRANTED) {
            Log.d("value","Check Permission Granted");
            return true;
        } else {
            return false;
        }
    }

    private void requestPermission(){
        if (ActivityCompat.shouldShowRequestPermissionRationale(AddAnnouncement.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Toast.makeText(AddAnnouncement.this, "Write External Storage permission allows us to get image. Please allow this permission in App Settings.", Toast.LENGTH_LONG).show();
            btn_image.setEnabled(false);
        } else {
            ActivityCompat.requestPermissions(AddAnnouncement.this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("value", "Permission Granted, Now you can use local drive .");
                    btn_image.setEnabled(true);
                } else {
                    Toast.makeText(AddAnnouncement.this, "Write External Storage permission allows us to save image. Please allow this permission in App Settings.", Toast.LENGTH_LONG).show();
                    Log.d("value", "Permission Denied, You cannot use local drive .");
                    btn_image.setEnabled(false);
                }
                break;
        }
    }


    public void cancel(View v){
        Intent goback = new Intent(this, Dashboard.class);
        startActivity(goback);
        finish();
    }


    public void save(View v){
        InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        new save_announcement().execute();
    }

    public void pickPhoto(View v){
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent,RESULT_LOAD_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && data != null){
            Log.d("new_check" , data.getData().toString());
            selectedImage = data.getData();
//            pimageview.setImageURI(selectedImage);

            path = getPath(selectedImage);
            file = new File(path);

            String extension = MimeTypeMap.getFileExtensionFromUrl(path);
            tv_imagename.setText(file.getName());
            if (extension != null) {
                type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
            }
        }
    }

    public String getPath(Uri uri) {
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();

        return cursor.getString(column_index);
    }


    class save_announcement extends AsyncTask<String,Void,String>{

        ProgressDialog pd;
        String title = et_title.getText().toString();
        String details = et_details.getText().toString();
        @Override
        protected String doInBackground(String... strings) {
            OkHttpClient getstudents = new OkHttpClient();
            RequestBody requestBody;
            if (path==null){
                requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("title",title)
                        .addFormDataPart("details",details)
                        .build();
            }
            else{
                requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("title",title)
                        .addFormDataPart("details",details)
                        .addFormDataPart("image",file.getName(),RequestBody.create(MediaType.parse(type),file))
                        .build();
            }

            Request request = new Request.Builder()
                    .url(MyConfig.base_url+"/announcements")
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
            pd = new ProgressDialog(AddAnnouncement.this);
            pd.setMessage("Loading...");
            pd.setIndeterminate(false);
            pd.setCancelable(false);
            pd.show();
        }

        @Override
        protected void onPostExecute(String s) {
            try {
                JSONObject jsonObject = new JSONObject(s);
                boolean response = jsonObject.getBoolean("success");
                if (response){

                    Intent success = new Intent(AddAnnouncement.this,Dashboard.class);
                    success.putExtra("from_activity","announcement");
                    startActivity(success);
                    finish();
                    etl_title.setError(null);
                    etl_details.setError(null);
                    Toast.makeText(AddAnnouncement.this,"Announcement has been added successfully.",Toast.LENGTH_LONG).show();
                }
                else{
                    JSONObject error = jsonObject.getJSONObject("response");
                    if (error.has("title")){
                        JSONArray name = error.getJSONArray("title");
                        etl_title.setError(name.get(0).toString());
                    }

                    if (error.has("details")){
                        JSONArray email = error.getJSONArray("details");
                        etl_details.setError(email.get(0).toString());
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            pd.dismiss();
        }
    }



    @Override
    public void onBackPressed() {
        Intent goback = new Intent(this, Dashboard.class);
        startActivity(goback);
        finish();
    }

    @Override
    public boolean onSupportNavigateUp() {
        Intent goback = new Intent(this, Dashboard.class);
        startActivity(goback);
        finish();
        return true;
    }
}
