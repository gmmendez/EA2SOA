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

public class ActivityRegistrar extends AppCompatActivity {
    public EditText nombre, apellido, DNI, email, contraseña, comision;
    public JSONObject body = new JSONObject();
    public JSONObject rtaJson;
    public String resultado, token;
    public hiloRegistrar connectionThread;
    AlertDialog.Builder popUpError;
    ReceiverConexion receiverConectividad = new ReceiverConexion();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrar);
        popUpError = new AlertDialog.Builder(ActivityRegistrar.this);
        popUpError.setTitle("Error");

        popUpError.setPositiveButton("Entendido", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        nombre = findViewById(R.id.txtName);
        apellido = findViewById(R.id.txtSurname);
        DNI = findViewById(R.id.txtDNI);
        email = findViewById(R.id.txtEmail);
        contraseña = findViewById(R.id.txtPassword);
        comision = findViewById(R.id.txtComision);

    }

    public void registrarUser(View vistaRegistro){
        try{
            body.put("env", "PROD");
            body.put("name", nombre.getText().toString());
            body.put("lastname", apellido.getText().toString());
            body.put("dni", DNI.getText().toString());
            body.put("email", email.getText().toString());
            body.put("password", contraseña.getText().toString());
            body.put("commission", comision.getText().toString());

            connectionThread = new hiloRegistrar();
            connectionThread.start();

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    class hiloRegistrar extends Thread{
        private Handler threadHandler = new Handler(Looper.getMainLooper());
        public void run(){
            try {
                resultado = Request.requestRegistrar("http://so-unlam.net.ar/api/api/register", body);
                rtaJson = new JSONObject(resultado);
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }

            threadHandler.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        verificarRegistro(rtaJson);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    public void verificarRegistro(JSONObject response) throws JSONException {
        if(!response.getString("success").equals("true")){
            popUpError.setMessage("No se ha podido registrar un nuevo usuario. Error: " + response.getString("msg"));
            popUpError.create().show();
        }
        else{
            body = new JSONObject();
            body.put("env", "PROD");
            body.put("type_events", "Nuevo usuario");
            body.put("description", "Se ha registrado un nuevo usuario en la aplicacion.");
            token = response.getString("token");
            hiloEventoRegistrarRequest eventoRegisterarHilo = new hiloEventoRegistrarRequest(token, body);
            eventoRegisterarHilo.start();

            Toast.makeText(this, "Nuevo usuario creado.", Toast.LENGTH_SHORT).show();
        }
    }

    class hiloEventoRegistrarRequest extends Thread{
        private Handler threadHandler = new Handler(Looper.getMainLooper());
        JSONObject respuestaJson, body;
        String tokenUsuario;

        public hiloEventoRegistrarRequest(String tokenUsuario, JSONObject body){
            this.tokenUsuario = tokenUsuario;
            this.body = body;
        }

        public void run(){
            try {
                resultado = Request.requestEventos("http://so-unlam.net.ar/api/api/event", body, tokenUsuario);
                respuestaJson = new JSONObject(resultado);
            }
            catch (IOException | JSONException e) {
                e.printStackTrace();
                try {
                    respuestaJson = new JSONObject("{'success':'false'}");
                }
                catch (JSONException jsonException) {
                    jsonException.printStackTrace();
                }
            }

            threadHandler.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        validarRegistroDeEvento(respuestaJson);
                    }
                    catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    public void validarRegistroDeEvento(JSONObject responseJson) throws JSONException {
        if(!responseJson.getString("success").equals("true")){
            Toast.makeText(this, "Atencion: imposible registrar el evento en el servidor", Toast.LENGTH_SHORT).show();
        }
        else{
            iniciarActivityIngresar(token);
        }
    }

    public void iniciarActivityIngresar(String token) {
        Intent intentIngreso = new Intent(this, ActivityIngresar.class);
        intentIngreso.putExtra("token", token);
        startActivity(intentIngreso);
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
}