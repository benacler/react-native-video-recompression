package com.facebook.react.bridge;

public interface ReadableMap {
    boolean hasKey(String name);
    String getString(String name);
    int getInt(String name);
    double getDouble(String name);
    boolean getBoolean(String name);
}
