package com.example.alonso.myapplication;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

public class UserMenu extends AppCompatActivity implements View.OnClickListener {

    ImageView foto;
    TextView DNI, NAME, LNAME, Welcome, labelDNI, labelName, labelApe;
    Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_layout);


        ProgressDialog progressDialog = new ProgressDialog(this);

        foto = (ImageView) findViewById(R.id.imgPic);
        DNI = (TextView) findViewById(R.id.textDNIshow);
        NAME = (TextView) findViewById(R.id.textNameshow);
        LNAME = (TextView) findViewById(R.id.textLNshow);

        Welcome = (TextView) findViewById(R.id.txtWelcome);
        labelDNI = (TextView) findViewById(R.id.lblNAME);
        labelName = (TextView) findViewById(R.id.lblNAME);
        labelApe = (TextView) findViewById(R.id.lblLN);

        button = (Button) findViewById(R.id.btnLogOut);

        button.setOnClickListener(this);

        LogOut();

    }

    public void LogOut(){

       Button btnLogIn = (Button) findViewById(R.id.btnLogOut);

        btnLogIn.setOnClickListener(new View.OnClickListener(){

             @Override
            public void onClick(View v) {

                Intent WelcomeIntent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(WelcomeIntent);

            }
        });

    }


    @Override
    public void onClick(View v) {



    }
}
