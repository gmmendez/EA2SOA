package com.example.ea2_soa;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;

import androidx.appcompat.app.AlertDialog;

public class ReceiverConexion extends BroadcastReceiver {

    AlertDialog.Builder popUpError;

    @Override
    public void onReceive(Context context, Intent intent) {
        if(ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())){
            boolean sinConexion = intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);

            if(sinConexion){
                popUpError = new AlertDialog.Builder(context);
                popUpError.setTitle("Atencion");
                popUpError.setMessage("No se ha detectado ninguna conexion a Internet.");

                popUpError.setPositiveButton("Entendido", new DialogInterface.OnClickListener() {
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