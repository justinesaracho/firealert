package com.example.raha_firealert;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.raha_firealert.Login;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;


public class Signout extends Fragment {

    GoogleSignInClient mGoogleSignInClient;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(getActivity(), gso);


        SharedPreferences loginlogout_pref,profileinfo_pref;
        loginlogout_pref = getActivity().getSharedPreferences(Login.LOGINLOGOUTPREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor loginlogoutedit = loginlogout_pref.edit();
        loginlogoutedit.clear();

        profileinfo_pref = getActivity().getSharedPreferences(Login.PROFILEPREF_NAME, Context.MODE_PRIVATE);
        String role = profileinfo_pref.getString("role","");

        if (role.equals("administrator")){
//            PushNotifications.stop();
            FirebaseMessaging.getInstance().unsubscribeFromTopic("alert");
        }
        else{
            FirebaseMessaging.getInstance().unsubscribeFromTopic("announcement");
        }

        SharedPreferences.Editor profileinfo_prefedit = profileinfo_pref.edit();
        profileinfo_prefedit.clear();
        if (loginlogoutedit.commit()){
            profileinfo_prefedit.commit();
            loginlogoutedit.apply();
            profileinfo_prefedit.apply();

            signOutGoogle();

            Intent logout_intent = new Intent(getActivity(),Login.class);
            startActivity(logout_intent);
            getActivity().finish();
        }
    }


    private void signOutGoogle(){
        mGoogleSignInClient.signOut()
                .addOnCompleteListener(getActivity(), new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Log.d("new_check","Account Signed out");
                    }
                });


        revokeAccess();
    }

    private void revokeAccess() {
        mGoogleSignInClient.revokeAccess()
                .addOnCompleteListener(getActivity(), new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Log.d("new_check","Access Revoked");
                    }
                });
    }

}
