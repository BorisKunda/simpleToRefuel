package com.happytrees.fulltankparsing.Activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.happytrees.fulltankparsing.Adapter.MyAdapter;
import com.happytrees.fulltankparsing.Objects.Station;
import com.happytrees.fulltankparsing.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;

//when gps is turned off last known location is deleted

//CHECK THAT KEYS WORK
//create key for sima
//try app in different devices
//HOVOT :
//onNullInstance
//close keyboard after search
//hebrew support
//https://maps.googleapis.com/maps/api/place/textsearch/json?query=gas+station+Ten%20%D7%A8%D7%9E%D7%9C%D7%94&key=AIzaSyDo6e7ZL0HqkwaKN-GwKgqZnW03FhJNivQ
public class MainActivity extends AppCompatActivity implements LocationListener {

    private final static String START_STRING = " https://www.fulltank.co.il/?s= ";
    private final static String END_STRING = "&latitude=undefined&longitude=undefined&sort=cheapest";
    private final static String STRING1 = "&latitude=";
    private final static String STRING2 = "&longitude=";
    private final static String STRING3 = "&sort=cheapest";

    private final static int REQUEST_CODE_LOCATION = 1;
    public List<Station> allStations = new Vector<>();
    public RecyclerView myRecycler;
    public EditText cityET;
    public Button goBtn;
    public ProgressDialog progressDialog;
    LocationManager locationManager;
    Location lastKnowLoc;
    public Double lat;
    public Double lng;
    public Double historyLat;
    public Double historyLng;
    public boolean mLocationPermissionsGranted = false;

    //KEY -> boriskunda@gmail.com
    //https://www.fulltank.co.il/?s=jerusalem&latitude=31.8055944&longitude=35.2298522&sort=cheapest


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //delete database
        //Station.deleteAll(Station.class);

        //CHANGE ACTION BAR COLOR
        ActionBar bar = getSupportActionBar();
        bar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#C5CAE9")));

        //hides text from action bar
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        goBtn = findViewById(R.id.GoButton);


        myRecycler = findViewById(R.id.MyRecyclerView);
        myRecycler.setLayoutManager(new LinearLayoutManager(this));


        //PROGRESS BAR
        // Set up progress before call
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("loading....");
        // progressDialog.setTitle("ProgressDialog bar ");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);


        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        //check if GPS enabled

        //GPS CHECK
        gpsCheck();


