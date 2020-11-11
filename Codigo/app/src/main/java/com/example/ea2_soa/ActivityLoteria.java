package com.example.ea2_soa;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Random;

public class ActivityLoteria extends AppCompatActivity implements SensorEventListener {

    public EditText numeroUsuario;
    public TextView numeroFinal, esGanador, ultimoNro;
    private SensorManager senManager;
    private Sensor senAcelerometro;
    public String token,resultado, nro;
    public float velocidad, ultimoX, ultimoY, ultimoZ;
    public long ultimaActualizacion;
    private static final int umbral = 250;
    Random numeroRandom = new Random();
    public hiloEventoRegistrarRequest eventRegisterThread;
    SharedPreferences sharedPref;
    SharedPreferences.Editor sharedPrefEditor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loteria);
        numeroUsuario = findViewById(R.id.txtUserNumber);
        numeroFinal = findViewById(R.id.lblFinalNumber);
        esGanador = findViewById(R.id.lblWinner);
        ultimoNro = findViewById(R.id.txtUltNro);
        token = getIntent().getStringExtra("token");
        senManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        senAcelerometro = senManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        senManager.registerListener(this, senAcelerometro, SensorManager.SENSOR_DELAY_NORMAL);
        sharedPref = getSharedPreferences("DatosLoteria", Context.MODE_PRIVATE);
        sharedPrefEditor = sharedPref.edit();
        ultimoNro.setText(sharedPref.getString("numero","-"));
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Sensor miSensor = event.sensor;
        if (miSensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            long curTime = System.currentTimeMillis();

            if ((curTime - ultimaActualizacion) > 100) {
                long diferenciaTiempo = (curTime - ultimaActualizacion);
                ultimaActualizacion = curTime;
                velocidad = Math.abs(x + y + z - ultimoX - ultimoY - ultimoZ)/ diferenciaTiempo * 10000;
                if (velocidad > umbral) {
                    esGanador.setBackgroundColor(Color.WHITE);
                    nro = String.valueOf(numeroRandom.nextInt(11));
                    numeroFinal.setText(nro);
                    if( numeroUsuario.getText().toString().equals(nro) ){
                        esGanador.setText("¡Ganador!");
                        esGanador.setBackgroundColor(Color.GREEN);
                    }
                    else{
                        esGanador.setText("Intentelo nuevamente");
                        esGanador.setBackgroundColor(Color.RED);
                    }
                }

                ultimoX = x;
                ultimoY = y;
                ultimoZ = z;
            }
        }
    }

    public void guardarSP(String nro){
        sharedPrefEditor.putString("numero",nro);
        sharedPrefEditor.commit();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public void iniciarActivityIngresar(View vista) {
        Intent intentIngresar = new Intent(this, ActivityIngresar.class);
        intentIngresar.putExtra("token", token);
        startActivity(intentIngresar);
    }


    public void registrarEvento(View vista){
        try {
            JSONObject body = new JSONObject();
            body.put("env", "TEST");
            body.put("type_events", "Actividad de Acelerometro");
            body.put("description", "El usuario comenzo a jugar a la loteria.");
            if(Conectividad.validarConexionAInternet(this)){
                eventRegisterThread = new hiloEventoRegistrarRequest(token, body);
                eventRegisterThread.start();
                Toast.makeText(this, "Registrando la actividad del acelerometro en el servidor.", Toast.LENGTH_SHORT).show();
            }
            else{
                Toast.makeText(this, "Usted no dispone de conexion a internet para registrar el evento", Toast.LENGTH_SHORT).show();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    class hiloEventoRegistrarRequest extends Thread{
        private Handler threadHandler = new Handler(Looper.getMainLooper());
        JSONObject rta, body;
        String token;

        public hiloEventoRegistrarRequest(String token, JSONObject body){
            this.token = token;
            this.body = body;
        }

        public void run(){
            try {
                resultado = Request.requestEventos("http://so-unlam.net.ar/api/api/event", body, token);
                rta = new JSONObject(resultado);
            }
            catch (IOException | JSONException e) {
                e.printStackTrace();
                try {
                    rta = new JSONObject("{'success':'false'}");
                }
                catch (JSONException jsonException) {
                    jsonException.printStackTrace();
                }
            }

            threadHandler.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        validarRegistroDeEvento(rta);
                    }
                    catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    public void validarRegistroDeEvento(JSONObject responseJson) throws JSONException {
        Log.i("Registro de evento",responseJson.toString());
        if(!responseJson.getString("success").equals("true")){
            Toast.makeText(this, "Su token ha expirado. Por favor ingrese nuevamente.", Toast.LENGTH_SHORT).show();
            iniciarMainActivity();
        }
        else{
            Toast.makeText(this, "Se registró el evento del sensor en el servidor", Toast.LENGTH_SHORT).show();
        }
    }

    public void iniciarMainActivity(){
        guardarSP(nro);
        Intent intentMain = new Intent(this,MainActivity.class);
        startActivity(intentMain);
    }


    protected void onPause() {
        super.onPause();
        senManager.unregisterListener(this);
        guardarSP(nro);
    }

    protected void onResume() {
        super.onResume();
        senManager.registerListener(this, senAcelerometro, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onDestroy() {
        guardarSP(nro);
        super.onDestroy();
        senManager.registerListener(this, senAcelerometro, SensorManager.SENSOR_DELAY_NORMAL);
    }
}