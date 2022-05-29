package com.thenav.thenav;

import android.app.Activity;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Parcel;
import android.widget.TextView;
import android.widget.Toast;

// classes needed to initialize map
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.JsonObject;
import com.mapbox.api.geocoding.v5.GeocodingCriteria;
import com.mapbox.api.geocoding.v5.MapboxGeocoding;
import com.mapbox.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.api.geocoding.v5.models.GeocodingResponse;
import com.mapbox.core.exceptions.ServicesException;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;

// classes needed to add the location component
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.modes.CameraMode;

// classes needed to add a marker
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;

// classes to calculate a route
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncherOptions;
import com.mapbox.services.android.navigation.ui.v5.route.NavigationMapRoute;
import com.mapbox.services.android.navigation.v5.navigation.NavigationRoute;
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

import android.util.Log;

// classes needed to launch navigation UI
import android.view.View;
import android.widget.Button;
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncher;

import com.mapbox.mapboxsdk.plugins.places.autocomplete.PlaceAutocomplete;
import com.mapbox.mapboxsdk.plugins.places.autocomplete.model.PlaceOptions;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconOffset;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.visibility;

public class Map extends AppCompatActivity implements OnMapReadyCallback, MapboxMap.OnMapClickListener, PermissionsListener {

    // variables for adding location layer
    private MapView mapView;
    private MapboxMap mapboxMap;

    // variables for adding location layer
    private PermissionsManager permissionsManager;
    private LocationComponent locationComponent;

    // variables for calculating and drawing a route
    private DirectionsRoute currentRoute;
    private static final String TAG = "DirectionsActivity";
    private NavigationMapRoute navigationMapRoute;

    // variables needed to start or cancel navigation
    private Button startNav, cancel;

    // variable needed to add user favourite as suggestions
    private CarmenFeature fav;

    // variables needed to initialize navigation
    private static final int REQUEST_CODE_AUTOCOMPLETE = 1;
    private String geoJsonSourceLayerId = "geojsonSourceLayerId";
    private String symbolIconId = "symbolIconId";
    private Style mStyle;

    // variable to add a bottom toolbar
    private Toolbar bottomToolbar;

    // variables to add buttons to add unfavourite and favourite
    private FloatingActionButton favourite, favouriteSelected;

    // variables to hold longitude and latitude if the user wants to add the location as a favourite
    private Double favLongi, favLatit;

    // variables needed to declare firebase Reference
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference refToUsersFavTableInFireBase = database.getReference("UsersFavourite");
    DatabaseReference refToUsersTableInFireBase = database.getReference("UsersInfo");
    DatabaseReference refToUsersRoutesTakenTableInFireBase = database.getReference("UsersRoutesTaken");
    private FirebaseAuth mAuth;

    // variables to hold user email, transportation choice and the firebase table key
    String userEmail, userTransport, favContainerKey;

    // static public variable to hold user utility choice
    public static String userMetImp;

    // List of CarmenFeatures to add as suggestions
    private List<CarmenFeature> favList = new ArrayList<>();

    // variable to hold placeOptions for mapbox global search feature
    private PlaceOptions placeOptions;

    // variable to hold the destination location to store if the user takes the route
    private CarmenFeature location;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Get the current user
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser userE = mAuth.getCurrentUser();
        userEmail = userE.getEmail();

        //Get the Instance of the map from mapbox
        Mapbox.getInstance(this, getString(R.string.access_token));
        setContentView(R.layout.activity_map);

        //Setting the layout component
        startNav = findViewById(R.id.btn_start);
        cancel = findViewById(R.id.btn_cancel);
        bottomToolbar = findViewById(R.id.tbBottom);
        favourite = findViewById(R.id.fab_favourite);
        favouriteSelected = findViewById(R.id.fab_favouriteSelected);
        startNav.setVisibility(View.INVISIBLE);
        cancel.setVisibility(View.INVISIBLE);
        bottomToolbar.setVisibility(View.INVISIBLE);