        //PERMISSIONS CHECK IF ANDROID VERSION IS 6.0 OR ABOVE
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)//VERSION_CODES.M = Android 6.0  --> we check if our minimum sdk greater or equal to 6.0 (this when runtime permissions first took place)
        {
            //check location permission
            checkLocationPermission();
        } else {
            mLocationPermissionsGranted = true;
            //no need in permission  check  proceed to check location
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, MainActivity.this);
            lastKnowLoc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (lastKnowLoc != null) {
                lat = lastKnowLoc.getLatitude();
                lng = lastKnowLoc.getLongitude();
                Log.e("location ", " location   " + lastKnowLoc.getLatitude() + "  " + lastKnowLoc.getLongitude());
            }


        }


        //FIX -> don't show results if location null
        goBtn.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void onClick(View v) {
                //////////////////////////////////////////////////////////////////////


                //opening new thread for using network
                progressDialog.show();//SHOW PROGRESS BAR BEFORE CALL
                new Thread(new Runnable() {
                    MyAdapter myAdapter;

                    @SuppressLint("MissingPermission")
                    @Override
                    public void run() {
                        Log.e("app", "step 1");
                        String cityImproved = "jerusalem";
                        // String fullUrl = START_STRING + cityImproved + END_STRING;
                        String fullUrl;

                        Log.e("p", "p");

                        if (mLocationPermissionsGranted) {
                            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, MainActivity.this);
                            lastKnowLoc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            if (lastKnowLoc != null) {
                                lat = lastKnowLoc.getLatitude();
                                lng = lastKnowLoc.getLongitude();
                                historyLat = lat;
                                historyLat = lng;
                            }
                            // Log.e("p","p");
                        }

                        if (lastKnowLoc != null) {
                            //if there was received location use this link --> https://www.fulltank.co.il/?s=PLACE&latitude=VALUE&longitude=VALUE&sort=cheapest
                            lat = lastKnowLoc.getLatitude();
                            lng = lastKnowLoc.getLongitude();
                            historyLat = lat;
                            historyLng = lng;

                            myAdapter = new MyAdapter(allStations, MainActivity.this, lat, lng);
                            myRecycler.post(new Runnable() {
                                @Override
                                public void run() {
                                    myRecycler.setAdapter(myAdapter);
                                }
                            });


                            //convert lat to String
                            String myLat = String.valueOf(lat);
                            String myLng = String.valueOf(lng);
                            fullUrl = START_STRING + cityImproved + STRING1 + myLat + STRING2 + myLng + STRING3;


                        } else {
                            if ((historyLat != null) && (historyLng != null)) {
                                fullUrl = START_STRING + cityImproved + STRING1 + historyLat + STRING2 + historyLng + STRING3;
                            } else {
                                //if there is no latitude and longitude received use alternative link  --> https://www.fulltank.co.il/?s=PLACE&latitude=undefined&longitude=undefined&sort=cheapest
                                fullUrl = START_STRING + cityImproved + END_STRING;
                            }

                            myAdapter = new MyAdapter(allStations, MainActivity.this);
                            myRecycler.post(new Runnable() {
                                @Override
                                public void run() {
                                    myRecycler.setAdapter(myAdapter);
                                }
                            });
                        }

                        //downloading html and keeping it under "line" variable
                        InputStream is = null;
                        try {
                            URL url = new URL(fullUrl);
                            is = url.openStream();  // throws an IOException
                            BufferedReader br = new BufferedReader(new InputStreamReader(is));
                            String currentLine;
                            String line = " ";
                            //while loop stops when currentLine becomes null ,so we keep whole growing String  under line variable
                            while ((currentLine = br.readLine()) != null) {
                                line += currentLine;
                            }

                            Log.e("app", "step 2");
                            //parsing html
                            Document parsedDocument = Jsoup.parse(line);
                            //selecting names from  html -->    <h2><a href="https://www.fulltank.co.il/station/296/דלק/זטלר">NAME</a></h2>

                            //NAMES
                            Elements myElements = parsedDocument.select("h2 > a");
                            final List<String> names = new ArrayList<>();
                            for (Element myElement : myElements) {
                                //getting station's name
                                String name = myElement.ownText();
                                // Log.e("name","name :  " + name);
                                //keeping all names under array of names
                                names.add(name);
                            }

                            //PRICES
                            //<div class=""><span class="search-data-num">6.37</span> ₪</div>
                            //selecting prices from html
                            Elements pricesElements = parsedDocument.select("span.search-data-num");
                            final ArrayList<String> prices = new ArrayList<>();
                            for (Element priceElement : pricesElements) {
                                String price = priceElement.ownText();
                                //keeping all prices under array of prices
                                prices.add(price);
                            }

                            //IMAGES
                                /*
                                  <figure class="search-figure">
                                           <a href="https://www.fulltank.co.il/station/415/Ten/ירושלים%20תלפיות"><img src="https://www.fulltank.co.il/wp-content/uploads/2015/08/TenPT@2x.png" width="267" height="193"></a>
                                 </figure>

                                 */

                            Elements urlImages = parsedDocument.select("figure.search-figure>a>img");
                            final ArrayList<String> urlImgs = new ArrayList<>();
                            for (Element urlElement : urlImages) {
                                //getting value  --> ("src") <-- <img src="https://maps.googleapis.com/maps/api/streetview?size=260x150&location=31.749428,35.206287" >
                                String urlImg = urlElement.attr("src");
                                urlImgs.add(urlImg);
                            }

                            //delete old results if exist
                            if (allStations != null) {
                                allStations.clear();
                            }

                            Log.e("app", "step 3");


//                                final ArrayList<String> googleLats = new ArrayList<>();
//                                final ArrayList<String> googleLngs = new ArrayList<>();
                            //https://maps.googleapis.com/maps/api/place/textsearch/json?query=gas+station+Ten&key=AIzaSyDo6e7ZL0HqkwaKN-GwKgqZnW03FhJNivQ


                            for (int n = 0; n < names.size(); n++) {
                                //run on array of names ,insert name into google link respectively
                                /////////////////////////////////////////////////
                                final int myIndex = n;
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        String googlePlacesStart = "https://maps.googleapis.com/maps/api/place/textsearch/json?query=gas+station+";
                                        String googlePlaceName = names.get(myIndex);
                                        String googlePlaceNameFixed = googlePlaceName.replace(" ", "+");
                                        String googlePlacesStartEnd = "&key=AIzaSyBf2hEYs5ocCd89aBICOiR5Dmh3ufaWtbA";
                                        String fullGoogleLink = googlePlacesStart + googlePlaceNameFixed + googlePlacesStartEnd;


                                        Log.e("app", "step 4");

                                        //download http from url and keep it under String
                                        InputStream inputStreamFromGoogle = null;

                                        try {
                                            URL googleUrl = new URL(fullGoogleLink);
                                            inputStreamFromGoogle = googleUrl.openStream();
                                            BufferedReader googleBufferedReader = new BufferedReader(new InputStreamReader(inputStreamFromGoogle));
                                            String currentLineFromGoogle;
                                            String lineGoogle = " ";
                                            //while loop stops when currentLine becomes null ,so we keep whole growing String  under line variable
                                            while ((currentLineFromGoogle = googleBufferedReader.readLine()) != null) {
                                                lineGoogle += currentLineFromGoogle;
                                            }

                                            Log.e("url", "url " + myIndex);
                                            String latFromJson;
                                            String lngFromJson;
                                            //json parsing
                                            try {
                                                //JSON parsing
                                                JSONObject jsonObjectContainer = new JSONObject(lineGoogle);
                                                JSONArray jsonArrayResults = jsonObjectContainer.getJSONArray("results");//"results" is name of array of movies in json link
                                                JSONObject resultObject = jsonArrayResults.getJSONObject(0);
                                                JSONObject geometryObject = resultObject.getJSONObject("geometry");
                                                JSONObject locationObject = geometryObject.getJSONObject("location");
                                                Log.e("TEST", "TEST: " + myIndex);

                                                if (locationObject != null) {
                                                    latFromJson = locationObject.getString("lat");
                                                    lngFromJson = locationObject.getString("lng");
                                                } else {
                                                    latFromJson = "unknown";
                                                    lngFromJson = "unknown";
                                                }
                                                Station station = new Station(googlePlaceName, prices.get(myIndex * 3), prices.get((myIndex * 3) + 1), prices.get((myIndex * 3) + 2), urlImgs.get(myIndex), latFromJson, lngFromJson);
                                                allStations.add(station);


                                             if (allStations.size() == names.size()) { //if they do equal then all threads finished their work
                                                    myRecycler.post(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                      for (int i = allStations.size() - 1; i >= 0; i--) {
                                                                if (allStations.get(i).placeLat.contains("unknown")) {
                                                                    allStations.remove(i);
                                                                }
                                                            }

                                                 Collections.sort(allStations, new Comparator<Station>() {
                                                                @Override
                                                                public int compare(Station s1, Station s2) {
                                                                 if( s1.placeLat.contains("unknown")){
                                                                     return -1;
                                                                 }
                                                                 if(s2.placeLat.contains("unknown")){
                                                                     return  1;
                                                                 }
                                                                    if (getDistanceInKM(s1, lat, lng) > getDistanceInKM(s2, lat, lng))
                                                                        return 1;
                                                                    return -1;
                                                                }
                                                            });
                                                            myAdapter.notifyDataSetChanged();
                                                            progressDialog.dismiss();//dismiss progress bar after call was completed
                                                      }
                                                           });
                                                }

                                            } catch (JSONException e) {
                                                latFromJson = "unknown";
                                                lngFromJson = "unknown";
                                                Station station = new Station(googlePlaceName, prices.get(myIndex * 3), prices.get((myIndex * 3) + 1), prices.get((myIndex * 3) + 2), urlImgs.get(myIndex), latFromJson, lngFromJson);
                                                allStations.add(station);
                                                Log.e("JsonException", "JsonException" + myIndex);

                                            }


                                        } catch (MalformedURLException mue) {
                                            mue.printStackTrace();
                                            Log.e("MalformedURLException", "MalformedURLException");
                                        } catch (IOException ioe) {
                                            Log.e("IOException", "IOException1 ");
                                            ioe.printStackTrace();
                                        } finally {
                                            try {
                                                if (inputStreamFromGoogle != null)
                                                    inputStreamFromGoogle.close();
                                            } catch (IOException ioe) {
                                                Log.e("IOException", "IOException2 ");
                                            }

                                        }
                                        Log.e("T", "T  " + myIndex);
                                    }
                                }).start();
                            }

