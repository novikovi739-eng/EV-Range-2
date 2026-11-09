package ru.ivan.evrangetest;

import android.content.Context;
import org.json.JSONArray;
import org.json.JSONObject;

/** Local event history. No credentials or location are stored here. */
public final class NotificationHistory {
 private static final String PREF="evrange_notifications";
 private static final String KEY="events";
 private static final int MAX=500;
 private static final long RETENTION=90L*24*60*60*1000;
 private NotificationHistory(){}
 public static synchronized JSONArray list(Context c){
  JSONArray source;try{source=new JSONArray(c.getSharedPreferences(PREF,0).getString(KEY,"[]"));}catch(Exception e){source=new JSONArray();}
  JSONArray valid=new JSONArray();long now=System.currentTimeMillis();
  for(int i=0;i<source.length()&&valid.length()<MAX;i++){
   JSONObject item=source.optJSONObject(i);if(item==null)continue;
   long t=item.optLong("detectedAt",0);if(t>0&&t<=now+60000&&now-t<=RETENTION)valid.put(item);
  }
  return valid;
 }
 private static void save(Context c,JSONArray a){c.getSharedPreferences(PREF,0).edit().putString(KEY,a.toString()).apply();}
 public static synchronized void add(Context c,String message){
  JSONArray old=list(c),next=new JSONArray();
  try{next.put(new JSONObject().put("id",System.currentTimeMillis()).put("detectedAt",System.currentTimeMillis()).put("text",message).put("read",false));}catch(Exception ignored){}
  for(int i=0;i<old.length()&&next.length()<MAX;i++)next.put(old.optJSONObject(i));save(c,next);
 }
 public static synchronized int unread(Context c){JSONArray a=list(c);int n=0;for(int i=0;i<a.length();i++)if(!a.optJSONObject(i).optBoolean("read",false))n++;return n;}
 public static synchronized void markAllRead(Context c){JSONArray a=list(c);for(int i=0;i<a.length();i++)try{a.getJSONObject(i).put("read",true);}catch(Exception ignored){}save(c,a);}
 public static synchronized void delete(Context c,long id){JSONArray a=list(c),b=new JSONArray();for(int i=0;i<a.length();i++){JSONObject v=a.optJSONObject(i);if(v!=null&&v.optLong("id")!=id)b.put(v);}save(c,b);}
 public static synchronized void clear(Context c){save(c,new JSONArray());}
}
