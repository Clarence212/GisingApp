package com.example.gisingv3;

import android.content.Context;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class AlarmStorage {

    private static final String FILE_NAME = "alarms.json";

    public static void saveAlarms(Context context, List<Alarm> alarms) {
        Gson gson = new Gson();
        String json = gson.toJson(alarms);
        
        try (FileOutputStream fos = context.openFileOutput(FILE_NAME, Context.MODE_PRIVATE)) {
            fos.write(json.getBytes());
        } catch (Exception e) {
            e.printStackTrace(); // In a real app, handle this more gracefully
        }
    }

    public static ArrayList<Alarm> loadAlarms(Context context) {
        File file = new File(context.getFilesDir(), FILE_NAME);
        if (!file.exists()) {
            return new ArrayList<>(); // Return an empty list if the file doesn't exist
        }

        try (FileInputStream fis = context.openFileInput(FILE_NAME);
             InputStreamReader isr = new InputStreamReader(fis);
             BufferedReader bufferedReader = new BufferedReader(isr)) {

            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line);
            }

            Gson gson = new Gson();
            Type type = new TypeToken<ArrayList<Alarm>>() {}.getType();
            ArrayList<Alarm> alarms = gson.fromJson(sb.toString(), type);
            
            return alarms != null ? alarms : new ArrayList<>();
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>(); // Return empty list on error
        }
    }
}