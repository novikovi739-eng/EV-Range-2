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

public class MainActivity extends Activity {
 private static final String AUTH="https://fleet-gateway.technotrek.ru";
 private static final String API="https://mobile-api.technotrek.ru";
 private final ExecutorService worker=Executors.newSingleThreadExecutor();
 private TextView status,output,hero,rangeNote,vehicle,cabin,weekly,weather,method; private Button monitoring; private EditText phone,code,car; private Button send,login,read,forget;
 private Button bell; private CarScene scene; private SwipePages pages; private android.app.Dialog accountDialog; private TextView odometer,ambient,batteryLabel,chargeLabel,tripList,pageLabel; private LinearLayout account; private String access="",refresh=""; private boolean busy=false;
 private static final int BG=0xff101a29, CARD=0xff1b2a3d, TEXT=0xffedf3fa, MUTED=0xffb8c9db, MINT=0xff64e6bd;
 @Override public void onCreate(Bundle b){super.onCreate(b);
  getWindow().setStatusBarColor(BG);getWindow().setNavigationBarColor(BG);
  FrameLayout frame=new FrameLayout(this);frame.setBackgroundColor(BG);setContentView(frame);
  frame.setOnApplyWindowInsetsListener((v,i)->{v.setPadding(0,i.getSystemWindowInsetTop(),0,i.getSystemWindowInsetBottom());return i;});frame.requestApplyInsets();
  LinearLayout shell=new LinearLayout(this);shell.setOrientation(1);FrameLayout.LayoutParams content=new FrameLayout.LayoutParams(-1,-1);content.bottomMargin=dp(68);frame.addView(shell,content);
  pages=new SwipePages(this);shell.addView(pages,new LinearLayout.LayoutParams(-1,0,1));
  LinearLayout root=page();LinearLayout history=page();
  LinearLayout heading=new LinearLayout(this);heading.setGravity(android.view.Gravity.CENTER_VERTICAL);root.addView(heading);
  TextView title=label(heading,"EVOLUTE",15);title.setTextColor(MINT);title.setLayoutParams(new LinearLayout.LayoutParams(0,-2,1));
  bell=new Button(this);bell.setAllCaps(false);bell.setTextColor(MINT);bell.setText("♧ Уведомления");bell.setTextSize(12);bell.setOnClickListener(v->openNotificationHistory());heading.addView(bell);
  ambient=label(heading,"— °C",23);ambient.setContentDescription("Температура воздуха возле автомобиля");
  label(root,"i-PRO · Приложение от Васи",27);
  scene=new CarScene(this);root.addView(scene,new LinearLayout.LayoutParams(-1,dp(245)));
  chargeLabel=label(root,"Обновите данные машины",14);chargeLabel.setTextColor(MUTED);
  odometer=label(root,"Общий пробег  — км",17);
  LinearLayout row=new LinearLayout(this);root.addView(row);batteryLabel=label(row,"Заряд  — %",19);batteryLabel.setLayoutParams(new LinearLayout.LayoutParams(0,-2,1));vehicle=label(row,"ВВБ  — °C",17);vehicle.setContentDescription("Температура тяговой батареи");
  cabin=label(root,"Салон  — · нет данных датчика",16);
  LinearLayout rangeCard=card(root);label(rangeCard,"РЕАЛЬНО ПРОЕДЕТ · ПО РАСХОДУ ПОЕЗДОК",12).setTextColor(MUTED);
  hero=label(rangeCard,"До 50%     — км\nДо 20%     — км\nДо 5%        — км",25);hero.setTextColor(MINT);
  rangeNote=label(rangeCard,"Обновите прогноз",13);
  read=button(root,"↻  Обновить",()->work(()->readData()));
  status=label(root,"",12);status.setTextColor(MUTED);
  label(root,"Свайп влево — расход и поездки  →",12).setTextColor(MUTED);
  label(history,"ТВОЯ НЕДЕЛЯ",14).setTextColor(MINT);label(history,"Расход и поездки",28);
  LinearLayout weekCard=card(history);weekly=label(weekCard,"Обновите данные на странице автомобиля",17);
  label(history,"ПОЕЗДКИ",14).setTextColor(MINT);tripList=label(history,"—",15);
  method=label(history,"Расход — оценка по падению заряда и указанной ёмкости. Износ, округление процентов и километров влияют на точность. Погода не вносит произвольную поправку. Изображение автомобиля с анимациями; это не вращаемая 3D-модель. Фары в анимации обозначают включённое зажигание или движение; это не отдельный индикатор датчика фар.",13);method.setTextColor(MUTED);
  weather=label(history,"Погода не загружена",13);weather.setTextColor(MUTED);
  label(history,"Open-Meteo получает координаты машины с округлением до 0,01°. Токен ТехноТрека не передаётся. Фоновую проверку можно включить во вкладке входа. Интервал от 15 минут; Android может задерживать проверки. Короткие события между проверками могут быть пропущены.",12).setTextColor(MUTED);
  output=label(history,"",13);output.setTextColor(MUTED);
  LinearLayout bottom=new LinearLayout(this);bottom.setGravity(android.view.Gravity.CENTER_VERTICAL);bottom.setPadding(dp(16),0,dp(16),0);FrameLayout.LayoutParams bp=new FrameLayout.LayoutParams(-1,dp(64),android.view.Gravity.BOTTOM);frame.addView(bottom,bp);
  Button up=new Button(this);up.setText("↑");up.setTextSize(25);up.setTextColor(MINT);up.setBackgroundColor(android.graphics.Color.TRANSPARENT);up.setContentDescription("Открыть вход и настройки автомобиля");bottom.addView(up,new LinearLayout.LayoutParams(dp(56),dp(56)));up.setOnClickListener(v->openAccount());
  pageLabel=label(bottom,"● Авто     ○ Поездки  →",14);pageLabel.setGravity(android.view.Gravity.CENTER);pageLabel.setLayoutParams(new LinearLayout.LayoutParams(0,-2,1));pageLabel.setOnClickListener(v->pages.page(pages.getDisplayedChild()==0?1:0));pages.changed=()->{pageLabel.setText(pages.getDisplayedChild()==0?"● Авто     ○ Поездки  →":"←  ○ Авто     ● Поездки");scene.active(pages.getDisplayedChild()==0);};
  accountDialog=new android.app.Dialog(this);ScrollView accountScroll=new ScrollView(this);account=new LinearLayout(this);account.setOrientation(1);account.setBackgroundColor(CARD);account.setPadding(dp(22),dp(20),dp(22),dp(28));accountScroll.addView(account);accountDialog.setContentView(accountScroll);
  label(account,"Вход и автомобиль",23);label(account,"Номер и SMS-код отправляются напрямую ТехноТреку. Только чтение данных.",13);
  phone=field(account,"Телефон: +79…",InputType.TYPE_CLASS_PHONE);send=button(account,"Получить SMS-код",()->work(()->sendCode()));
  code=field(account,"Код из SMS",InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_VARIATION_PASSWORD);login=button(account,"Войти",()->work(()->signIn()));
  car=field(account,"ID автомобиля",InputType.TYPE_CLASS_NUMBER);car.setText("1141");
  forget=button(account,"Удалить вход с телефона",()->{MonitorService.disable(this);monitoring.setText("Включить уведомления");access="";refresh="";getSharedPreferences("session",0).edit().clear().apply();code.setText("");clearResults();status.setText("Локальный вход удалён.");});
  monitoring=button(account,MonitorService.enabled(this)?"Уведомления: включены · выключить":"Включить уведомления",()->toggleMonitoring());
  label(account,"Проверка примерно раз в 15 минут, возможны задержки Android. Это не мгновенные push-события. Первый успешный опрос сохраняет исходное состояние.",12);
  button(account,"Готово  ↓",()->accountDialog.dismiss());
  updateNotificationBadge();try{load();status.setText(access.isEmpty()?"Вход — стрелка ↑ внизу слева.":"Вход сохранён. Нажмите «Обновить».");}catch(Exception e){status.setText("Войдите заново через стрелку ↑.");}
 }
 private void updateNotificationBadge(){if(bell!=null){int n=NotificationHistory.unread(this);bell.setText(n>0?"♧ "+n:"♧ История");}}
 private void openNotificationHistory(){
  NotificationHistory.markAllRead(this);updateNotificationBadge();
  android.app.Dialog dialog=new android.app.Dialog(this);ScrollView scroll=new ScrollView(this);LinearLayout list=new LinearLayout(this);list.setOrientation(1);list.setPadding(dp(18),dp(18),dp(18),dp(18));list.setBackgroundColor(BG);scroll.addView(list);dialog.setContentView(scroll);
  label(list,"История уведомлений",25);label(list,"Сохраняются новые события, обнаруженные после обновления приложения. Хранение до 90 дней.",13).setTextColor(MUTED);
  org.json.JSONArray events=NotificationHistory.list(this);
  if(events.length()==0)label(list,"Пока нет уведомлений",16);
  for(int i=0;i<events.length();i++){
   org.json.JSONObject item=events.optJSONObject(i);if(item==null)continue;
   final long id=item.optLong("id");LinearLayout card=card(list);
   String date=new java.text.SimpleDateFormat("dd.MM.yyyy HH:mm",java.util.Locale.forLanguageTag("ru-RU")).format(new java.util.Date(item.optLong("detectedAt")));
   label(card,date+" · обнаружено при проверке",12).setTextColor(MUTED);
   label(card,item.optString("text"),15);
   button(card,"Удалить запись",()->{NotificationHistory.delete(this,id);dialog.dismiss();openNotificationHistory();});
  }
  button(list,"Очистить историю",()->new android.app.AlertDialog.Builder(this).setMessage("Удалить всю историю уведомлений?").setNegativeButton("Отмена",null).setPositiveButton("Удалить",(d,w)->{NotificationHistory.clear(this);dialog.dismiss();updateNotificationBadge();}).show());
  button(list,"Закрыть",()->dialog.dismiss());dialog.show();android.view.Window window=dialog.getWindow();if(window!=null)window.setLayout(-1,-1);
 }
 private void toggleMonitoring(){
  if(MonitorService.enabled(this)){MonitorService.disable(this);monitoring.setText("Включить уведомления");return;}
  if(access.isEmpty()){Toast.makeText(this,"Сначала войдите",Toast.LENGTH_LONG).show();return;}
  if(android.os.Build.VERSION.SDK_INT>=33&&checkSelfPermission("android.permission.POST_NOTIFICATIONS")!=android.content.pm.PackageManager.PERMISSION_GRANTED){requestPermissions(new String[]{"android.permission.POST_NOTIFICATIONS"},45);return;}
  if(!getSystemService(android.app.NotificationManager.class).areNotificationsEnabled()){Toast.makeText(this,"Разрешите уведомления EV Range в настройках телефона",Toast.LENGTH_LONG).show();return;}
  String id=car.getText().toString().trim();if(!id.matches("[0-9]{1,12}"))return;
  boolean ok=MonitorService.enable(this,id);monitoring.setText(ok?"Уведомления: включены · выключить":"Не удалось включить проверку");
 }
 @Override public void onRequestPermissionsResult(int r,String[] p,int[] g){super.onRequestPermissionsResult(r,p,g);if(r==45&&g.length>0&&g[0]==android.content.pm.PackageManager.PERMISSION_GRANTED)toggleMonitoring();}
 private LinearLayout page(){ScrollView sc=new ScrollView(this);sc.setFillViewport(true);LinearLayout l=new LinearLayout(this);l.setOrientation(1);l.setPadding(dp(20),dp(12),dp(20),dp(24));sc.addView(l);pages.addView(sc);return l;}
 private void openAccount(){accountDialog.show();android.view.Window w=accountDialog.getWindow();if(w!=null){w.setBackgroundDrawableResource(android.R.color.transparent);w.setGravity(android.view.Gravity.BOTTOM);w.setLayout(-1,(int)(getResources().getDisplayMetrics().heightPixels*.78));}}
 @Override protected void onPause(){super.onPause();if(scene!=null)scene.active(false);}
 @Override protected void onResume(){super.onResume();updateNotificationBadge();if(scene!=null)scene.active(pages.getDisplayedChild()==0);}
 private int dp(int n){return Math.round(n*getResources().getDisplayMetrics().density);}
 private LinearLayout card(LinearLayout p){LinearLayout c=new LinearLayout(this);c.setOrientation(1);c.setPadding(dp(16),dp(12),dp(16),dp(12));android.graphics.drawable.GradientDrawable bg=new android.graphics.drawable.GradientDrawable();bg.setColor(CARD);bg.setCornerRadius(dp(18));c.setBackground(bg);LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.setMargins(0,dp(10),0,dp(6));p.addView(c,lp);return c;}
 private TextView label(LinearLayout p,String s,int size){TextView t=new TextView(this);t.setText(s);t.setTextColor(TEXT);t.setTextSize(size);t.setPadding(0,dp(6),0,dp(6));p.addView(t);return t;}
 private EditText field(LinearLayout p,String hint,int type){EditText e=new EditText(this);e.setTextColor(TEXT);e.setHintTextColor(MUTED);e.setHint(hint);e.setInputType(type);e.setSingleLine(true);p.addView(e);return e;}
 private Button button(LinearLayout p,String s,Runnable r){Button b=new Button(this);b.setText(s);b.setAllCaps(false);b.setTextColor(BG);b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(MINT));b.setOnClickListener(v->r.run());p.addView(b,new LinearLayout.LayoutParams(-1,-2));return b;}
 interface Job {void run() throws Exception;}
 private String phoneInput,codeInput,carInput;
 private void work(Job j){if(busy)return;phoneInput=phone.getText().toString().replaceAll("[^0-9+]","");codeInput=code.getText().toString().trim();carInput=car.getText().toString().trim();busy=true;toggle(false);status.setText("Запрашиваю данные…");worker.execute(()->{try{j.run();}catch(Exception e){show("Ошибка: "+safeMessage(e));}finally{runOnUiThread(()->{busy=false;toggle(true);code.setText("");});codeInput="";}});}
 private void toggle(boolean on){send.setEnabled(on);login.setEnabled(on);read.setEnabled(on);forget.setEnabled(on);phone.setEnabled(on);code.setEnabled(on);car.setEnabled(on);}
 private String safeMessage(Exception e){if(e instanceof ApiError)return e.getMessage();if(e instanceof java.net.SocketTimeoutException)return "Сервер не ответил вовремя. Повторите позже.";if(e instanceof IOException)return "Не удалось связаться с сервером по HTTPS.";return "Не удалось обработать ответ или сохранить вход. Данные не подтверждены.";}
 private void show(String s){runOnUiThread(()->{status.setText(s);if(accountDialog.isShowing())Toast.makeText(this,s,Toast.LENGTH_LONG).show();});}
 private void clearResults(){scene.state(false,false,false,false);hero.setText("До 50%     — км\nДо 20%     — км\nДо 5%        — км");rangeNote.setText("Прогноз ещё не рассчитан");vehicle.setText("ВВБ  — °C");cabin.setText("Салон  — · нет данных датчика");batteryLabel.setText("Заряд  — %");ambient.setText("— °C");chargeLabel.setText("Состояние неизвестно");odometer.setText("Общий пробег  — км");weekly.setText("История не загружена");tripList.setText("—");weather.setText("Погода не загружена");output.setText("");}
 private void checkPhone()throws Exception{if(!phoneInput.matches("\\+?[0-9]{10,15}"))throw new ApiError("Введите номер с кодом страны.");}
 private void sendCode()throws Exception{checkPhone();JSONObject body=new JSONObject().put("phone_number",phoneInput);request(AUTH+"/api/auth/send-code","POST",body,false);show("Запрос SMS принят. Введите пришедший код ниже.");}
 private void signIn()throws Exception{checkPhone();if(!codeInput.matches("[0-9]{4,8}"))throw new ApiError("Введите код из SMS.");JSONObject b=new JSONObject().put("phone_number",phoneInput).put("code",codeInput).put("push_token","");JSONObject result=request(AUTH+"/api/auth/token/sms","POST",b,false);setTokens(result);save();runOnUiThread(()->accountDialog.dismiss());show("Вход выполнен. Нажмите «Обновить прогноз».");}
 private void setTokens(JSONObject j)throws Exception{JSONObject t=j;for(String k:new String[]{"payload","result","data"}){JSONObject v=j.optJSONObject(k);if(v!=null&&v.has("access_token")){t=v;break;}}String a=t.optString("access_token","");if(a.isEmpty())throw new ApiError("Сервер не вернул ожидаемый токен. Вход не подтверждён.");access=a;refresh=t.optString("refresh_token",refresh);}
 private static String f(double v,int n){return RangeData.fmt(v,n);}
 private static double num(JSONObject o,String k){return RangeData.number(o,k);}
 private String time(double sec){if(!Double.isFinite(sec)||sec<=0)return "неизвестно";java.text.SimpleDateFormat df=new java.text.SimpleDateFormat("dd.MM.yyyy HH:mm",new java.util.Locale("ru"));df.setTimeZone(java.util.TimeZone.getTimeZone("Europe/Moscow"));return df.format(new java.util.Date((long)(sec*1000)))+" МСК";}
 private String bool(JSONObject s,String k,String yes,String no){if(s==null||!(s.opt(k) instanceof Boolean))return "неизвестно";return s.optBoolean(k)?yes:no;}
 private void readData()throws Exception{
  if(access.isEmpty())throw new ApiError("Сначала войдите.");if(!carInput.matches("[0-9]{1,12}"))throw new ApiError("Некорректный ID автомобиля.");
  runOnUiThread(()->{clearResults();rangeNote.setText("Загружаю показания и поездки…");});
  long until=System.currentTimeMillis()/1000; java.util.Calendar cal=java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("Europe/Moscow"));cal.setTimeInMillis(until*1000L);cal.add(java.util.Calendar.DAY_OF_MONTH,-7);cal.set(java.util.Calendar.HOUR_OF_DAY,0);cal.set(java.util.Calendar.MINUTE,0);cal.set(java.util.Calendar.SECOND,0);cal.set(java.util.Calendar.MILLISECOND,0);long from=cal.getTimeInMillis()/1000L;
  JSONObject state=readAuthorized(API+"/car/"+carInput+"/info");JSONObject result=RangeData.result(state);
  if(result==null)throw new ApiError("Нет объекта автомобиля в ответе.");JSONObject sensors=result.optJSONObject("sensors");
  if(sensors==null)throw new ApiError("Нет показаний датчиков в ответе.");
  double soc=num(sensors,"battery"),capacity=num(result,"battery_capacity"),last=num(sensors,"last_online"),volts=num(sensors,"board_network_voltage");
  JSONObject board=sensors.optJSONObject("board_network_battery");if(!Double.isFinite(volts))volts=num(board,"voltage");
  String warning=board!=null&&"critical".equals(board.optString("state"))?"\n⚠ ТехноТрек: критическое состояние АКБ":"";
  boolean fresh=RangeData.fresh(last,until);double speed=num(sensors,"speed");boolean ignition=sensors.optBoolean("ignition",false),moving=fresh&&ignition&&Double.isFinite(speed)&&speed>0.5,charging=fresh&&sensors.optBoolean("charging",false);
  String chargeState=!fresh?"Данные устарели":charging?"⚡ Заряжается · пистолет подключён":sensors.opt("charging") instanceof Boolean?"Зарядка не идёт · пистолет: нет данных":"Зарядка и подключение неизвестны";
  String stateLine=chargeState+"\n"+(fresh?bool(sensors,"ignition","Зажигание включено","Зажигание выключено"):"Состояние неизвестно")+(moving?" · в движении":"");
  String detail="АКБ: "+f(volts,2)+" В"+warning+"\nЦентральный замок: "+bool(sensors,"locked","закрыт","открыт")+"\nПоследняя связь: "+time(last)+"\nВремя отдельных датчиков не указано.";
  runOnUiThread(()->{scene.state(fresh,moving,ignition,charging);chargeLabel.setText(stateLine);odometer.setText("Общий пробег  "+f(num(sensors,"odometer"),0)+" км");batteryLabel.setText("Заряд  "+f(soc,0)+"%");vehicle.setText("ВВБ  "+signedTemp(num(sensors,"battery_temp")));double ct=num(sensors,"temp_from_remote_control");cabin.setText("Салон  "+(Double.isFinite(ct)?signedTemp(ct):"нет данных датчика"));output.setText(detail);});
  String historyStatus="";
  try{
   JSONObject trips=readAuthorized(API+"/family_sharing/history/"+carInput+"/ride?date_from="+from+"&date_to="+until);
   RangeData.Stats stats=RangeData.calculate(trips,capacity,from,until);double range=stats.range(capacity,soc,5);
   boolean ready=RangeData.fresh(last,until)&&Double.isFinite(range)&&!stats.partial;
   boolean small=stats.used<3||stats.km<30;
   String rangeText=ready?"До 50%     "+rangeValue(stats,capacity,soc,50)+"\nДо 20%     "+rangeValue(stats,capacity,soc,20)+"\nДо 5%        "+rangeValue(stats,capacity,soc,5):"До 50%     — км\nДо 20%     — км\nДо 5%        — км";
   String note=ready?"Оценка по завершённым поездкам за 7 предыдущих дней и сегодня"+(small?" · мало данных":""):(stats.partial?"История неполная.":!RangeData.fresh(last,until)?"Нет свежей связи с машиной.":"Недостаточно данных для расчёта.");
   String summary=f(stats.average(),1)+" кВт·ч/100 км\nОценка по падению заряда\n\nУчтено поездок: "+stats.used+"\nРасстояние: "+f(stats.km,1)+" км\nЭнергия: ≈ "+f(stats.kwh,2)+" кВт·ч\nЁмкость для расчёта: "+f(capacity,0)+" кВт·ч\n\nИсключено: "+stats.excluded()+"\nНет данных: "+stats.missing+" · нет падения заряда: "+stats.noDrop+"\nНекорректные: "+stats.invalid+" · вне периода: "+stats.outside+" · повторы: "+stats.duplicates+"\n\n"+time(from)+" —\n"+time(until)+"\nТолько завершённые поездки целиком внутри периода."+(stats.partial?"\n⚠ Сервер указал, что история неполная.":"")+(small?"\nМало данных: желательно хотя бы 3 поездки и 30 км.":"")+"\nСредний расход = суммарная оценка энергии / суммарные километры × 100. Поездки с нулевым падением заряда исключены; это не доказательство GPS-сбоя.";
   final String rn=note, rows=tripSummary(trips,capacity,from,until);runOnUiThread(()->{hero.setText(rangeText);rangeNote.setText(rn);weekly.setText(summary);tripList.setText(rows);});
  }catch(Exception e){historyStatus=" История недоступна.";String msg=safeMessage(e);runOnUiThread(()->{weekly.setText("Историю не удалось получить. "+msg);rangeNote.setText("Нет истории для расчёта.");});}
  updateWeather(sensors,last,until);
  show("Запрос завершён: "+time(until)+historyStatus);
 }
 private String signedTemp(double t){return Double.isFinite(t)?(t>=0?"+":"−")+f(Math.abs(t),0)+" °C":"— °C";}
 private String rangeValue(RangeData.Stats s,double cap,double soc,int reserve){return soc<=reserve?"уже ≤ "+reserve+"%":"≈ "+f(s.range(cap,soc,reserve),0)+" км";}
 private String tripSummary(JSONObject data,double cap,long from,long until)throws Exception{
  JSONArray a=data.optJSONArray("result");if(a==null||a.length()==0)return "В ответе нет поездок за этот период.";
  java.util.ArrayList<JSONObject> rows=new java.util.ArrayList<>();for(int i=0;i<a.length();i++)if(a.optJSONObject(i)!=null)rows.add(a.optJSONObject(i));java.util.Collections.sort(rows,(x,y)->Double.compare(num(y,"ride_start"),num(x,"ride_start")));
  StringBuilder text=new StringBuilder();java.util.HashSet<String> ids=new java.util.HashSet<>();
  for(JSONObject r:rows){double km=num(r,"odometer_mileage"),aSoc=num(r,"soc_start"),bSoc=num(r,"soc_end"),start=num(r,"ride_start");String id=r.optString("id","");boolean duplicate=!id.isEmpty()&&!ids.add(id);RangeData.Stats one=RangeData.calculate(new JSONObject().put("result",new JSONArray().put(r)),cap,from,until);
   text.append(time(start)).append("\n").append(f(km,1)).append(" км  ·  ").append(f(aSoc,0)).append(" → ").append(f(bSoc,0)).append("%\n");
   text.append(one.used==1&&!duplicate?"≈ "+f(one.average(),1)+" кВт·ч/100 км":"Не учтена: "+(duplicate?"повтор":one.outside>0?"вне периода":one.noDrop>0?"нет падения заряда":one.missing>0?"нет данных":"некорректные данные")).append("\n\n");
  }return text.toString();
 }
 private void updateWeather(JSONObject sensors,double last,long now){
  JSONObject geo=sensors.optJSONObject("geo"),coords=geo==null?null:geo.optJSONObject("coordinates");
  double lat=num(coords,"lat"),lon=num(coords,"lng");
  if(!Double.isFinite(lat)||!Double.isFinite(lon)||Math.abs(lat)>90||Math.abs(lon)>180){runOnUiThread(()->weather.setText("Нет координат автомобиля. Погода недоступна."));return;}
  if(!RangeData.fresh(last,now)){runOnUiThread(()->weather.setText("Координаты могут быть устаревшими. Обновите связь автомобиля."));return;}
  try{
   String url=String.format(java.util.Locale.US,"https://api.open-meteo.com/v1/forecast?latitude=%.2f&longitude=%.2f&current=temperature_2m&temperature_unit=celsius&timeformat=unixtime&timezone=GMT",lat,lon);
   JSONObject j=request(url,"GET",null,false),current=j.optJSONObject("current");double temp=num(current,"temperature_2m"),at=num(current,"time");
   if(!Double.isFinite(temp)||!Double.isFinite(at))throw new IOException();
   String text=f(temp,1)+" °C\nВ районе автомобиля\nНа "+time(at)+(now-at>10800||at>now+900?"\n⚠ Время погоды устарело или некорректно.":"")+"\nТемпература не меняет прогноз автоматически.";
   runOnUiThread(()->{weather.setText(text);ambient.setText(now-at<=10800&&at<=now+900?signedTemp(temp):"— °C");});
  }catch(Exception e){runOnUiThread(()->weather.setText("Погода временно недоступна. Расчёт по поездкам работает независимо от неё."));}
 }
 private JSONObject readAuthorized(String url)throws Exception { synchronized(TelemetryClient.LOCK){TelemetryClient c=new TelemetryClient(this);c.load();JSONObject r=c.readAuthorized(url);access=c.access;refresh=c.refresh;return r;} }
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
 private void save()throws Exception{Cipher c=Cipher.getInstance("AES/GCM/NoPadding");c.init(Cipher.ENCRYPT_MODE,key());String value=new JSONObject().put("access",access).put("refresh",refresh).toString();byte[] data=c.doFinal(value.getBytes(StandardCharsets.UTF_8));getSharedPreferences("session",0).edit().putString("iv",Base64.encodeToString(c.getIV(),Base64.NO_WRAP)).putString("data",Base64.encodeToString(data,Base64.NO_WRAP)).commit();}
 private void load()throws Exception{android.content.SharedPreferences p=getSharedPreferences("session",0);String data=p.getString("data","");if(data.isEmpty())return;Cipher c=Cipher.getInstance("AES/GCM/NoPadding");c.init(Cipher.DECRYPT_MODE,key(),new GCMParameterSpec(128,Base64.decode(p.getString("iv",""),Base64.NO_WRAP)));JSONObject j=new JSONObject(new String(c.doFinal(Base64.decode(data,Base64.NO_WRAP)),StandardCharsets.UTF_8));access=j.getString("access");refresh=j.optString("refresh","");}
 private static class ApiError extends Exception{int http;ApiError(String m){super(m);}ApiError(String m,int h){super(m);http=h;}}
 @Override protected void onDestroy(){super.onDestroy();worker.shutdown();}
}
