package com.example.practical15_miniproject_todo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

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
                if(!txt.equals("")) {
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
                }
                else {
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
        for(Task t : allTasks) {
            Log.d("dbTest", "ID : " + t.getID() + "\n"
            + "Text : " + t.getText());
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
}