package com.pratama.myservice;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private MapView mapView;
    private GoogleMap mMap;
    private TextView tanggalgempa,waktugempa,srgempa,kedalamangempa,lokasigempa,statusgempa;
    private String koordinat,tanggal,jam,wilayah,sr,kedalaman,potensi;
    private LatLng latLng;
    private SwipeRefreshLayout swipeRefreshLayout;
    private Handler mHandler = new Handler();
    private int mInterval = 100; // Interval waktu dalam milidetik antara perubahan gambar marker
    private int[] mFrames = { R.drawable.quake1, R.drawable.quake2, R.drawable.quake3, R.drawable.quake4, R.drawable.quake5, R.drawable.quake6, R.drawable.quake7 }; // Daftar frame GIF
    private ImageView mn1,mn2,mn3,mn4,mn5,mn6,mn7,mn8,mn9;
    private Marker mMarker;
    private int targetWidth = 150; // Lebar target gambar marker (sesuaikan dengan kebutuhan Anda)
    private int targetHeight = 150; // Tinggi target gambar marker (sesuaikan dengan kebutuhan Anda)


    private RequestQueue queue;
//    private NotificationManager notificationManager;
//    private static final int NOTIFICATION_ID = 123;
//    private static final String CHANNEL_ID = "Channel_Id";

    // Metode untuk mengganti gambar marker
    // Metode untuk mengganti gambar marker
    private Runnable mRunnable = new Runnable() {
        int frameIndex = 0;

        @Override
        public void run() {
            if (mMarker != null) {
                int drawableId = mFrames[frameIndex];
                Bitmap resizedBitmap = resizeBitmap(drawableId, targetWidth, targetHeight);
                mMarker.setIcon(BitmapDescriptorFactory.fromBitmap(resizedBitmap));
                frameIndex = (frameIndex + 1) % mFrames.length;
                mHandler.postDelayed(this, mInterval);
            }
        }
    };


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

        //notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Memulai polling untuk memeriksa pembaruan setiap 10 detik
        //startPolling();

    }



    private void goToTsunami() {
        mn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, Edu_tsunami.class));
            }
        });
    }

    private void goToGunungMeletus() {
        mn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, Edu_gunungmeletus.class));
            }
        });
    }

    private void goToLongsor() {
        mn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, Edu_longsor.class));
            }
        });
    }

    private void goToBanjir() {
        mn4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, Edu_banjir.class));
            }
        });
    }

    private void goToAnginTopan() {
        mn5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, Edu_angintopan.class));
            }
        });
    }

    private void goToKebakaran() {
        mn6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, Edu_kebakaran.class));
            }
        });
    }

    private void goToKekeringan() {
        mn7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, Edu_kekeringan.class));
            }
        });
    }

    private void goToGempa() {
        mn8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, Edu_gempabumi.class));
            }
        });
    }

    private void goToWabah() {
        mn9.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, Edu_wabah.class));
            }
        });
    }

    private void refresh() {
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(false);
                loadDataFromApi();
            }
        });
    }

//    private void startPolling() {
//        Timer timer = new Timer();
//        timer.scheduleAtFixedRate(new TimerTask() {
//            @Override
//            public void run() {
//                // Memuat data JSON dari URL API menggunakan Volley
//                loadDataFromApi();
//            }
//        }, 0, 10000); // Memeriksa setiap 10 detik
//    }

    private void initV() {
        tanggalgempa = findViewById(R.id.tanggal);
        waktugempa = findViewById(R.id.waktu);
        srgempa = findViewById(R.id.sr);
        kedalamangempa = findViewById(R.id.kedalaman);
        lokasigempa = findViewById(R.id.lokasi);
        statusgempa = findViewById(R.id.status);
        swipeRefreshLayout = findViewById(R.id.refreshmain);

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
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // Menangani respon JSON dari server
                        try {
                            JSONObject infogempa = response.getJSONObject("Infogempa").getJSONObject("gempa");
                            tanggal = infogempa.getString("Tanggal");
                            jam = infogempa.getString("Jam");
                            koordinat = infogempa.getString("Coordinates");
                            sr = infogempa.getString("Magnitude");
                            kedalaman = infogempa.getString("Kedalaman");
                            wilayah = infogempa.getString("Wilayah");
                            potensi = infogempa.getString("Potensi");



                            // Memeriksa pembaruan data
                            // Untuk sederhana, kita memeriksa apakah tanggal gempa telah berubah
                            // Anda dapat memeriksa lebih banyak detail sesuai kebutuhan Anda

                           // if (!Objects.equals(jam, waktugempa.getText().toString())) {
                                // Mendapatkan nilai lintang dan bujur dari string koordinat
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
                                // Menampilkan notifikasi
                                //showNotification("Info Gempa", "Ada pembaruan data gempa");
                            //}

                            // Menampilkan lokasi gempa di peta setelah mendapatkan koordinat
                            if (mMap != null && latLng != null) {
                                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 7),6000,null);
                                addMarkerToLocation(latLng);
                            }


                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(MainActivity.this, "error "+e, Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // Menangani kesalahan saat permintaan
                progressDialog.dismiss();
                Toast.makeText(MainActivity.this, "Gagal Menampilkan Data, Periksa Jaringan", Toast.LENGTH_SHORT).show();
            }
        });

        // Menambahkan permintaan ke antrian
        queue.add(jsonObjectRequest);
    }

//    private void showNotification(String title, String message) {
//        // Membuat Intent untuk menavigasi ke aplikasi Anda saat notifikasi diklik
//        Intent intent = new Intent(this, MainActivity.class);
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);
//
//        // Membuat kanal notifikasi untuk Android Oreo (API level 26) ke atas
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Channel Name", NotificationManager.IMPORTANCE_DEFAULT);
//            channel.setDescription("Channel Description");
//            channel.enableLights(true);
//            channel.setLightColor(Color.RED);
//            notificationManager.createNotificationChannel(channel);
//        }

        // Membuat dan menampilkan notifikasi
//        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
//                .setSmallIcon(R.drawable.baseline_notifications_24)
//                .setContentTitle(title)
//                .setContentText(message)
//                .setContentIntent(pendingIntent)
//                .setAutoCancel(true);
//
//        // Membatalkan notifikasi sebelum menampilkan yang baru
//        notificationManager.cancel(NOTIFICATION_ID);
//        notificationManager.notify(NOTIFICATION_ID, builder.build());
//    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        // Inisialisasi GoogleMap
        mMap = googleMap;

    }

    private Marker addMarkerToLocation(LatLng latLng) {
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        // Resize gambar sebelum digunakan sebagai ikon marker
        //markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));

//        Bitmap resizedBitmap = resizeBitmap(mFrames, targetWidth, targetHeight); // Anda dapat memilih frame awal atau yang sesuai dengan kebutuhan Anda
//        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(resizedBitmap));

        mMarker = mMap.addMarker(markerOptions);
        // Mulai animasi
        mHandler.postDelayed(mRunnable, mInterval);
        return mMap.addMarker(markerOptions);
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