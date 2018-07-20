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

import java.util.List;


public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {

    public List<Station> stations;
    public Context context;
    public double latFromMainActivity;// -> pass latitude  from main activity to adapter using adapter's constructor
    public double lngFromMainActivity;// -> pass longitude from main activity to adapter using adapter's constructor
    public float []  distanceResults = new float[10];//10 random number.you need any number higher than 3

    public MyAdapter(List<Station> stations, Context context, double latFromMainActivity, double lngFromMainActivity) {
        this.stations = stations;
        this.context = context;
        this.latFromMainActivity = latFromMainActivity;
        this.lngFromMainActivity = lngFromMainActivity;
    }

    public MyAdapter(List<Station> stations, Context context) {
        this.stations = stations;
        this.context = context;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.station_item, null);
        MyViewHolder myViewHolder = new MyViewHolder(v);
        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Station station = stations.get(position);
        holder.bindDataFromArrayToView(station);
    }

    @Override
    public int getItemCount() {
        return stations.size();
    }


    //INNER CLASS
    public class MyViewHolder extends RecyclerView.ViewHolder {
        View myView;

        public MyViewHolder(View itemView) {
            super(itemView);
            myView = itemView;
        }

        double roundedDis;

        public void bindDataFromArrayToView(final Station currentStation) {
            TextView nameTV = myView.findViewById(R.id.nameTV);
            nameTV.setText(currentStation.name);


            TextView priceTV1 = myView.findViewById(R.id.price1);
            priceTV1.setText(currentStation.price1);


            TextView priceTV2 = myView.findViewById(R.id.price2);
            priceTV2.setText(currentStation.price2);

            TextView priceTV3 = myView.findViewById(R.id.price3);
            priceTV3.setText(currentStation.price3);


            TextView distanceTV = myView.findViewById(R.id.distanceValTV);


            if(currentStation.placeLat.contains("unknown")||currentStation.placeLng.contains("unknown")) {
                distanceTV.setText("distance unknown");
            }else{
                double placeLatConvertedToDouble = Double.parseDouble(currentStation.placeLat);
                double placeLngConvertedToDouble = Double.parseDouble(currentStation.placeLng);
                Location.distanceBetween(latFromMainActivity,lngFromMainActivity,placeLatConvertedToDouble,placeLngConvertedToDouble,distanceResults);//DEFAULT IN KILOMETERS
                roundedDis =  (double)Math.round( (distanceResults[0]/1000 ) * 100d) / 100d;//number of zeros must be same in and outside parenthesis.number of zeroes equals to number of numbers after dot that will remain after rounding up
                if(roundedDis>70) {
                    distanceTV.setText("distance unknown");
                }else{
                    distanceTV.setText(roundedDis + " KM" );
                }

            }




            ImageView stationIV = myView.findViewById(R.id.stationIV);


            final ProgressBar progressBar = myView.findViewById(R.id.progress);
            progressBar.setVisibility(View.VISIBLE);////make progress bar visible




            Glide.with(context).load(currentStation.urlImage).override(500, 500)
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
            }).into(stationIV);//SET IMAGE THROUGH GLIDE


            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //open map
                    if(currentStation.placeLat.contains("unknown")||currentStation.placeLng.contains("unknown")||(roundedDis>20)){
                        Toast.makeText(context,"location unknown",Toast.LENGTH_SHORT).show();
                    }else{
                        Intent mapIntent = new Intent(context, MyMapActivity.class);
                        mapIntent.putExtra("ExtraLat", currentStation.placeLat);
                        mapIntent.putExtra("ExtraLng", currentStation.placeLng);
                        context.startActivity(mapIntent);
                    }


                }
            });


            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    //DIALOG - SAVE TO FAVOURITES OR OPEN NAVIGATION
                    //alert dialog
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setPositiveButton("Add to Favorites", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                           if(Station.find(Station.class,"Name =" + "'" + currentStation.name + "'").isEmpty()){//if it does empty there is no already existing record with this name you can proceed to save
                        //   Log.e("ok to save","ok to save");
                               Station station = new Station(currentStation.name, currentStation.price1, currentStation.price2, currentStation.price3, currentStation.urlImage,currentStation.placeLat,currentStation.placeLng);
                               station.save();
                               Toast.makeText(context, "Saved", Toast.LENGTH_SHORT).show();

                          }else{
                              Toast.makeText(context,"can't save duplicate locations",Toast.LENGTH_SHORT).show();

                           }


                                    //check if specific record already exists in database
   /*    if( Book.find(Book.class," TITLE = 'Harry potter' ").isEmpty()){// if row with title HarryPotter  returns true on isEmpty method then there no such row in database
         Log.e("true","true");
       }else{
           Log.e("false ","false");
       }  */


                       /*     Station station = new Station(currentStation.name, currentStation.price1, currentStation.price2, currentStation.price3, currentStation.urlImage,currentStation.placeLat,currentStation.placeLng);
                            station.save();
                            Toast.makeText(context, "Saved", Toast.LENGTH_SHORT).show();  */
                            //save to DB
                        }
                    });

                    builder.setNeutralButton("Go to destination", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //begin direction
                            if(currentStation.placeLat.contains("unknown")||currentStation.placeLng.contains("unknown")||(roundedDis>20)) {
                              Toast.makeText(context,"location unknown",Toast.LENGTH_SHORT).show();
                            }else{

                                String myUrl = "http://maps.google.com/maps?saddr="+latFromMainActivity+","+lngFromMainActivity+"&daddr="+currentStation.placeLat + "," + currentStation.placeLng +"&mode=driving";
                                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(myUrl));
                                context.startActivity(intent);
                            }

                        }
                    });

            /*        builder.setNegativeButton("exit", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }); */
                    builder.show();
                    return true;
                }
            });
        }
    }

}
