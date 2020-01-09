package com.example.easyparkapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class register extends AppCompatActivity {
//Define variables
    private EditText inputName, inputUsername, inputNumber, inputEmail, inputPassword, inputConfirmPassword;
    private Button btnSignIn;
 //Define Firebase
    FirebaseAuth auth;
    DatabaseReference db;
//Define actionbar
    private ProgressDialog Load;







    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        Load= new ProgressDialog(register.this);

        //ini views
        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance().getReference().child("Users");

       //Define components from resources
        btnSignIn = (Button) findViewById(R.id.regBtn);
        inputName = (EditText) findViewById(R.id.regName);
        inputUsername = (EditText) findViewById(R.id.regUserName);
        inputNumber = (EditText) findViewById(R.id.regPhone);
        inputEmail = (EditText) findViewById(R.id.regMail);
        inputPassword = (EditText) findViewById(R.id.regPassword);
        inputConfirmPassword = (EditText) findViewById(R.id.regPassword2);

//When Regster button clicks this happen
        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String aname = inputName.getText().toString().trim();
                String ausername = inputUsername.getText().toString().trim();
                String email = inputEmail.getText().toString().trim();
                String password = inputPassword.getText().toString().trim();
                String cpassword = inputConfirmPassword.getText().toString().trim();
//Check whether all the components are filled or not
                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(getApplicationContext(), "Enter email address", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(aname)) {
                    Toast.makeText(getApplicationContext(), "Enter Name", Toast.LENGTH_SHORT).show();
                    return;


                }
                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(getApplicationContext(), "Enter password", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(ausername)) {
                    Toast.makeText(getApplicationContext(), "Enter Parkname", Toast.LENGTH_SHORT).show();
                    return;
                }
//Password must contain more than 6 characters
                if (password.length() < 6) {
                    Toast.makeText(getApplicationContext(), "Enter minimum 6 characters", Toast.LENGTH_SHORT).show();
                    return;
                }
//Check whether password is equal with confirmpassword
                if (!password.equals(cpassword)) {

                    Toast.makeText(getApplicationContext(), "Confirm Password Is Not Matching", Toast.LENGTH_SHORT).show();
                    return;
                } else {
//Loadingbar
                    Load.setTitle("Registration");
                    Load.setMessage("Registration on progress");
                    Load.show();
                    Load.setCanceledOnTouchOutside(true);

                    auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(getApplicationContext(), "Successfully Registered", Toast.LENGTH_SHORT).show();
                                Load.dismiss();
                                SendData();
                            } else {

                                Toast.makeText(getApplicationContext(), "Connection Error", Toast.LENGTH_SHORT).show();
                                Load.dismiss();

                            }
                        }
                    });


                }


            }
        });

    }
    //Method to read data
    private void SendData() {
        String name = inputName.getText().toString();
        String username = inputUsername.getText().toString();
        String number = inputNumber.getText().toString();
        String email = inputEmail.getText().toString();
        String id=auth.getUid().toString();

    //Methods to set newuser
        Newuser a = new Newuser(name,username,email,number,id);
        db.child(auth.getUid()).setValue(a);
        UserDash();


    }
//After successfully registered, go to home
    private void UserDash() {
        Intent dash = new Intent(register.this,home.class);
        startActivity(dash);
        finish();
    }

}
