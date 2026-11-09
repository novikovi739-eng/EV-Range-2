package ru.ivan.evrangetest;
import android.app.Activity;
import android.os.Bundle;
import android.widget.*;
import android.text.InputType;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import org.json.*;
import java.net.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;


final class TelemetryClient extends android.content.ContextWrapper {
 static final Object LOCK=new Object();
 private static final String AUTH="https://fleet-gateway.technotrek.ru"; String access="",refresh=""; TelemetryClient(android.content.Context c){super(c);}
 void setTokens(JSONObject j)throws Exception{JSONObject t=j;for(String k:new String[]{"payload","result","data"}){JSONObject v=j.optJSONObject(k);if(v!=null&&v.has("access_token")){t=v;break;}}String a=t.optString("access_token","");if(a.isEmpty())throw new ApiError("Сервер не вернул ожидаемый токен. Вход не подтверждён.");access=a;refresh=t.optString("refresh_token",refresh);}
 synchronized JSONObject readAuthorized(String url)throws Exception{try{return request(url,"GET",null,true);}catch(ApiError e){if(e.http!=401||refresh.isEmpty())throw e;JSONObject r=request(AUTH+"/api/auth/refresh","POST",new JSONObject().put("refresh_token",refresh),false);setTokens(r);save();return request(url,"GET",null,true);}}
 private JSONObject request(String url,String method,JSONObject body,boolean authorized)throws Exception{
  URL u=new URL(url);String host=u.getHost();
  if(!u.getProtocol().equals("https")||!(host.equals("fleet-gateway.technotrek.ru")||host.equals("mobile-api.technotrek.ru")||host.equals("api.open-meteo.com")))throw new IOException();
  HttpURLConnection c=(HttpURLConnection)u.openConnection();c.setInstanceFollowRedirects(false);c.setConnectTimeout(15000);c.setReadTimeout(20000);c.setRequestMethod(method);
  c.setRequestProperty("Accept","application/json");c.setRequestProperty("Content-Type","application/json");c.setRequestProperty("Accept-Language","ru");
  if(!host.equals("api.open-meteo.com")){c.setRequestProperty("X-App-Name","technotrek");c.setRequestProperty("X-App-Version","2.7.10");c.setRequestProperty("X-App-Id","7542fd74-47bd-4652-b422-6ef7d610582e");}
  if(authorized&&!host.equals("api.open-meteo.com"))c.setRequestProperty("Authorization","Bearer "+access);
  try{
   if(body!=null){c.setDoOutput(true);try(OutputStream out=c.getOutputStream()){out.write(body.toString().getBytes(StandardCharsets.UTF_8));}}
   int http=c.getResponseCode();InputStream in=http>=200&&http<300?c.getInputStream():c.getErrorStream();String text="";
   if(in!=null){try(InputStream s=in;ByteArrayOutputStream out=new ByteArrayOutputStream()){byte[] buf=new byte[8192];int n;while((n=s.read(buf))!=-1){if(out.size()+n>3000000)throw new IOException();out.write(buf,0,n);}text=out.toString("UTF-8");}}
   if(http<200||http>=300){String low=text.toLowerCase(java.util.Locale.ROOT);if(low.contains("captcha"))throw new ApiError("Сервер требует CAPTCHA. Эта тестовая версия её пока не поддерживает; дальнейший вход остановлен.",http);throw new ApiError("HTTP "+http+". "+(http==401?"Авторизация не принята.":http==429?"Слишком много запросов. Не запрашивайте SMS повторно сейчас.":"Запрос не принят. Подключение ещё требует доработки."),http);}
   JSONObject j=new JSONObject(text);if(j.has("success")&&!j.optBoolean("success",true))throw new ApiError("Сервер сообщил об ошибке; успешный результат не подтверждён.");return j;
  }finally{c.disconnect();}
 }
 private javax.crypto.SecretKey key()throws Exception{KeyStore ks=KeyStore.getInstance("AndroidKeyStore");ks.load(null);if(!ks.containsAlias("evrange.session")){KeyGenerator g=KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES,"AndroidKeyStore");g.init(new KeyGenParameterSpec.Builder("evrange.session",KeyProperties.PURPOSE_ENCRYPT|KeyProperties.PURPOSE_DECRYPT).setBlockModes(KeyProperties.BLOCK_MODE_GCM).setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE).build());g.generateKey();}return (javax.crypto.SecretKey)ks.getKey("evrange.session",null);}
 void save()throws Exception{Cipher c=Cipher.getInstance("AES/GCM/NoPadding");c.init(Cipher.ENCRYPT_MODE,key());String value=new JSONObject().put("access",access).put("refresh",refresh).toString();byte[] data=c.doFinal(value.getBytes(StandardCharsets.UTF_8));getSharedPreferences("session",0).edit().putString("iv",Base64.encodeToString(c.getIV(),Base64.NO_WRAP)).putString("data",Base64.encodeToString(data,Base64.NO_WRAP)).commit();}
 void load()throws Exception{android.content.SharedPreferences p=getSharedPreferences("session",0);String data=p.getString("data","");if(data.isEmpty())return;Cipher c=Cipher.getInstance("AES/GCM/NoPadding");c.init(Cipher.DECRYPT_MODE,key(),new GCMParameterSpec(128,Base64.decode(p.getString("iv",""),Base64.NO_WRAP)));JSONObject j=new JSONObject(new String(c.doFinal(Base64.decode(data,Base64.NO_WRAP)),StandardCharsets.UTF_8));access=j.getString("access");refresh=j.optString("refresh","");}
 private static class ApiError extends Exception{int http;ApiError(String m){super(m);}ApiError(String m,int h){super(m);http=h;}}
}
