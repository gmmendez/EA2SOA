package com.example.ea2_soa;

import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class Request {

    public static int codigoRespuesta;
    private static String respuesta;
    public static URL endpoint;
    public static HttpURLConnection request;
    public static DataOutputStream data;



    public static String requestLogin(String ep, JSONObject jsonBody) throws IOException {
        endpoint = new URL(ep);
        request = (HttpURLConnection)endpoint.openConnection();

        request.setDoOutput(true);
        request.setRequestMethod("POST");
        request.setRequestProperty("Content-Type","application/json");

        data = new DataOutputStream(request.getOutputStream());
        data.writeBytes(jsonBody.toString());
        request.connect();

        codigoRespuesta = request.getResponseCode();

        BufferedReader in;
        if(codigoRespuesta == HttpURLConnection.HTTP_OK || codigoRespuesta == HttpURLConnection.HTTP_CREATED){
            in = new BufferedReader(new InputStreamReader(request.getInputStream()));
        }
        else{
            in = new BufferedReader(new InputStreamReader(request.getErrorStream()));
        }

        respuesta = RespuestaAString(in).toString();

        request.disconnect();

        Log.i("Autenticacion-Response", respuesta);
        return respuesta;
    }

    public static StringBuffer RespuestaAString(BufferedReader input) throws IOException {
        String inputLine;
        StringBuffer stringResponse = new StringBuffer();

        while ((inputLine = input.readLine()) != null) {
            stringResponse.append(inputLine);
        }
        input.close();

        return stringResponse;
    }

    public static String requestEventos(String ep, JSONObject jsonBody, String token) throws IOException {
        endpoint = new URL(ep);
        request = (HttpURLConnection)endpoint.openConnection();

        request.setDoInput(true);
        request.setDoOutput(true);
        request.setRequestMethod("POST");
        request.setRequestProperty("Content-Type","application/json");
        request.setRequestProperty("Authorization", "Bearer "+token);

        data = new DataOutputStream(request.getOutputStream());
        data.writeBytes(jsonBody.toString());

        request.connect();

        codigoRespuesta = request.getResponseCode();
        Log.i("ResponseCode",String.valueOf(codigoRespuesta));
        BufferedReader in;
        if(codigoRespuesta == HttpURLConnection.HTTP_OK || codigoRespuesta == HttpURLConnection.HTTP_CREATED){
            in = new BufferedReader(new InputStreamReader(request.getInputStream()));
        }
        else{
            in = new BufferedReader(new InputStreamReader(request.getErrorStream()));
        }

        respuesta = RespuestaAString(in).toString();

        request.disconnect();
        Log.i("Registracion-Response", respuesta);
        return respuesta;
    }

    public static String requestRegistrar(String ep, JSONObject jsonBody) throws IOException {
        endpoint = new URL(ep);
        request = (HttpURLConnection)endpoint.openConnection();

        request.setDoOutput(true);
        request.setRequestMethod("POST");
        request.setRequestProperty("Content-Type","application/json");

        data = new DataOutputStream(request.getOutputStream());
        data.writeBytes(jsonBody.toString());
        request.connect();

        codigoRespuesta = request.getResponseCode();

        BufferedReader in;
        if(codigoRespuesta == HttpURLConnection.HTTP_OK || codigoRespuesta == HttpURLConnection.HTTP_CREATED){
            in = new BufferedReader(new InputStreamReader(request.getInputStream()));
        }
        else{
            in = new BufferedReader(new InputStreamReader(request.getErrorStream()));
        }

        respuesta = RespuestaAString(in).toString();

        request.disconnect();

        Log.i("Autenticacion-Response", respuesta);
        return respuesta;
    }

}
