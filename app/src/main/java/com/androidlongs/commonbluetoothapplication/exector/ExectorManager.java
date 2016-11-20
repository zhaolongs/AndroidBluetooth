package com.androidlongs.commonbluetoothapplication.exector;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by androidlongs on 16/11/20.
 * 站在顶峰，看世界
 * 落在谷底，思人生
 */

public class ExectorManager {

    private ExecutorService mExecutorService;

    private ExectorManager(){
        initFunction();
    }
    private static class SingleExectorManager{
        private static ExectorManager sExectorManager = new ExectorManager();
    }

    public static  ExectorManager getInstance(){
        return SingleExectorManager.sExectorManager;
    }

    private void initFunction(){
        mExecutorService = Executors.newCachedThreadPool();
    }


    public void addTask(Runnable runnable){
        if (mExecutorService != null) {
            mExecutorService.execute(runnable);
        }
    }

}
