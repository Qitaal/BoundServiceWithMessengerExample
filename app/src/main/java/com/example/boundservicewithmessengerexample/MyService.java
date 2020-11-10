package com.example.boundservicewithmessengerexample;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.lang.ref.WeakReference;
import java.util.Random;

public class MyService extends Service implements ServiceHandlerCallback{

    public static final String KEY_RESULT = "KEY_RESULT";
    private static final String TAG = "MyService";
    public static final int GENERATE_NUMBER = 3;
    public static final int PROSSES_INPUT = 4;
    private Messenger myActivity; // MESSENGER BUAT REMOTE ACTIVITY
    public MyService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind: Service Binded");
        Toast.makeText(this, "Service Running", Toast.LENGTH_SHORT).show();

        // MESSENGER YANG DIKIRIM KE ACTIVITY BIAR ACTIVITY BISA AKSES SERVICE LEWAT IncomingHandler
        Messenger messenger = new Messenger(new IncomingHandler(this, this));

        // NGAMBIL MESSENGER YANG DIKIRIM DARI ACTIVITY
        myActivity = intent.getParcelableExtra(MainActivity.ACTIVITY_MESSENGER);
        return messenger.getBinder();
    }

    @Override
    public void onPrssesFinish(String result) {
        Message message = Message.obtain(null, MainActivity.UPDATE_RESULT);
        Bundle bundle = new Bundle();
        bundle.putString(KEY_RESULT, result);
        message.setData(bundle);
        try {
            myActivity.send(message);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: Service Unbind");
    }

    static class IncomingHandler extends Handler{
        private Context context;
        private WeakReference<ServiceHandlerCallback> callback;
        IncomingHandler(Context context, ServiceHandlerCallback callback){
            this.context = context;
            this.callback = new WeakReference<>(callback);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what){
                case GENERATE_NUMBER :
                    Random random = new Random();
                    int randomNumber = random.nextInt(100);
                    Log.d(TAG, "handleMessage: Random Number = " + randomNumber);
                    callback.get().onPrssesFinish(String.valueOf(randomNumber));
                    break;
                case PROSSES_INPUT :
                    String inputResult = msg.getData().getString(MainActivity.KEY_DATA_INPUT) + " Mantull";
                    Log.d(TAG, "handleMessage: Prosses Data Input = " + inputResult);
                    callback.get().onPrssesFinish(inputResult);
                default:
                    super.handleMessage(msg);
            }
        }
    }
}
interface ServiceHandlerCallback{
    void onPrssesFinish(String result);
}