package com.example.padelwear;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.DismissOverlayView;
import android.support.wearable.view.SwipeDismissFrameLayout;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.comun.DireccionesGestureDetector;
import com.example.comun.Partida;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

/**
 * Created by jamarfal on 6/6/17.
 */

public class Contador extends WearableActivity implements DataApi.DataListener, GoogleApiClient.ConnectionCallbacks {
    private static final String WEAR_ARRANCAR_ACTIVIDAD = "/arrancar_actividad";
    private Typeface fuenteNormal = Typeface.create("sans-serif", 0);
    private Typeface fuenteFina = Typeface.create("sans-serif-thin", 0);
    private Partida partida;
    private TextView misPuntosTextView, misJuegosTextView, misSetsTextView, susPuntosTextView, susJuegosTextView, susSetsTextView;
    private Vibrator vibrador;
    private long[] vibrEntrada = {0l, 500};
    private long[] vibrDeshacer = {0l, 500, 500, 500};
    private DismissOverlayView dismissOverlay;
    private RelativeLayout timeContainer;


    private String misPuntos;
    private String misJuegos;
    private String misSets;
    private String susPuntos;
    private String susJuegos;
    private String susSets;


    private GoogleApiClient apiClient;
    public static final String KEY_PROCESA_STRING = "procesa_string";
    private static final String WEAR_PUNTUACION = "/puntuacion";
    private static final String KEY_MIS_PUNTOS = "com.example.padel.key.mis_puntos";
    private static final String KEY_MIS_JUEGOS = "com.example.padel.key.mis_juegos";
    private static final String KEY_MIS_SETS = "com.example.padel.key.mis_sets";
    private static final String KEY_SUS_PUNTOS = "com.example.padel.key.sus_puntos";
    private static final String KEY_SUS_JUEGOS = "com.example.padel.key.sus_juegos";
    private static final String KEY_SUS_SETS = "com.example.padel.key.sus_sets";

