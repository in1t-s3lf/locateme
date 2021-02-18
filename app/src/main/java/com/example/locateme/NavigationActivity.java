package com.example.locateme;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FetchPlaceResponse;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse;
import com.google.android.libraries.places.api.net.PlacesClient;

import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.PendingResult;
import com.google.maps.internal.PolylineEncoding;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.mancj.materialsearchbar.MaterialSearchBar;
import com.mancj.materialsearchbar.adapter.SuggestionsAdapter;

import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.app.AppCompatActivity;

import android.view.Menu;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

import static java.lang.Thread.sleep;

public class
NavigationActivity extends AppCompatActivity  implements
        NavigationView.OnNavigationItemSelectedListener,
        OnMapReadyCallback, GoogleMap.OnPolylineClickListener,
        GoogleMap.OnPolygonClickListener, GoogleMap.OnInfoWindowClickListener {

    private static final int  LOCATION_UPDATE_INTERVAL = 3000;
    private static final int  REQUEST_CODE = 101;

    private GeoApiContext geoApiContext =null;
    private GoogleMap mMap;
    private Location currentLocation;
    private  LatLng userLatLng;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private CircleImageView circleImageView;
    private MaterialSearchBar materialSearchBar;
    private PlacesClient placesClient;
    private List<AutocompletePrediction> predictionList;


    private TextView mName,mEmail, mDistance,mDuration,mRoute;
    private FirebaseFirestore mFirstore;
    private FirebaseAuth mAuth;
    private String mUid, username, email, code, address;
    private String friendName;
    private Boolean isRunning=null, isSharing = null;
    CardView mapCardview;
    Button mNavigate;

    private Handler mHandler = new Handler();
    private Handler handler = new Handler();
    private  Runnable mRunnable;
    private  Runnable runnable;
    private ArrayList<LatLng> listPoints;
    private  ArrayList<PolylineData>  mPolylinesData = new ArrayList<>( );
    private  Marker mSelectedMarker = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation1);
        circleImageView =(CircleImageView)findViewById(R.id.imgUser);
        mAuth = FirebaseAuth.getInstance();
        mFirstore = FirebaseFirestore.getInstance();
        mUid = mAuth.getCurrentUser().getUid();
        listPoints = new ArrayList<>();
        mDistance = (TextView)findViewById( R.id.txtDistance );
        mDuration = (TextView)findViewById( R.id.txtDuration );
        mRoute = (TextView)findViewById( R.id.txtRoadname );
        mapCardview = (CardView)findViewById( R.id.mapcard );
        mNavigate = (Button)findViewById( R.id.btnNavigate );
        mEmail = (TextView)findViewById( R.id.tUsrEmail );
        mName = (TextView)findViewById( R.id.tUsrName );

        materialSearchBar =(MaterialSearchBar)findViewById(R.id.searchBar);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        fetchLastLocation();


        //mEmail.setText( email );
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener( new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                int id = menuItem.getItemId();

                if (id==R.id.nav_settings){
                    Intent settingsIntent = new Intent( NavigationActivity.this, SettingsActivity.class );
                    settingsIntent.putExtra( "username", username );
                    settingsIntent.putExtra( "email", email );
                    settingsIntent.putExtra( "code", code );
                    settingsIntent.putExtra( "isSharing", isSharing );
                    settingsIntent.putExtra( "address", address );
                    startActivity(settingsIntent);
                }

                else if (id==R.id.nav_joined) {
                    startActivity(new Intent(getApplicationContext(), JoinedCircles.class));
                }
                else if (id==R.id.nav_joincircle){
                    startActivity(new Intent(getApplicationContext(), JoinCircleActivity.class));
                }
                else if  (id==R.id.nav_my_circle){
                    startActivity(new Intent(getApplicationContext(), MyCircle.class));
                }
                else if (id==R.id.nav_share){
                    String link = "http://maps.google.com/?ie=UTF8&hq=&ll="
                            + currentLocation.getLatitude() + "," +currentLocation.getLongitude() + "&z=13";
                    Intent myLocation = new Intent( Intent.ACTION_SEND );
                    myLocation.setType( "text/plain");

                    myLocation.putExtra(Intent.EXTRA_TEXT, "See my real-time location on Maps: " + link);
                    startActivity(Intent.createChooser(myLocation, "Share using: "));
                }

                else if (id==R.id.nav_logout){
                    final AlertDialog.Builder builder = new AlertDialog.Builder( NavigationActivity.this );
                    builder.setMessage( "You\'re about to sign-out. Continue?")
                            .setCancelable( true )
                            .setPositiveButton( "Yes",  new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    FirebaseAuth.getInstance().signOut();
                                    Intent intent = new Intent(NavigationActivity.this, LoginActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                    finish();
                                    dialog.dismiss();
                                }
                            } )
                            .setNegativeButton( "No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            } );
                    final AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                }
                return true;
            }
        } );
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.


        Places.initialize(NavigationActivity.this, getString(R.string.google_maps_api));
        placesClient = Places.createClient(this);
        final AutocompleteSessionToken token = AutocompleteSessionToken.newInstance();

        materialSearchBar.setOnSearchActionListener(new MaterialSearchBar.OnSearchActionListener() {
            @Override
            public void onSearchStateChanged(boolean enabled) {

            }

            @Override
            public void onSearchConfirmed(CharSequence text) {
                startSearch(text.toString(), true, null, true);
            }

            @Override
            public void onButtonClicked(int buttonCode) {
                if (buttonCode == MaterialSearchBar.BUTTON_NAVIGATION) {
                    Intent userIntent = getIntent();
                    if(userIntent!=null){
                        username = userIntent.getStringExtra("username");
                        email = userIntent.getStringExtra("email");
                        code = userIntent.getStringExtra("code");
                        address = userIntent.getStringExtra("address");
                        isSharing = userIntent.getBooleanExtra("isSharing", false);
                    }
                    //opening or closing a navigation drawer
                    DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
                    drawerLayout.openDrawer(GravityCompat.START);
                } else if (buttonCode == MaterialSearchBar.BUTTON_BACK) {
                    materialSearchBar.disableSearch();
                }
            }
        });

        materialSearchBar.addTextChangeListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                FindAutocompletePredictionsRequest predictionsRequest = FindAutocompletePredictionsRequest.builder()
                        .setCountry("ru")
                        .setSessionToken(token)
                        .setQuery(s.toString())
                        .build();
                placesClient.findAutocompletePredictions(predictionsRequest).addOnCompleteListener(new OnCompleteListener<FindAutocompletePredictionsResponse>() {
                    @Override
                    public void onComplete(@NonNull Task<FindAutocompletePredictionsResponse> task) {
                        if (task.isSuccessful()) {
                            FindAutocompletePredictionsResponse predictionsResponse = task.getResult();
                            if (predictionsResponse != null) {
                                predictionList = predictionsResponse.getAutocompletePredictions();
                                List<String> suggestionsList = new ArrayList<>();
                                for (int i = 0; i < predictionList.size(); i++) {
                                    AutocompletePrediction prediction = predictionList.get(i);
                                    suggestionsList.add(prediction.getFullText(null).toString());
                                }
                                materialSearchBar.updateLastSuggestions(suggestionsList);
                                if (!materialSearchBar.isSuggestionsVisible()) {
                                    materialSearchBar.showSuggestionsList();
                                }
                            }
                        } else {
                            Log.i("mytag", "prediction fetching task unsuccessful");
                        }
                    }
                });
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        materialSearchBar.setSuggestionsClickListener(new SuggestionsAdapter.OnItemViewClickListener() {
            @Override
            public void OnItemClickListener(int position, View v) {
                if (position >= predictionList.size()) {
                    return;
                }
                AutocompletePrediction selectedPrediction = predictionList.get(position);
                String suggestion = materialSearchBar.getLastSuggestions().get(position).toString();
                materialSearchBar.setText(suggestion);

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        materialSearchBar.clearSuggestions();
                    }
                }, 1000);
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                if (imm != null)
                    imm.hideSoftInputFromWindow(materialSearchBar.getWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY);
                final String placeId = selectedPrediction.getPlaceId();
                List<Place.Field> placeFields = Arrays.asList(Place.Field.LAT_LNG);

                FetchPlaceRequest fetchPlaceRequest = FetchPlaceRequest.builder(placeId, placeFields).build();
                placesClient.fetchPlace(fetchPlaceRequest).addOnSuccessListener(new OnSuccessListener<FetchPlaceResponse>() {
                    @Override
                    public void onSuccess(FetchPlaceResponse fetchPlaceResponse) {
                        Place place = fetchPlaceResponse.getPlace();
                        Log.i("mytag", "Place found: " + place.getName());
                        LatLng latLngOfPlace = place.getLatLng();
                        if (latLngOfPlace != null) {
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLngOfPlace, 15));
                            mMap.addMarker( new MarkerOptions().icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)).position(latLngOfPlace ));
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if (e instanceof ApiException) {
                            ApiException apiException = (ApiException) e;
                            apiException.printStackTrace();
                            int statusCode = apiException.getStatusCode();
                            Log.i("mytag", "place not found: " + e.getMessage());
                            Log.i("mytag", "status code: " + statusCode);
                        }
                    }
                });
            }

            @Override
            public void OnItemDeleteListener(int position, View v) {

            }
        });
    }


    private  void checkNotificationRunable(){
        handler.postDelayed( runnable = new Runnable() {
            @Override
            public void run() {
                LatLng myLocation = new LatLng( currentLocation.getLatitude(), currentLocation.getLongitude() );
                Location l1 = new Location("Me");
                l1.setLatitude(myLocation.latitude);
                l1.setLongitude(myLocation.longitude);

                Location l2 = new Location(friendName);
                l2.setLatitude(userLatLng.latitude);
                l2.setLongitude(userLatLng.longitude);

                float distance = l1.distanceTo(l2);

                if (distance <=800.0f){
                    Toast.makeText( NavigationActivity.this,friendName + "is nearby!", Toast.LENGTH_LONG ).show();
                }else if (distance >=800.0f){
                    //Dont run
                }
                handler.postDelayed( runnable, 60000*5 );
            }
        }, 60000*5);
    }

    private  void startUserLocationsRunnable(){
        mHandler.postDelayed( mRunnable = new Runnable() {
            @Override
            public void run() {
                fetchUsers();
                mHandler.postDelayed( mRunnable, LOCATION_UPDATE_INTERVAL );
                //checkNotificationRunable();
            }

        }, LOCATION_UPDATE_INTERVAL );

    }

    private  void stopLocationUpdates(){
        mHandler.removeCallbacks( mRunnable );
    }
    private void fetchUsers() {
        mFirstore.collection( "users" ).whereEqualTo( "isSharing", true )
        .get().addOnCompleteListener( new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (final QueryDocumentSnapshot document : task.getResult()) {
                        final GeoPoint userLocation = document.getGeoPoint( "geopoint" );
                        String userId = document.getId();
                        mFirstore.collection( "users" ).document(mUid)
                                .collection( "mycircles" ).whereEqualTo( "mycircles",userId ).get().addOnCompleteListener( new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if(task.isSuccessful()){
                                    for (final QueryDocumentSnapshot document : task.getResult()) {
                                        userLatLng = new LatLng(userLocation.getLatitude(), userLocation.getLongitude());
                                        mMap.addMarker(new MarkerOptions().position(userLatLng)
                                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                                                .title(document.getString( "username" )));
                                        friendName = document.getString( "username" );
                                    }

                                }
                            }
                        } );

                    }


                }
            }
        } );
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.navigation, menu);
        return true;
    }

    private void fetchLastLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
            return;
        }
        Task<Location> task = fusedLocationProviderClient.getLastLocation();
        task.addOnSuccessListener( new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if(location!=null){
                    currentLocation = location;
                    SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                            .findFragmentById(R.id.map);
                    mapFragment.getMapAsync(NavigationActivity.this);

                }
            }
        } );

    }
    private void calculateDirections(final Marker marker){
        if(geoApiContext==null){
            geoApiContext = new GeoApiContext.Builder()
                    .apiKey( getString(R.string.google_maps_key) )
                    .build();
        }
        com.google.maps.model.LatLng destination = new com.google.maps.model.LatLng(
                marker.getPosition().latitude, marker.getPosition().longitude );
        com.google.maps.model.LatLng origin = new com.google.maps.model.LatLng(
                        currentLocation.getLatitude(), currentLocation.getLongitude());

        DirectionsApiRequest directions = new DirectionsApiRequest(geoApiContext);
        directions.alternatives(true)
                .origin(origin)
                .destination(destination).setCallback( new PendingResult.Callback<DirectionsResult>() {
                    @Override
                    public void onResult(DirectionsResult result) {
                        Log.e("mytag", "route: "+result.routes[0].toString());
                        Log.e("mytag", "duration: "+result.routes[0].legs[0].duration.toString());
                        Log.e("mytag", "distance: "+result.routes[0].legs[0].distance.toString());
                        Log.e("mytag", "calculateDirections: geocodedWaypoints:"+result.geocodedWaypoints[0].toString());
                        addPolyLinesToMap(result);
                      }

                    @Override
                    public void onFailure(Throwable e) {
                        Log.e("mytag", "3rror: "+e.getMessage());
                    }
                } );
    }

    private void addPolyLinesToMap(DirectionsResult result) {
        new Handler( Looper.getMainLooper() ).post( new Runnable() {
            @Override
            public void run() {
                Log.e("mystring","run: result routes "+ result.routes.length);
                if(mPolylinesData.size()>0){
                    for(PolylineData polylineData: mPolylinesData){
                        polylineData.getPolyline().remove();
                    }
                    mPolylinesData.clear();
                    mPolylinesData = new ArrayList<>( );
                }

                double duration = 99999999;
                for(DirectionsRoute route: result.routes){
                    Log.e("mystring", "run: leg: "+ route.legs[0].toString());
                    List<com.google.maps.model.LatLng> decodedPath = PolylineEncoding.decode(route.overviewPolyline.getEncodedPath());
                    List<LatLng> newDecodedPath = new ArrayList<>(  );

                    //looping through all Latlong coordinates of 1 polyline
                    for(com.google.maps.model.LatLng latLng: decodedPath){
                        //Log.e("mytag", "run: latLng: " + latLng.toString());
                        newDecodedPath.add( new LatLng( latLng.lat, latLng.lng ) );
                        Polyline polyline = mMap.addPolyline( new PolylineOptions().addAll(newDecodedPath) );
                        polyline.setColor( ContextCompat.getColor( NavigationActivity.this, R.color.colorAccent ) );
                        polyline.setClickable(true);
                        mPolylinesData.add( new PolylineData( polyline,route.legs[0] ) );

                        double  tempDuration = route.legs[0].duration.inSeconds;
                        if(tempDuration < duration){
                            duration = tempDuration;
                            onPolylineClick( polyline );
                        }

                        //mSelectedMarker.setVisible( false);
                    }
                }
            }
        } );
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnInfoWindowClickListener( this );
        mMap.setOnPolylineClickListener( this );
        LatLng latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
        mMap.addMarker(new MarkerOptions().position(latLng)
                .title("I'm here"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode){
            case REQUEST_CODE:
                if(grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    fetchLastLocation();
                }
                break;
        }
    }


    @Override
    protected void onResume() {
        startUserLocationsRunnable();
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        stopLocationUpdates();
        super.onDestroy();
    }

    @Override
    public void onPolygonClick(Polygon polygon) {

    }

    @Override
    public void onPolylineClick(Polyline polyline) {
        int index =0;
        for(PolylineData polylineData:  mPolylinesData){
            index++;
            Log.e("mytag", "onPolylineClick: toString: " + polylineData.toString());
            if(polyline.getId().equals( polylineData.getPolyline().getId() )){
                polylineData.getPolyline().setColor( ContextCompat.getColor( this, R.color.blue ) );
                polylineData.getPolyline().setZIndex( 1 );

                LatLng endLocation = new LatLng(
                        polylineData.getLeg().endLocation.lat,
                        polylineData.getLeg().endLocation.lng
                );

                mRoute.setText("Route: " + index  );
                mDuration.setText("Duration: "+String.valueOf( polylineData.getLeg().duration )  );
                mDistance.setText("Distance: "+String.valueOf( polylineData.getLeg().distance )  );
                mapCardview.setVisibility( View.VISIBLE);

                mNavigate.setOnClickListener( new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(NavigationActivity.this);
                        builder.setMessage( "Open Google Maps?" )
                                .setCancelable( true )
                                .setPositiveButton( "Yes", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Uri gmmIntentUri = Uri.parse(
                                                "google.navigation:q="+polylineData.getLeg().endLocation.lat+","+
                                                polylineData.getLeg().endLocation.lng+"&mode=w");
                                        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                                        mapIntent.setPackage("com.google.android.apps.maps");
                                        startActivity(mapIntent);
                                        mapCardview.setVisibility( View.INVISIBLE);
                                    }
                                } )
                                .setNegativeButton( "No", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.cancel();
                                    }
                                } );
                        androidx.appcompat.app.AlertDialog alertDialog = builder.create();
                        alertDialog.show();

                    }
                } );
            }
            else {
                polylineData.getPolyline().setColor( ContextCompat.getColor( this, R.color.grey ) );
                polylineData.getPolyline().setZIndex( 0 );
            }
        }
    }

    @Override
    public void onInfoWindowClick(final Marker marker) {
        if (marker.getTitle().equals("I'm here" )){
            marker.hideInfoWindow();
        }
        else{
            final AlertDialog.Builder builder = new AlertDialog.Builder( this );
            builder.setMessage( "Do you want to navigate to " + marker.getTitle() + "?")
            .setCancelable( true )
            .setPositiveButton( "Yes",  new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mSelectedMarker = marker;
                    calculateDirections(marker);
                    dialog.dismiss();
                }
            } )
            .setNegativeButton( "No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            } );
            final AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        return true;
    }
}