//                                Log.e("arraylat", "arraylat" + googleLats);
//                                Log.e("arraylat", "arraylat" + googleLngs);

                        } catch (MalformedURLException mue) {
                            mue.printStackTrace();
                        } catch (IOException ioe) {
                            ioe.printStackTrace();
                        } finally {
                            try {
                                if (is != null)
                                    is.close();
                            } catch (IOException ioe) {
                            }
//////////////////////////////////////////////////////////////////////////
                        }

                        //post to UI (main thread) through post method .without post method there will be CalledFromWrongThreadException
                      /*      myRecycler.post(new Runnable() {//alternatively use  runOnUiThread();
                                @Override
                                public void run() {
                                    //remove all unknown entries from array and display only known ones(in order  prevent index mistakes  you need iterate loop backwards(i--) when removing  )
                                    for (int i = allStations.size() - 1; i >= 0; i--) {
                                        if (allStations.get(i).placeLat.contains("unknown")) {
                                            allStations.remove(i);
                                        }
                                    }
                                    myAdapter.notifyDataSetChanged();
                                    progressDialog.dismiss();//dismiss progress bar after call was completed


                                }
                            }); */
                    }
                }).start();


///////////////////////////////////////////////////////////////////////////////
            }
        });


    }

    @Override
    public void onLocationChanged(Location location) {
        //Will be called every time location gets updated
        if (mLocationPermissionsGranted) {
            lat = location.getLatitude();
            lng = location.getLongitude();
            historyLat = lat;
            historyLng = lng;
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {//when accuracy changes
        //do nothing
    }

    @Override
    public void onProviderEnabled(String provider) {//if the user turns on the provider (GPS)
        Toast.makeText(MainActivity.this, " thank you :) ", Toast.LENGTH_SHORT).show();
        // Log.e("GPS", "ENABLED");
    }

    @Override
    public void onProviderDisabled(String provider) {//user disabled GPS
        //request user enable gps
        LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);//dialog warning that gps disabled
            builder.setMessage("Your GPS seems to be disabled, please  enable it?").setCancelable(false).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                }
            });

            builder.show();
        }
    }

    public void gpsCheck() {
        LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);//dialog warning that gps disabled
            builder.setMessage("Your GPS seems to be disabled, please  enable it?").setCancelable(false).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                }
            });

            builder.show();//don't forget otherwise dialog wont show
        } else {
            //  Log.e("GPS", "ENABLED");
        }
    }


    //METHOD CHECKS LOCATION PERMISSION
    public void checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {//if there wasn't already permission granted
            //if there no permission enabled we can request the permission.
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE_LOCATION);
        } else {
            //if there is already permission granted request location update
            mLocationPermissionsGranted = true;
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, MainActivity.this);
            lastKnowLoc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (lastKnowLoc != null) {
                lat = lastKnowLoc.getLatitude();
                lng = lastKnowLoc.getLongitude();
                historyLat = lat;
                historyLng = lng;
                Log.e("location ", " location   " + lastKnowLoc.getLatitude() + "  " + lastKnowLoc.getLongitude());
            }
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == REQUEST_CODE_LOCATION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //if there is  permission granted request location update
                mLocationPermissionsGranted = true;
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, MainActivity.this);
                lastKnowLoc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (lastKnowLoc != null) {
                    lat = lastKnowLoc.getLatitude();
                    lng = lastKnowLoc.getLongitude();
                    lat = historyLat;
                    lng = historyLng;
                    Log.e("location ", " location   " + lastKnowLoc.getLatitude() + "  " + lastKnowLoc.getLongitude());
                }


            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE_LOCATION);
            }
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.my_menu, menu);//inflate menu via xml template
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.favouriteMenuItem:
                Intent fIntent = new Intent(MainActivity.this, FavouritesActivity.class);
                if (lastKnowLoc == null) {
                    String unkown = "unknown";
                    fIntent.putExtra("latMainToFav", unkown);
                    fIntent.putExtra("lngMainToFav", unkown);
                } else {
                    double latNext = lastKnowLoc.getLatitude();
                    double lngNext = lastKnowLoc.getLongitude();
                    String stringMainLatToFav = String.valueOf(latNext);
                    String stringMainLngToFav = String.valueOf(lngNext);
                    fIntent.putExtra("latMainToFav", stringMainLatToFav);
                    fIntent.putExtra("lngMainToFav", stringMainLngToFav);
                }
                startActivity(fIntent);
                break;

            case R.id.exitMenuItem:
                ActivityCompat.finishAffinity(MainActivity.this);//closes all activities at time.for API 4.1+ (in this case 4.3+) you can use "finishAffinity();"
                break;
        }
        return true;
    }


    private static double getDistanceInKM(Station s, double myLat, double myLng) {
        float[] fDistanceResults = new float[10];
        double placeLatConvertedToDouble = Double.parseDouble(s.placeLat);
        double placeLngConvertedToDouble = Double.parseDouble(s.placeLng);
        Location.distanceBetween(myLat, myLng, placeLatConvertedToDouble, placeLngConvertedToDouble, fDistanceResults);//DEFAULT IN KILOMETERS
        return (double) Math.round((fDistanceResults[0] / 1000) * 100d) / 100d;//number of zeros must be same in and outside parenthesis.number of zeroes equals to number of numbers after dot that will remain after rounding up
    }
}


//Runnable ->  we create a new Thread to download the content of the website.
// connect() ->  connects the application to the website
// get() ->  method to download the content. These calls return a Document object instance.
//element.ownText() -> gets element's content
//Jsoup.parse -> when we are dealing with raw string html text
//Jsoup.connect -> when we are dealing with url HTML link

