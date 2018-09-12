package com.example.alonso.myapplication;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;

import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.Objects;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText textDNI;
    private EditText textName;
    private EditText textApel;
    private EditText textEmail;
    private EditText textPw;
    private ProgressDialog progressDialog;

    private FirebaseAuth firebaseAuth;

    private DatabaseReference mDatabase;

    private StorageReference storageReference;

    private final String CARPETA_RAIZ="misImagenesPrueba/";
    private final String RUTA_IMAGEN=CARPETA_RAIZ+"misFotos";

    final int COD_SELECCIONA=10;
    final int COD_FOTO=20;

    private ImageView image;
    private String path;
    private Button btnLoad;
    private Button btnSu;
    private Button btnF;

    private Uri miPath  = null;

    private boolean retorno = false;

    private TextView DNI, NAME, LNAME, Mail, Pass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.setTitle(getResources().getString(R.string.app_name));

        firebaseAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        storageReference = FirebaseStorage.getInstance().getReference();

        textDNI = (EditText) findViewById(R.id.txtDNI);
        textName = (EditText) findViewById(R.id.txtName);
        textApel = (EditText) findViewById(R.id.txtApel);
        textEmail = (EditText) findViewById(R.id.txtEmail);
        textPw = (EditText) findViewById(R.id.txtPw);
        image = (ImageView) findViewById(R.id.imgFoto);

        btnSu = (Button) findViewById(R.id.btnSubmt);
        btnF = (Button) findViewById(R.id.btnPic);
        btnLoad = (Button) findViewById(R.id.btnLogIn);

        DNI = (TextView) findViewById(R.id.txt_Dni);
        NAME = (TextView) findViewById(R.id.txt_Name);
        LNAME = (TextView) findViewById(R.id.txt_Ape);
        Mail = (TextView) findViewById(R.id.txt_Mail);
        Pass = (TextView) findViewById(R.id.txt_Pw);

        progressDialog = new ProgressDialog(this);

        btnSu.setOnClickListener(this);
        btnF.setOnClickListener(this);

        if(validaPermisos()){
            btnF.setEnabled(true);
        }else{
            btnF.setEnabled(false);
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        LogIn();

    }

    private boolean validaPermisos() {

        if(Build.VERSION.SDK_INT<Build.VERSION_CODES.M){
            return true;
        }

        if((checkSelfPermission(CAMERA)== PackageManager.PERMISSION_GRANTED)&&
                (checkSelfPermission(WRITE_EXTERNAL_STORAGE)==PackageManager.PERMISSION_GRANTED)){
            return true;
        }

        if((shouldShowRequestPermissionRationale(CAMERA)) ||
                (shouldShowRequestPermissionRationale(WRITE_EXTERNAL_STORAGE))){
            cargarDialogoRecomendacion();
        }else{
            requestPermissions(new String[]{WRITE_EXTERNAL_STORAGE,CAMERA},100);
        }

        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode==100){
            if(grantResults.length==2 && grantResults[0]==PackageManager.PERMISSION_GRANTED
                    && grantResults[1]==PackageManager.PERMISSION_GRANTED){
                btnLoad.setEnabled(true);
            }else{
                solicitarPermisosManual();
            }
        }

    }

    private void solicitarPermisosManual() {
        final CharSequence[] opciones={"si","no"};
        final AlertDialog.Builder alertOpciones=new AlertDialog.Builder(MainActivity.this);
        alertOpciones.setTitle("¿Desea configurar los permisos de forma manual?");
        alertOpciones.setItems(opciones, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (opciones[i].equals("si")){
                    Intent intent=new Intent();
                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri=Uri.fromParts("package",getPackageName(),null);
                    intent.setData(uri);
                    startActivity(intent);
                }else{
                    Toast.makeText(getApplicationContext(),"Los permisos no fueron aceptados",Toast.LENGTH_SHORT).show();
                    dialogInterface.dismiss();
                }
            }
        });
        alertOpciones.show();
    }

    private void cargarDialogoRecomendacion() {
        AlertDialog.Builder dialogo=new AlertDialog.Builder(MainActivity.this);
        dialogo.setTitle("Permisos Desactivados");
        dialogo.setMessage("Debe aceptar los permisos para el correcto funcionamiento de la App");

        dialogo.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                requestPermissions(new String[]{WRITE_EXTERNAL_STORAGE,CAMERA},100);
            }
        });
        dialogo.show();
    }

    private void submitUser(){

        String dni = textDNI.getText().toString().trim();
        String nombre = textName.getText().toString().trim();
        String apellido = textApel.getText().toString().trim();
        String correo = textEmail.getText().toString().trim();
        String password = textPw.getText().toString().trim();
        Bitmap picture = image.getDrawingCache();

        //para que el identificador en la base de datos sea por defecto


        if(TextUtils.isEmpty(correo)){
            Toast.makeText(this,"Se debe ingresar un email",Toast.LENGTH_LONG).show();
            return;
        }
        if(TextUtils.isEmpty(password)){
            Toast.makeText(this,"Falta ingresar la contraseña", Toast.LENGTH_LONG).show();
            return;
        }
        if(TextUtils.isEmpty(nombre)){
            Toast.makeText(this,"Se debe ingresar su nombre",Toast.LENGTH_LONG).show();
            return;
        }
        if(TextUtils.isEmpty(apellido)){
            Toast.makeText(this,"Falta ingresar su apellido",Toast.LENGTH_LONG).show();
            return;
        }
        if(TextUtils.isEmpty(dni)){
            Toast.makeText(this,"Se debe ingresar su DNI",Toast.LENGTH_LONG).show();
            return;
        }

        progressDialog.setMessage("Efectuando el registro...");
        progressDialog.show();

        firebaseAuth.createUserWithEmailAndPassword(correo, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(!task.isSuccessful()){

                            try{

                                throw Objects.requireNonNull(task.getException());

                            }catch(FirebaseAuthWeakPasswordException fb){

                                Toast.makeText(MainActivity.this,"la contraseña es muy débil ",Toast.LENGTH_LONG).show();

                            }catch(FirebaseAuthInvalidCredentialsException fbr){

                                Toast.makeText(MainActivity.this,"El correo es inválido o la contraseña se ingresó de manera incorrecta",Toast.LENGTH_LONG).show();

                            }catch(FirebaseAuthUserCollisionException fbr){

                                Toast.makeText(MainActivity.this,"Este usuario ya existe ",Toast.LENGTH_LONG).show();

                            } catch (Exception e) {

                                Toast.makeText(MainActivity.this, "No se pudo registrar el usuario ", Toast.LENGTH_LONG).show();
                            }

                        }else{

                            String foto = miPath.toString();
                            Usuario user = new Usuario(dni,nombre,apellido,correo,foto);
                            String id = mDatabase.push().getKey();
                            mDatabase.child("usuario").child(id).setValue(user);

                            Toast.makeText(MainActivity.this, "Se ha registrado el usuario con el email: " + textEmail.getText(), Toast.LENGTH_SHORT).show();

                            Intent intencion = new Intent(getApplication(), LoginActivity.class);
                            startActivity(intencion);

                        }

                        progressDialog.dismiss();
                    }
                });

    }

    public  void LogIn(){

        Button btnLogIn = (Button) findViewById(R.id.btnLogIn);

        btnLogIn.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {

                Intent LoginIntent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(LoginIntent);

            }
        });

    }

    public void LoadPicture(){

        final CharSequence[] opciones={"Tomar Foto","Cargar Imagen","Cancelar"};
        final AlertDialog.Builder alertOpciones=new AlertDialog.Builder(MainActivity.this);
        alertOpciones.setTitle("Seleccione una Opción");
        alertOpciones.setItems(opciones, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (opciones[i].equals("Tomar Foto")){
                    tomarFotografia();
                }else{
                    if (opciones[i].equals("Cargar Imagen")){
                        Intent intent=new Intent(Intent.ACTION_GET_CONTENT, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        intent.setType("image/*");
                        startActivityForResult(Intent.createChooser(intent,"Seleccione la Aplicación"),COD_SELECCIONA);
                    }else{
                        dialogInterface.dismiss();
                    }
                }
            }
        });
        alertOpciones.show();
    }

    private void tomarFotografia() {
        File fileImagen=new File(Environment.getExternalStorageDirectory(),RUTA_IMAGEN);
        boolean isCreada=fileImagen.exists();
        String nombreImagen="";
        if(!isCreada){
            isCreada=fileImagen.mkdirs();
        }

        if(isCreada){
            nombreImagen=(System.currentTimeMillis()/1000)+".jpg";
        }


        path=Environment.getExternalStorageDirectory()+
                File.separator+RUTA_IMAGEN+File.separator+nombreImagen;

        File imagen=new File(path);

        Intent intent=null;
        intent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        ////
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.N)
        {
            String authorities=getApplicationContext().getPackageName()+".provider";
            Uri imageUri= FileProvider.getUriForFile(this,authorities,imagen);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        }else
        {
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(imagen));
        }
        startActivityForResult(intent,COD_FOTO);

        ////
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){

        MainActivity.super.onActivityResult(requestCode, resultCode, data);
        if (resultCode==RESULT_OK) {

            switch (requestCode) {
                case COD_SELECCIONA:
                    miPath = data.getData();
                    image.setImageURI(miPath);

                    StorageReference filePath = storageReference.child("fotos").child(miPath.getLastPathSegment());

                    filePath.putFile(miPath).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            Toast.makeText(MainActivity.this, "La foto se guardó exitosamente ", Toast.LENGTH_SHORT).show();

                        }
                    });

                    retorno = true;

                    break;

                case COD_FOTO:
                    MediaScannerConnection.scanFile(this, new String[]{path}, null,
                            new MediaScannerConnection.OnScanCompletedListener() {
                                @Override
                                public void onScanCompleted(String path, Uri uri) {

                                    Log.i("Ruta de almacenamiento", "Path: " + path);

                                }
                            });

                    Bitmap bitmap = BitmapFactory.decodeFile(path);
                    image.setImageBitmap(bitmap);

                    retorno = true;

                    break;
            }
        }
    }

    @Override
    public void onClick(View view) {


        switch (view.getId()) {

            case R.id.btnPic:

                LoadPicture();

                break;

            case R.id.btnSubmt:

                submitUser();

                break;
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
