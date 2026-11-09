package ru.ivan.evrangetest;
import org.json.*;
final class Changes {
 static final String[] keys={"ignition","locked","charging","battery","battery_temp","temp_from_remote_control","board_network_voltage","odometer"};
 static final String[] labels={"Зажигание","Центральный замок","Зарядка","Заряд, %","ВВБ, °C","Салон, °C","АКБ, В","Пробег, км"};
 static JSONObject snapshot(JSONObject s)throws JSONException{JSONObject n=new JSONObject();for(String k:keys)if(s.opt(k) instanceof Boolean||Double.isFinite(RangeData.number(s,k)))n.put(k,s.opt(k));double speed=RangeData.number(s,"speed");if(s.opt("ignition") instanceof Boolean&&Double.isFinite(speed))n.put("moving",s.optBoolean("ignition")&&speed>.5);return n;}
 static String compare(JSONObject a,JSONObject b){StringBuilder out=new StringBuilder();for(int i=0;i<keys.length;i++){String k=keys[i];if(!a.has(k)||!b.has(k))continue;Object x=a.opt(k),y=b.opt(k);if(x instanceof Boolean&&y instanceof Boolean){if(!x.equals(y)){String yes=i==1?"закрыт":"включено",no=i==1?"открыт":"выключено";if(i==2){yes="идёт";no="не идёт";}out.append(labels[i]+": "+((Boolean)y?yes:no)+"\n");}}else{double old=RangeData.number(a,k),v=RangeData.number(b,k),threshold=k.equals("board_network_voltage")?.1:1;if(Double.isFinite(old)&&Double.isFinite(v)&&Math.abs(old-v)>=threshold-1e-8)out.append(labels[i]+": "+RangeData.fmt(old,k.equals("board_network_voltage")?2:0)+" → "+RangeData.fmt(v,k.equals("board_network_voltage")?2:0)+"\n");}}
 if(a.has("moving")&&b.has("moving")&&a.optBoolean("moving")!=b.optBoolean("moving"))out.append(b.optBoolean("moving")?"Обнаружено движение\n":"По показаниям движение прекратилось\n");return out.toString().trim();}
}