    protected void onCreate(Bundle savedInstanceState) {
        //getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.onCreate(savedInstanceState);
        setAmbientEnabled();
        setContentView(R.layout.contador);

        SwipeDismissFrameLayout root = (SwipeDismissFrameLayout) findViewById(R.id.swipe_dismiss_root);
        root.addCallback(new SwipeDismissFrameLayout.Callback() {
            @Override
            public void onDismissed(SwipeDismissFrameLayout layout) {
                Contador.this.finish();
            }
        });

        apiClient = new GoogleApiClient.Builder(this).addApi(Wearable.API).addConnectionCallbacks(this).build();

        partida = new Partida();

        dismissOverlay = (DismissOverlayView) findViewById(R.id.dismiss_overlay);
        dismissOverlay.setIntroText("Para salir de la aplicación, haz una pulsación larga");
        dismissOverlay.showIntroIfNecessary();

        vibrador = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
        misPuntosTextView = (TextView) findViewById(R.id.misPuntos);
        susPuntosTextView = (TextView) findViewById(R.id.susPuntos);
        misJuegosTextView = (TextView) findViewById(R.id.misJuegos);
        susJuegosTextView = (TextView) findViewById(R.id.susJuegos);
        misSetsTextView = (TextView) findViewById(R.id.misSets);
        susSetsTextView = (TextView) findViewById(R.id.susSets);
        timeContainer = (RelativeLayout) findViewById(R.id.relativelayout_contador_time_container);
        actualizaNumeros();
        View fondo = findViewById(R.id.fondo);
        fondo.setOnTouchListener(new View.OnTouchListener() {
            GestureDetector detector = new DireccionesGestureDetector(Contador.this, new DireccionesGestureDetector.SimpleOnDireccionesGestureListener() {
                @Override
                public boolean onArriba(MotionEvent e1, MotionEvent e2, float distX, float distY) {
                    partida.rehacerPunto();
                    sincronizaDatos();
                    vibrador.vibrate(vibrDeshacer, -1);
                    actualizaNumeros();
                    return true;
                }

                @Override
                public boolean onAbajo(MotionEvent e1, MotionEvent e2, float distX, float distY) {
                    partida.deshacerPunto();
                    sincronizaDatos();
                    vibrador.vibrate(vibrDeshacer, -1);
                    actualizaNumeros();
                    return true;
                }

                @Override
                public void onLongPress(MotionEvent e) {
                    dismissOverlay.show();
                }
            });

            @Override
            public boolean onTouch(View v, MotionEvent evento) {
                detector.onTouchEvent(evento);
                return true;
            }
        });

        misPuntosTextView.setOnTouchListener(new View.OnTouchListener() {


            GestureDetector detector = new DireccionesGestureDetector(
                    Contador.this,
                    new DireccionesGestureDetector.SimpleOnDireccionesGestureListener() {
                        @Override
                        public boolean onDerecha(MotionEvent e1, MotionEvent e2, float distX, float distY) {
                            partida.puntoPara(true);
                            vibrador.vibrate(vibrEntrada, -1);
                            actualizaNumeros();
                            sincronizaDatos();
                            return true;
                        }

                        @Override
                        public void onLongPress(MotionEvent e) {
                            dismissOverlay.show();
                        }
                    });

            @Override
            public boolean onTouch(View v, MotionEvent evento) {
                detector.onTouchEvent(evento);
                return true;
            }
        });

        susPuntosTextView.setOnTouchListener(new View.OnTouchListener()

        {
            GestureDetector detector = new DireccionesGestureDetector(Contador.this, new DireccionesGestureDetector.SimpleOnDireccionesGestureListener() {
                @Override
                public boolean onDerecha(MotionEvent e1, MotionEvent e2, float distX, float distY) {
                    partida.puntoPara(false);
                    vibrador.vibrate(vibrEntrada, -1);
                    actualizaNumeros();
                    sincronizaDatos();
                    return true;
                }

                @Override
                public void onLongPress(MotionEvent e) {
                    dismissOverlay.show();
                }
            });

            @Override
            public boolean onTouch(View v, MotionEvent evento) {
                detector.onTouchEvent(evento);
                return true;
            }
        });

        mandarMensaje(WEAR_ARRANCAR_ACTIVIDAD, "");
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
    public void onEnterAmbient(Bundle ambientDetails) {
        super.onEnterAmbient(ambientDetails);
        misPuntosTextView.setTypeface(fuenteFina);
        misPuntosTextView.getPaint().setAntiAlias(false);
        susPuntosTextView.setTypeface(fuenteFina);
        susPuntosTextView.getPaint().setAntiAlias(false);
        misJuegosTextView.setTypeface(fuenteFina);
        misJuegosTextView.getPaint().setAntiAlias(false);
        susJuegosTextView.setTypeface(fuenteFina);
        susJuegosTextView.getPaint().setAntiAlias(false);
        misSetsTextView.setTypeface(fuenteFina);
        misSetsTextView.getPaint().setAntiAlias(false);
        susSetsTextView.setTypeface(fuenteFina);
        susSetsTextView.getPaint().setAntiAlias(false);
        timeContainer.setVisibility(View.VISIBLE);
    }

    @Override
    public void onExitAmbient() {
        super.onExitAmbient();
        misPuntosTextView.setTypeface(fuenteNormal);
        misPuntosTextView.getPaint().setAntiAlias(false);
        susPuntosTextView.setTypeface(fuenteNormal);
        susPuntosTextView.getPaint().setAntiAlias(false);
        misJuegosTextView.setTypeface(fuenteNormal);
        misJuegosTextView.getPaint().setAntiAlias(false);
        susJuegosTextView.setTypeface(fuenteNormal);
        susJuegosTextView.getPaint().setAntiAlias(false);
        misSetsTextView.setTypeface(fuenteNormal);
        misSetsTextView.getPaint().setAntiAlias(false);
        susSetsTextView.setTypeface(fuenteNormal);
        susSetsTextView.getPaint().setAntiAlias(false);
        timeContainer.setVisibility(View.GONE);
    }


    private void sincronizaDatos() {
        Log.d("Padel Wear", "Sincronizando");
        PutDataMapRequest putDataMapReq = PutDataMapRequest.create(WEAR_PUNTUACION);
//        if (partida.getMisPuntos().equals("-") || partida.getSusPuntos().equals("-")) {
//            putDataMapReq.getDataMap().putBoolean(KEY_PROCESA_STRING, true);
//        } else {
//            putDataMapReq.getDataMap().putBoolean(KEY_PROCESA_STRING, true);
//        }
        putDataMapReq.getDataMap().putString(KEY_MIS_PUNTOS, partida.getMisPuntos());
        putDataMapReq.getDataMap().putString(KEY_MIS_JUEGOS, partida.getMisJuegos());
        putDataMapReq.getDataMap().putString(KEY_MIS_SETS, partida.getMisSets());
        putDataMapReq.getDataMap().putString(KEY_SUS_PUNTOS, partida.getSusPuntos());
        putDataMapReq.getDataMap().putString(KEY_SUS_JUEGOS, partida.getSusJuegos());
        putDataMapReq.getDataMap().putString(KEY_SUS_SETS, partida.getSusSets());
        PutDataRequest putDataReq = putDataMapReq.asPutDataRequest();
        Wearable.DataApi.putDataItem(apiClient, putDataReq);
    }

    private void mandarMensaje(final String path, final String texto) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                NodeApi.GetConnectedNodesResult nodos = Wearable.NodeApi.getConnectedNodes(apiClient).await();
                for (Node nodo : nodos.getNodes()) {
                    Wearable.MessageApi.sendMessage(apiClient, nodo.getId(), path, texto.getBytes()).setResultCallback(new ResultCallback<MessageApi.SendMessageResult>() {
                        @Override
                        public void onResult(MessageApi.SendMessageResult resultado) {
                            if (!resultado.getStatus().isSuccess()) {
                                Log.e("sincronizacion", "Error al mandar mensaje. Código:" + resultado.getStatus().getStatusCode());
                            }
                        }
                    });
                }
            }
        }).start();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Wearable.DataApi.addListener(apiClient, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onDataChanged(DataEventBuffer dataEventBuffer) {
        for (DataEvent evento : dataEventBuffer) {
            if (evento.getType() == DataEvent.TYPE_CHANGED) {
                DataItem item = evento.getDataItem();
                if (item.getUri().getPath().equals(WEAR_PUNTUACION)) {
                    DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap();
                    misPuntos = dataMap.getString(KEY_MIS_PUNTOS);
                    misJuegos = dataMap.getString(KEY_MIS_JUEGOS);
                    misSets = dataMap.getString(KEY_MIS_SETS);
                    susPuntos = dataMap.getString(KEY_SUS_PUNTOS);
                    susJuegos = dataMap.getString(KEY_SUS_JUEGOS);
                    susSets = dataMap.getString(KEY_SUS_SETS);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            misPuntosTextView.setText(misPuntos);
                            susPuntosTextView.setText(susPuntos);
                            misJuegosTextView.setText(misJuegos);
                            susJuegosTextView.setText(susJuegos);
                            misSetsTextView.setText(misSets);
                            susSetsTextView.setText(susSets);
                        }
                    });
                }
            } else if (evento.getType() == DataEvent.TYPE_DELETED) { // Algún ítem ha sido borrado } }
            }
        }
    }
}
