package com.example.ea2_soa;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;

public class ReceiverConexion extends BroadcastReceiver {

    AlertDialog.Builder popUpError;

    @Override
    public void onReceive(Context context, Intent intent) {
        if(ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())){
            ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            boolean estaConectado = activeNetwork != null &&
                    activeNetwork.isConnectedOrConnecting();


            if(!estaConectado){
                popUpError = new AlertDialog.Builder(context);
                popUpError.setTitle("Atencion");
                popUpError.setMessage("No se ha detectado ninguna conexion a Internet.");

                popUpError.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                popUpError.create().show();
            }
        }
    }
}