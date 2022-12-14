package com.example.cpastone.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import com.example.cpastone.databinding.ActivityLoginBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    // view binding
    private ActivityLoginBinding binding;

    // firebase auth
    private FirebaseAuth firebaseAuth;

    // progress dialog
    private ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // inti firebase auth
        firebaseAuth = FirebaseAuth.getInstance();


        // setup progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please Wait");
        progressDialog.setCanceledOnTouchOutside(false);

        // handle click, go to register screen
        binding.noAccountTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        });

        // handle click, begin login
        binding.loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateData();
            }
        });

        // handles click, open forgot password
        binding.forgotTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class));
            }
        });
    }


    private String email = "", password = "";
    private void validateData() {
        // Before logging in, doing some data validation

        // get data
        email = binding.emailEt.getText().toString().trim();
        password = binding.passwordEt.getText().toString().trim();

        // validation of data
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Invalid Email pattern", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Enter password!", Toast.LENGTH_SHORT).show();
        }
        else {
            // data is validated, beginning login
            loginUser();
        }
    }

    private void loginUser() {
        progressDialog.setMessage("Logging In...");
        progressDialog.show();

        // login user
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        // login success, checking if user is user or admin
                        checkUser();
                        //startActivity(new Intent(LoginActivity.this, DashboardAdminActivity.class));
                       //finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        // login failed
                        progressDialog.dismiss();
                        Toast.makeText(LoginActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                 });

    }

    private void checkUser() {
        progressDialog.setMessage("Checking User...");
       // checking if user is user or admin from database
        // getting current user
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

        // check in db
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("User");
     //   Log.d("Ref", ref.toString());
        ref.child(firebaseUser.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        progressDialog.dismiss();
                        // get uer type
                        String userType = ""+ snapshot.child("userType").getValue(String.class);
                     //    Log.d("Sarthak123", userType);
                      //  Log.d("Snapshot", snapshot.toString());
                        // check User type
                       if(userType.equals("user")) {
                            // this is simple user, open user dashboard
                            startActivity(new Intent(LoginActivity.this, DashboardUserActivity.class));
                            finish();
                        }
                        else if (userType.equals("admin")) {
                            // this is admin, open admin dashboard
                            startActivity(new Intent(LoginActivity.this, DashboardAdminActivity.class));
                            finish();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {

                    }
                });

    }

}