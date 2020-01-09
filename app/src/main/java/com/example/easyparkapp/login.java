package com.example.easyparkapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class login extends AppCompatActivity {
//Define firebase
    FirebaseAuth log;
     //Define variables
    private EditText emailA, passwordA;
    private Button btnlogin;
    private Button btnreg;
    private ProgressDialog Load;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        //Define Firebase
        log= FirebaseAuth.getInstance();
        //Action bar  load
        Load =new ProgressDialog(login.this);
    //Define components from resources
        btnlogin=(Button)findViewById(R.id.loginBtn);
        btnreg = (Button)findViewById(R.id.registerBtn);
        emailA=(EditText)findViewById(R.id.Logmail);
        passwordA=(EditText)findViewById(R.id.LogPassword);

        //To go to Register page
        btnreg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(login.this,register.class);
            }
        });
      //Process after Login button clicked
        btnlogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logA();
            }
        });

    }
    protected void onStart(){
        super.onStart();
        FirebaseUser current=log.getCurrentUser();
        if (current!= null){
            Intent logout = new Intent(login.this,home.class);
            startActivity(logout);
        }
      }
      //Method of getting details
      private void logA(){
          Load.setTitle("Login");
          Load.setMessage("Login on progress");
          Load.show();
          Load.setCanceledOnTouchOutside(true);


          String email=emailA.getText().toString().trim();
          String password=passwordA.getText().toString().trim();
          Load= new  ProgressDialog(login.this);
          log.signInWithEmailAndPassword(email,password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
              @Override
              public void onComplete(@NonNull Task<AuthResult> task) {
                  if(task.isSuccessful()){
                      Toast.makeText(getApplicationContext(),"Successfully Login", Toast.LENGTH_SHORT).show();
                      Load.dismiss();
                      senddash();

                  }
                  else{

                      Toast.makeText(getApplicationContext(),"Check Your Email Or Password",Toast.LENGTH_SHORT).show();
                      Load.dismiss();

                  }
              }
          });
    }
    private void senddash(){
        Intent home = new Intent(login.this,home.class);
        startActivity(home);
        finish();
    }
}
