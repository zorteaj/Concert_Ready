package com.jzap.setlist.setlistui.SetListFm.Requestor;

import android.content.Context;
import android.os.Handler;

import com.jzap.setlist.setlistui.Config;
import com.jzap.setlist.setlistui.SetListFm.SetListFmAPIAdapter;

/**
 * Created by JZ_W541 on 9/29/2017.
 */

public abstract class SetListAPIRequestor
{
    private static final String TAG = Config.TAG_HEADER + "SetListAPIRqstr";

    protected SetListFmAPIAdapter mSetListFmAPIAdapter;
    private Handler mResponseHandler;

    public SetListAPIRequestor(Context context, Handler handler) {
        mSetListFmAPIAdapter = new SetListFmAPIAdapter(context);
        mResponseHandler = handler;
    }

    // Returns the runnable that executes the request
    protected abstract Runnable getRunnable();

    public void request() {
        new Thread(getRunnable()).start();
    }

    protected void deliverResponse()
    {
        mResponseHandler.obtainMessage(getResponseType(), getResponse()).sendToTarget();
    }

    protected abstract Object getResponse();

    protected abstract int getResponseType();
}
