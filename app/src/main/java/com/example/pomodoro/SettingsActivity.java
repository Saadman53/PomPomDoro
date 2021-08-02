package com.example.pomodoro;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

public class SettingsActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    Spinner work,short_break,long_break, rounds;
    String workDuration="", short_breakDuration="", long_breakDuration="", number_of_rounds="";

    Switch notifSwitch;

    Button saveBtn;

    boolean enableNotification=true;

    DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        setTitle("Options");

        work = findViewById(R.id.workID);
        short_break = findViewById(R.id.shortID);
        long_break = findViewById(R.id.longID);
        rounds = findViewById(R.id.roundID);

        ArrayAdapter<CharSequence> work_adapter = ArrayAdapter.createFromResource(this,R.array.work_array, R.layout.support_simple_spinner_dropdown_item);
        work_adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        work.setAdapter(work_adapter);
        work.setOnItemSelectedListener(this);

        ArrayAdapter<CharSequence> short_break_adapter = ArrayAdapter.createFromResource(this,R.array.short_break_array, R.layout.support_simple_spinner_dropdown_item);
        short_break_adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        short_break.setAdapter(short_break_adapter);
        short_break.setOnItemSelectedListener(this);

        ArrayAdapter<CharSequence> long_break_adapter = ArrayAdapter.createFromResource(this,R.array.long_break_array, R.layout.support_simple_spinner_dropdown_item);
        long_break_adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        long_break.setAdapter(long_break_adapter);
        long_break.setOnItemSelectedListener(this);

        ArrayAdapter<CharSequence> rounds_adapter = ArrayAdapter.createFromResource(this,R.array.rounds_array, R.layout.support_simple_spinner_dropdown_item);
        rounds_adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        rounds.setAdapter(rounds_adapter);
        rounds.setOnItemSelectedListener(this);

        notifSwitch = findViewById(R.id.notificationSwitchID);

        saveBtn = findViewById(R.id.saveBtnID);

        //database
        db = new DatabaseHelper(this);

        notifSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                enableNotification = isChecked;
                Log.i("SWITCH","SWITCH --------------------> "+String.valueOf(enableNotification));

            }
        });

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(workDuration.isEmpty() || short_breakDuration.isEmpty() || long_breakDuration.isEmpty() || number_of_rounds.isEmpty()){
                    Toast.makeText(SettingsActivity.this,"Please Select A Value For Each Field.",Toast.LENGTH_SHORT).show();

                    Log.i("ERRROR", "onClick: --> work:"+workDuration+" short:"+short_breakDuration+" long:"+long_breakDuration+" rounds:"+number_of_rounds);
                }
                else{
                   boolean successfully_updated = db.updateData(1,Integer.valueOf(workDuration),
                            Integer.valueOf(short_breakDuration),
                            Integer.valueOf(long_breakDuration),
                            Integer.valueOf(number_of_rounds),
                            enableNotification);
                   if(successfully_updated){
                       finish();
                       Toast.makeText(SettingsActivity.this,"Restart App To View Changes!",Toast.LENGTH_LONG).show();
                   }
                   else{
                       Toast.makeText(SettingsActivity.this,"ERROR In Updating Options!",Toast.LENGTH_SHORT).show();
                   }
                }
            }
        });


    }




    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if(parent.getId()==R.id.workID){
            workDuration = parent.getItemAtPosition(position).toString();
            Log.i("SPINNER","SELECTED --------------------> "+workDuration);
        }

        if(parent.getId()==R.id.shortID){
            short_breakDuration = parent.getItemAtPosition(position).toString();
            Log.i("SPINNER","SELECTED --------------------> "+short_breakDuration);
        }

        if(parent.getId()==R.id.longID){
            long_breakDuration = parent.getItemAtPosition(position).toString();
            Log.i("SPINNER","SELECTED --------------------> "+long_breakDuration);
        }

        if(parent.getId()==R.id.roundID){
            number_of_rounds = parent.getItemAtPosition(position).toString();
            Log.i("SPINNER","SELECTED --------------------> "+number_of_rounds);

        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}