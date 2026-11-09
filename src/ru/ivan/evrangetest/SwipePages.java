package ru.ivan.evrangetest;
import android.content.Context;
import android.view.*;
import android.widget.ViewFlipper;
public class SwipePages extends ViewFlipper {
 private float x,y;public Runnable changed;
 public SwipePages(Context c){super(c);}
 @Override public boolean onInterceptTouchEvent(MotionEvent e){if(e.getActionMasked()==MotionEvent.ACTION_DOWN){x=e.getX();y=e.getY();}if(e.getActionMasked()==MotionEvent.ACTION_MOVE&&Math.abs(e.getX()-x)>48*getResources().getDisplayMetrics().density&&Math.abs(e.getX()-x)>1.5*Math.abs(e.getY()-y))return true;return false;}
 @Override public boolean onTouchEvent(MotionEvent e){if(e.getActionMasked()==MotionEvent.ACTION_UP){float dx=e.getX()-x;if(Math.abs(dx)>48*getResources().getDisplayMetrics().density){if(dx<0)page(1);else page(0);}return true;}return true;}
 public void page(int n){setDisplayedChild(n);if(changed!=null)changed.run();}
}
