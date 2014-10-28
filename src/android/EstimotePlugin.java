package com.estimote.sdk;

import java.util.List;

import org.apache.cordova.PluginResult;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

import android.os.RemoteException;
import android.util.Log;

import com.estimote.sdk.Region;
import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;

public class EstimotePlugin extends CordovaPlugin {

    private static final String ESTIMOTE_PROXIMITY_UUID = "B9407F30-F5F8-466E-AFF9-25556B57FE6D";

    private Region region;

    private static final String LOG_TAG	= "EstimotePlugin";
    private static final String ACTION_START_RANGING = "startRanging";
    private static final String ACTION_STOP_RANGING = "stopRanging";

    private CallbackContext rangingCallback;
    private BeaconManager beaconManager;

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        try {
            if (action.equals(ACTION_START_RANGING)){
                startRanging(args, callbackContext);
            }
            else if (action.equals(ACTION_STOP_RANGING)){
                stopRanging(args, callbackContext);
            }
        } catch (Exception e) {
            callbackContext.error(e.getMessage());
            return false;
        }
        return true;
    }

    private void startRanging(JSONArray args, final CallbackContext callbackCtx) throws JSONException
    {
        region =  new Region(args.getString(0), ESTIMOTE_PROXIMITY_UUID, null, null);

        Log.d(LOG_TAG, "startRanging-method called");

        rangingCallback = callbackCtx;
        try
        {
            beaconManager = new BeaconManager(cordova.getActivity().getBaseContext());

            beaconManager.setRangingListener(new BeaconManager.RangingListener() {
                @Override
                public void onBeaconsDiscovered(Region region, List<Beacon> beacons) {
                    Log.d(LOG_TAG, "Ranged beacons: " + beacons);
                    for(Beacon b: beacons) {
                        try {
                            String name = b.getName();
                            String address = b.getMacAddress();
                            String proximityUUID = b.getProximityUUID();
                            JSONObject device = new JSONObject();
                            device.put("name", name);
                            device.put("address", address);
                            device.put("proximityUUID", proximityUUID);
                            device.put("major", b.getMajor());
                            device.put("minor", b.getMinor());
                            device.put("macAddress", b.getMacAddress());

                            // Send one device at a time, keeping callback to be used again
                            if(rangingCallback != null) {
                                PluginResult result = new PluginResult(PluginResult.Status.OK, device);
                                result.setKeepCallback(true);
                                rangingCallback.sendPluginResult(result);
                            } else {
                                Log.e(LOG_TAG, "CallbackContext for discovery doesn't exist.");
                            }
                        } catch(JSONException e) {
                            if(rangingCallback != null) {
                                EstimotePlugin.this.error(rangingCallback,
                                        e.getMessage(),
                                        BluetoothError.ERR_UNKNOWN
                                );
                                rangingCallback = null;
                            }
                        }
                    }
                }
            });

            beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
                @Override
                public void onServiceReady() {
                    try {
                        beaconManager.startRanging(region);
                        JSONObject event = new JSONObject();
                        event.put("event", "connected");
                        PluginResult result = new PluginResult(PluginResult.Status.OK, event);
                        result.setKeepCallback(true);
                        rangingCallback.sendPluginResult(result);
                    } catch (Throwable e) {
                        Log.e(LOG_TAG, "Cannot start ranging", e);
                        EstimotePlugin.this.error(callbackCtx, "Cannot start ranging::" + e.getMessage(), BluetoothError.ERR_UNKNOWN);
                    }
                }
            });
        }
        catch(Exception e)
        {
        	this.error(callbackCtx, "Outer exception handler. " + e.getMessage(), BluetoothError.ERR_UNKNOWN);
        }
    }

    private void stopRanging(JSONArray args, final CallbackContext callbackCtx) throws RemoteException
    {
    	if (beaconManager != null){
    		beaconManager.stopRanging(region);
    	}
    }

	/**
	 * Send an error to given CallbackContext containing the error code and message.
	 *
	 * @param ctx	Where to send the error.
	 * @param msg	What seems to be the problem.
	 * @param code	Integer value as a an error "code"
	 */
	private void error(CallbackContext ctx, String msg, int code)
	{
		try
		{
			JSONObject result = new JSONObject();
			result.put("message", msg);
			result.put("code", code);

			ctx.error(result);
		}
		catch(Exception e)
		{
			Log.e(LOG_TAG, "Error with... error raising, " + e.getMessage());
		}
	}
}
