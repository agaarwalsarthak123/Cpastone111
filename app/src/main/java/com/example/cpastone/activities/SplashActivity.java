package com.example.cpastone.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.example.cpastone.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SplashActivity extends AppCompatActivity {

    // firebase Auth
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        Log.d("Check Logging in11","Check Logging in");
        // init firebase auth
        firebaseAuth = FirebaseAuth.getInstance();
        Log.d("Check Logging in","Check Logging in");

        // start main screen after 2 seconds
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                //startActivity(new Intent(SplashActivity.this, MainActivity.class));
                checkUser();
               //finish();
            }
        },2000); // 2000 means 2 seconds
    }



    private void checkUser() {
        // getting current user, if logged in
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if(firebaseUser == null) {
            // user not logged in
            // start main screen
            startActivity(new Intent(SplashActivity.this, DashboardUserActivity.class));
            finish();
        }


      else {
            // user logged in checking user types, same as done in login screen
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("User");
            ref.child(firebaseUser.getUid())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot snapshot) {
                            // get user type

                            String userType = ""+snapshot.child("userType").getValue();
                           // Log.d("Sarthak123", userType);
                            // check User type
                              if(userType.equals("user")) {
                               //   Log.d("Sarthak", "I am in user");
                                // this is simple user, open user dashboard
                                startActivity(new Intent(SplashActivity.this, DashboardUserActivity.class));
                                finish();

                            }
                             else if (userType.equals("admin")) {
                              //   Log.d("Sarthak", "I am in admin");
                                // this is admin, open admin dashboard
                                startActivity(new Intent(SplashActivity.this, DashboardAdminActivity.class));
                                finish();
                            }
                             else
                             {
                                 startActivity(new Intent(SplashActivity.this, DashboardAdminActivity.class));
                                 finish();
                             }
                        //    Log.d("Sarthak", "I am outside of if");

                        }

                        @Override
                        public void onCancelled(DatabaseError error) {

                        }
                    });
        }
    }
}