package my.assignment.androidwebservice;

import android.app.Activity;
import android.content.SharedPreferences;

/**
 * Created by root on 9/22/16.
 */

public class CityPreference {
    SharedPreferences prefs;
    public CityPreference(Activity activity){
        prefs=activity.getPreferences(Activity.MODE_PRIVATE);
    }
    public String getCity(){
        return prefs.getString("city","Hyderbad");
    }
    public void setCity(String city){
        prefs.edit().putString("city",city).commit();
    }
}
