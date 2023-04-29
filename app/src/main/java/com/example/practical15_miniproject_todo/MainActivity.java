package com.example.practical15_miniproject_todo;

import static android.content.Context.NOTIFICATION_SERVICE;

import static androidx.core.content.ContextCompat.getSystemService;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.sql.Time;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    //Declaring entities
    EditText editText;
    Button btnAdd, btnReset;
    RecyclerView recyclerView;

    //Declaring array list of model aka tasks
    List<Task> taskList = new ArrayList<>();

    //Declaring linear layout manager
    LinearLayoutManager linearLayoutManager;

    //Declaring adapter
    TaskAdapter taskAdapter;

    //Declaring DB
    DbHandler dbHandler;

    //Declaring alert dialog builder
    AlertDialog.Builder builder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Setting entities
        editText = (EditText) findViewById(R.id.editTxt);
        btnAdd = (Button) findViewById(R.id.btnAdd);
        btnReset = (Button) findViewById(R.id.btnReset);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);

        //Initializing DB
        dbHandler = new DbHandler(MainActivity.this);

//        //Populating dummy tasks in array list
//        for(int i=0;i<100;i++) {
//            Task task = new Task();
//            task.setID(100 + i);
//            task.setText("Task #" + i);
//            dbHandler.addTask(task);
//        }

        taskList = dbHandler.getAllTasks();

        //Initialize linear layout manager
        linearLayoutManager = new LinearLayoutManager(this);

        //Set layout manager
        recyclerView.setLayoutManager(linearLayoutManager);

        //Initialize adapter
        taskAdapter = new TaskAdapter(MainActivity.this, taskList);

        //Set adapter
        recyclerView.setAdapter(taskAdapter);

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Get string from edit text
                String txt = editText.getText().toString().trim();

                //Check if text is empty
                if (!txt.equals("")) {
                    //Init task
                    Task task = new Task();
                    task.setText(txt);

                    //Insert task in DB
                    dbHandler.addTask(task);

                    //clear text area
                    editText.setText("");
                    taskList.clear();

                    //Notify when task is inserted
                    Toast.makeText(getApplicationContext(), "Task Successfully Added!", Toast.LENGTH_LONG).show();

                    taskList.addAll(dbHandler.getAllTasks());
                    taskAdapter.notifyDataSetChanged();
                } else {
                    builder = new AlertDialog.Builder(MainActivity.this);

                    //Setting message manually
                    builder.setMessage("The text field cannot be empty!")
                            .setCancelable(false)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.cancel();
                                }
                            });

                    //Init alert box
                    AlertDialog alert = builder.create();

                    //Setting title
                    alert.setTitle("Invalid Action Alert");
                    alert.show();
                }
            }
        });

        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                builder = new AlertDialog.Builder(MainActivity.this);

                //Setting message manually
                builder.setMessage("Are you sure you want to delete all your tasks?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Delete all tasks from DB
                                dbHandler.reset();
                                taskList.clear();
                                taskList.addAll(dbHandler.getAllTasks());
                                taskAdapter.notifyDataSetChanged();
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.cancel();
                            }
                        });

                //Init alert box
                AlertDialog alert = builder.create();

                //Setting title
                alert.setTitle("Reset Confirmation");
                alert.show();
            }
        });

        configureReminders();

//        TODO: Add a reminder mechanism to send notification 5min before the time set in database

//       ------------- TESTING SECTION -------------


//        //Inserting tasks
//        Task task = new Task();
//        task.setID(101);
//        task.setText("Task 101");
//        dbHandler.addTask(task);
//
//        Task task2 = new Task();
//        task2.setID(102);
//        task2.setText("Task 102");
//        dbHandler.addTask(task2);
//
//        Task task3 = new Task();
//        task3.setID(104);
//        task3.setText("Task 104");
//        dbHandler.addTask(task3);
//
//        //Delete task
//        dbHandler.deleteTask(104);

        //Fetching all tasks
        List<Task> allTasks = dbHandler.getAllTasks();
        for (Task t : allTasks) {
            Log.d("dbTest", "ID : " + t.getID() + "\n"
                    + "Text : " + t.getText() + "\n"
                    + "Reminder : " + t.getReminder());
        }

