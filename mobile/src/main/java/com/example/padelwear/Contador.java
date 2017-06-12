package com.example.padelwear;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.example.comun.DireccionesGestureDetector;
import com.example.comun.Partida;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataItemBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Wearable;

/**
 * Created by jamarfal on 6/6/17.
 */

public class Contador extends Activity implements DataApi.DataListener, GoogleApiClient.ConnectionCallbacks {
    private Partida partida;
    private TextView misPuntosTextView, misJuegosTextView, misSetsTextView, susPuntosTextView, susJuegosTextView, susSetsTextView;
    private Vibrator vibrador;
    private long[] vibrEntrada = {0l, 500};
    private long[] vibrDeshacer = {0l, 500, 500, 500};

    private GoogleApiClient apiClient;

    private static final String WEAR_PUNTUACION = "/puntuacion";
    private static final String KEY_MIS_PUNTOS = "com.example.padel.key.mis_puntos";
    private static final String KEY_MIS_JUEGOS = "com.example.padel.key.mis_juegos";
    private static final String KEY_MIS_SETS = "com.example.padel.key.mis_sets";
    private static final String KEY_SUS_PUNTOS = "com.example.padel.key.sus_puntos";
    private static final String KEY_SUS_JUEGOS = "com.example.padel.key.sus_juegos";
    private static final String KEY_SUS_SETS = "com.example.padel.key.sus_sets";
    private int misPuntos;
    private int misJuegos;
    private int misSets;
    private int susPuntos;
    private int susJuegos;
    private int susSets;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.contador);

        apiClient = new GoogleApiClient.Builder(this).addApi(Wearable.API).addConnectionCallbacks(this).build();


        partida = new Partida();
        vibrador = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
        misPuntosTextView = (TextView) findViewById(R.id.misPuntos);
        susPuntosTextView = (TextView) findViewById(R.id.susPuntos);
        misJuegosTextView = (TextView) findViewById(R.id.misJuegos);
        susJuegosTextView = (TextView) findViewById(R.id.susJuegos);
        misSetsTextView = (TextView) findViewById(R.id.misSets);
        susSetsTextView = (TextView) findViewById(R.id.susSets);
        actualizaNumeros();
        View fondo = findViewById(R.id.fondo);
        fondo.setOnTouchListener(new View.OnTouchListener() {
            GestureDetector detector = new DireccionesGestureDetector(Contador.this, new DireccionesGestureDetector.SimpleOnDireccionesGestureListener() {
                @Override
                public boolean onArriba(MotionEvent e1, MotionEvent e2, float distX, float distY) {
                    partida.rehacerPunto();
                    vibrador.vibrate(vibrDeshacer, -1);
                    actualizaNumeros();
                    return true;
                }

                @Override
                public boolean onAbajo(MotionEvent e1, MotionEvent e2, float distX, float distY) {
                    partida.deshacerPunto();
                    vibrador.vibrate(vibrDeshacer, -1);
                    actualizaNumeros();
                    return true;
                }
            });

            @Override
            public boolean onTouch(View v, MotionEvent evento) {
                detector.onTouchEvent(evento);
                return true;
            }
        });
        misPuntosTextView.setOnTouchListener(new View.OnTouchListener() {
            GestureDetector detector = new DireccionesGestureDetector(Contador.this, new DireccionesGestureDetector.SimpleOnDireccionesGestureListener() {
                @Override
                public boolean onDerecha(MotionEvent e1, MotionEvent e2, float distX, float distY) {
                    partida.puntoPara(true);
                    vibrador.vibrate(vibrEntrada, -1);
                    actualizaNumeros();
                    return true;
                }
            });

            @Override
            public boolean onTouch(View v, MotionEvent evento) {
                detector.onTouchEvent(evento);
                return true;
            }
        });
        susPuntosTextView.setOnTouchListener(new View.OnTouchListener() {
            GestureDetector detector = new DireccionesGestureDetector(Contador.this, new DireccionesGestureDetector.SimpleOnDireccionesGestureListener() {
                @Override
                public boolean onDerecha(MotionEvent e1, MotionEvent e2, float distX, float distY) {
                    partida.puntoPara(false);
                    vibrador.vibrate(vibrEntrada, -1);
                    actualizaNumeros();
                    return true;
                }
            });

            @Override
            public boolean onTouch(View v, MotionEvent evento) {
                detector.onTouchEvent(evento);
                return true;
            }
        });

        PendingResult<DataItemBuffer> resultado = Wearable.DataApi.getDataItems(apiClient);
        resultado.setResultCallback(new ResultCallback<DataItemBuffer>() {
            @Override
            public void onResult(DataItemBuffer dataItems) {
                for (DataItem dataItem : dataItems) {
                    if (dataItem.getUri().getPath().equals(WEAR_PUNTUACION)) {
                        DataMap dataMap = DataMapItem.fromDataItem(dataItem).getDataMap();
                        misPuntos = dataMap.getInt(KEY_MIS_PUNTOS);
                        misJuegos = dataMap.getInt(KEY_MIS_JUEGOS);
                        misSets = dataMap.getInt(KEY_MIS_SETS);
                        susPuntos = dataMap.getInt(KEY_SUS_PUNTOS);
                        susJuegos = dataMap.getInt(KEY_SUS_JUEGOS);
                        susSets = dataMap.getInt(KEY_SUS_SETS);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                misPuntosTextView.setText(String.valueOf(misPuntos));
                                susPuntosTextView.setText(String.valueOf(susPuntos));
                                misJuegosTextView.setText(String.valueOf(misJuegos));
                                susJuegosTextView.setText(String.valueOf(susJuegos));
                                misSetsTextView.setText(String.valueOf(misSets));
                                susSetsTextView.setText(String.valueOf(susSets));
                            }
                        });
                    }
                }
                dataItems.release();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        apiClient.connect();
    }

    @Override
    protected void onStop() {
        Wearable.DataApi.removeListener(apiClient, this);
        if (apiClient != null && apiClient.isConnected()) {
            apiClient.disconnect();
        }
        super.onStop();
    }

    void actualizaNumeros() {
        misPuntosTextView.setText(partida.getMisPuntos());
        susPuntosTextView.setText(partida.getSusPuntos());
        misJuegosTextView.setText(partida.getMisJuegos());
        susJuegosTextView.setText(partida.getSusJuegos());
        misSetsTextView.setText(partida.getMisSets());
        susSetsTextView.setText(partida.getSusSets());
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEventBuffer) {
        for (DataEvent evento : dataEventBuffer) {
            if (evento.getType() == DataEvent.TYPE_CHANGED) {
                DataItem item = evento.getDataItem();
                if (item.getUri().getPath().equals(WEAR_PUNTUACION)) {
                    DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap();
                    misPuntos = dataMap.getInt(KEY_MIS_PUNTOS);
                    misJuegos = dataMap.getInt(KEY_MIS_JUEGOS);
                    misSets = dataMap.getInt(KEY_MIS_SETS);
                    susPuntos = dataMap.getInt(KEY_SUS_PUNTOS);
                    susJuegos = dataMap.getInt(KEY_SUS_JUEGOS);
                    susSets = dataMap.getInt(KEY_SUS_SETS);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            misPuntosTextView.setText(String.valueOf(misPuntos));
                            susPuntosTextView.setText(String.valueOf(susPuntos));
                            misJuegosTextView.setText(String.valueOf(misJuegos));
                            susJuegosTextView.setText(String.valueOf(susJuegos));
                            misSetsTextView.setText(String.valueOf(misSets));
                            susSetsTextView.setText(String.valueOf(susSets));
                        }
                    });
                }
            } else if (evento.getType() == DataEvent.TYPE_DELETED) { // Algún ítem ha sido borrado } }
            }
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Wearable.DataApi.addListener(apiClient, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }
}