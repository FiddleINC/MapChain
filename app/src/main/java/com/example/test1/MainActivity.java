package com.example.test1;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TableLayout;
import android.widget.TextView;
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

        ImageButton button = findViewById(R.id.AddButton);
        ImageButton button1 = findViewById(R.id.DoneButton);
        ImageButton button2 = findViewById(R.id.ValidatorButton);

        button.setOnClickListener(view -> {
            Toast.makeText(getBaseContext(),"Select Points", Toast.LENGTH_SHORT).show();
            map.getOverlayManager().add(new MapEventsOverlay(getPoints()));
        });

        button1.setOnClickListener(view -> {
            Toast.makeText(getBaseContext(),"Polygon Created", Toast.LENGTH_SHORT).show();
            boundingBox();
            LayoutInflater layoutInflater = LayoutInflater.from(MainActivity.this);
            //LayoutInflater layoutInflater = (LayoutInflater)getBaseContext().getSystemService(LAYOUT_INFLATER_SERVICE);
            View popupInputView = layoutInflater.inflate(R.layout.metadata_form, null);

            final PopupWindow popupWindow = new PopupWindow(popupInputView, TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.MATCH_PARENT);
            popupWindow.showAsDropDown(popupInputView, 20, 20);

            Button addButton = popupInputView.findViewById(R.id.addpropbutton);
            Button doneButton = popupInputView.findViewById(R.id.propdoneButton);
            LinearLayout formLayout = popupInputView.findViewById(R.id.formLayout);

            addButton.setOnClickListener(v -> {
                TextView tagName = new TextView(this);
                tagName.setText("Tag Name");
                formLayout.addView(tagName);

                EditText tagNameInput = new EditText(this);
                formLayout.addView(tagNameInput);
                String tagNameInputString = tagNameInput.getText().toString();
                System.out.println(tagNameInputString);

                TextView tagValue = new TextView(this);
                tagValue.setText("Value");
                formLayout.addView(tagValue);

                EditText tagValueInput = new EditText(this);
                formLayout.addView(tagValueInput);
                String tagValueInputString = tagNameInput.getText().toString();
                System.out.println(tagValueInputString);
            });

            doneButton.setOnClickListener(v -> {
                popupWindow.dismiss();
            });
        });

        button2.setOnClickListener(view -> {
            LayoutInflater layoutInflater = LayoutInflater.from(MainActivity.this);
            //LayoutInflater layoutInflater = (LayoutInflater)getBaseContext().getSystemService(LAYOUT_INFLATER_SERVICE);
            View popupInputView = layoutInflater.inflate(R.layout.validator_form, null);

            final PopupWindow popupWindow = new PopupWindow(popupInputView, TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.MATCH_PARENT);
            popupWindow.showAsDropDown(popupInputView, 20, 20);

            Button doneButton = popupInputView.findViewById(R.id.validatorDoneButton);
            doneButton.setOnClickListener(v -> {
                popupWindow.dismiss();
            });
        });
    }

    public MapEventsReceiver getPoints() {
        MapEventsReceiver mReceive = new MapEventsReceiver(){
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint p) {
                Marker Marker = new Marker(map);
                Marker.setPosition(p);
                Marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                map.getOverlays().add(Marker);
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

    public static byte[] getSHA(String input) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        return md.digest(input.getBytes(StandardCharsets.UTF_8));
    }

    public static String toHexString(byte[] hash) {
        BigInteger number = new BigInteger(1, hash);
        StringBuilder hexString = new StringBuilder(number.toString(16));
        while (hexString.length() < 32) {
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