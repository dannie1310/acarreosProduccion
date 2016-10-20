package mx.grupohi.acarreos;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.telephony.TelephonyManager;
import android.widget.Toast;

import org.json.JSONObject;

import java.net.URL;

/**
 * Creado por JFEsquivel on 19/10/2016.
 */

public class Sync extends AsyncTask<Void, Void, Boolean> {

    private Context context;
    private ProgressDialog progressDialog;
    private Usuario usuario;

    // GPSTracker class
    GPSTracker gps;
    double latitude;
    double longitude;
    public String IMEI;

    private JSONObject JSON;

    Sync(Context context, ProgressDialog progressDialog) {
        this.context = context;
        this.progressDialog = progressDialog;
        usuario = new Usuario(context);
        usuario = usuario.getUsuario();
        gps = new GPSTracker(context);

    }

    @Override
    protected Boolean doInBackground(Void... params) {

        if(!gps.canGetLocation()) {
            gps.showSettingsAlert();
            return false;
        } else {
            latitude = gps.getLatitude();
            longitude = gps.getLongitude();
            TelephonyManager phneMgr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            IMEI = phneMgr.getDeviceId();

            ContentValues values = new ContentValues();

            values.put("IMEI", IMEI);
            values.put("idevento", 4);
            values.put("latitud", latitude);
            values.put("longitud", longitude);
            values.put("fecha_hora", Util.timeStamp());
            values.put("code", "");

            Coordenada coordenada = new Coordenada(context);
            coordenada.create(values);

            values.clear();

            values.put("metodo", "captura");
            values.put("usr", usuario.usr);
            values.put("pass", usuario.pass);
            values.put("bd", usuario.baseDatos);
            values.put("idusuario", usuario.getId());
            if (!Viaje.getJSON().equals(null)) {
                values.put("carddata", String.valueOf(Viaje.getJSON()));
            }
            if (!Coordenada.getJSON().equals(null)) {
                values.put("coordenadas", String.valueOf(Coordenada.getJSON()));
            }

            try {
                URL url = new URL("http://sca.grupohi.mx/android20160923.php");
                JSON = HttpConnection.POST(url, values);
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        super.onPostExecute(aBoolean);
        progressDialog.dismiss();
        if(aBoolean) {
            try {
                if (JSON.has("error")) {
                    Toast.makeText(context, (String) JSON.get("error"), Toast.LENGTH_SHORT).show();
                } else if(JSON.has("msj")) {
                    Viaje.sync();
                    new android.app.AlertDialog.Builder(context)
                            .setTitle("¡Hecho!")
                            .setMessage((String) JSON.get("msj"))
                            .setCancelable(false)
                            .setPositiveButton("Aceptar", null)
                            .create()
                            .show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}