package com.example.padelwear;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.DismissOverlayView;
import android.support.wearable.view.SwipeDismissFrameLayout;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.comun.DireccionesGestureDetector;
import com.example.comun.Partida;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

/**
 * Created by jamarfal on 6/6/17.
 */

public class Contador extends WearableActivity {
    private Typeface fuenteNormal = Typeface.create("sans-serif", 0);
    private Typeface fuenteFina = Typeface.create("sans-serif-thin", 0);
    private Partida partida;
    private TextView misPuntos, misJuegos, misSets, susPuntos, susJuegos, susSets;
    private Vibrator vibrador;
    private long[] vibrEntrada = {0l, 500};
    private long[] vibrDeshacer = {0l, 500, 500, 500};
    private DismissOverlayView dismissOverlay;
    private RelativeLayout timeContainer;

    private GoogleApiClient apiClient;
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

        apiClient = new GoogleApiClient.Builder(this).addApi(Wearable.API).build();

        partida = new Partida();

        dismissOverlay = (DismissOverlayView) findViewById(R.id.dismiss_overlay);
        dismissOverlay.setIntroText("Para salir de la aplicación, haz una pulsación larga");
        dismissOverlay.showIntroIfNecessary();

        vibrador = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
        misPuntos = (TextView) findViewById(R.id.misPuntos);
        susPuntos = (TextView) findViewById(R.id.susPuntos);
        misJuegos = (TextView) findViewById(R.id.misJuegos);
        susJuegos = (TextView) findViewById(R.id.susJuegos);
        misSets = (TextView) findViewById(R.id.misSets);
        susSets = (TextView) findViewById(R.id.susSets);
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

        misPuntos.setOnTouchListener(new View.OnTouchListener() {


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

        susPuntos.setOnTouchListener(new View.OnTouchListener()

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
    }

    @Override
    protected void onStart() {
        super.onStart();
        apiClient.connect();
    }

    @Override
    protected void onStop() {
        if (apiClient != null && apiClient.isConnected()) {
            apiClient.disconnect();
        }
        super.onStop();
    }

    void actualizaNumeros() {
        misPuntos.setText(partida.getMisPuntos());
        susPuntos.setText(partida.getSusPuntos());
        misJuegos.setText(partida.getMisJuegos());
        susJuegos.setText(partida.getSusJuegos());
        misSets.setText(partida.getMisSets());
        susSets.setText(partida.getSusSets());
    }

    @Override
    public void onEnterAmbient(Bundle ambientDetails) {
        super.onEnterAmbient(ambientDetails);
        misPuntos.setTypeface(fuenteFina);
        misPuntos.getPaint().setAntiAlias(false);
        susPuntos.setTypeface(fuenteFina);
        susPuntos.getPaint().setAntiAlias(false);
        misJuegos.setTypeface(fuenteFina);
        misJuegos.getPaint().setAntiAlias(false);
        susJuegos.setTypeface(fuenteFina);
        susJuegos.getPaint().setAntiAlias(false);
        misSets.setTypeface(fuenteFina);
        misSets.getPaint().setAntiAlias(false);
        susSets.setTypeface(fuenteFina);
        susSets.getPaint().setAntiAlias(false);
        timeContainer.setVisibility(View.VISIBLE);
    }

    @Override
    public void onExitAmbient() {
        super.onExitAmbient();
        misPuntos.setTypeface(fuenteNormal);
        misPuntos.getPaint().setAntiAlias(false);
        susPuntos.setTypeface(fuenteNormal);
        susPuntos.getPaint().setAntiAlias(false);
        misJuegos.setTypeface(fuenteNormal);
        misJuegos.getPaint().setAntiAlias(false);
        susJuegos.setTypeface(fuenteNormal);
        susJuegos.getPaint().setAntiAlias(false);
        misSets.setTypeface(fuenteNormal);
        misSets.getPaint().setAntiAlias(false);
        susSets.setTypeface(fuenteNormal);
        susSets.getPaint().setAntiAlias(false);
        timeContainer.setVisibility(View.GONE);
    }


    private void sincronizaDatos() {
        Log.d("Padel Wear", "Sincronizando");
        PutDataMapRequest putDataMapReq = PutDataMapRequest.create(WEAR_PUNTUACION);
        putDataMapReq.getDataMap().putInt(KEY_MIS_PUNTOS, Integer.parseInt(partida.getMisPuntos()));
        putDataMapReq.getDataMap().putInt(KEY_MIS_JUEGOS, Integer.parseInt(partida.getMisJuegos()));
        putDataMapReq.getDataMap().putInt(KEY_MIS_SETS, Integer.parseInt(partida.getMisSets()));
        putDataMapReq.getDataMap().putInt(KEY_SUS_PUNTOS, Integer.parseInt(partida.getSusPuntos()));
        putDataMapReq.getDataMap().putInt(KEY_SUS_JUEGOS, Integer.parseInt(partida.getSusJuegos()));
        putDataMapReq.getDataMap().putInt(KEY_SUS_SETS, Integer.parseInt(partida.getSusSets()));
        PutDataRequest putDataReq = putDataMapReq.asPutDataRequest();
        Wearable.DataApi.putDataItem(apiClient, putDataReq);
    }
}
