package com.happytrees.fulltankparsing.Objects;

import com.orm.SugarRecord;

public class Station extends SugarRecord{
    public String name;
    public String price1;
    public String price2;
    public String price3;
    public String urlImage;
    public String placeLat;
    public String placeLng;


    public Station(String name, String price1, String price2, String price3, String urlImage, String placeLat, String placeLng) {
        this.name = name;
        this.price1 = price1;
        this.price2 = price2;
        this.price3 = price3;
        this.urlImage = urlImage;
        this.placeLat = placeLat;
        this.placeLng = placeLng;
    }

    //required constructor
    public Station() {
    }



}
