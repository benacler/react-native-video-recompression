package com.facebook.react.bridge;

import android.content.Context;

public class ReactApplicationContext {
    private Context context;
    
    public ReactApplicationContext(Context context) {
        this.context = context;
    }
    
    public Context getApplicationContext() {
        return context;
    }
}
