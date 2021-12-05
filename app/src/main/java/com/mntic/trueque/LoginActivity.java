package com.mntic.trueque;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class LoginActivity extends AppCompatActivity {

    //creating variable for edit text, textview, button, progress bar and firebase auth.
    private TextInputEditText userNameEdt, passwordEdt;
    private Button loginBtn;
    private TextView newUserTV;
    private FirebaseAuth mAuth;
    private ProgressBar loadingPB;
    Button ingresargoogle;

    private GoogleSignInClient mGoogleSignInClient;
    private final static int RC_SIGN_IN = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        //initializing all our variables.
        userNameEdt = findViewById(R.id.idEdtUserName);
        passwordEdt = findViewById(R.id.idEdtPassword);
        loginBtn = findViewById(R.id.idBtnLogin);
        newUserTV = findViewById(R.id.idTVNewUser);
        mAuth = FirebaseAuth.getInstance();
        loadingPB = findViewById(R.id.idPBLoading);
        ingresargoogle = findViewById(R.id.ingresargoogle);

        /*Creamos la solicitud para iniciar sesión con google*/
        crearSolicitud();

        //adding click listner for our new user tv.
        newUserTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //on below line opening a login activity.
                Intent i = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(i);
            }
        });

        //adding on click listener for our login button.
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //hiding our progress bar.
                loadingPB.setVisibility(View.VISIBLE);
                //getting data from our edit text on below line.
                String email = userNameEdt.getText().toString();
                String password = passwordEdt.getText().toString();
                //on below line validating the text input.
                if (TextUtils.isEmpty(email) && TextUtils.isEmpty(password)) {
                    Toast.makeText(LoginActivity.this, "Ocurrió un error al acceder", Toast.LENGTH_SHORT).show();
                    return;
                }
                //on below line we are calling a sign in method and passing email and password to it.
                mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        //on below line we are checking if the task is succes or not.
                        if (task.isSuccessful()) {
                            //on below line we are hiding our progress bar.
                            loadingPB.setVisibility(View.GONE);
                            Toast.makeText(LoginActivity.this, "Inicio de sesión correcto", Toast.LENGTH_SHORT).show();
                            //on below line we are opening our mainactivity.
                            Intent i = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(i);
                            finish();
                        } else {
                            //hiding our progress bar and displaying a toast message.
                            loadingPB.setVisibility(View.GONE);
                            Toast.makeText(LoginActivity.this, "Por favor ingrese credenciales válidas", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

        /*Evento al precionar el boton de iniciar con google*/
        ingresargoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { singIn();
            }
        });

    }

    /*Solicitud para google*/
    private void crearSolicitud() {
        //Configuramos el inicio de sesión de google
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        //Creamos un google sign in con las opciones especificas de google
        mGoogleSignInClient = GoogleSignIn.getClient(this,gso);
    }

    //Creamos la pantalla del google
    private void singIn(){
        Intent signIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signIntent,RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //Resultado devuelto al iniciar la intencion desde googlesignApi.getsignIntent
        if (requestCode == RC_SIGN_IN ){
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                //el inicio de sesion fue exitoso, autentiqqeu con firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                AutenticacionFirebase(account);//aqui se ejecuta el metodo para logearnos con google
            }catch (ApiException e){
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    /*Metodo para autenticar con firebase google*/
    private void AutenticacionFirebase(GoogleSignInAccount account){
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(),null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            //si inicia correctamente
                            FirebaseUser user = mAuth.getCurrentUser();//obtenemos al usuario, el cual quiere iniciar sesion

                            /*Si el usuario inicia sesion por primera vez*/
                            if (task.getResult().getAdditionalUserInfo().isNewUser()){



                                String uid = user.getUid();
                                String email = user.getEmail();
                                String name = user.getDisplayName();


                                //aqui pasamos los parametros
                                /*Creamos un hashmap para enviar los datos a firebase*/

                                HashMap<Object,String> DatosUsuario = new HashMap<>();

                                DatosUsuario.put("uid",uid);
                                DatosUsuario.put("email",email);
                                DatosUsuario.put("name",name);
                                /*Dejo la imagen vacia*/
                                DatosUsuario.put("image","");

                                /*Iniciamos la instancia a la base de datos*/
                                FirebaseDatabase database = FirebaseDatabase.getInstance();
                                /*Creamos la base de datos*/
                                DatabaseReference reference = database.getReference("Usuarios_de_la_app");
                                /*El nombre de la base de datos es Ususarios_de_la_app*/
                                reference.child(uid).setValue(DatosUsuario);

                            }
                            //Nos dirija al main activity
                            startActivity(new Intent(LoginActivity.this,MainActivity.class));
                        }
                        else{
                            Toast.makeText(LoginActivity.this, "Error al iniciar sesión", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }



    @Override
    protected void onStart() {
        super.onStart();
        //in on start method checking if the user is already sign in.
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            //if the user is not null then we are opening a main activity on below line.
            Intent i = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(i);
            this.finish();
        }

    }

}