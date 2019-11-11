package com.example.test1;

import androidx.appcompat.app.AppCompatActivity;

import geohasher.GeoHasher;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;
import android.content.Context;
import android.graphics.Color;
import android.preference.PreferenceManager;

import com.github.davidmoten.geo.GeoHash;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.MapTileProviderBasic;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.MinimapOverlay;
import org.osmdroid.views.overlay.Polygon;
import org.osmdroid.views.overlay.TilesOverlay;
import org.osmdroid.views.overlay.compass.CompassOverlay;
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    MapView map = null;
    //private CompassOverlay mCompassOverlay;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        System.out.println("geohash:" );
        //handle permissions first, before map is created. not depicted here
        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));

        //inflate and create the map
        setContentView(R.layout.activity_main);


        map = (MapView) findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setMultiTouchControls(true);
        IMapController mapController = map.getController();
        GeoPoint startPoint = new GeoPoint(22.308208, 87.281945);
        mapController.setZoom(7);
        mapController.setCenter(startPoint);

        MinimapOverlay mMiniMap = new MinimapOverlay(this, map.getTileRequestCompleteHandler());
        map.getOverlays().add(mMiniMap);

        MapTileProviderBasic mProvider = new MapTileProviderBasic(getApplicationContext());
        mProvider.setTileSource(TileSourceFactory.FIETS_OVERLAY_NL);
        TilesOverlay mTilesOverlay = new TilesOverlay(mProvider, this.getBaseContext());
        map.getOverlays().add(mTilesOverlay);

        MyLocationNewOverlay mLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(getApplicationContext()), map);
        mLocationOverlay.enableMyLocation();
        map.getOverlays().add(mLocationOverlay);

        CompassOverlay mCompassOverlay = new CompassOverlay(getApplicationContext(), new InternalCompassOrientationProvider(getApplicationContext()), map);
        mCompassOverlay.enableCompass();
        map.getOverlays().add(mCompassOverlay);

        map.getOverlays().add(new MapEventsOverlay(getData()));

        Marker startMarker = new Marker(map);
        startMarker.setPosition(startPoint);
        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        map.getOverlays().add(startMarker);

        ImageButton button = (ImageButton) findViewById(R.id.imageButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getBaseContext(),"Make Polygon", Toast.LENGTH_SHORT).show();
                map.getOverlays().add(new MapEventsOverlay(getPoints()));
                //map.getOverlayManager().add(makePolygon(NULL));
            }
        });

        ImageButton button1 = (ImageButton) findViewById(R.id.clickButton);
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getBaseContext(),"Polygon Created", Toast.LENGTH_SHORT).show();
                map.getOverlays().add(new MapEventsOverlay(getPoints()));
                //map.getOverlayManager().add(makePolygon(NULL));
            }
        });
    }

    public MapEventsReceiver getPoints() {
        MapEventsReceiver mReceive = new MapEventsReceiver(){
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint p) {
                Marker startMarker = new Marker(map);
                startMarker.setPosition(p);
                startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                map.getOverlays().add(startMarker);
                return true;
            }

            @Override
            public boolean longPressHelper(GeoPoint p) {
                List<GeoPoint> points = new ArrayList<>();
                return false;
            }
        };
        return mReceive;
    }


    public Polygon makePolygon(List<GeoPoint> points) {
        List<GeoPoint> geoPoints = new ArrayList<>();
        for (GeoPoint p : points) {
            geoPoints.add(p);
        }
        Polygon polygon = new Polygon();    //see note below
        polygon.setFillColor(Color.argb(75, 255,0,0));
        //geoPoints.add(geoPoints.get(0));    //forces the loop to close
        polygon.setPoints(geoPoints);
        polygon.setTitle("A sample polygon");
        return polygon;
    }

    public void onResume() {
        super.onResume();
        map.onResume(); //needed for compass, my location overlays, v6.0.0 and up
    }

    public void onPause() {
        super.onPause();
        map.onPause();  //needed for compass, my location overlays, v6.0.0 and up
    }

    public MapEventsReceiver getData() {
        MapEventsReceiver mReceive = new MapEventsReceiver(){
                @Override
            public boolean singleTapConfirmedHelper(GeoPoint p) {
                double lat = p.getLatitude();
                double longt = p.getLongitude();
                int length = 9;
                String geohash = GeoHash.encodeHash(lat, longt, length);
                System.out.println("geohash:" + geohash);
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
    }
}