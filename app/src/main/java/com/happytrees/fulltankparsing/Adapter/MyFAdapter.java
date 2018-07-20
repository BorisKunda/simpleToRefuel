package com.happytrees.fulltankparsing.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.happytrees.fulltankparsing.Activities.MyMapActivity;
import com.happytrees.fulltankparsing.Objects.Station;
import com.happytrees.fulltankparsing.R;

import java.util.ArrayList;
import java.util.List;

public class MyFAdapter extends RecyclerView.Adapter<MyFAdapter.MyFViewHolder> {

    public List<Station> fStations;
    public Context fContext;
    public String fLatFromFavActivity;// -> pass latitude  from main activity to adapter using adapter's constructor
    public String fLngFromFavActivity;// -> pass longitude from main activity to adapter using adapter's constructor
    public float []  fDistanceResults = new float[10];//10 random number.you need any number higher than 3


    public MyFAdapter(List<Station> fStations, Context fContext, String fLatFromFavActivity, String fLngFromFavActivity) {
        this.fStations = fStations;
        this.fContext = fContext;
        this.fLatFromFavActivity = fLatFromFavActivity;
        this.fLngFromFavActivity = fLngFromFavActivity;
    }


    public MyFAdapter(ArrayList<Station> fStations, Context fContext) {
        this.fStations = fStations;
        this.fContext = fContext;
    }

    @NonNull
    @Override
    public MyFViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View fV = LayoutInflater.from(fContext).inflate(R.layout.station_item,null);
        MyFViewHolder myFViewHolder = new MyFViewHolder(fV);
        return myFViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull MyFViewHolder holder, int position) {
        Station fStation = fStations.get(position);
        holder.bindDataFromArrayToView(fStation);
    }

    @Override
    public int getItemCount() {
        return fStations.size();
    }




    //INNER CLASS
    public class MyFViewHolder extends RecyclerView.ViewHolder {
        View myFView;

        public MyFViewHolder(View itemView) {
            super(itemView);
            myFView = itemView;
        }

        double roundedDis;
        public void bindDataFromArrayToView(final Station currentFStation) {
            TextView fNameTV = myFView.findViewById(R.id.nameTV);
            fNameTV.setText(currentFStation.name);


            TextView priceTV1 = myFView.findViewById(R.id.price1);
            priceTV1.setText(currentFStation.price1);


            TextView priceTV2 = myFView.findViewById(R.id.price2);
            priceTV2.setText(currentFStation.price2);

            TextView priceTV3 = myFView.findViewById(R.id.price3);
            priceTV3.setText(currentFStation.price3);


            TextView distanceTV = myFView.findViewById(R.id.distanceValTV);


            if(currentFStation.placeLat.contains("unknown")||currentFStation.placeLng.contains("unknown")||fLatFromFavActivity.contains("unknown")||fLngFromFavActivity.contains("unknown") ) {
                distanceTV.setText("distance unknown");
            }else{
                double placeLatConvertedToDouble = Double.parseDouble(currentFStation.placeLat);
                double placeLngConvertedToDouble = Double.parseDouble(currentFStation.placeLng);
                double fLatFromFavActivityConverted = Double.parseDouble(fLatFromFavActivity);
                double fLngFromFavActivityConverted = Double.parseDouble(fLngFromFavActivity);
              Location.distanceBetween(fLatFromFavActivityConverted,fLngFromFavActivityConverted,placeLatConvertedToDouble,placeLngConvertedToDouble,fDistanceResults);//DEFAULT IN KILOMETERS
               roundedDis =  (double)Math.round( (fDistanceResults[0]/1000 ) * 100d) / 100d;//number of zeros must be same in and outside parenthesis.number of zeroes equals to number of numbers after dot that will remain after rounding up
                if(roundedDis>70) {
                    distanceTV.setText("distance unknown");
                }else{
                    distanceTV.setText(roundedDis + " KM" );
                }

            }




            ImageView stationFIV = myFView.findViewById(R.id.stationIV);


            final ProgressBar progressBar = myFView.findViewById(R.id.progress);
            progressBar.setVisibility(View.VISIBLE);////make progress bar visible




            Glide.with(fContext).load(currentFStation.urlImage).override(500, 500)
                    .centerCrop() .listener(new RequestListener<String, GlideDrawable>() {
                @Override
                public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                    progressBar.setVisibility(View.GONE);//removes progress bar if there was exception
                    return false;
                }

                @Override
                public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                    progressBar.setVisibility(View.GONE);////removes progress bar if picture finished loading
                    return false;

                }
            }).into(stationFIV);//SET IMAGE THROUGH GLIDE

            myFView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //open map
                    if(currentFStation.placeLat.contains("unknown")||currentFStation.placeLng.contains("unknown")){
                        Toast.makeText(fContext,"location unknown",Toast.LENGTH_SHORT).show();
                    }else{
                        Intent mapIntent = new Intent(fContext, MyMapActivity.class);
                        mapIntent.putExtra("ExtraLat", currentFStation.placeLat);
                        mapIntent.putExtra("ExtraLng", currentFStation.placeLng);
                        fContext.startActivity(mapIntent);
                    }
                }
            });

            myFView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(fContext);

                    builder.setNeutralButton("Go to destination", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //begin direction
                            if(currentFStation.placeLat.contains("unknown")||currentFStation.placeLng.contains("unknown")) {
                                Toast.makeText(fContext,"location unknown",Toast.LENGTH_SHORT).show();
                            }else{

                                String myUrl = "http://maps.google.com/maps?saddr="+ fLatFromFavActivity+","+fLngFromFavActivity+"&daddr="+currentFStation.placeLat + "," + currentFStation.placeLng +"&mode=driving";
                                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(myUrl));
                                fContext.startActivity(intent);
                            }

                        }
                    });

               /*     builder.setNegativeButton("exit", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }); */
                    builder.show();
                    return true ;
                }
            });
        }
    }

}