//        //Update task
//        task3.setID(103);
//        task3.setText("Task 103");
//        int rowAffected = dbHandler.updateTask(task3);
//        Log.d("DBTest", "Number of affected rows are = " + rowAffected);

//        //Reset
//        dbHandler.reset();
//        Log.d("dbTest", "Reset successful");
//
//        //Fetching all tasks
//        List<Task> allTasks2 = dbHandler.getAllTasks();
//        for(Task t : allTasks2) {
//            Log.d("dbTest", "ID : " + t.getID() + "\n"
//                    + "Text : " + t.getText());
//        }
    }

    public void configureReminders() {
        for (int i=0;i<taskList.size();i++) {
            Task task = taskList.get(i);

            Log.d("Reload test 2", String.valueOf(taskList.size()));
            int isReminder = dbHandler.getIsReminder(task);
            if(!(task.getReminder().equals("00:00:00")) && (isReminder == 0)) {
                String reminder = dbHandler.getReminder(task);
                Date dateCombined;
                Date finalDate;

                try {
                    Date date = new SimpleDateFormat("hh:mm a").parse(reminder);
                    String time24 = new SimpleDateFormat("HH:mm:ss").format(date);
                    LocalTime localTime = null;
                    LocalDate localDate = null;
                    LocalDateTime localDateTime = null;
                    dateCombined = null;

                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        localTime = LocalTime.parse(time24);
                        localDate = LocalDate.now();
                        localDateTime = localTime.atDate(localDate);
                        dateCombined = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
                    }

                    Calendar c = Calendar.getInstance();
                    c.setTime(dateCombined);
                    c.add(Calendar.MINUTE, -5);
                    finalDate = c.getTime();

                    Log.d("Notification Test 2", reminder + "\n" + date + "\n" + time24 + "\n" + localTime + "\n" + localDate + "\n" + localDateTime + "\n" + finalDate);

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

                Timer timer = new Timer();
                timer.schedule(new MyTimeTask(task, finalDate, i), finalDate);

                dbHandler.setIsReminder(task);
            }


        }
    }

    private class MyTimeTask extends TimerTask {

        private Task task;
        private Date date;

        private int i;

        public MyTimeTask(Task task, Date date, int i) {
            this.task = task;
            this.date = date;
            this.i = i;
        }

        @Override
        public void run() {
            NotificationManager mNotificationManager;

            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(getApplicationContext(), "notify" + String.valueOf(i));
            Intent ii = new Intent(getApplicationContext(), MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, ii, 0);

            NotificationCompat.BigTextStyle bigText = new NotificationCompat.BigTextStyle();
            bigText.bigText("Reminder to start the task in 5 minutes!");
            bigText.setBigContentTitle(task.getText());


            mBuilder.setContentIntent(pendingIntent);
            mBuilder.setSmallIcon(R.drawable.ic_alarm);
            mBuilder.setContentTitle(task.getText());
            mBuilder.setContentText("Task Reminder");
            mBuilder.setPriority(Notification.PRIORITY_MAX);
            mBuilder.setStyle(bigText);

            mNotificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            {
                String channelId = "channelID"  + String.valueOf(i);
                NotificationChannel channel = new NotificationChannel(
                        channelId,
                        "Channel human readable title",
                        NotificationManager.IMPORTANCE_HIGH);
                mNotificationManager.createNotificationChannel(channel);
                mBuilder.setChannelId(channelId);
            }
            mNotificationManager.notify(i, mBuilder.build());

            Log.d("Notification Test", "Notification test working properly " + date);
        }
    }

}

