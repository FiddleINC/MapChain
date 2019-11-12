package com.example.test1;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
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
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.ArrayList;
import java.util.List;


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

        //map.getOverlays().add(new MapEventsOverlay(getData()));

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


    public void boundingBox() {
        double left = +180.00000000, right = -180.0000000, top = -90.00000000 ,bottom = 90.0000000;
        for(GeoPoint p : points) {
            //System.out.println(p.getLatitude() + " " + p.getLongitude());
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
            //System.out.println(left + " " + right + " " + top + " " + bottom);
        }
        int precision = 2;
        Coverage hashL = GeoHash.coverBoundingBox(top, left, bottom, right, 1);
        double diff = hashL.getRatio() - 1;
        while( diff > 0.1) {
            hashL = GeoHash.coverBoundingBox(top, left, bottom, right, precision);
            diff = hashL.getRatio() - 1;
            precision++;
        }
        System.out.println(hashL.getHashes());
        System.out.println(hashL.getRatio());
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

    /*public MapEventsReceiver getData() {
        MapEventsReceiver mReceive = new MapEventsReceiver(){
                @Override
            public boolean singleTapConfirmedHelper(GeoPoint p) {
                double lat = p.getLatitude();
                double longt = p.getLongitude();
                int length = 9;
                String geohash = GeoHash.encodeHash(lat, longt, length);
                System.out.println("Geohash: " + geohash);
                DBHelper mDatabase = new DBHelper(getBaseContext(),lat, longt, geohash);
                if (mDatabase.insertData())
                    Toast.makeText(getBaseContext(),"Added", Toast.LENGTH_SHORT).show();
                else
                    //Toast.makeText(getBaseContext(), Toast.LENGTH_SHORT).show();
                    Toast.makeText(getBaseContext(),"Not Added", Toast.LENGTH_SHORT).show();

                    return true;
                }

            @Override
            public boolean longPressHelper(GeoPoint p) {
                return false;
            }
        };
        return mReceive;
    }*/
}