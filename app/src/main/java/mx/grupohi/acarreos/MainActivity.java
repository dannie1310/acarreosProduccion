package mx.grupohi.acarreos;

import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Color;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.MifareUltralight;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.StringTokenizer;

import mx.grupohi.acarreos.Destino.DestinoTiro;
import mx.grupohi.acarreos.TiposTag.TagNFC;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {


    private NFCTag nfcTag;
    private NFCUltralight nfcUltra;
    private NfcAdapter nfc_adapter;
    private PendingIntent pendingIntent;
    private PendingIntent resp;
    private IntentFilter writeTagFilters[];
    private Snackbar snackbar;
    private String UID;
    private Integer idOrigen;
    private Intent setOrigenActivity;
    private Intent setDestinoActivity;
    private Intent listaViajes;
    private Integer CamionID;


    //Referencias UI
    private LinearLayout infoLayout;
    private LinearLayout origenLayout;
    private LinearLayout estad;
    private Button actionButton;
    private ImageView nfcImage;
    private TextView infoTag;
    private ProgressDialog progressDialogSync;

    private TextView tCamion;
    private TextView tCapacidad;
    private TextView tCapacidad2;
    private TextView tMarca;
    private TextView tModelo;
    private TextView tAlto;
    private TextView tAncho;
    private TextView tLargo;

    private TextView tOrigen;
    private TextView tMaterial;
    private TextView tFecha;
    private TextView tHora;
    String validacion;
    private String mensaje = "";

    Intent descarga;
    Camion camion;

    private Boolean writeMode;

    Usuario usuario;
    Viaje viaje;
    Coordenada coordenada;
    Configuraciones c;
    Certificados certificados;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setOrigenActivity = new Intent(this, SetOrigenActivity.class);
        setDestinoActivity = new Intent(this, SetDestinoActivity.class);
        listaViajes =new Intent(this, ListaViajesActivity.class);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle(getString(R.string.title_activity_main));
        certificados = new Certificados(this);
        usuario = new Usuario(this);
        camion = new Camion(getApplicationContext());
        usuario = usuario.getUsuario();
        viaje = new Viaje(this);
        coordenada = new Coordenada(this);
        writeMode = true;
        infoLayout = (LinearLayout) findViewById(R.id.LayoutInfo);
        origenLayout = (LinearLayout) findViewById(R.id.LayoutOrigen);
        actionButton = (Button) findViewById(R.id.ButtonAction);
        infoTag = (TextView) findViewById(R.id.textInfoTag);
        nfcImage = (ImageView) findViewById(R.id.nfc_background);

        tCamion = (TextView) findViewById(R.id.txtCamion);
        tCapacidad = (TextView) findViewById(R.id.txtCapacidad);
        tCapacidad2 = (TextView) findViewById(R.id.textViewCapacidad);
        tMarca = (TextView) findViewById(R.id.textViewMarca);
        tModelo = (TextView) findViewById(R.id.textViewModelo);
        tAlto = (TextView) findViewById(R.id.textViewAlto);
        tAncho = (TextView) findViewById(R.id.textViewAncho);
        tLargo = (TextView) findViewById(R.id.textViewLargo);

        tOrigen = (TextView) findViewById(R.id.textViewOrigen);
        tMaterial = (TextView) findViewById(R.id.textViewMaterial);
        tFecha = (TextView) findViewById(R.id.textViewFecha);
        tHora = (TextView) findViewById(R.id.textViewHora);
        estad= (LinearLayout) findViewById(R.id.estatus);

        infoLayout.setVisibility(View.GONE);
        origenLayout.setVisibility(View.GONE);
        actionButton.setVisibility(View.GONE);

        validacion = getIntent().getStringExtra("validacion");
        c = new  Configuraciones(getApplicationContext());
        c = c.find();

        pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        writeTagFilters = new IntentFilter[]{tagDetected};

        nfc_adapter = NfcAdapter.getDefaultAdapter(this);
        if (nfc_adapter == null) {
            Toast.makeText(this, getString(R.string.error_no_nfc), Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        String imagen = usuario.getImagen();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        if(drawer != null)
            drawer.post(new Runnable() {
                @Override
                public void run() {
                    for (int i = 0; i < drawer.getChildCount(); i++) {
                        View child = drawer.getChildAt(i);
                        TextView tvp = (TextView) child.findViewById(R.id.textViewProyecto);
                        TextView tvu = (TextView) child.findViewById(R.id.textViewUser);
                        TextView tpe = (TextView) child.findViewById(R.id.textViewPerfil);
                        TextView tvv = (TextView) child.findViewById(R.id.textViewVersion);
                        TextView tim = (TextView) child.findViewById(R.id.textViewImpresora);
                        Integer impresora = CelularImpresora.getId(getApplicationContext());
                        if (tim != null){
                            if(impresora == 0){
                                tim.setTextColor(Color.RED);
                                tim.setText("Sin Impresora Asignada");
                            }else{
                                tim.setText("Impresora "+impresora);
                            }
                        }
                        if (tvp != null) {
                            tvp.setText(usuario.descripcionBaseDatos);
                        }
                        if (tvu != null) {
                            tvu.setText(usuario.nombre);
                        }
                        if (tpe != null){
                            if(usuario.origen_name == "0"){
                                tpe.setText("PERFIL: "+usuario.getNombreEsquema()+" - "+usuario.tiro_name);
                            }else if(usuario.tiro_name == "0"){
                                tpe.setText("PERFIL: "+usuario.getNombreEsquema()+" - "+usuario.origen_name);
                            }
                        }
                        if (tvv != null) {
                            tvv.setText(getString(R.string.app_name)+"     "+"Versión " + String.valueOf(BuildConfig.VERSION_NAME));
                        }
                    }
                }
            });
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        WriteModeOn();
        nfc_adapter = NfcAdapter.getDefaultAdapter(this);
        checkNfcEnabled();
    }

    @Override
    protected void onPause() {
        super.onPause();
        WriteModeOff();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            Intent intent = getIntent();
            finish();
            startActivity(intent);
        }
    }

    class TirosTarea extends AsyncTask<Void, Void, Boolean> {
        TagNFC tag_nfc = new TagNFC();
        Context context;
        Intent intent;
        Camion camion;
        Origen origen;
        Material material;

        public TirosTarea(Context context, Intent intent) {
            this.context = context;
            this.intent = intent;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            Tag myTag;
            //// se lee el tag y se inicializa la clase con los datos
            if (writeMode) {
                if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
                    myTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
                    String[] techs = myTag.getTechList();
                    for (String t : techs) {
                        if (MifareClassic.class.getName().equals(t)) {
                            nfcTag = new NFCTag(myTag, context);
                            tag_nfc.setUID(nfcTag.byteArrayToHexString(myTag.getId()));
                            tag_nfc.setTipo(1);
                            String camion_proyecto = null;
                            try {
                                camion_proyecto = nfcTag.readSector(null, 0, 1);
                            } catch (IOException e) {
                                e.printStackTrace();
                                mensaje = "¡Error! No se puede establecer la comunicación con el TAG, por favor mantenga el TAG cerca del dispositivo";
                                return false;
                            }
                            if (camion_proyecto.length() == 8) {
                                tag_nfc.setIdcamion(Util.getIdCamion(camion_proyecto, 4));
                                tag_nfc.setIdproyecto(Util.getIdProyecto(camion_proyecto, 4));
                            } else {
                                tag_nfc.setIdcamion(Util.getIdCamion(camion_proyecto, 5));
                                tag_nfc.setIdproyecto(Util.getIdProyecto(camion_proyecto, 5));
                            }
                            String material_origen = null;
                            try {
                                material_origen = nfcTag.readSector(null, 1, 4);
                            } catch (IOException e) {
                                e.printStackTrace();
                                mensaje = "¡Error! No se puede establecer la comunicación con el TAG, por favor mantenga el TAG cerca del dispositivo";
                                return false;
                            }
                            tag_nfc.setIdmaterial(String.valueOf(Util.getIdMaterial(material_origen)));
                            tag_nfc.setIdorigen(String.valueOf(Util.getIdOrigen(material_origen)));
                            try {
                                tag_nfc.setFecha(nfcTag.readSector(null, 1, 5));
                            } catch (IOException e) {
                                e.printStackTrace();
                                mensaje = "¡Error! No se puede establecer la comunicación con el TAG, por favor mantenga el TAG cerca del dispositivo";
                                return false;
                            }
                            try {
                                tag_nfc.setUsuario(nfcTag.readSector(null, 1, 6));
                            } catch (IOException e) {
                                e.printStackTrace();
                                mensaje = "¡Error! No se puede establecer la comunicación con el TAG, por favor mantenga el TAG cerca del dispositivo";
                                return false;
                            }
                            try {
                                tag_nfc.setTipo_viaje(nfcTag.readSector(null, 2, 9));
                            } catch (IOException e) {
                                e.printStackTrace();
                                mensaje = "¡Error! No se puede establecer la comunicación con el TAG, por favor mantenga el TAG cerca del dispositivo";
                                return false;
                            }
                            try {
                                tag_nfc.setVolumen(nfcTag.readSector(null, 3, 12));
                            } catch (IOException e) {
                                e.printStackTrace();
                                mensaje = "¡Error! No se puede establecer la comunicación con el TAG, por favor mantenga el TAG cerca del dispositivo";
                                return false;
                            }
                            try {
                                tag_nfc.setTipo_perfil(nfcTag.readSector(null, 3, 14));
                            } catch (IOException e) {
                                e.printStackTrace();
                                mensaje = "¡Error! No se puede establecer la comunicación con el TAG, por favor mantenga el TAG cerca del dispositivo";
                                return false;
                            }
                            try {
                                tag_nfc.setVolumen_entrada(nfcTag.readSector(null, 4, 16));
                            } catch (IOException e) {
                                e.printStackTrace();
                                mensaje = "¡Error! No se puede establecer la comunicación con el TAG, por favor mantenga el TAG cerca del dispositivo";
                                return false;
                            }

                        } else if (MifareUltralight.class.getName().equals(t)) {
                            nfcUltra = new NFCUltralight(myTag, context);
                            tag_nfc.setUID(nfcUltra.byteArrayToHexString(myTag.getId()));
                            tag_nfc.setTipo(2);
                            String camion_proyecto = null;
                            try {
                                camion_proyecto = nfcUltra.readDeductiva(null, 4) + nfcUltra.readDeductiva(null, 5) + nfcUltra.readDeductiva(null, 6);
                            } catch (IOException e) {
                                e.printStackTrace();
                                mensaje = "¡Error! No se puede establecer la comunicación con el TAG, por favor mantenga el TAG cerca del dispositivo";
                                return false;
                            }
                            if (camion_proyecto.length() == 8) {
                                tag_nfc.setIdcamion(Util.getIdCamion(camion_proyecto, 4));
                                tag_nfc.setIdproyecto(Util.getIdProyecto(camion_proyecto, 4));
                            } else {
                                tag_nfc.setIdcamion(Util.getIdCamion(camion_proyecto, 8));
                                tag_nfc.setIdproyecto(Util.getIdProyecto(camion_proyecto, 8));
                            }
                            try {
                                tag_nfc.setIdmaterial(nfcUltra.readDeductiva(null, 7));
                            } catch (IOException e) {
                                e.printStackTrace();
                                mensaje = "¡Error! No se puede establecer la comunicación con el TAG, por favor mantenga el TAG cerca del dispositivo";
                                return false;
                            }
                            try {
                                tag_nfc.setIdorigen(nfcUltra.readDeductiva(null, 8));
                            } catch (IOException e) {
                                e.printStackTrace();
                                mensaje = "¡Error! No se puede establecer la comunicación con el TAG, por favor mantenga el TAG cerca del dispositivo";
                                return false;
                            }
                            try {
                                tag_nfc.setFecha(nfcUltra.readDeductiva(null, 9) + nfcUltra.readDeductiva(null, 10) + nfcUltra.readDeductiva(null, 11) + nfcUltra.readDeductiva(null, 12));
                            } catch (IOException e) {
                                e.printStackTrace();
                                mensaje = "¡Error! No se puede establecer la comunicación con el TAG, por favor mantenga el TAG cerca del dispositivo";
                                return false;
                            }
                            try {
                                tag_nfc.setUsuario(nfcUltra.readDeductiva(null, 13));
                            } catch (IOException e) {
                                e.printStackTrace();
                                mensaje = "¡Error! No se puede establecer la comunicación con el TAG, por favor mantenga el TAG cerca del dispositivo";
                                return false;
                            }
                            try {
                                tag_nfc.setTipo_viaje(nfcUltra.readDeductiva(null, 15));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            try {
                                tag_nfc.setVolumen(nfcUltra.readDeductiva(null, 16));
                            } catch (IOException e) {
                                e.printStackTrace();
                                mensaje = "¡Error! No se puede establecer la comunicación con el TAG, por favor mantenga el TAG cerca del dispositivo";
                                return false;
                            }
                            try {
                                tag_nfc.setTipo_perfil(nfcUltra.readDeductiva(null, 18));
                            } catch (IOException e) {
                                e.printStackTrace();
                                mensaje = "¡Error! No se puede establecer la comunicación con el TAG, por favor mantenga el TAG cerca del dispositivo";
                                return false;
                            }
                            try {
                                tag_nfc.setVolumen_entrada(nfcUltra.readDeductiva(null, 19));
                            } catch (IOException e) {
                                e.printStackTrace();
                                mensaje = "¡Error! No se puede establecer la comunicación con el TAG, por favor mantenga el TAG cerca del dispositivo";
                                return false;
                            }
                        }
                    }
                }
            }
            //// inicia seccion de validaciones
            DestinoTiro destinoTiro = new DestinoTiro(context, tag_nfc);
            mensaje = destinoTiro.validarDatosTag();

            if(mensaje == "destino") {
                camion = new Camion(context);
                origen = new Origen(context);
                material = new Material(context);
                camion = camion.find(tag_nfc.getIdcamion());
                origen = origen.find(Integer.valueOf(tag_nfc.getIdorigen()));
                material = material.find(Integer.valueOf(tag_nfc.getIdmaterial()));
                return true;
            }
            if(mensaje == "libreAbordo") {
                return true;
            }

            return false;
        }

        @Override
        protected void onPostExecute(Boolean registro) {
            super.onPostExecute(registro);
            if (registro) {
                WriteModeOff();
                if(mensaje == "destino"){
                    setCamionInfo(camion, tag_nfc.getTipo_viaje());
                    setOrigenInfo(origen, material, tag_nfc.getFecha());
                    actionButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            setDestinoActivity.putExtra("UID", tag_nfc.getUID());
                            setDestinoActivity.putExtra("idOrigen", tag_nfc.getIdorigen());
                            setDestinoActivity.putExtra("camion", String.valueOf(tag_nfc.getIdcamion()));
                            setDestinoActivity.putExtra("tipo_suministro", tag_nfc.getTipo_viaje());
                            startActivity(setDestinoActivity);
                        }
                    });
                }
                if(mensaje == "libreAbordo") {
                    Intent r = new Intent(getApplicationContext(), TiroUnicoActivity.class);
                    r.putExtra("UID", tag_nfc.getUID());
                    r.putExtra("camion", tag_nfc.getIdcamion()); // enviar el POJO TagNFC
                    startActivity(r);
                }
            } else {
                WriteModeOff();
                Toast.makeText(context, mensaje, Toast.LENGTH_SHORT).show();
            }
        }
    }


    @Override
    protected void onNewIntent(Intent intent) {

        new TirosTarea(this,  intent).execute();

      /*  int tipo=0;
        Integer tagCamion =0;
        Integer tagProyecto =0;
        Integer tagOrigen =0;
        Integer tagMaterial = 0;
        String tipo_sum="";
        String fechaString = "";
        String deduct = "";
        String idmot = "";
        String tipoPerfil = "0";
        if (nfc_adapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
            clearCamionInfo();
            clearOrigenInfo();
            if (snackbar != null && snackbar.isShown()) snackbar.dismiss();
            final Tag myTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            TagModel tagModel = new TagModel(getApplicationContext());
            Origen origen = new Origen(getApplicationContext());
            Material material = new Material(getApplicationContext());
            String[] techs = myTag.getTechList();

            for (String t : techs) {
                if (MifareClassic.class.getName().equals(t)) {
                    nfc = new NFCTag(myTag, this);
                    try {
                        UID =  nfc.byteArrayToHexString(myTag.getId());
                        String tagString = nfc.readSector(myTag, 0, 1);
                        String origenString = nfc.readSector(myTag, 1, 4);
                        fechaString = nfc.readSector(myTag, 1, 5);
                        tagString = tagString.replace(" ","");
                        tipoPerfil = nfc.readSector(myTag, 3, 14);
                        if(tagString.length() <= 8){
                            tagCamion = Util.getIdCamion(tagString,4);
                            tagProyecto = Util.getIdProyecto(tagString,4);
                        }else {
                            tagCamion = Util.getIdCamion(tagString, 5);
                            tagProyecto = Util.getIdProyecto(tagString, 5);
                        }
                        tagModel = tagModel.find(UID, tagCamion, tagProyecto);
                        tagOrigen = Util.getIdOrigen(origenString);
                        tagMaterial = Util.getIdMaterial(origenString);
                        origen = origen.find(tagOrigen);
                        material = material.find(tagMaterial);
                        tipo_sum = nfc.readSector(myTag,2,9);
                        if(tipo_sum != null){
                            tipo_sum = tipo_sum.replace(" ", "");
                        }else{
                            tipo_sum = "0";
                        }
                    }catch (Exception e){
                        tagModel=null;
                    }
                } else if (MifareUltralight.class.getName().equals(t)) {
                    nfcUltra = new NFCUltralight(myTag, this);
                    UID = nfcUltra.byteArrayToHexString(myTag.getId());
                    try {
                        String tagS = nfcUltra.readPage(myTag, 4) + nfcUltra.readPage(myTag, 5);
                        String aux = nfcUltra.readPage(myTag, 6);
                        tipoPerfil = nfcUltra.readPage(myTag, 18);
                        if(tipoPerfil != null){
                            tipoPerfil = tipoPerfil.substring(0,1);
                        }else{
                            tipoPerfil = "";
                        }
                        if(aux != null){
                            tagS = tagS+aux;
                        }
                        if(tagS.length() <= 8){
                            tagCamion = Util.getIdCamion(tagS,4);
                            tagProyecto = Util.getIdProyecto(tagS,4);
                        }else {
                            tagCamion = Util.getIdCamion(tagS, 8);
                            tagProyecto = Util.getIdProyecto(tagS, 8);
                        }

                        String origen1 = nfcUltra.readPage(myTag, 8);
                        String material1 = nfcUltra.readPage(myTag, 7);
                        tagModel = tagModel.find(UID, tagCamion, tagProyecto);
                        tipo_sum = nfcUltra.readPage(myTag, 15);
                        if (tipo_sum != null) {
                            tipo_sum = tipo_sum.substring(0, 1);
                        }else{
                            tipo_sum ="0";
                        }
                        if (origen1 != null && material1 != null) {
                            tagOrigen = Integer.valueOf(origen1);
                            tagMaterial = Integer.valueOf(material1);
                            origen = origen.find(tagOrigen);
                            material = material.find(tagMaterial);
                            fechaString = nfcUltra.readPage(myTag, 9) + nfcUltra.readPage(myTag, 10) + nfcUltra.readPage(myTag, 11) + nfcUltra.readPage(myTag, 12).substring(0, 2);
                        } else {
                            origen = null;
                            material = null;
                        }
                    }catch (Exception e){
                        tagModel=null;
                    }

                }
            }


            if (tagModel != null) {
                try {
                    //valida tag status

                    camion = camion.find(tagModel.idCamion);
                    CamionID = camion.idCamion;
                    if(tagModel.estatus == 1) {
                        if (usuario.tipo_permiso == 3) {
                           // tipoPerfil = tipoPerfil.replace(" ","");
                            if(tipoPerfil == null){
                                tipoPerfil="";
                            }else{
                                tipoPerfil= tipoPerfil.replace(" ", "");
                            }
                            if(tipoPerfil == "") {
                                Intent r = new Intent(getApplicationContext(), TiroUnicoActivity.class);
                                r.putExtra("UID", UID);
                                r.putExtra("camion", String.valueOf(CamionID));
                                startActivity(r);
                            }else{
                                Toast.makeText(MainActivity.this, "El TAG cuenta con datos de Origen Mina, Favor de pasar a un filtro de salida", Toast.LENGTH_SHORT).show();
                            }
                        } else if (usuario.tipo_permiso == 2 || usuario.tipo_permiso == 5) {
                            if (origen != null && material != null) {
                                if(tipo_sum.equals("1")){
                                    estad.setVisibility(View.VISIBLE);
                                }else{
                                    estad.setVisibility(View.GONE);
                                }
                                setCamionInfo(camion);
                                setTitle("INFORMACIÓN DEL TAG");
                                if (validacion == null && c.validacion == 1) {
                                    Intent validar = new Intent(getApplicationContext(), ValidacionActivity.class);
                                    validar.putExtra("economico", camion.economico);
                                    validar.putExtra("placas", camion.placas);
                                    validar.putExtra("placasCaja", camion.placasCaja);
                                    validar.putExtra("capacidad", String.valueOf(camion.capacidad));

                                    startActivityForResult(validar, 0);
                                }
                                setOrigenInfo(origen, material, fechaString);
                                idOrigen = origen.idOrigen;
                                Integer tipoS = 0;
                                if(!tipo_sum.equals("")) {
                                   tipoS = Integer.valueOf(tipo_sum);
                                }
                                final Integer finalTipoS = tipoS;
                                actionButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        setDestinoActivity.putExtra("UID", UID);
                                        setDestinoActivity.putExtra("idOrigen", idOrigen);
                                        setDestinoActivity.putExtra("camion", String.valueOf(CamionID));
                                        setDestinoActivity.putExtra("tipo_suministro", finalTipoS);
                                        startActivity(setDestinoActivity);
                                    }
                                });
                            }else {
                                snackbar = Snackbar.make(findViewById(R.id.content_main), R.string.error_sin_origen, Snackbar.LENGTH_LONG);
                                View snackBarView = snackbar.getView();
                                snackBarView.setBackgroundColor(Color.RED);
                                snackbar.show();
                            }
                        }
                    }else{
                        Toast.makeText(getApplicationContext(),"El camión "+camion.economico+" se encuentra inactivo. Por favor contacta al encargado.", Toast.LENGTH_LONG).show();
                    }
                }catch (Exception e){
                    snackbar = Snackbar.make(findViewById(R.id.content_main),R.string.error_tag, Snackbar.LENGTH_LONG);
                    View snackBarView = snackbar.getView();
                    snackBarView.setBackgroundColor(Color.RED);
                                                                                                                                 snackbar.show();
                }
            }else {
                snackbar = Snackbar.make(findViewById(R.id.content_main), "El TAG que intentas utilizar no es valido para el control de viajes", Snackbar.LENGTH_LONG);
                View snackBarView = snackbar.getView();
                snackBarView.setBackgroundColor(Color.RED);
                snackbar.show();
            }
        } else if (nfc_adapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
            Toast.makeText(getApplicationContext(), "El TAG que intentas utilizar no es compatible", Toast.LENGTH_LONG).show();
        }
        */
    }

    private void WriteModeOn() {
        nfc_adapter.enableForegroundDispatch(this, pendingIntent, writeTagFilters, null);
    }

    private void WriteModeOff() {
        nfc_adapter.disableForegroundDispatch(this);
        infoLayout.setEnabled(true);
    }

    private void checkNfcEnabled() {
        Boolean nfcEnabled = nfc_adapter.isEnabled();
        if (!nfcEnabled) {
            new android.app.AlertDialog.Builder(MainActivity.this)
                    .setTitle(getString(R.string.text_warning_nfc_is_off))
                    .setMessage(getString(R.string.text_turn_on_nfc))
                    .setCancelable(true)
                    .setPositiveButton(
                            getString(R.string.text_update_settings),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int id) {
                                    startActivity(new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS));
                                }
                            })
                    .create()
                    .show();
        }
    }

    private void setCamionInfo(Camion camion, String tipoViaje) {

        if(tipoViaje == "1"){
            estad.setVisibility(View.VISIBLE);
        }else{
            estad.setVisibility(View.GONE);
        }
        setTitle("INFORMACIÓN DEL TAG");
        tCamion.setText(camion.economico + " [" + camion.placas + "]");
        tCapacidad.setText("CAPACIDAD: " + camion.capacidad + " m3");
        tCapacidad2.setText(String.valueOf(camion.capacidad));
        tMarca.setText(camion.marca);
        tModelo.setText(camion.modelo);
        tAlto.setText(String.valueOf(camion.alto));
        tAncho.setText(String.valueOf(camion.ancho));
        tLargo.setText(String.valueOf(camion.largo));

        infoTag.setVisibility(View.GONE);
        nfcImage.setVisibility(View.GONE);
        infoLayout.setVisibility(View.VISIBLE);
        actionButton.setVisibility(View.VISIBLE);


    }

    private void clearCamionInfo() {

        infoTag.setVisibility(View.VISIBLE);
        nfcImage.setVisibility(View.VISIBLE);
        infoLayout.setVisibility(View.GONE);
        actionButton.setVisibility(View.GONE);

        tCamion.setText("");
        tCapacidad.setText("");
        tCapacidad2.setText("");
        tMarca.setText("");
        tModelo.setText("");
        tAlto.setText("");
        tAncho.setText("");
        tLargo.setText("");
    }

    private void setOrigenInfo(Origen origen, Material material, String fechaHora) {

        origenLayout.setVisibility(View.VISIBLE);
        tOrigen.setText(origen.descripcion);
        tMaterial.setText(material.descripcion);

        tFecha.setText(Util.getFecha(fechaHora));
        tHora.setText(Util.getTime(fechaHora));

        actionButton.setText("ESCRIBIR DESTINO");
    }

    private void clearOrigenInfo() {
        origenLayout.setVisibility(View.GONE);
        tOrigen.setText("");
        tMaterial.setText("");
        tFecha.setText("");
        tHora.setText("");
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            Intent mainActivity;
            Integer tipo = usuario.getTipo_permiso();
            if(tipo == 0){
                mainActivity = new Intent(getApplicationContext(), SetOrigenActivity.class);
                mainActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(mainActivity);
            }else if(tipo == 1){
                Intent intent = getIntent();
                finish();
                startActivity(intent);
            }

        } else if (id == R.id.nav_sync) {
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("¡ADVERTENCIA!")
                    .setMessage("Se borrarán los registros de viajes almacenados en este dispositivo. \n ¿Deséas continuar con la sincronización?")
                    .setNegativeButton("NO", null)
                    .setPositiveButton("SI", new DialogInterface.OnClickListener() {
                        @Override public void onClick(DialogInterface dialog, int which) {
                            if (Util.isNetworkStatusAvialable(getApplicationContext())) {
                                if(!Viaje.isSync(getApplicationContext()) || !InicioViaje.isSync(getApplicationContext())){
                                    progressDialogSync = ProgressDialog.show(MainActivity.this, "Sincronizando datos", "Por favor espere...", true);
                                    new Sync(getApplicationContext(), progressDialogSync).execute((Void) null);
                                } else {
                                    Toast.makeText(getApplicationContext(), "No es necesaria la sincronización en este momento", Toast.LENGTH_LONG).show();
                                }
                            } else {
                                Toast.makeText(getApplicationContext(), R.string.error_internet, Toast.LENGTH_LONG).show();
                            }
                        }
                    })
                    .create()
                    .show();


        } else if (id == R.id.nav_list) {
            startActivity(listaViajes);
        }else if (id == R.id.nav_desc) {
            descarga = new Intent(this, DescargaActivity.class);
            startActivity(descarga);
        } else if (id == R.id.nav_logout) {
            if(!Viaje.isSync(getApplicationContext()) || !InicioViaje.isSync(getApplicationContext())){
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("¡ADVERTENCIA!")
                        .setMessage("Hay viajes aún sin sincronizar, se borrarán los registros de viajes almacenados en este dispositivo,  \n ¿Deséas sincronizar?")
                        .setNegativeButton("NO", null)
                        .setPositiveButton("SI", new DialogInterface.OnClickListener() {
                            @Override public void onClick(DialogInterface dialog, int which) {
                                if (Util.isNetworkStatusAvialable(getApplicationContext())) {
                                    progressDialogSync = ProgressDialog.show(MainActivity.this, "Sincronizando datos", "Por favor espere...", true);
                                    new Sync(getApplicationContext(), progressDialogSync).execute((Void) null);

                                    Intent login_activity = new Intent(getApplicationContext(), LoginActivity.class);
                                    usuario.destroy();
                                    startActivity(login_activity);

                                } else {
                                    Toast.makeText(getApplicationContext(), R.string.error_internet, Toast.LENGTH_LONG).show();
                                }
                            }
                        })
                        .create()
                        .show();

            }
            else {
                Intent login_activity = new Intent(getApplicationContext(), LoginActivity.class);
                usuario.destroy();
                startActivity(login_activity);
            }

        }else if(id == R.id.nav_cambio){
            Intent cambio = new Intent(this, CambioClaveActivity.class);
            startActivity(cambio);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
