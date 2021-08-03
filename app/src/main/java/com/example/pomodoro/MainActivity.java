package com.example.pomodoro;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static android.content.ContentValues.TAG;
import static com.example.pomodoro.NotifApp.CHANNEL_1_ID;
//https://postimg.cc/YhK6gKx5
//https://postimg.cc/McvMNdD0
class SavedState{
    boolean exists;
    long startTime;
    long pauseTime;

    public SavedState(boolean exists) {
        this.exists = exists;
    }

    public boolean isExists() {
        return exists;
    }

    public void setExists(boolean exists) {
        this.exists = exists;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getPauseTime() {
        return pauseTime;
    }

    public void setPauseTime(long pauseTime) {
        this.pauseTime = pauseTime;
    }
}
public class MainActivity extends AppCompatActivity {

    TextView timer,title, cycleNumber;
    Button btn;

    boolean started = false;

    List<Long> state_timer;
    int rounds;
    boolean enabledNotification = true;

    String state_name[]= {"FOCUS","BREAK","BREAK"};

    int curr_state = 0;
    SavedState savedState;

    long display_min;
    long display_sec;

    long startTime, currentTime;

    public NotificationManagerCompat notificationManager;

    DatabaseHelper db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("PomPom Doro");

        cycleNumber = findViewById(R.id.cycleNo);
        timer = findViewById(R.id.timerID);
        title = findViewById(R.id.titleID);
        btn = findViewById(R.id.btn);
        savedState = new SavedState(false);
        started = false;




