package ru.ivan.evrangetest;
import android.content.Context;
import android.graphics.*;
import android.view.View;
/** Photo-based vehicle. Animated overlays express telemetry, not a 360 model. */
public final class CarScene extends View {
 private final Paint p=new Paint(3);private final Bitmap car;private boolean active=true,moving,lights,charging;private long start=android.os.SystemClock.uptimeMillis();
 public CarScene(Context c){super(c);int id=getResources().getIdentifier("car","drawable",c.getPackageName());car=BitmapFactory.decodeResource(getResources(),id);setContentDescription("Белый тонированный Evolute i-Pro с анимацией состояния");}
 public void state(boolean fresh,boolean move,boolean lamp,boolean charge){moving=fresh&&move;lights=fresh&&(lamp||move);charging=fresh&&charge;invalidate();}
 public void active(boolean a){active=a;if(a)invalidate();}
 @Override protected void onDraw(Canvas c){super.onDraw(c);if(car==null)return;float w=getWidth(),h=getHeight(),scale=Math.min(w/car.getWidth(),(h-30)/car.getHeight());float bw=car.getWidth()*scale,bh=car.getHeight()*scale,x=(w-bw)/2,y=(h-bh)/2;float t=(android.os.SystemClock.uptimeMillis()-start)/1000f;
 p.setColor(0xff08121d);c.drawOval(w*.08f,h*.75f,w*.94f,h*.88f,p);
 if(moving){p.setColor(0xff355060);p.setStrokeWidth(2);for(int i=0;i<7;i++){float xx=((i*.18f+t*.5f)%1)*w;c.drawLine(xx,h*.88f,xx+w*.07f,h*.88f,p);}}
 c.drawBitmap(car,null,new RectF(x,y,x+bw,y+bh),p);
 if(moving){wheel(c,x+bw*.418f,y+bh*.727f,bw*.041f,bh*.113f,t);wheel(c,x+bw*.839f,y+bh*.699f,bw*.031f,bh*.103f,t);}
 if(lights){float lx=x+bw*.293f,ly=y+bh*.494f; p.setShader(new RadialGradient(lx,ly,bw*.15f,new int[]{0xbbd9faff,0x00d9faff},null,Shader.TileMode.CLAMP));c.drawOval(lx-bw*.15f,ly-bh*.10f,lx+bw*.12f,ly+bh*.10f,p);p.setShader(null);p.setColor(0xddf1ffff);c.drawOval(lx-bw*.025f,ly-bh*.012f,lx+bw*.025f,ly+bh*.012f,p);}
 if(charging){p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(5);p.setColor(0xff64e6bd);Path cable=new Path();cable.moveTo(x+bw*.13f,y+bh*.57f);cable.cubicTo(w*.05f,h*.7f,w*.17f,h*.97f,w*.02f,h*.94f);c.drawPath(cable,p);p.setStyle(Paint.Style.FILL);p.setColor(0xffb2ffdf);c.drawRoundRect(x+bw*.11f,y+bh*.55f,x+bw*.15f,y+bh*.59f,5,5,p);p.setAlpha((int)(140+100*Math.sin(t*3)));c.drawCircle(w*.06f,h*.91f,5,p);p.setAlpha(255);}
 if(active&&(moving||charging))postInvalidateDelayed(33);
 }
 private void wheel(Canvas c,float x,float y,float rx,float ry,float t){c.save();c.translate(x,y);c.scale(rx,ry);c.rotate(t*320);p.setColor(0xffaeb8c1);c.drawCircle(0,0,1,p);p.setColor(0xff1a2025);for(int i=0;i<5;i++){c.save();c.rotate(i*72);Path a=new Path();a.moveTo(-.12f,-.1f);a.lineTo(-.3f,-.84f);a.lineTo(.16f,-.94f);a.lineTo(.1f,0);a.close();c.drawPath(a,p);c.restore();}p.setColor(0xff535e67);c.drawCircle(0,0,.2f,p);c.restore();}
}
