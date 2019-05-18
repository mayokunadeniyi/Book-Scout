package utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

public class Prefs {
    private SharedPreferences sharedPreferences;

    public Prefs(Activity activity){

        sharedPreferences = activity.getPreferences(Context.MODE_PRIVATE);
    }

    public void setSearch(String search){
        sharedPreferences.edit().putString("search",search).commit();
    }

    public String getSearch(){
        return sharedPreferences.getString("search","Harry Potter");
    }

    public void setStart(Boolean value){
        sharedPreferences.edit().putBoolean("FirstTime",value).commit();
    }

    public boolean getStart(){
        return sharedPreferences.getBoolean("FirstTime",true);
    }

}
