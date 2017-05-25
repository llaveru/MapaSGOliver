package com.example.hpasarin.mapasgoliver;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;

import static com.example.hpasarin.mapasgoliver.R.id.map;

//OnMapReadyCallback sirve para referenciar el mapa justo cuando esté listo.

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {


    final int MARKER_UPDATE_INTERVAL = 3000; /* milliseconds */

    //handler para comunicarnos con el hilo principal donde esta LA UI
    Handler handler = new Handler();
    JSONArray arrayJSON;
    Marker marker;
    Location location;
    private GoogleMap mapa;
    Random alea;
    Thread hilo;
    LocationManager manager;
    static String datosObtenidos;
    static URL url=null;
    long tiempo = 8000; // 5 segundos
    float distancia = 10; // 10 metros
    CameraUpdate cameraUpdate;
    TareaPesada tarea;
    JSONObject objetoJSON;
    ArrayList<LatLng> arrayPuntosRuta;
    CameraUpdate actualizacionCamara;
    CameraPosition cameraPosition;



    Runnable updateMarker = new Runnable() {
        @Override
        public void run() {

           String datos= obtenerDatos();
            Log.d("prueba",datos);

if (datosObtenidos!=null){

   // Toast.makeText(getApplicationContext(),datosObtenidos,Toast.LENGTH_SHORT).show();

    try {
         arrayJSON = new JSONArray(datosObtenidos);
        arrayPuntosRuta = new ArrayList<LatLng>();

        arrayPuntosRuta.clear(); //necesario para refrescar los markers de la ruta al menos

        for (int i=0;i<arrayJSON.length();i++){
             objetoJSON = arrayJSON.getJSONObject(i);
            marker = mapa.addMarker(new MarkerOptions().position(new LatLng(objetoJSON.getDouble("lat"),objetoJSON.getDouble("lon") )));

            //añado un punto a la ruta:
            arrayPuntosRuta.add(new LatLng(objetoJSON.getDouble("lat"),objetoJSON.getDouble("lon") ));


        }

        cameraPosition = new CameraPosition.Builder()
                .target(arrayPuntosRuta.get(0))
                .zoom(12)
                .bearing(300)
                .tilt(30)
                .build();




        mapa.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        //una vez tengo todos los puntos en el arrayPuntosRuta dibujo la polilinea
        Polyline ruta = mapa.addPolyline(
                new PolylineOptions()
                .addAll(arrayPuntosRuta)
                .color(Color.GREEN)
                .width(5)
        );

        //CENTRO EL MAPA EN LA ULTIMA POSICION DEL ARRAY //
        if  (arrayPuntosRuta.size()>0) {

   Log.d("prueba","el ultimo punto es: "+arrayPuntosRuta.size());



         actualizacionCamara =CameraUpdateFactory.newLatLngZoom(arrayPuntosRuta.get(arrayPuntosRuta.size()-1),12);
         mapa.animateCamera(actualizacionCamara);


        }


    } catch (JSONException e) {
        e.printStackTrace();
    }


}



//            if (marker!=null){

  //          marker.remove();}
    //        marker = mapa.addMarker(new MarkerOptions().position(new LatLng(alea.nextFloat()/2 + 42.6, -(alea.nextFloat()/2 + 5.2))));
            handler.postDelayed(this, MARKER_UPDATE_INTERVAL);

        }
    };



    public String obtenerDatos(){
        tarea = new TareaPesada(MainActivity.this);
        tarea.execute(url);

//al terminar la ejecucion de la tarea, tendremos algo en datosObtenidos
        return datosObtenidos;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        datosObtenidos ="";
        alea = new Random();

        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(map);

        mapFragment.getMapAsync(this);

        //ejecuta el metodo updateMarker, a intervalo definido en el segundo parametro
        handler.postDelayed(updateMarker, MARKER_UPDATE_INTERVAL);




        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

//este hilo no esta haciendo nada hasta que no se descomente //hilo.run();
        hilo = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    actualizarMarcador();

                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }//fin while
            }

            private void actualizarMarcador() {
                Log.d("prueba", "marcador actualizado");
                MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(map);

                GoogleMap miMapa = mapa;


                miMapa.clear();

                miMapa.addMarker(new MarkerOptions()
                        .position(new LatLng(alea.nextInt(4) + 41, -(alea.nextInt(1) + 5)))
                        .title("Hello world"));


            }
        });


    }

    @Override
    public void onMapReady(GoogleMap map) {
        mapa = map;

        mapa.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mapa.setMinZoomPreference(5);


//centro mapa en asturias
       //  cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(43.3,-5.2), 3);
        //mapa.animateCamera(cameraUpdate);

    }



    @Override
    protected void onDestroy() {
        handler.removeCallbacks(updateMarker);

        super.onDestroy();
    }
}


class TareaPesada extends AsyncTask<URL,String,String>{
String linea="";
StringBuffer buffer = null;
Activity actividadUI;

    public TareaPesada(MainActivity mainActivity) {
    this.actividadUI = mainActivity;
    }


    @Override
    protected void onProgressUpdate(String... values) {
        super.onProgressUpdate(values);




    }

    @Override
    protected String doInBackground(URL... params) {

        try {
            params[0]= new URL("http://www.motosmieres.com/muestraJSON.php");
            HttpURLConnection conn = (HttpURLConnection) params[0].openConnection();
            conn.setDoInput(true);
            conn.connect();

            InputStream is = conn.getInputStream();
            InputStreamReader isr= new InputStreamReader(is);

            BufferedReader br = new BufferedReader(isr);

            buffer = new StringBuffer();

            linea= "";



            while   (((linea=br.readLine())!=null)){

                buffer.append(linea);

            }
            is.close();
            isr.close();
            br.close();
            conn.disconnect();

            publishProgress(buffer.toString());
            return  buffer.toString();


        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }


    @Override
        protected void onPostExecute(String s) {
        super.onPostExecute(s);
Log.d("pruebas","se recibio:"+s);
//ejecuta el metodo updateMarker, a intervalo definido en el segundo parametro
 //handler.postDelayed(updateMarker, MARKER_UPDATE_INTERVAL);

        //de esta manera podemos pasar datos a la activity principal
MainActivity.datosObtenidos=s;
    }
}

