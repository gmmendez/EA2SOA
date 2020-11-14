package com.example.ea2_soa;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class ActivityIngresar extends AppCompatActivity implements SensorEventListener{
    public TextView bateria, acelerometroX, acelerometroY, acelerometroZ, giroscopoX, giroscopoY, giroscopoZ, hora;
    public String token;
    private SensorManager senManager;
    private Sensor senAcelerometro;
    private Sensor senGiroscopo;
    public hiloEventoRegistrarRequest eventRegisterThread;
    public String resultado, resultadoRequest, horaServer;
    public HiloHora hilo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ingresar);
        bateria = findViewById(R.id.txtBattery);
        acelerometroX = findViewById(R.id.txtAccelerometerX);
        acelerometroY = findViewById(R.id.txtAccelerometerY);
        acelerometroZ = findViewById(R.id.txtAccelerometerZ);
        giroscopoX = findViewById(R.id.txtGiroscopeX);
        giroscopoY = findViewById(R.id.txtGiroscopeY);
        giroscopoZ = findViewById(R.id.txtGiroscopeZ);
        hora = findViewById(R.id.txtTime);
        senManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        senAcelerometro = senManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        senGiroscopo = senManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        senManager.registerListener(this, senAcelerometro, SensorManager.SENSOR_DELAY_NORMAL);
        senManager.registerListener(this, senGiroscopo, SensorManager.SENSOR_DELAY_NORMAL);
        token = getIntent().getStringExtra("token");
        Log.i("Token-Usuario", token);
    }

    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter intentBateria = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent estadoBateria = registerReceiver(null, intentBateria);
        int nivel = estadoBateria.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int maximo = estadoBateria.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        float bateriaFinal = (nivel / (float)maximo)*100;
        Log.i("Bateria: ", String.valueOf(bateriaFinal));
        bateria.setText(String.valueOf(bateriaFinal) + " %");
        hora.setText(horaServer);
    }

    public void buscarHora(View vista){
        if(Conectividad.validarConexionAInternet(this)) {
            hilo = new HiloHora();
            hilo.start();
            Toast.makeText(this, "Actualizando hora...", Toast.LENGTH_SHORT).show();
        }
        else{
            Toast.makeText(this, "Para actualizar la hora necesita estar conectado a internet", Toast.LENGTH_SHORT).show();
        }
        /*try{
            hilo = new HiloHora();
            Toast.makeText(this, "Actualizando hora...", Toast.LENGTH_SHORT).show();
            hilo.start();
        }
        catch(Exception ex){
            Toast.makeText(this, "Imposible actualizar hora: "+ex, Toast.LENGTH_SHORT).show();
        }*/
    }

    class HiloHora extends Thread{
        private Handler hiloHandler = new Handler(Looper.getMainLooper());
        JSONObject jsonRta;
        private String horaServer="";

        public void run(){
            try {
                resultadoRequest = Request.requestTimezone("http://worldtimeapi.org/api/timezone/America/Argentina/Buenos_Aires", "GET");
                jsonRta = new JSONObject(resultadoRequest);
                //Log.i("Hora",jsonRta.toString());
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }

            hiloHandler.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        //JSONArray jsonArray = jsonRta.getJSONArray("data"); // Parseo de la respuesta
                        horaServer = (String) jsonRta.get("datetime");
                        //JSONArray jsonArray = jsonRta.getJSONArray("data"); // Parseo de la respuesta
                        //horaServer = jsonArray.getJSONObject(0).optString("datetime");// + " ºC";
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    finally {
                        mostrarHora(horaServer);
                    }
                }
            });
        }
    }

    public void mostrarHora(String h){
        hora.setText(h.substring(11,16));
        Log.i("Hora", h);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Sensor miSensor = event.sensor;
        if (miSensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            acelerometroX.setText(String.valueOf(event.values[0]));
            acelerometroY.setText(String.valueOf(event.values[1]));
            acelerometroZ.setText(String.valueOf(event.values[2]));
        }
        if (miSensor.getType() == Sensor.TYPE_GYROSCOPE) {
            giroscopoX.setText(String.valueOf(event.values[0]));
            giroscopoY.setText(String.valueOf(event.values[1]));
            giroscopoZ.setText(String.valueOf(event.values[2]));
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    protected void onPause() {
        super.onPause();
        senManager.unregisterListener(this);
    }

    protected void onResume() {
        super.onResume();
        senManager.registerListener(this, senAcelerometro, SensorManager.SENSOR_DELAY_NORMAL);
        senManager.registerListener(this, senGiroscopo, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        senManager.unregisterListener(this);
    }

    public void iniciarActivityLoteria(View vista){
        Intent intentLoteria = new Intent(this, ActivityLoteria.class);
        intentLoteria.putExtra("token", token);
        startActivity(intentLoteria);
    }

    public void registrarEventoAcelerometro(View view){
        try {
            JSONObject body = new JSONObject();
            body.put("env", "PROD");
            body.put("type_events", "Actividad de Acelerometro");
            body.put("description", "La aplicacion esta sensando el acelerometro del smartphone.");
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

    public void registrarEventoGiroscopo(View view){
        try {
            JSONObject body = new JSONObject();
            body.put("env", "PROD");
            body.put("type_events", "Actividad de Giroscopo");
            body.put("description", "La aplicacion esta sensando el giroscopo del smartphone.");
            if(Conectividad.validarConexionAInternet(this)){
                eventRegisterThread = new hiloEventoRegistrarRequest(token, body);
                eventRegisterThread.start();
                Toast.makeText(this, "Registrando la actividad del giroscopo en el servidor.", Toast.LENGTH_SHORT).show();
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
            Toast.makeText(this, "Se registro el evento del sensor en el servidor", Toast.LENGTH_SHORT).show();
        }
    }

    public void iniciarMainActivity(){
        Intent intentMain = new Intent(this,MainActivity.class);
        startActivity(intentMain);
    }
}