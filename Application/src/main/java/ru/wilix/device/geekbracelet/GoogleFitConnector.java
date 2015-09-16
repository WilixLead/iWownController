package ru.wilix.device.geekbracelet;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;

import java.util.concurrent.TimeUnit;

import ru.wilix.device.geekbracelet.model.Sport;

public class GoogleFitConnector {
    private static final String TAG = "FitnessService";
    private static GoogleApiClient mClient;
    private static boolean authInProgress = false;

    private static Sport lastSport;

    public static void buildClient(final Context context){
        // Create the Google API Client
        mClient = new GoogleApiClient.Builder(context)
                .addApi(Fitness.HISTORY_API)
                .addScope(new Scope(Scopes.FITNESS_ACTIVITY_READ_WRITE))
                .addScope(new Scope(Scopes.FITNESS_LOCATION_READ_WRITE))
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle bundle) {
                        Log.i(TAG, "Connected!!!");
                        // Now you can make calls to the Fitness APIs.  What to do?
                        // Look at some data!!
                        SharedPreferences.Editor ed = App.sPref.edit();
                        ed.putBoolean("fit_connected", true);
                        ed.apply();

                        Intent intent = new Intent(BroadcastConstants.ACTION_CONNECT_TO_GFIT);
                        context.sendBroadcast(intent);
                    }

