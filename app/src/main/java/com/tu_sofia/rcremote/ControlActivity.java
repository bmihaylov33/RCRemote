package com.tu_sofia.rcremote;

import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.DragEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

public class ControlActivity extends AppCompatActivity {

    LinearLayout target1, target2, target3;
    Button test1, test2, test3, btn_motors, btn_time, btn_distance, btn_subimt;

    String str_motors = "";
    String str_time = "";
    String str_distance = "";

    List<String> list = new ArrayList<String>();
    StringBuilder csvList = new StringBuilder();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                finish();
                startActivity(getIntent());

                Snackbar.make(view, "Create new", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        for(String s : list){
            csvList.append(s);
            csvList.append(",");
        }


        //editor.putString("myList", csvList.toString());
        
        target1 = (LinearLayout) findViewById(R.id.target1);
        target2 = (LinearLayout) findViewById(R.id.target2);
        target3 = (LinearLayout) findViewById(R.id.target3);

        test1 = (Button) findViewById(R.id.test1);
        test2 = (Button) findViewById(R.id.test2);
        test3 = (Button) findViewById(R.id.test3);

        btn_motors   = (Button) findViewById(R.id.btn_motors);
        btn_time     = (Button) findViewById(R.id.btn_time);
        btn_distance = (Button) findViewById(R.id.btn_distance);

        btn_subimt = (Button) findViewById(R.id.btn_submit);

        target1.setOnDragListener(dragListener);
        target2.setOnDragListener(dragListener);
        target3.setOnDragListener(dragListener);

        btn_motors.setOnLongClickListener(longClickListener);
        btn_time.setOnLongClickListener(longClickListener);
        btn_distance.setOnLongClickListener(longClickListener);

        btn_motors.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        btn_subimt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //..add strings to list and save them
            }
        });

    }

    View.OnLongClickListener longClickListener = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            ClipData data = ClipData.newPlainText("", "");
            View.DragShadowBuilder myShadowBuilder = new View.DragShadowBuilder(v);
            v.startDrag(data, myShadowBuilder, v, 0);
            return true;
        }
    };

    View.OnDragListener dragListener = new View.OnDragListener() {
        @Override
        public boolean onDrag(View v, DragEvent event) {

            int dragEvent = event.getAction();
            final View view = (View) event.getLocalState();

            switch (dragEvent) {
                case DragEvent.ACTION_DRAG_ENTERED:

                    break;
                case DragEvent.ACTION_DRAG_EXITED:

                    break;
                case DragEvent.ACTION_DROP:

                    if (view.getId() == R.id.btn_motors && v.getId() == R.id.target1) {
                        LinearLayout oldparent = (LinearLayout) view.getParent();
                        oldparent.removeView(view);
                        LinearLayout newParent = (LinearLayout) v;
                        test1.setVisibility(View.GONE);
                        newParent.addView(view);
                        str_motors = "20";
                        list.add(str_motors);
                        Toast.makeText(ControlActivity.this, "Dropped", Toast.LENGTH_SHORT).show();
                    } else if (view.getId() == R.id.btn_time && v.getId() == R.id.target2) {
                        LinearLayout oldparent = (LinearLayout) view.getParent();
                        oldparent.removeView(view);
                        LinearLayout newParent = (LinearLayout) v;
                        test2.setVisibility(View.GONE);
                        newParent.addView(view);
                        str_time = "30";
                        list.add(str_time);
                        Toast.makeText(ControlActivity.this, "Dropped", Toast.LENGTH_SHORT).show();
                    } else if (view.getId() == R.id.btn_distance && v.getId() == R.id.target3) {
                        LinearLayout oldparent = (LinearLayout) view.getParent();
                        oldparent.removeView(view);
                        LinearLayout newParent = (LinearLayout) v;
                        test3.setVisibility(View.GONE);
                        newParent.addView(view);
                        str_distance = "50";
                        list.add(str_distance);
                        Toast.makeText(ControlActivity.this, "Dropped", Toast.LENGTH_SHORT).show();
//                    } else if (view.getId() == R.id.btn_motors && v.getId() == R.id.target2) {
//                        LinearLayout oldparent = (LinearLayout) view.getParent();
//                        oldparent.removeView(view);
//                        LinearLayout newParent = (LinearLayout) v;
//                        test1.setVisibility(View.GONE);
//                        newParent.addView(view);
//                        str_motors = "20";
//                        Toast.makeText(ControlActivity.this, "Dropped", Toast.LENGTH_SHORT).show();
//                    } else if (view.getId() == R.id.btn_time && v.getId() == R.id.target3) {
//                        LinearLayout oldparent = (LinearLayout) view.getParent();
//                        oldparent.removeView(view);
//                        LinearLayout newParent = (LinearLayout) v;
//                        test2.setVisibility(View.GONE);
//                        newParent.addView(view);
//                        str_time = "30";
//                        Toast.makeText(ControlActivity.this, "Dropped", Toast.LENGTH_SHORT).show();
//                    } else if (view.getId() == R.id.btn_distance && v.getId() == R.id.target1) {
//                        LinearLayout oldparent = (LinearLayout) view.getParent();
//                        oldparent.removeView(view);
//                        LinearLayout newParent = (LinearLayout) v;
//                        test3.setVisibility(View.GONE);
//                        newParent.addView(view);
//                        str_distance = "50";
//                        Toast.makeText(ControlActivity.this, "Dropped", Toast.LENGTH_SHORT).show();
                    }

                    break;
            }
            return true;
        }
    };



    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(ControlActivity.this, MainActivity.class);
        startActivity(intent);
    }

    public void save(String file, String text) {
        try {
            FileOutputStream fos = openFileOutput(file, Context.MODE_PRIVATE);
            fos.write(text.getBytes());
            fos.close();
            Toast.makeText(ControlActivity.this, "Saved!", Toast.LENGTH_SHORT).show();
        } catch(Exception e) {
            e.printStackTrace();
            Toast.makeText(ControlActivity.this, "Error saving file!", Toast.LENGTH_SHORT).show();
        }
    }

    public String read(String file) {
        String text = "";

        try{
            FileInputStream fis = openFileInput(file);
            int size = fis.available();
            byte[] buffer = new byte[size];
            fis.read(buffer);
            fis.close();
            text = new String(buffer);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(ControlActivity.this, "Error reading file!", Toast.LENGTH_SHORT).show();
        }
        return text;
    }
}