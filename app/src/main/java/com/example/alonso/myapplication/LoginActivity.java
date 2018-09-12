package com.example.alonso.myapplication;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;

import java.util.Objects;


public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText TextEmail;
    private EditText TextPassword;
    private Button button, btnSign;
    private ProgressDialog progressDialog;

    private TextView lblMail, lblPw ;

    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_layout);


        firebaseAuth = FirebaseAuth.getInstance();

        TextEmail = (EditText) findViewById(R.id.Text_Email);
        TextPassword = (EditText) findViewById(R.id.Text_Password);

        lblMail = (TextView) findViewById(R.id.lbl_M);
        lblPw = (TextView) findViewById(R.id.lbl_p);

        button = (Button) findViewById(R.id.btnLogIn);
        btnSign = (Button) findViewById(R.id.btnSignIn);

        progressDialog = new ProgressDialog(this);

        button.setOnClickListener(this);
        btnSign.setOnClickListener(this);

    }

    private void Login() {

        String email = TextEmail.getText().toString().trim();
        final String password  = TextPassword.getText().toString().trim();

        if(TextUtils.isEmpty(email)){
            Toast.makeText(this,"Se debe ingresar un email",Toast.LENGTH_LONG).show();
            return;
        }

        if(TextUtils.isEmpty(password)){
            Toast.makeText(this,"Falta ingresar la contraseña",Toast.LENGTH_LONG).show();
            return;
        }

        progressDialog.setMessage("Ingresando en la aplicación...");
        progressDialog.show();

        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if(!task.isSuccessful()){

                            try{

                                throw Objects.requireNonNull(task.getException());


                            }catch(FirebaseAuthInvalidUserException fb){

                                Toast.makeText(LoginActivity.this,"El usuario no existe, es inválido o debe registrarse"+ TextEmail.getText(),Toast.LENGTH_LONG).show();

                            }catch(FirebaseAuthInvalidCredentialsException fbr){

                                Toast.makeText(LoginActivity.this,"La contraseña es incorrecta ",Toast.LENGTH_LONG).show();

                            }catch(Exception e){

                                Toast.makeText(LoginActivity.this," "+e.getMessage(),Toast.LENGTH_LONG).show();

                            }

                        }else{

                            //int pos = email.indexOf("@");
                            //String user = email.substring(0, pos);
                            Toast.makeText(LoginActivity.this,"Bienvenido "+ TextEmail.getText(),Toast.LENGTH_LONG).show();
                            Intent intencion = new Intent(getApplication(), UserMenu.class);
                            //intencion.putExtra(UserMenu.user, user);
                            startActivity(intencion);

                        }

                        progressDialog.dismiss();
                    }
                });
    }

        @Override
            public void onClick(View v) {

            switch (v.getId()) {

                case R.id.btnSignIn:

                    Intent i= new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(i);

                    break;

                case R.id.btnLogIn:

                    Login();

                    break;
            }
        }
    }












