package com.facebook.react.bridge;

import android.content.Context;

public abstract class ReactContextBaseJavaModule {
    protected ReactApplicationContext reactContext;
    
    public ReactContextBaseJavaModule(ReactApplicationContext reactContext) {
        this.reactContext = reactContext;
    }
    
    public abstract String getName();
}
