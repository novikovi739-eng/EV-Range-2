package ru.ivan.evrangetest;
import org.json.*;
import java.util.*;

/** Calculations use only fields observed in the user's API responses. */
public final class RangeData {
 public static double number(JSONObject o,String k){if(o==null||o.isNull(k))return Double.NaN;Object v=o.opt(k);if(v instanceof Boolean)return Double.NaN;try{double n=Double.parseDouble(String.valueOf(v));return Double.isFinite(n)?n:Double.NaN;}catch(Exception e){return Double.NaN;}}
 public static JSONObject result(JSONObject j){return j==null?null:j.optJSONObject("result");}
 public static final class Stats {
  public int used,missing,noDrop,invalid,outside,duplicates;public double km,kwh;public boolean partial;
  public double average(){return km>0?kwh/km*100:Double.NaN;}
  public double range(double capacity,double soc,double reserve){return Double.isFinite(soc)&&soc>=0&&soc<=100&&capacity>0&&average()>0?capacity*Math.max(0,soc-reserve)/average():Double.NaN;}
  public int excluded(){return missing+noDrop+invalid+outside+duplicates;}
 }
 public static Stats calculate(JSONObject envelope,double capacity,long from,long until)throws JSONException {
  JSONArray rows=envelope.optJSONArray("result");if(rows==null)throw new JSONException("No result array");
  Stats s=new Stats();Set<String> ids=new HashSet<>();
  JSONObject page=envelope.optJSONObject("pagination");
  s.partial=envelope.optBoolean("has_more",false)||(page!=null&&(page.optBoolean("has_more",false)||page.optInt("total",rows.length())>rows.length()));
  for(int i=0;i<rows.length();i++){
   JSONObject r=rows.optJSONObject(i);if(r==null){s.invalid++;continue;}
   String id=r.isNull("id")?"":r.optString("id","");if(!id.isEmpty()&&!ids.add(id)){s.duplicates++;continue;}
   double start=number(r,"ride_start"),finish=number(r,"ride_finish");
   if(!Double.isFinite(start)||!Double.isFinite(finish)||finish<=start){s.invalid++;continue;}
   if(start<from||finish>until){s.outside++;continue;}
   double km=number(r,"odometer_mileage"),a=number(r,"soc_start"),b=number(r,"soc_end");
   if(!Double.isFinite(km)||!Double.isFinite(a)||!Double.isFinite(b)){s.missing++;continue;}
   if(km<=0||a<0||a>100||b<0||b>100||!Double.isFinite(capacity)||capacity<=0){s.invalid++;continue;}
   if(a<=b){s.noDrop++;continue;}
   s.used++;s.km+=km;s.kwh+=capacity*(a-b)/100;
  }
  return s;
 }
 public static String fmt(double n,int digits){return Double.isFinite(n)?String.format(new Locale("ru"),"%."+digits+"f",n):"—";}
 public static boolean fresh(double timestamp,long now){return Double.isFinite(timestamp)&&timestamp>0&&timestamp<=now+300&&now-timestamp<=900;}
}
