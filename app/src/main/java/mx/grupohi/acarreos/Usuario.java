package mx.grupohi.acarreos;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Base64DataException;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Creado por JFEsquivel on 05/10/2016.
 */

public class Usuario {

    private Integer idUsuario;
    Integer idProyecto;
    String usr;
    String pass;
    String nombre;
    String baseDatos;
    String descripcionBaseDatos;
    String empresa;
    Integer logo;
    String imagen;
    public Integer tipo_permiso;
    Integer idorigen;
    String origen_name;
    String tiro_name;
    Integer  idtiro;

    private Context context;

    private SQLiteDatabase db;
    private DBScaSqlite db_sca;
    Tiro tiro;
    Origen origen;

    public Usuario(Context context) {
        this.context = context;
        db_sca = new DBScaSqlite(context, "sca", null, 1);
        origen = new Origen(context);
        tiro = new Tiro(context);
    }

    boolean create(ContentValues data) {
        db = db_sca.getWritableDatabase();
        Boolean result = db.insert("user", null, data) > -1;
        if (result) {
            this.idUsuario = Integer.valueOf(data.getAsString("idusuario"));
            this.idProyecto = Integer.valueOf(data.getAsString("idproyecto"));
            this.nombre = data.getAsString("nombre");
            this.baseDatos = data.getAsString("base_datos");
            this.descripcionBaseDatos = data.getAsString("descripcion_database");
            this.empresa = data.getAsString("empresa");
            this.logo = data.getAsInteger("logo");
            this.imagen = data.getAsString("imagen");
            this.tipo_permiso = data.getAsInteger("tipo_permiso");
        }
        db.close();
        return result;
    }

    void destroy() {
        db = db_sca.getWritableDatabase();
        db.execSQL("DELETE FROM user");
        db.close();
    }

    boolean isAuth() {
        db = db_sca.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM user LIMIT 1", null);
        try {
            return c != null && c.moveToFirst();
        } finally {
            c.close();
            db.close();
        }
    }

    public Integer getId() {
        db = db_sca.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM user LIMIT 1", null);
        try {
            if(c != null && c.moveToFirst()) {
                this.idUsuario = c.getInt(0);
            }
            return this.idUsuario;
        } finally {
            c.close();
            db.close();
        }
    }

    public Usuario getUsuario() {
        db = db_sca.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM user LIMIT 1", null);
        try {
            if(c != null && c.moveToFirst()) {
                try {
                    Util.copyDataBase(context);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                this.idUsuario = c.getInt(c.getColumnIndex("idusuario"));
                this.idProyecto = c.getInt(c.getColumnIndex("idproyecto"));
                this.nombre = c.getString(c.getColumnIndex("nombre"));
                this.baseDatos = c.getString(c.getColumnIndex("base_datos"));
                this.descripcionBaseDatos = c.getString(c.getColumnIndex("descripcion_database"));
                this.usr = c.getString(c.getColumnIndex("user"));
                this.pass = c.getString(c.getColumnIndex("pass"));
                this.tipo_permiso = c.getInt(c.getColumnIndex("tipo_permiso"));
                this.idorigen = c.getInt(c.getColumnIndex("idorigen"));
                this.idtiro = c.getInt(c.getColumnIndex("idtiro"));

                if(idorigen.toString() == "0"){
                    tiro = tiro.find(c.getInt(c.getColumnIndex("idtiro")));
                    if(tiro !=null) {
                        this.tiro_name = tiro.descripcion;
                        this.origen_name = "0";
                    }else {
                        this.tiro_name =  "NO SE ENCONTRO";
                        this.origen_name = "0";
                    }
                }
                else if (idtiro.toString() == "0") {
                    origen = origen.find(c.getInt(c.getColumnIndex("idorigen")));
                    if(origen!=null) {
                        this.origen_name = origen.descripcion;
                        this.tiro_name = "0";
                    }else{
                        this.origen_name = "NO SE ENCONTRO";
                        this.tiro_name = "0";
                    }

                }
                return this;
            } else {
                return null;
            }
        }finally {
            c.close();
            db.close();
        }
    }

    String getNombre(){
        db = db_sca.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT nombre FROM user LIMIT 1", null);
        try {
            if(c!=null && c.moveToFirst()){
                this.nombre =  c.getString(0);
            }
            return this.nombre;
        } finally {
            c.close();
            db.close();
        }
    }

    public String getDescripcion(){
        db = db_sca.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT descripcion_database FROM user LIMIT 1", null);
        try {
            if(c!=null && c.moveToFirst()){
                return c.getString(0);
            }
            else{
                return null;
            }
        } finally {
            c.close();
            db.close();
        }
    }

    public String getEmpresa(){
        db = db_sca.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT empresa FROM user LIMIT 1", null);
        try {
            if(c!=null && c.moveToFirst()){
                return c.getString(0);
            }
            else{
                return null;
            }
        } finally {
            c.close();
            db.close();
        }
    }

    public Integer getLogo(){
        db = db_sca.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT logo FROM user LIMIT 1", null);
        try {
            if(c!=null && c.moveToFirst()){
                return c.getInt(0);
            }
            else{
                return null;
            }
        } finally {
            c.close();
            db.close();
        }
    }
    public Integer getProyecto(){
        db = db_sca.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT idproyecto FROM user LIMIT 1", null);
        try {
            if(c!=null && c.moveToFirst()){
                return c.getInt(0);
            }
            else{
                return null;
            }
        } finally {
            c.close();
            db.close();
        }
    }

    public String getImagen(){
        db = db_sca.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT imagen FROM user LIMIT 1", null);
        try {
            if(c!=null && c.moveToFirst()){
                return c.getString(0);
            }
            else{
                return null;
            }
        } finally {
            c.close();
            db.close();
        }
    }

    public static Bitmap decodeBase64(String input)
    {
        byte[] decodedBytes = Base64.decode(input, 0);
        Bitmap imagen=null;
        try {
            imagen = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);

        }catch(Exception e){
            e.printStackTrace();
        }finally {
            return imagen;
        }
    }

