package com.neowine.fmanager;

import java.util.ArrayList;
class MyData {
    public ArrayList<Integer> imgs;
    public ArrayList<String>  strs;

    public MyData( ArrayList<Integer> timgs, ArrayList<String>  tstrs) {
        this.imgs = timgs;
        this.strs = tstrs;
    }
    public void add(int img, String text)
    {
        this.imgs.add(img);
        this.strs.add(text);
    }
}
