package com.example.test1;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;
import android.content.Context;
import android.preference.PreferenceManager;

import com.github.davidmoten.geo.Coverage;
import com.github.davidmoten.geo.GeoHash;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polygon;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.ArrayList;
import java.util.List;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class MainActivity extends AppCompatActivity {

    MapView map = null;
    List<GeoPoint> points = new ArrayList<>();


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //handle permissions first, before map is created. not depicted here
        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));

        //inflate and create the map
        setContentView(R.layout.activity_main);

        map = findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setMultiTouchControls(true);
        IMapController mapController = map.getController();
        GeoPoint startPoint = new GeoPoint(22.308208, 87.281945);
        mapController.setZoom(7);
        mapController.setCenter(startPoint);

        MyLocationNewOverlay mLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(getApplicationContext()), map);
        mLocationOverlay.enableMyLocation();
        map.getOverlays().add(mLocationOverlay);

        Marker startMarker = new Marker(map);
        startMarker.setPosition(startPoint);
        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        map.getOverlays().add(startMarker);

        ImageButton button = findViewById(R.id.AddButton);
        ImageButton button1 = findViewById(R.id.DoneButton);

        button.setOnClickListener(view -> {
            Toast.makeText(getBaseContext(),"Select Points", Toast.LENGTH_SHORT).show();
            map.getOverlayManager().add(new MapEventsOverlay(getPoints()));
        });

        button1.setOnClickListener(view -> {
            Toast.makeText(getBaseContext(),"Polygon Created", Toast.LENGTH_SHORT).show();
            boundingBox();
            LayoutInflater layoutInflater = LayoutInflater.from(MainActivity.this);
            View popupInputDialogView = layoutInflater.inflate(R.layout.metadata_form, null);
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
            alertDialogBuilder.setView(popupInputDialogView);

            // Create AlertDialog and show.
            final AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();


        });
    }

    public MapEventsReceiver getPoints() {
        MapEventsReceiver mReceive = new MapEventsReceiver(){
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint p) {
                System.out.println(p.getLatitude() + " " + p.getLongitude());
                Marker startMarker = new Marker(map);
                startMarker.setPosition(p);
                startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                map.getOverlays().add(startMarker);
                points.add(p);
                Toast.makeText(getBaseContext(),"Selected", Toast.LENGTH_SHORT).show();
                return true;
            }

            @Override
            public boolean longPressHelper(GeoPoint p) {
                return false;
            }
        };
        return mReceive;
    }

    public static byte[] getSHA(String input) throws NoSuchAlgorithmException
    {
        // Static getInstance method is called with hashing SHA
        MessageDigest md = MessageDigest.getInstance("SHA-256");

        // digest() method called
        // to calculate message digest of an input
        // and return array of byte
        return md.digest(input.getBytes(StandardCharsets.UTF_8));
    }

    public static String toHexString(byte[] hash)
    {
        // Convert byte array into signum representation
        BigInteger number = new BigInteger(1, hash);

        // Convert message digest into hex value
        StringBuilder hexString = new StringBuilder(number.toString(16));

        // Pad with leading zeros
        while (hexString.length() < 32)
        {
            hexString.insert(0, '0');
        }

        return hexString.toString();
    }


    public void boundingBox() {
        List<GeoPoint> geoPoints = points;
        Polygon polygon = new Polygon();    //see note below
        polygon.setFillColor(Color.argb(30, 0,0,255));
        geoPoints.add(points.get(0));    //forces the loop to close
        map.getOverlayManager().add(polygon);
        double left = +180.00000000, right = -180.0000000, top = -90.00000000 ,bottom = 90.0000000;
        for(GeoPoint p : points) {
            if ( p.getLatitude() < bottom) {
                bottom = p.getLatitude();
            }
            if ( p.getLatitude() > top) {
                top = p.getLatitude();
            }
            if ( p.getLongitude() < left) {
                left = p.getLongitude();
            }
            if ( p.getLongitude() > right) {
                right = p.getLongitude();
            }
        }
        int precision = 2;
        Coverage hashL = GeoHash.coverBoundingBox(top, left, bottom, right, 1);
        double diff = hashL.getRatio() - 1;
        while( diff > 1) {
            hashL = GeoHash.coverBoundingBox(top, left, bottom, right, precision);
            diff = hashL.getRatio() - 1;
            precision++;
        }
        String hash = "";
        for(String s : hashL.getHashes()) {
            hash = hash + s;
        }
        try {
            String hexHash = toHexString(getSHA(hash));
            DBHelper dbhelper = new DBHelper(getBaseContext(), hexHash, hashL.getHashes());
            dbhelper.insertData();
        }
        catch(NoSuchAlgorithmException e) {
            System.out.println("Exception thrown for incorrect algorithm: " + e);
        }
        points.clear();
        Toast.makeText(getBaseContext(),"Cleared", Toast.LENGTH_SHORT).show();
    }

    public void onResume() {
        super.onResume();
        map.onResume(); //needed for compass, my location overlays, v6.0.0 and up
    }

    public void onPause() {
        super.onPause();
        map.onPause();  //needed for compass, my location overlays, v6.0.0 and up
    }
}