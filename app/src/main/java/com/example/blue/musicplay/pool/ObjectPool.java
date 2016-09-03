package com.example.blue.musicplay.pool;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by blue on 2016/8/24.
 */

public class ObjectPool {
    private static ObjectPool mInstance;
    private Map<String,Object> objectMap = new HashMap<String,Object>();
    public static ObjectPool getInstance() {
        if (mInstance == null) {
            synchronized (ThreadPool.class) {
                if (mInstance == null) {
                    mInstance = new ObjectPool();
                }
            }
        }
        return mInstance;
    }
    public void creatObject(String objectName,Object object) {
        objectMap.put(objectName,object);
    }
    public Object getObject(String objectName) {
        return objectMap.get(objectName);
    }
    public void destroyObject(String objectName) {
       Object ob = objectMap.get(objectName);
        ob = null;
    }
}
