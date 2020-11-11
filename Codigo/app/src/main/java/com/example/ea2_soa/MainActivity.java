package com.example.ea2_soa;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private EditText usuario,contraseña;
    public JSONObject body = new JSONObject();
    public String resultadoRequest, token;
    AlertDialog.Builder popupError;
    public HiloLogin loginThread;
    public hiloEventoLoginRequest eventRegisterThread;
    ReceiverConexion receiverConectividad = new ReceiverConexion();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        popupError = new AlertDialog.Builder(MainActivity.this);
        popupError.setTitle("Atencion");
        popupError.setMessage("Usuario y/o contraseña invalidos. Por favor, reintente.");

        usuario = findViewById(R.id.txtUser);
        contraseña = findViewById(R.id.txtPassword);



        popupError.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(receiverConectividad, filter);

    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(receiverConectividad);
    }

    public void login(View vistaInicial){
        try{
            body.put("email", usuario.getText().toString());
            body.put("password", contraseña.getText().toString());
            loginThread = new HiloLogin();
            Toast.makeText(this, "Verificando credenciales...", Toast.LENGTH_SHORT).show();
            loginThread.start();
        }
        catch(Exception ex){
            Toast.makeText(this, "La aplicacion lanzó una excepcion: "+ex, Toast.LENGTH_SHORT).show();
        }
    }

    class HiloLogin extends Thread{
        // Instancio un handler para el thread, y lo asocio al hilo principal a través del Looper
        private Handler hiloHandler = new Handler(Looper.getMainLooper());
        JSONObject jsonRta;

        public void run(){
            try {
                resultadoRequest = Request.requestLogin("http://so-unlam.net.ar/api/api/login", body);
                jsonRta = new JSONObject(resultadoRequest);
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }

            hiloHandler.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        validarIngreso(jsonRta);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }}}
            );
        }
    }

    public void validarIngreso(JSONObject responseJson) throws JSONException {

        if(!responseJson.getString("success").equals("true")){
            popupError.create().show();
        }
        else{
            token = responseJson.getString("token"); //Obtengo el token otorgado por el servidor.
            body = new JSONObject();//Armo el body para la nueva request
            body.put("env", "TEST");
            body.put("type_events", "Login");
            body.put("description", "Un usuario se ha logueado al sistema");

            eventRegisterThread = new hiloEventoLoginRequest(responseJson.getString("token"), body);
            eventRegisterThread.start();
        }
    }

    class hiloEventoLoginRequest extends Thread{
        // Instancio un handler para el thread, y lo asocio al hilo principal a través del Looper
        private Handler threadHandler = new Handler(Looper.getMainLooper());
        String token;
        JSONObject body, rtaJson;

        public hiloEventoLoginRequest(String tokenUser, JSONObject body){
            this.token = tokenUser;
            this.body = body;
        }

        public void run(){
            try {
                resultadoRequest = Request.requestEventos("http://so-unlam.net.ar/api/api/event", body, token);
                rtaJson = new JSONObject(resultadoRequest);
            }
            catch (IOException | JSONException e) {
                e.printStackTrace();
            }

            threadHandler.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        validarRegistroDeEvento(rtaJson);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    public void validarRegistroDeEvento(JSONObject responseJson) throws JSONException {
        if(!responseJson.getString("success").equals("true")){
            Toast.makeText(this, "Imposible registrar el evento de login en el servidor", Toast.LENGTH_SHORT).show();
        }
        else{
            iniciarActivityIngreso(token);
        }
    }

    public void iniciarActivityIngreso(String token){
        Intent intentIngresar = new Intent(this, ActivityIngresar.class);
        intentIngresar.putExtra("token", token);
        startActivity(intentIngresar);
    }

    public void iniciarActivityRegistrar(View vista){
        Intent intentRegistrar = new Intent(this, ActivityRegistrar.class);
        startActivity(intentRegistrar);
    }


}