        //Hiding the favourite
        favourite.hide();
        favouriteSelected.hide();

        //Set the Map
        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
    }

    @Override
    public void onMapReady(@NonNull final MapboxMap mapboxMap) {
        this.mapboxMap = mapboxMap;
        mapboxMap.setStyle(getString(R.string.navigation_guidance_day), new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull final Style style) {
                //Method to set the user location
                enableLocationComponent(style);

                //Method for when the user clicks on the global search button
                initSearchFab();

                //Method for when the user clicks on the settings button
                initSettingsFab();

                //Method for when the user clicks on the favourite button
                initFavouriteFab();

                //Method for when the user clicks on the unfavourite button
                initFavouriteSelectedFab();

                // Add the symbol layer icon to map for future use
                style.addImage(symbolIconId, BitmapFactory.decodeResource(
                        Map.this.getResources(), R.drawable.mapbox_marker_icon_default));

                mStyle = style;

                // Create an empty GeoJSON source using the empty feature collection
                setUpSource(style);

                // Set up a new symbol layer for displaying the searched location's feature coordinates
                setupLayer(style);

                mapboxMap.addOnMapClickListener(Map.this);

                //Save the route and starts navigating the user
                startNav.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try{
                            double durH = currentRoute.duration()/ 3600;
                            double durM = currentRoute.duration()/ 60;

                            double disKM = currentRoute.distance() / 1000;
                            double disMI = currentRoute.distance() / 1609;

                            int checkTimeH = (int) (durH);
                            int checkTimeM = (int) (durM);

                            double durCalcMM = (durM - checkTimeM) * 60;

                            double roundOffDisKM = Math.round(disKM * 100.0) / 100.0;
                            double roundOffDisMI = Math.round(disMI * 100.0) / 100.0;

                            String userContainerKey = refToUsersRoutesTakenTableInFireBase.child("UsersRoutesTaken").push().getKey();
                            UserRoutesTakenUpload userRouteTakenUpload;
                            if (checkTimeH == 0){
                               userRouteTakenUpload = new UserRoutesTakenUpload(userEmail, userContainerKey, location.placeName(), roundOffDisKM + " km", roundOffDisMI +" mi", userTransport, checkTimeM + " min");

                            } else {
                               userRouteTakenUpload = new UserRoutesTakenUpload(userEmail, userContainerKey, location.placeName(), roundOffDisKM + " km", roundOffDisMI +" mi", userTransport, checkTimeH + " hr " + Math.round(durCalcMM) + " min");
                            }
                            refToUsersRoutesTakenTableInFireBase.child(userContainerKey).setValue(userRouteTakenUpload);

                            boolean simulateRoute = true;
                            NavigationLauncherOptions options = NavigationLauncherOptions.builder()
                                    .directionsRoute(currentRoute)
                                    .shouldSimulateRoute(simulateRoute)
                                    .build();
                            // Call this method with Context from within an Activity
                            NavigationLauncher.startNavigation(Map.this, options);
                        }catch (Exception e){

                        }
                    }
                });

                //Cancels the route the user has taken and hides certain components
                cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        startNav.setVisibility(View.INVISIBLE);
                        cancel.setVisibility(View.INVISIBLE);
                        favourite.hide();
                        favouriteSelected.hide();
                        bottomToolbar.setVisibility(View.INVISIBLE);
                        if (navigationMapRoute != null){
                            navigationMapRoute.updateRouteVisibilityTo(false);
                        }
                        style.removeImage(symbolIconId);
                    }
                });
            }
        });
    }

    //Method to open up the global search Activity and add the favourite suggestions
    //---------------------------------------------------------------------------------
    private void initSearchFab() {
        findViewById(R.id.btnSearch).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = null;

                placeOptions = PlaceOptions.builder()
                        .backgroundColor(Color.parseColor("#FFFFFFFF"))
                        .country("ZA")
                        .limit(10)
                        .build(PlaceOptions.MODE_CARDS);

                if (!favList.isEmpty()){
                    for (int i = 0; i < favList.size(); i++) {

                        placeOptions.injectedPlaces().add(favList.get(i).toJson());

                    }
                }

                intent = new PlaceAutocomplete.IntentBuilder()
                        .accessToken(Mapbox.getAccessToken() != null ? Mapbox.getAccessToken() : getString(R.string.access_token))
                        .placeOptions(placeOptions)
                        .build(Map.this);

                startActivityForResult(intent, REQUEST_CODE_AUTOCOMPLETE);

                PlaceAutocomplete.clearRecentHistory(Map.this);
            }
        });
    }
    //---------------------------------------------------------------------------------

    //Method to unfavourite a location
    //---------------------------------------------------------------------------------
    private void initFavouriteSelectedFab() {
        findViewById(R.id.fab_favouriteSelected).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                favList.clear();
                placeOptions.injectedPlaces().clear();
                refToUsersFavTableInFireBase.child(favContainerKey).removeValue();
                getFavoritePlaces();
                favouriteSelected.hide();
                favourite.show();
            }
        });
    }
    //---------------------------------------------------------------------------------

    //Method to take the user to the setting page
    //---------------------------------------------------------------------------------
    private void initSettingsFab() {
        findViewById(R.id.fab_settings).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Map.this, Settings.class);
                startActivity(intent);
                finish();
            }
        });
    }
    //---------------------------------------------------------------------------------

    //Method to get the location details that will be used to when the user takes the route
    //---------------------------------------------------------------------------------
    private void getLocation(){
        try {
            MapboxGeocoding client = MapboxGeocoding.builder()
                    .accessToken(getString(R.string.access_token))
                    .query(Point.fromLngLat(favLongi, favLatit))
                    .geocodingTypes(GeocodingCriteria.TYPE_ADDRESS)
                    .build();

            client.enqueueCall(new Callback<GeocodingResponse>() {
                @Override
                public void onResponse(Call<GeocodingResponse> call, Response<GeocodingResponse> response) {
                    if (response.body() != null) {
                        List<CarmenFeature> results = response.body().features();
                        if (results.size() > 0) {
                            final CarmenFeature feature = results.get(0);
                            location = feature;

                            // If the geocoder returns a result, we take the first in the list and show a Toast with the place name.
                            mapboxMap.getStyle(new Style.OnStyleLoaded() {
                                @Override
                                public void onStyleLoaded(@NonNull Style style) {

                                }
                            });

                        } else {
                            Toast.makeText(Map.this,
                                    "Did not add as favourite", Toast.LENGTH_LONG).show();
                        }
                    }
                }
                @Override
                public void onFailure(Call<GeocodingResponse> call, Throwable throwable) {
                    Timber.e("Geocoding Failure: %s", throwable.getMessage());
                }
            });
        } catch (ServicesException servicesException) {
            Timber.e("Error geocoding: %s", servicesException.toString());
            servicesException.printStackTrace();
        }
    }
    //---------------------------------------------------------------------------------

    //Method to set a location as a favourite
    //---------------------------------------------------------------------------------
    private void initFavouriteFab() {
        findViewById(R.id.fab_favourite).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String userContainerKey = refToUsersFavTableInFireBase.child("UsersFavourite").push().getKey();
                UserFavouriteUpload userFavouriteUpload = new UserFavouriteUpload(userEmail, userContainerKey, location.placeName(), favLongi + "," + favLatit);
                refToUsersFavTableInFireBase.child(userContainerKey).setValue(userFavouriteUpload);
                Toast.makeText(Map.this, location.placeName() + " Added as favourite", Toast.LENGTH_LONG).show();
            }
        });
    }
    //---------------------------------------------------------------------------------

    //Method to get all the favourites in firebase
    //---------------------------------------------------------------------------------
    public void getFavoritePlaces() {
        refToUsersFavTableInFireBase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                favList.clear();
                for (DataSnapshot dddd : dataSnapshot.getChildren()) {
                    UserFavouriteUpload UserContainerInFirebase = dddd.getValue(UserFavouriteUpload.class);
                    if (UserContainerInFirebase.getUpEmail().equals(userEmail)){
                        String[] address = UserContainerInFirebase.getUpFavourite().split(",");
                        String[] longLati = UserContainerInFirebase.getUpLongLati().split(",");

                        fav = CarmenFeature.builder().text(address[0])
                                .geometry(Point.fromLngLat(Double.parseDouble(longLati[0]), Double.parseDouble(longLati[1])))
                                .placeName(UserContainerInFirebase.getUpFavourite())
                                .id(UserContainerInFirebase.getUpContainer())
                                .properties(new JsonObject())
                                .build();

                        favList.add(fav);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    //---------------------------------------------------------------------------------

    //Method to set the source GeoJsonSource
    //---------------------------------------------------------------------------------
    private void setUpSource(@NonNull Style loadedMapStyle) {
        loadedMapStyle.addSource(new GeoJsonSource(geoJsonSourceLayerId));
    }
    //---------------------------------------------------------------------------------

    //Method set the map layer
    //---------------------------------------------------------------------------------
    private void setupLayer(@NonNull Style loadedMapStyle) {
        loadedMapStyle.addLayer(new SymbolLayer("SYMBOL_LAYER_ID", geoJsonSourceLayerId).withProperties(
                iconImage(symbolIconId),
                iconAllowOverlap(true),
                iconIgnorePlacement(true),
                iconOffset(new Float[] {0f, -8f})
        ));
    }
    //---------------------------------------------------------------------------------


    //Method that will execute after the user selected a location from the global search feature
    //It will set the map to show the route the user selected and go to the get route method
    //---------------------------------------------------------------------------------
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_AUTOCOMPLETE) {

            // Retrieve selected location's CarmenFeature
            CarmenFeature selectedCarmenFeature = PlaceAutocomplete.getPlace(data);

            double longi = ((Point) selectedCarmenFeature.geometry()).longitude();
            double latit = ((Point) selectedCarmenFeature.geometry()).latitude();

            Point destinationPoint = Point.fromLngLat(longi, latit);
            Point originPoint = Point.fromLngLat(locationComponent.getLastKnownLocation().getLongitude(),
                    locationComponent.getLastKnownLocation().getLatitude());

            // Create a new FeatureCollection and add a new Feature to it using selectedCarmenFeature above.
            // Then retrieve and update the source designated for showing a selected location's symbol layer icon
            if (mapboxMap != null) {
                Style style = mapboxMap.getStyle();
                if (style != null) {
                    GeoJsonSource source = style.getSourceAs(geoJsonSourceLayerId);
                    if (source != null) {
                        source.setGeoJson(FeatureCollection.fromFeatures(
                                new Feature[] {Feature.fromJson(selectedCarmenFeature.toJson())}));
                    }

                    LatLng start = new LatLng(latit, longi);
                    LatLng end = new LatLng(locationComponent.getLastKnownLocation().getLatitude(), locationComponent.getLastKnownLocation().getLongitude());

                    LatLngBounds latLngBounds = new LatLngBounds.Builder()
                            .include(start)
                            .include(end)
                            .build();

                    mapboxMap.animateCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, 180));
                }
            }

            getRoute(originPoint, destinationPoint);
            startNav.setVisibility(View.VISIBLE);
            cancel.setVisibility(View.VISIBLE);
            bottomToolbar.setVisibility(View.VISIBLE);
            getLocation();
        }
    }
    //---------------------------------------------------------------------------------

    //Method that will listen for when the user click on the map and the it will get the route to the location the user selected
    //---------------------------------------------------------------------------------
    @SuppressWarnings( {"MissingPermission"})
    @Override
    public boolean onMapClick(@NonNull LatLng point) {
        try {
            Point destinationPoint = Point.fromLngLat(point.getLongitude(), point.getLatitude());
            Point originPoint = Point.fromLngLat(locationComponent.getLastKnownLocation().getLongitude(),
                    locationComponent.getLastKnownLocation().getLatitude());

            GeoJsonSource source = mapboxMap.getStyle().getSourceAs(geoJsonSourceLayerId);
            if (source != null) {
                source.setGeoJson(Feature.fromGeometry(destinationPoint));
            }

            mStyle.addImage(symbolIconId, BitmapFactory.decodeResource(
                    Map.this.getResources(), R.drawable.mapbox_marker_icon_default));

            getRoute(originPoint, destinationPoint);
            startNav.setVisibility(View.VISIBLE);
            cancel.setVisibility(View.VISIBLE);
            bottomToolbar.setVisibility(View.VISIBLE);
            getFav();
            getLocation();
            return true;
        } catch(Exception e) {
            startNav.setVisibility(View.INVISIBLE);
            cancel.setVisibility(View.INVISIBLE);
            favourite.hide();
            favouriteSelected.hide();
            bottomToolbar.setVisibility(View.INVISIBLE);
            if (navigationMapRoute != null){
                navigationMapRoute.updateRouteVisibilityTo(false);
            }
            mStyle.removeImage(symbolIconId);
        }
        return false;
    }
    //---------------------------------------------------------------------------------

    //Method to get the route the user selected and draw that on the map
    //---------------------------------------------------------------------------------
    private void getRoute(Point origin, Point destination) {
        favLongi = destination.coordinates().get(0).doubleValue();
        favLatit = destination.coordinates().get(1).doubleValue();

        getFav();

        NavigationRoute.builder(this)
                .accessToken(Mapbox.getAccessToken())
                .origin(origin)
                .destination(destination)
                .profile(userTransport)
                .voiceUnits(userMetImp)
                .build()
                .getRoute(new Callback<DirectionsResponse>() {
                    @Override
                    public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {
                        // You can get the generic HTTP info about the response
                        Log.d(TAG, "Response code: " + response.code());
                        if (response.body() == null) {
                            Log.e(TAG, "No routes found, make sure you set the right user and access token.");
                            return;
                        } else if (response.body().routes().size() < 1) {
                            Log.e(TAG, "No routes found");
                            return;
                        }

                        currentRoute = response.body().routes().get(0);

                        // Draw the route on the map
                        if (navigationMapRoute != null) {
                            navigationMapRoute.updateRouteVisibilityTo(false);
                        } else {
                            navigationMapRoute = new NavigationMapRoute(null, mapView, mapboxMap, R.style.NavigationMapRoute);
                        }
                        navigationMapRoute.addRoute(currentRoute);
                    }

                    @Override
                    public void onFailure(Call<DirectionsResponse> call, Throwable throwable) {
                        Log.e(TAG, "Error: " + throwable.getMessage());
                    }
                });
    }
    //---------------------------------------------------------------------------------

    //Method get the location of the user
    //---------------------------------------------------------------------------------
    @SuppressWarnings( {"MissingPermission"})
    private void enableLocationComponent(@NonNull Style loadedMapStyle) {
        // Check if permissions are enabled and if not request
        if (PermissionsManager.areLocationPermissionsGranted(this)) {
            // Activate the MapboxMap LocationComponent to show user location
            // Adding in LocationComponentOptions is also an optional parameter
            locationComponent = mapboxMap.getLocationComponent();
            locationComponent.activateLocationComponent(this, loadedMapStyle);
            locationComponent.setLocationComponentEnabled(true);
            // Set the component's camera mode
            locationComponent.setCameraMode(CameraMode.TRACKING);
        } else {
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(this);
        }
    }
    //---------------------------------------------------------------------------------

    //Method to check if the user allowed permission to access their location
    //---------------------------------------------------------------------------------
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
    //---------------------------------------------------------------------------------

    //Method to display a message to the user if the did not allow their location
    //---------------------------------------------------------------------------------
    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {
        Toast.makeText(this, R.string.user_location_permission_explanation, Toast.LENGTH_LONG).show();
    }
    //---------------------------------------------------------------------------------

    //Method to get the permission result from the user
    //---------------------------------------------------------------------------------
    @Override
    public void onPermissionResult(boolean granted) {
        if (granted) {
            enableLocationComponent(mapboxMap.getStyle());
        } else {
            Toast.makeText(this, R.string.user_location_permission_not_granted, Toast.LENGTH_LONG).show();
            finish();
        }
    }
    //---------------------------------------------------------------------------------

    //Method get the user choice of transport
    //---------------------------------------------------------------------------------
    private void getTransport(){
        refToUsersTableInFireBase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot userInfo : dataSnapshot.getChildren()) {
                    UserInfoUpload UserContainerInFirebase = userInfo.getValue(UserInfoUpload.class);

                    if (UserContainerInFirebase.getUpEmail().equals(userEmail)) {
                        if (UserContainerInFirebase.getUpTransport().equals("Car")){
                            userTransport = "driving";
                        } else if (UserContainerInFirebase.getUpTransport().equals("Walking")){
                            userTransport = "walking";
                        } else if (UserContainerInFirebase.getUpTransport().equals("Bicycle")){
                            userTransport = "cycling";
                        }
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }
    //---------------------------------------------------------------------------------

    //Method get the user system
    //---------------------------------------------------------------------------------
    private void getMetImp(){
        refToUsersTableInFireBase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot userInfo : dataSnapshot.getChildren()) {
                    UserInfoUpload UserContainerInFirebase = userInfo.getValue(UserInfoUpload.class);

                    if (UserContainerInFirebase.getUpEmail().equals(userEmail)) {
                        if (UserContainerInFirebase.getUpMetOrImp().equals("Metric System (Kilometers - Km)")){
                            userMetImp = "metric";
                        } else if(UserContainerInFirebase.getUpMetOrImp().equals("Imperial System (Miles - Mi)")){
                            userMetImp = "imperial";
                        }
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }
    //---------------------------------------------------------------------------------

    //Method to switched the buttons between favourites and unfavourites
    //---------------------------------------------------------------------------------
    private void getFav(){
        refToUsersFavTableInFireBase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                boolean check = false;

                for (DataSnapshot value : dataSnapshot.getChildren()) {

                    UserFavouriteUpload UserContainerInFirebase = value.getValue(UserFavouriteUpload.class);

                    if (UserContainerInFirebase.getUpEmail().equals(userEmail)) {
                        String[] longiLatit = UserContainerInFirebase.getUpLongLati().split(",");

                        if (!Arrays.asList(longiLatit).isEmpty()){
                            if ((favLongi.toString().equals(longiLatit[0])) && (favLatit.toString().equals(longiLatit[1]))) {
                                favContainerKey = UserContainerInFirebase.getUpContainer();
                                check = true;
                            }
                        }
                    }
                }

                if (check == true) {
                    favourite.hide();
                    favouriteSelected.show();
                }
                else{
                    favouriteSelected.hide();
                    favourite.show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    //---------------------------------------------------------------------------------

    //Method that will start the map and get information needed when the activity starts
    //---------------------------------------------------------------------------------
    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
        getTransport();
        getMetImp();
        getFavoritePlaces();
    }
    //---------------------------------------------------------------------------------

    //Method that will resume the map
    //---------------------------------------------------------------------------------
    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }
    //---------------------------------------------------------------------------------

    //Method that will pause the map
    //---------------------------------------------------------------------------------
    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }
    //---------------------------------------------------------------------------------


    //Method that will stop the map
    //---------------------------------------------------------------------------------
    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }
    //---------------------------------------------------------------------------------

    //Method that saves the instance of the previous state
    //---------------------------------------------------------------------------------
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }
    //---------------------------------------------------------------------------------

    //Method that will destroy the map when the user exits
    //---------------------------------------------------------------------------------
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }
    //---------------------------------------------------------------------------------

    // Method to tell everyone we are now low on memory.
    //---------------------------------------------------------------------------------
    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }
    //---------------------------------------------------------------------------------
}
