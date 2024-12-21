package com.pratama.myservice;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
    private MapView mapView;
    private GoogleMap mMap;
    private TextView tanggalgempa,waktugempa,srgempa,kedalamangempa,lokasigempa,statusgempa;
    private String koordinat,tanggal,jam,wilayah,sr,kedalaman,potensi;
    private LatLng latLng;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ImageView mn1,mn2,mn3,mn4,mn5,mn6,mn7,mn8,mn9;
    private LottieAnimationView lottieMarker;
    private RequestQueue queue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //maps
        mapView = findViewById(R.id.mapViewGempa);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
        initV();

        goToTsunami();
        goToGunungMeletus();
        goToLongsor();
        goToBanjir();
        goToAnginTopan();
        goToKebakaran();
        goToKekeringan();
        goToGempa();
        goToWabah();

        queue = Volley.newRequestQueue(this);
        loadDataFromApi();
        refresh();
    }

    private void goToTsunami() {
        mn1.setOnClickListener(view -> startActivity(new Intent(MainActivity.this, Edu_tsunami.class)));
    }

    private void goToGunungMeletus() {
        mn2.setOnClickListener(view -> startActivity(new Intent(MainActivity.this, Edu_gunungmeletus.class)));
    }

    private void goToLongsor() {
        mn3.setOnClickListener(view -> startActivity(new Intent(MainActivity.this, Edu_longsor.class)));
    }

    private void goToBanjir() {
        mn4.setOnClickListener(view -> startActivity(new Intent(MainActivity.this, Edu_banjir.class)));
    }

    private void goToAnginTopan() {
        mn5.setOnClickListener(view -> startActivity(new Intent(MainActivity.this, Edu_angintopan.class)));
    }

    private void goToKebakaran() {
        mn6.setOnClickListener(view -> startActivity(new Intent(MainActivity.this, Edu_kebakaran.class)));
    }

    private void goToKekeringan() {
        mn7.setOnClickListener(view -> startActivity(new Intent(MainActivity.this, Edu_kekeringan.class)));
    }

    private void goToGempa() {
        mn8.setOnClickListener(view -> startActivity(new Intent(MainActivity.this, Edu_gempabumi.class)));
    }

    private void goToWabah() {
        mn9.setOnClickListener(view -> startActivity(new Intent(MainActivity.this, Edu_wabah.class)));
    }

    private void refresh() {
        swipeRefreshLayout.setOnRefreshListener(() -> {
            swipeRefreshLayout.setRefreshing(false);
            loadDataFromApi();
        });
    }

    private void initV() {
        tanggalgempa = findViewById(R.id.tanggal);
        waktugempa = findViewById(R.id.waktu);
        srgempa = findViewById(R.id.sr);
        kedalamangempa = findViewById(R.id.kedalaman);
        lokasigempa = findViewById(R.id.lokasi);
        statusgempa = findViewById(R.id.status);
        swipeRefreshLayout = findViewById(R.id.refreshmain);
        lottieMarker = findViewById(R.id.lottieMarker);

        mn1 = findViewById(R.id.mn_tsunami);
        mn2 = findViewById(R.id.mn_gunungmeletus);
        mn3 = findViewById(R.id.mn_longsor);
        mn4 = findViewById(R.id.mn_banjir);
        mn5 = findViewById(R.id.mn_angintopan);
        mn6 = findViewById(R.id.mn_kebakaran);
        mn7 = findViewById(R.id.mn_kekeringan);
        mn8 = findViewById(R.id.mn_gempa);
        mn9 = findViewById(R.id.mn_wabah);
    }

    private void loadDataFromApi() {
        String url = "https://data.bmkg.go.id/DataMKG/TEWS/autogempa.json";

        ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setMessage("memeriksa koneksi");
        progressDialog.setCancelable(false);
        progressDialog.show();

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    // respon JSON dari server
                    try {
                        JSONObject infogempa = response.getJSONObject("Infogempa").getJSONObject("gempa");
                        tanggal = infogempa.getString("Tanggal");
                        jam = infogempa.getString("Jam");
                        koordinat = infogempa.getString("Coordinates");
                        sr = infogempa.getString("Magnitude");
                        kedalaman = infogempa.getString("Kedalaman");
                        wilayah = infogempa.getString("Wilayah");
                        potensi = infogempa.getString("Potensi");

                        progressDialog.dismiss();
                            String[] parts = koordinat.split(",");
                            double latitude = Double.parseDouble(parts[0]);
                            double longitude = Double.parseDouble(parts[1]);
                            latLng = new LatLng(latitude, longitude);
                            tanggalgempa.setText(tanggal);
                            waktugempa.setText(jam);
                            srgempa.setText("Kekuatan " + sr + " SR");
                            kedalamangempa.setText("Kedalaman " + kedalaman);
                            lokasigempa.setText(wilayah);
                            statusgempa.setText(potensi);

                        // Menampilkan lokasi gempa di peta setelah mendapatkan koordinat
                        if (mMap != null && latLng != null) {
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 5),7000,null);
                            addMarkerToLocation(latLng);
                            mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(MainActivity.this, "error "+e, Toast.LENGTH_SHORT).show();
                    }
                }, error -> {
                    // Menangani kesalahan saat permintaan
                    progressDialog.dismiss();
                    Toast.makeText(MainActivity.this, "Gagal Menampilkan Data, Periksa Jaringan", Toast.LENGTH_SHORT).show();
                });

        // Menambahkan permintaan ke antrian
        queue.add(jsonObjectRequest);
    }


    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        // Inisialisasi GoogleMap
        mMap = googleMap;
        lottieMarker.setVisibility(View.VISIBLE);
        mMap.setOnCameraMoveListener(() -> {
            if (latLng != null) {
                updateLottiePosition(latLng);
            }
        });

    }

    private void updateLottiePosition(LatLng latLng) {
        if (mMap != null) {
            //mMap.addMarker(new MarkerOptions().position(latLng).title("Lokasi Gempa"));

            Point screenPosition = mMap.getProjection().toScreenLocation(latLng);
            lottieMarker.setX(screenPosition.x - (lottieMarker.getWidth() / 2f));
            lottieMarker.setY(screenPosition.y - (lottieMarker.getHeight() / 2f));
        }
    }

    private void addMarkerToLocation(LatLng latLng) {
        if (mMap != null) {
            mMap.addMarker(new MarkerOptions().position(latLng).title("Lokasi Gempa"));

            Point screenPosition = mMap.getProjection().toScreenLocation(latLng);
            lottieMarker.setX(screenPosition.x - (lottieMarker.getWidth() / 2f));
            lottieMarker.setY(screenPosition.y - (lottieMarker.getHeight() / 2f));
        }
    }

    private Bitmap resizeBitmap(int resId, int width, int height) {
        Bitmap imageBitmap = BitmapFactory.decodeResource(getResources(), resId);
        return Bitmap.createScaledBitmap(imageBitmap, width, height, false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }
}