package com.myproject.joy.bartapplication;

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by Vamshik B D on 2/28/2018.
 */

public class App extends Application {

    public void onCreate(){
        super.onCreate();
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
}
