package com.fittimer.app;

import android.app.*;
import android.content.*;
import android.content.pm.ServiceInfo;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.*;
import android.speech.tts.TextToSpeech;
import org.json.*;
import java.util.*;

/** Foreground workout clock. Time is based on elapsedRealtime, not Handler tick counts. */
public class WorkoutService extends Service {
 public static final String ACTION_STATE="com.fittimer.app.STATE";
 public static final String ACTION_CONTROL="com.fittimer.app.CONTROL";
 private final Handler handler=new Handler(Looper.getMainLooper());
 private final ArrayList<Segment> segments=new ArrayList<>();
 private int index=0,remaining=0; private boolean running=false,completed=false;
 private long deadline=0; private String mode="음성 + 알림음";
 private TextToSpeech tts; private boolean ttsReady=false; private ToneGenerator tone;
 private PowerManager.WakeLock wakeLock;
 private static final String CHANNEL="fit_workout_clock";
 static class Segment {String name;int seconds,round,exercise,set,sets;boolean rest;
 Segment(JSONObject j){name=j.optString("name","운동");seconds=Math.max(1,j.optInt("seconds",30));round=j.optInt("round",1);exercise=j.optInt("exercise",0);set=j.optInt("set",1);sets=j.optInt("sets",1);rest=j.optBoolean("rest",false);}}
 @Override public void onCreate(){super.onCreate();NotificationChannel channel=new NotificationChannel(CHANNEL,"운동 타이머",NotificationManager.IMPORTANCE_LOW);channel.setDescription("운동 중 화면이 꺼져도 진행되는 타이머");getSystemService(NotificationManager.class).createNotificationChannel(channel);
 tts=new TextToSpeech(this,status->{if(status==TextToSpeech.SUCCESS){ttsReady=tts.setLanguage(Locale.KOREAN)!=TextToSpeech.LANG_MISSING_DATA;}});
 try{tone=new ToneGenerator(AudioManager.STREAM_MUSIC,70);}catch(Exception ignored){}
 PowerManager pm=(PowerManager)getSystemService(POWER_SERVICE);wakeLock=pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"FitTimer:WorkoutClock");wakeLock.setReferenceCounted(false);
 }
 @Override public int onStartCommand(Intent intent,int flags,int startId){if(intent==null){publish();return START_STICKY;}String command=intent.getStringExtra("command");if(command==null)command="query";
 switch(command){case "init":init(intent);break;case "play":if(completed)break;if(!running&&!segments.isEmpty()){running=true;deadline=SystemClock.elapsedRealtime()+remaining*1000L;hold();announce(segments.get(index).rest?"휴식 시작":segments.get(index).name+" 시작");}break;
 case "mode":mode=intent.getStringExtra("mode");break;case "pause":updateRemaining();running=false;release();break;
 case "next":advance();break;
 case "reset":running=false;release();index=0;completed=false;remaining=segments.isEmpty()?0:segments.get(0).seconds;break;
 case "stop":running=false;release();stopForeground(STOP_FOREGROUND_REMOVE);stopSelf();return START_NOT_STICKY;
 }
 publish();return START_STICKY;}
 private void init(Intent intent){running=false;release();segments.clear();mode=intent.getStringExtra("mode");if(mode==null)mode="음성 + 알림음";
 try{JSONArray array=new JSONArray(intent.getStringExtra("plan"));for(int i=0;i<Math.min(array.length(),5000);i++)segments.add(new Segment(array.getJSONObject(i)));}catch(Exception ignored){}
 index=0;completed=false;remaining=segments.isEmpty()?0:segments.get(0).seconds;}
 private void updateRemaining(){if(running)remaining=Math.max(0,(int)Math.ceil((deadline-SystemClock.elapsedRealtime())/1000.0));}
 private void advance(){if(completed||segments.isEmpty())return;index++;if(index>=segments.size()){running=false;release();completed=true;remaining=0;announce("운동 완료. 수고하셨습니다");return;}
 Segment s=segments.get(index);remaining=s.seconds;if(running)deadline=SystemClock.elapsedRealtime()+remaining*1000L;announce(s.rest?"휴식 시작":s.name+" 시작");}
 private final Runnable ticker=new Runnable(){@Override public void run(){if(!running)return;long now=SystemClock.elapsedRealtime();int before=remaining;int transitions=0;while(running&&now>=deadline&&transitions++<5000){long overshoot=now-deadline;advance();if(running)deadline=now+Math.max(0,segments.get(index).seconds*1000L-overshoot);}
 if(running){remaining=Math.max(0,(int)Math.ceil((deadline-now)/1000.0));if(remaining!=before&&remaining>=1&&remaining<=3)announce(""+remaining);}
 publish();}};
 private void hold(){if(wakeLock!=null&&!wakeLock.isHeld())wakeLock.acquire(8*60*60*1000L);}
 private void release(){if(wakeLock!=null&&wakeLock.isHeld())wakeLock.release();}
 private void announce(String s){if("무음".equals(mode))return;if(!"음성만".equals(mode)&&tone!=null)tone.startTone(ToneGenerator.TONE_PROP_BEEP,140);
 if(!"알림음만".equals(mode)&&ttsReady&&tts!=null)tts.speak(s,TextToSpeech.QUEUE_FLUSH,null,"workout_step");}
 private void publish(){handler.removeCallbacks(ticker);if(running)handler.postDelayed(ticker,200);
 Intent data=new Intent(ACTION_STATE).setPackage(getPackageName());data.putExtra("index",index);data.putExtra("remaining",remaining);data.putExtra("running",running);data.putExtra("completed",completed);sendBroadcast(data);
 String title=completed?"운동 완료":segments.isEmpty()?"운동 준비":segments.get(Math.min(index,segments.size()-1)).name;
 String time=String.format(Locale.KOREA,"%02d:%02d",remaining/60,remaining%60);
 Intent open=new Intent(this,MainActivity.class);open.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP|Intent.FLAG_ACTIVITY_CLEAR_TOP);
 PendingIntent content=PendingIntent.getActivity(this,0,open,PendingIntent.FLAG_IMMUTABLE|PendingIntent.FLAG_UPDATE_CURRENT);
 Notification.Builder b=new Notification.Builder(this,CHANNEL).setSmallIcon(android.R.drawable.ic_media_play).setContentTitle("FIT TIMER · "+title).setContentText(time+(running?" · 진행 중":" · 일시정지")).setContentIntent(content).setOngoing(running).setOnlyAlertOnce(true);
 Intent action=new Intent(this,WorkoutService.class).putExtra("command",running?"pause":"play");PendingIntent toggle=PendingIntent.getService(this,1,action,PendingIntent.FLAG_IMMUTABLE|PendingIntent.FLAG_UPDATE_CURRENT);
 b.addAction(new Notification.Action.Builder(android.R.drawable.ic_media_pause,running?"일시정지":"계속",toggle).build());
 startForeground(101,b.build());if(completed){stopForeground(STOP_FOREGROUND_DETACH);stopSelf();}}
 @Override public IBinder onBind(Intent intent){return null;}
 @Override public void onDestroy(){handler.removeCallbacks(ticker);release();if(tts!=null){tts.stop();tts.shutdown();}if(tone!=null)tone.release();super.onDestroy();}
}
