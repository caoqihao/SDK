package com.shafa.market.library.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class UPreference {

	public static SharedPreferences getPreference(Context context){
		return PreferenceManager.getDefaultSharedPreferences(context);
	}
	
	public static boolean putString(Context context, String key, String value){
		return PreferenceManager.getDefaultSharedPreferences(context).edit().putString(key, value).commit();
	}
	
	public static boolean putBoolean(Context context, String key, boolean value){
		return PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(key, value).commit();
	}
	
	public static boolean putFloat(Context context, String key, float value){
		return PreferenceManager.getDefaultSharedPreferences(context).edit().putFloat(key, value).commit();
	}
	
	public static boolean putLong(Context context, String key, long value){
		return PreferenceManager.getDefaultSharedPreferences(context).edit().putLong(key, value).commit();
	}
	
	public static boolean putInt(Context context, String key, int value){
		 return PreferenceManager.getDefaultSharedPreferences(context).edit().putInt(key, value).commit();
	}
	
	public static String getString(Context context, String key, String defValue){
		 return PreferenceManager.getDefaultSharedPreferences(context).getString(key, defValue);
	}
	
	public static int getInt(Context context, String key, int defValue){
		 return PreferenceManager.getDefaultSharedPreferences(context).getInt(key, defValue);
	}
	
	public static float getFloat(Context context, String key, float defValue){
		 return PreferenceManager.getDefaultSharedPreferences(context).getFloat(key, defValue);
	}
	
	public static long getLong(Context context, String key, long defValue){
		 return PreferenceManager.getDefaultSharedPreferences(context).getLong(key, defValue);
	}
	
	public static boolean getBoolean(Context context, String key, boolean defValue){
		 return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(key, defValue);
	}
}
