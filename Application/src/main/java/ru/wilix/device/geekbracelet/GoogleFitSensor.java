package ru.wilix.device.geekbracelet;

import android.util.Log;

import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Device;
import com.google.android.gms.fitness.service.FitnessSensorService;
import com.google.android.gms.fitness.service.FitnessSensorServiceRequest;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import ru.wilix.device.geekbracelet.model.Sport;

public class GoogleFitSensor extends FitnessSensorService{
    private static final String TAG = "FitnessService";
    private static FitnessSensorServiceRequest mRequest;
    private static HashMap<String, DataSource> dataSources = new HashMap<>();

    @Override
    public void onCreate() {
        super.onCreate();
        // 1. Initialize your software sensor(s).
        // 2. Create DataSource representations of your software sensor(s).
        // 3. Initialize some data structure to keep track of a registration for each sensor.
        Log.d(TAG, "Service Started!");

        dataSources = new HashMap<>();
        Device device = new Device("WiliX iWown Sensor", "WiliX", "WiliX", Device.TYPE_WATCH);

        dataSources.put("STEP", new DataSource.Builder()
                .setDataType(DataType.TYPE_STEP_COUNT_DELTA)
                .setName("iWown Step counter")
                .setType(DataSource.TYPE_DERIVED)
                .setDevice(device)
                .build());

        dataSources.put("DISTANCE", new DataSource.Builder()
                .setDataType(DataType.TYPE_DISTANCE_DELTA)
                .setName("iWown Distance counter")
                .setType(DataSource.TYPE_DERIVED)
                .setDevice(device)
                .build());

        dataSources.put("CALORIE", new DataSource.Builder()
                .setDataType(DataType.TYPE_CALORIES_EXPENDED)
                .setName("iWown Calories counter")
                .setType(DataSource.TYPE_DERIVED)
                .setDevice(device)
                .build());
    }

    public static void publishData(Sport sport){
        if( mRequest == null )
            return;

        try {
            long time = sport.getTimestamp();
            DataPoint point;
            ArrayList<DataPoint> dp = new ArrayList<>();

            point = DataPoint.create(dataSources.get("STEP"));
            point.setTimestamp(time, TimeUnit.MILLISECONDS);
            point.setIntValues(sport.getSteps());
            dp.add(point);

            point = DataPoint.create(dataSources.get("DISTANCE"));
            point.setTimestamp(time, TimeUnit.MILLISECONDS);
            point.setFloatValues(sport.getDistance());
            dp.add(point);

            point = DataPoint.create(dataSources.get("CALORIE"));
            point.setTimestamp(time, TimeUnit.MILLISECONDS);
            point.setFloatValues(sport.getCalorie());
            dp.add(point);

            mRequest.getDispatcher().publish(dp);
        }catch (Exception e){
            Log.e(TAG, "Publish fitness data problem");
            e.printStackTrace();
        }
    }

    @Override
    public List<DataSource> onFindDataSources(List<DataType> dataTypes) {
        // 1. Find which of your software sensors provide the data types requested.
        // 2. Return those as a list of DataSource objects.
        Log.d(TAG, "Google fit onFindDataSources");

        ArrayList<DataSource> ds = new ArrayList<>();
        for(DataSource source : dataSources.values())
            ds.add(source);

        return ds;
    }

    @Override
    public boolean onRegister(FitnessSensorServiceRequest request) {
        // 1. Determine which sensor to register with request.getDataSource().
        // 2. If a registration for this sensor already exists, replace it with this one.
        // 3. Keep (or update) a reference to the request object.
        // 4. Configure your sensor according to the request parameters.
        // 5. When the sensor has new data, deliver it to the platform by calling
        //    request.getDispatcher().publish(List<DataPoint> dataPoints)
        mRequest = request;
        if( mRequest != null ){
            if( BLEService.getSelf() != null && BLEService.getSelf().getDevice() != null ){
                BLEService.getSelf().getDevice().askDailyData();
                BLEService.getSelf().getDevice().subscribeForSportUpdates();
            }
        }
        Log.d(TAG, "Google fit request stored");
        return true;
    }

    @Override
    public boolean onUnregister(DataSource dataSource) {
        // 1. Configure this sensor to stop delivering data to the platform
        // 2. Discard the reference to the registration request object
        mRequest = null;
        Log.d(TAG, "Google fit request forgotten");
        return true;
    }
}