    public static String encodeToBase64(Bitmap image, Bitmap.CompressFormat compressFormat, int quality)
    {

        ByteArrayOutputStream byteArrayOS = new ByteArrayOutputStream();
        image.compress(compressFormat, quality, byteArrayOS);
        return Base64.encodeToString(byteArrayOS.toByteArray(), Base64.DEFAULT);
    }

    public static String encodeToBase64Imagen(Bitmap image, int quality)
    {

        Bitmap.CompressFormat compressFormat = Bitmap.CompressFormat.JPEG;
        ByteArrayOutputStream byteArrayOS = new ByteArrayOutputStream();
        image.compress(compressFormat, quality, byteArrayOS);
        String respuesta = Base64.encodeToString(byteArrayOS.toByteArray(), Base64.DEFAULT);
        respuesta = respuesta.replace("\n","");
        return respuesta;
    }


    static boolean updateLogo(String logo, String tipo, String idorigen, String idtiro, Context context) {
        boolean resp=false;
        ContentValues data = new ContentValues();

        DBScaSqlite db_sca = new DBScaSqlite(context, "sca", null, 1);
        SQLiteDatabase db = db_sca.getWritableDatabase();

            try{

                data.put("imagen", logo);
                data.put("tipo_permiso", tipo);//idperfil
                data.put("idorigen", idorigen);//idorigen
                data.put("idtiro",idtiro);//idtiro

                db.update("user", data, "", null);
                resp = true;
            } finally {
                db.close();
            }
        return resp;
    }

    static boolean updatePass(String pass, Context context) {
        boolean resp=false;
        ContentValues data = new ContentValues();

        DBScaSqlite db_sca = new DBScaSqlite(context, "sca", null, 1);
        SQLiteDatabase db = db_sca.getWritableDatabase();

        try{

            data.put("pass", pass);

            db.update("user", data, "", null);
            resp = true;
        } finally {
            db.close();
        }
        return resp;
    }

    public Integer getTipo_permiso(){
        db = db_sca.getWritableDatabase();
        Integer tipo;
        Cursor c = db.rawQuery("SELECT tipo_permiso FROM user LIMIT 1", null);
        try {
            if(c!=null && c.moveToFirst()){
                tipo = c.getInt(0);
                if(tipo == 1 || tipo == 4){
                    return 0; //Origen
                }else if(tipo == 2 || tipo == 3 || tipo == 5){
                    return 1;
                }else{
                    return null;
                }
            }
            else{
                return null;
            }
        } finally {
            c.close();
            db.close();
        }
    }

    public Integer getTipoEsquema() {
        db = db_sca.getWritableDatabase();
        Integer tipo;
        Integer resp = 0;
        Cursor c = db.rawQuery("SELECT tipo_permiso FROM user LIMIT 1", null);
        try {
            if (c != null && c.moveToFirst()) {
                tipo = c.getInt(0);
                if (tipo == 1 || tipo == 2) {
                    resp = 1; //Esquema 1:  Origen  y Tiro
                } else if(tipo == 3){
                    resp = 2; //Esquema 2: Tiro Libre Abordo
                } else if (tipo == 4 || tipo == 5){
                    resp = 3; //Esquema 3: Tiro con Control Entrada y Salida
                }
            }

        } finally {
            c.close();
            db.close();
            return resp;
        }
    }
    public String getNombreEsquema() {
        db = db_sca.getWritableDatabase();
        Integer tipo;
        String resp = null;
        Cursor c = db.rawQuery("SELECT tipo_permiso FROM user LIMIT 1", null);
        try {
            if (c != null && c.moveToFirst()) {
                tipo = c.getInt(0);
                if (tipo == 1){
                    resp = "CHECADOR ORIGEN";
                } else if(tipo == 2){
                    resp = "CHECADOR TIRO";
                } else if(tipo == 3){
                    resp = "LIBRE A BORDO";
                } else if (tipo == 4){
                    resp = "CONTROL ENTRADA";
                } else if (tipo == 5){
                    resp = "CONTROL SALIDA";
                }
            }

        } finally {
            c.close();
            db.close();
            return resp;
        }
    }
}

