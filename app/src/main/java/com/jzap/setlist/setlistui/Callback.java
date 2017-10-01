package com.jzap.setlist.setlistui;

/**
 * Created by JZ_W541 on 9/28/2017.
 */

public interface Callback {
    int TOUR_NAME = 0;
    int SETLIST = 1;
    int ARTISTS = 2;
    void call(int what, Object obj);
}