        notificationManager = NotificationManagerCompat.from(this);
        db = new DatabaseHelper(this);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("HELLO","BUTTON CLICKED==========================");
                if(!started){
                    started = true;
                    btn.setBackgroundResource(R.drawable.ic_pause);
                    call_timer();
                }
                else{
                    started = false;
                    btn.setBackgroundResource(R.drawable.ic_play);
                }
            }
        });

        state_timer = new ArrayList<>();

        Cursor res = db.getAllData();
        if(res.getCount()==0){
            ///using default options
            db.insertData(25,5,20,4,true);
            state_timer.add(new Long(25));
            state_timer.add(new Long(5));
            state_timer.add(new Long(20));
            rounds = 4;
            enabledNotification = true;

            timer.setText(String.valueOf(state_timer.get(curr_state%2) +":00"));
            title.setText(state_name[curr_state%2]);
            cycleNumber.setText(String.valueOf(curr_state/2)+"/"+String.valueOf(rounds));
            setColor();
        }
        else{
            ///using userdefined options
            StringBuffer stringBuffer = new StringBuffer();
            while (res.moveToNext()){
                state_timer.add(new Long(res.getString(1)));
                state_timer.add(new Long(res.getString(2)));
                state_timer.add(new Long(res.getString(3)));
                rounds = Integer.valueOf(res.getString(4));
                enabledNotification = (Integer.valueOf(res.getString(5))==1);

                stringBuffer.append("ID: "+res.getString(0));
                stringBuffer.append("\nWORK: "+res.getString(1));
                stringBuffer.append("\nSHORT_BREAK: "+res.getString(2));
                stringBuffer.append("\nLONG_BREAK: "+res.getString(3));
                stringBuffer.append("\nROUNDS: "+res.getString(4));
                stringBuffer.append("\nNOTIFICATION: "+res.getString(5));
                stringBuffer.append("\n");
                break;
            }
            Log.i("ALL DATA ------------->",stringBuffer.toString());

            timer.setText(String.valueOf(state_timer.get(curr_state%2) +":00"));
            title.setText(state_name[curr_state%2]);
            cycleNumber.setText(String.valueOf((curr_state/2)+1)+"/"+String.valueOf(rounds));
            setColor();
        }
    }

    private void call_timer() {

        if(savedState.isExists()){
            startTime = System.currentTimeMillis()-(savedState.getPauseTime()-savedState.getStartTime());
            currentTime = System.currentTimeMillis();
            savedState.setExists(false);
        }
        else{
            startTime = System.currentTimeMillis();
            currentTime = System.currentTimeMillis();
        }


        Log.d("RUNNING","ITZ RUNNING BOIS.......................................................");

        Timer timerThread = new Timer();
        timerThread.schedule(new TimerTask() {
                    @Override
                    public void run() {

                        MainActivity.this.runOnUiThread(() -> {
                            int selector;
                            if(curr_state==((rounds*2)-1)){
                                selector = 2;
                            }
                            else{
                                selector = curr_state%2;
                            }

                            if(((currentTime-startTime) < (state_timer.get(selector) *60000)) && started){
                                Log.d("RUNNING","ITZ RUNNING BOIZ...........................................LOOPSIES............");
                                currentTime = System.currentTimeMillis();
                                long time = (state_timer.get(curr_state%2) *60000) - (currentTime-startTime);
                                display_min = time/60000;
                                display_sec = (time/1000)%60;


                                updateDisplay(display_min,display_sec,state_name[selector]);
                            }
                            else if(!started){
                                ///deliberately paused
                                //save current state

                                Log.d("State","ENTERED PAUSED STATE---------------------");
                                savedState.setStartTime(startTime);
                                savedState.setPauseTime(currentTime);
                                savedState.setExists(true);

                                timerThread.cancel();
                            }
                            else{
                                ///timer finished counting

                                Log.d("State","FINISHED COUNTING EVERYTHING---------------------");
                                if(curr_state==((rounds*2)-1)){
                                    curr_state = 0;
                                    //Toast.makeText(MainActivity.this,"Congratulations! You have completed a whole cycle!",Toast.LENGTH_LONG).show();
                                    ///upon completing a whole cycle
                                    new AlertDialog.Builder(MainActivity.this)
                                            .setTitle("Congratulations!")
                                            .setMessage("You have completed a whole pomodoro cycle! Press OK to continue.")

                                            // Specifying a listener allows you to take an action before dismissing the dialog.
                                            // The dialog is automatically dismissed when a dialog button is clicked.
                                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int which) {
                                                    dialog.cancel();
                                                }
                                            })
                                            .setIcon(R.drawable.star_icon)
                                            .show();
                                }
                                else{
                                    curr_state++;

                                    if(curr_state == ((rounds*2)-1)){
                                        Toast.makeText(MainActivity.this,"LONG BREAK", Toast.LENGTH_SHORT).show();
                                    }
                                }
                                savedState.setExists(false);

                                cycleNumber.setText(String.valueOf((curr_state/2)+1)+"/"+String.valueOf(rounds));
                                updateDisplay(state_timer.get(curr_state%2),0,state_name[curr_state%2]);

                                started = false;
                                btn.setBackgroundResource(R.drawable.ic_play);

                                if(enabledNotification){
                                    Log.i("HALLELUIYA", "run: ----+++++++++++++++++++++++++++++++++----> is enabled");
                                    sendNotification();
                                }
                                //vibrations
                                Vibrator vibrator;
                                vibrator = (Vibrator) getSystemService(MainActivity.this.VIBRATOR_SERVICE);
                                vibrator.vibrate(1500);

                                timerThread.cancel();
                            }
                        });



                    }
        }, 0,1000);

    }

    private void updateDisplay(long min,long sec, String header){
                String time = "";
                if(min<10){
                    time+= "0"+min+":";
                }
                else{
                    time+=min+":";
                }

                if(sec<10){
                    time+="0"+sec;
                }
                else{
                    time+=sec;
                }

                timer.setText(time);
                title.setText(header);


                setColor();
    }


    private void setColor() {
        if(curr_state%2==0){
            timer.setTextColor(Color.RED);
            title.setTextColor(Color.RED);
            cycleNumber.setTextColor(Color.RED);
        }
        else{
            timer.setTextColor(Color.BLUE);
            title.setTextColor(Color.BLUE);
            cycleNumber.setTextColor(Color.BLUE);
        }
    }

    ///handling the options menu here
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.settings, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.settingsMenuID) {
            Intent intent = new Intent(this,SettingsActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    private void sendNotification() {

        Intent activityIntent = new Intent(this,MainActivity.class);
        activityIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        PendingIntent contentIntent = PendingIntent.getActivity(this,0,activityIntent,0);

        notificationManager.cancel(1);

        int color;
        String message;
        if(curr_state%2==0){
            message = "Get Back To Work!";
            color = Color.RED;
        }
        else{
            message = "Take A Break!";
            color = Color.BLUE;
        }

        Notification notification = new NotificationCompat.Builder(this,CHANNEL_1_ID)
                .setSmallIcon(R.drawable.ic_pompom)
                .setContentTitle(message)
                .setContentText("Click to view.")
                .setColor(color)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setContentIntent(contentIntent)
                .setWhen(System.currentTimeMillis())
                .setAutoCancel(true)
                .build();
        notificationManager.notify(1,notification);
    }

}