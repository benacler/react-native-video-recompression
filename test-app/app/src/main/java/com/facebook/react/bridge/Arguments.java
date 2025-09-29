package com.facebook.react.bridge;

import java.util.HashMap;
import java.util.Map;

public class Arguments {
    public static WritableMap createMap() {
        return new WritableMapImpl();
    }
    
    private static class WritableMapImpl implements WritableMap, ReadableMap {
        private Map<String, Object> map = new HashMap<>();
        
        @Override
        public void putString(String key, String value) { map.put(key, value); }
        
        @Override
        public void putInt(String key, int value) { map.put(key, value); }
        
        @Override
        public void putDouble(String key, double value) { map.put(key, value); }
        
        @Override
        public void putBoolean(String key, boolean value) { map.put(key, value); }
        
        @Override
        public boolean hasKey(String name) { return map.containsKey(name); }
        
        @Override
        public String getString(String name) { 
            Object value = map.get(name);
            return value != null ? value.toString() : null;
        }
        
        @Override
        public int getInt(String name) { 
            Object value = map.get(name);
            if (value instanceof Integer) return (Integer) value;
            if (value instanceof String) return Integer.parseInt((String) value);
            return 0;
        }
        
        @Override
        public double getDouble(String name) { 
            Object value = map.get(name);
            if (value instanceof Double) return (Double) value;
            if (value instanceof Float) return ((Float) value).doubleValue();
            if (value instanceof String) return Double.parseDouble((String) value);
            return 0.0;
        }
        
        @Override
        public boolean getBoolean(String name) { 
            Object value = map.get(name);
            if (value instanceof Boolean) return (Boolean) value;
            if (value instanceof String) return Boolean.parseBoolean((String) value);
            return false;
        }
    }
}