                    @Override
                    public void onConnectionSuspended(int i) {
                        // If your connection to the sensor gets lost at some point,
                        // you'll be able to determine the reason and react to it here.
                        if (i == GoogleApiClient.ConnectionCallbacks.CAUSE_NETWORK_LOST) {
                            Log.i(TAG, "Connection lost.  Cause: Network Lost.");
                        } else if (i == GoogleApiClient.ConnectionCallbacks.CAUSE_SERVICE_DISCONNECTED) {
                            Log.i(TAG, "Connection lost.  Reason: Service Disconnected");
                        }
                        SharedPreferences.Editor ed = App.sPref.edit();
                        ed.putBoolean("fit_connected", false);
                        ed.apply();

                        Intent intent = new Intent(BroadcastConstants.ACTION_CONNECT_TO_GFIT);
                        context.sendBroadcast(intent);
                    }
                })
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    // Called whenever the API client fails to connect.
                    @Override
                    public void onConnectionFailed(ConnectionResult result) {
                        Log.i(TAG, "Connection failed. Cause: " + result.toString());
                        if (!result.hasResolution()) {
                            SharedPreferences.Editor ed = App.sPref.edit();
                            ed.putBoolean("fit_connected", false);
                            ed.apply();
                            // Show the localized error dialog
                            if( context instanceof Activity )
                                GooglePlayServicesUtil.getErrorDialog(result.getErrorCode(),
                                        (Activity)context, 0).show();

                            Intent intent = new Intent(BroadcastConstants.ACTION_CONNECT_TO_GFIT);
                            context.sendBroadcast(intent);
                            return;
                        }
                        // The failure has a resolution. Resolve it.
                        // Called typically when the app is not yet authorized, and an
                        // authorization dialog is displayed to the user.
                        if (!authInProgress) {
                            try {
                                Log.i(TAG, "Attempting to resolve failed connection");
                                authInProgress = true;
                                connect(context);
                                if( context instanceof Activity )
                                    result.startResolutionForResult((Activity)context, 1);
                            } catch (IntentSender.SendIntentException e) {
                                Log.e(TAG, "Exception while starting resolution activity", e);
                            }
                        }
                    }
                }).build();
    }

    public static void connect(final Context context){
        if( mClient == null )
            buildClient(context);
        mClient.connect();
    }

    public static void publish(Sport sport){
        Log.i(TAG, "Creating a new data insert request");

        if( mClient == null || !mClient.isConnected() ){
            Log.i(TAG, "Fit client not connected. Try connect");
            connect(App.mContext);
            return;
        }

        new InsertTask().execute(sport);
    }

    private static class InsertTask extends AsyncTask<Sport, Void, Void> {
        protected Void doInBackground(Sport... sports) {
            Sport sport = sports[0];
            DataSource DSTEP_SOURCE = new DataSource.Builder()
                    .setAppPackageName(App.mContext.getPackageName())
                    .setDataType(DataType.TYPE_STEP_COUNT_DELTA)
                    .setName("GeekFit Steps")
                    .setStreamName("GeekFit Steps")
                    .setType(DataSource.TYPE_RAW)
                    .build();

            DataSource DDISTANCE_SOURCE = new DataSource.Builder()
                    .setAppPackageName(App.mContext.getPackageName())
                    .setDataType(DataType.TYPE_DISTANCE_DELTA)
                    .setName("GeekFit Distance")
                    .setStreamName("GeekFit Distance")
                    .setType(DataSource.TYPE_RAW)
                    .build();

            DataSource DCALORIE_SOURCE = new DataSource.Builder()
                    .setAppPackageName(App.mContext.getPackageName())
                    .setDataType(DataType.TYPE_CALORIES_EXPENDED)
                    .setName("GeekFit Calorie")
                    .setStreamName("GeekFit Calorie")
                    .setType(DataSource.TYPE_RAW)
                    .build();

            if( lastSport == null )
                lastSport = new Sport();

            // FIXME make only one request for insert data
            // Steps data
            DataSet dataSet = DataSet.create(DSTEP_SOURCE);
            DataPoint dataPoint = dataSet.createDataPoint();
            dataPoint.setTimestamp(sport.getTimestamp(), TimeUnit.MILLISECONDS);
            dataPoint.getValue(Field.FIELD_STEPS).setInt(sport.getSteps() - lastSport.getSteps());
            dataSet.add(dataPoint);
            Fitness.HistoryApi.insertData(mClient, dataSet);

            Log.i(TAG, "Inserting the dataset in the History API");
            com.google.android.gms.common.api.Status insertStatus =
                    Fitness.HistoryApi.insertData(mClient, dataSet).await(1, TimeUnit.MINUTES);

            // Before querying the data, check to see if the insertion succeeded.
            if (!insertStatus.isSuccess()) {
                Log.i(TAG, "There was a problem inserting the dataset.");
                return null;
            }

            // Distance data
            dataSet = DataSet.create(DDISTANCE_SOURCE);
            dataPoint = dataSet.createDataPoint();
            dataPoint.setTimestamp(sport.getTimestamp(), TimeUnit.MILLISECONDS);
            dataPoint.getValue(Field.FIELD_DISTANCE).setFloat(sport.getDistance() - lastSport.getDistance());
            dataSet.add(dataPoint);
            Fitness.HistoryApi.insertData(mClient, dataSet);

            Log.i(TAG, "Inserting the dataset in the History API");
            insertStatus = Fitness.HistoryApi.insertData(mClient, dataSet).await(1, TimeUnit.MINUTES);

            // Before querying the data, check to see if the insertion succeeded.
            if (!insertStatus.isSuccess()) {
                Log.i(TAG, "There was a problem inserting the dataset.");
                return null;
            }

            // Calorie data
            dataSet = DataSet.create(DCALORIE_SOURCE);
            dataPoint = dataSet.createDataPoint();
            dataPoint.setTimestamp(sport.getTimestamp(), TimeUnit.MILLISECONDS);
            dataPoint.getValue(Field.FIELD_CALORIES).setFloat(sport.getCalorie() - lastSport.getCalorie());
            dataSet.add(dataPoint);

            Log.i(TAG, "Inserting the dataset in the History API");
            insertStatus = Fitness.HistoryApi.insertData(mClient, dataSet).await(1, TimeUnit.MINUTES);

            // Before querying the data, check to see if the insertion succeeded.
            if (!insertStatus.isSuccess()) {
                Log.i(TAG, "There was a problem inserting the dataset.");
                return null;
            }

            lastSport = sport;
            Log.i(TAG, "Data insert was successful!");
            return null;
        }
    }
}