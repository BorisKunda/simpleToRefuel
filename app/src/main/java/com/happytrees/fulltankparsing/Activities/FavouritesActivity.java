package com.happytrees.fulltankparsing.Activities;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.widget.Toast;

import com.happytrees.fulltankparsing.Adapter.MyAdapter;
import com.happytrees.fulltankparsing.Adapter.MyFAdapter;
import com.happytrees.fulltankparsing.Objects.Station;
import com.happytrees.fulltankparsing.R;


import java.util.ArrayList;
import java.util.List;


public class FavouritesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favourites);


        //CHANGE ACTION BAR COLOR
        ActionBar bar =  getSupportActionBar();
        bar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#C5CAE9")));

        //hides text from action bar
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        Intent intentFromMain = getIntent();//gets object passed by intent from previous activity
        String latPassedFromMain = intentFromMain.getStringExtra("latMainToFav");
        String lngPassedFromMain = intentFromMain.getStringExtra("lngMainToFav");

        final List<Station> allFavourites = Station.listAll(Station.class);
        RecyclerView recyclerView = findViewById(R.id.recyclerFavourites);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(FavouritesActivity.this);//layout manager defines look of RecyclerView -- > grid,list
        recyclerView.setLayoutManager(layoutManager);
        //adapter
        final MyFAdapter myFAdapter = new MyFAdapter((List<Station>) allFavourites,FavouritesActivity.this,latPassedFromMain,lngPassedFromMain);
        recyclerView.setAdapter(myFAdapter);



        //REMOVE ON SWEEP
        //INTERFACE ALLOWING LISTENING TO SWEEP ACTIONS
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0,ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {//REMOVE ON  LEFT/RIGHT SWIPE
                //fetch item position
                int position = viewHolder.getAdapterPosition();
                //remove item from database
                Station fStation = Station.findById(Station.class,allFavourites.get(position).getId());
                fStation.delete();
                //remove from list
                allFavourites.remove(position);
                myFAdapter.notifyItemRemoved(position);
                myFAdapter.notifyItemRangeChanged(position,allFavourites.size());//we used  "getAdapterPosition()" to get item  position (int)

                Toast.makeText(FavouritesActivity.this,"item removed",Toast.LENGTH_SHORT).show();
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView); //set swipe to recylcerview

    }
}
