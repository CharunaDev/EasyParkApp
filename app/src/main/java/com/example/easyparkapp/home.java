package com.example.easyparkapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;

public class home extends AppCompatActivity implements View.OnClickListener{
  //Define MAP Cardview
    private CardView bankcardId;
    private CardView add_park;
    FirebaseAuth auth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
//Find Map cardview by resource id
        bankcardId = (CardView) findViewById(R.id.bankcardId);
        bankcardId.setOnClickListener(this);

        add_park = (CardView) findViewById(R.id.add_park);
        add_park.setOnClickListener(this);

        auth = FirebaseAuth.getInstance();

    }

    public void onClick(View view) {
        Intent intent;
        switch (view.getId()){
            case R.id.bankcardId : intent = new Intent(this, MainActivity.class);startActivity(intent); break;

            case R.id.add_park : intent = new Intent(this, MapsActivity.class);startActivity(intent); break;
            default:break;
        }
    }

    //Toolbar
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

       switch (item.getItemId()){
           case R.id.logout:{
               FirebaseAuth.getInstance().signOut();
               Intent logout = new Intent(home.this,login.class);
               startActivity(logout);
               finish();
           }
       }
        return super.onOptionsItemSelected(item);
    }
}
