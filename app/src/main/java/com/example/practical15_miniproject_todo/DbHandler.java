package com.example.practical15_miniproject_todo;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class DbHandler extends SQLiteOpenHelper {

    //Pass Context to super constructor
    public DbHandler(Context context) {
        super(context, Params.DB_NAME, null, Params.DB_VERSION);
    }

    //SQL create table query
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String create_sql = "CREATE TABLE " + Params.TABLE_NAME + "( "
                + Params.KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + Params.COL_TEXT + " VARCHAR(200)"
                + ")";

        sqLiteDatabase.execSQL(create_sql);
        Log.d("DBTest", "Table created successfully");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    public void addTask(Task task) {
        //Initializing DB
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        //Assigning member variables with columns
        values.put(Params.COL_TEXT, task.getText());

        //Insert query
        db.insert(Params.TABLE_NAME, null, values);
        Log.d("DBTest", "Successfully inserted");

        //Closing DB
        db.close();
    }

    public List<Task> getAllTasks() {
        //Initializing array of tasks
        List<Task> taskList = new ArrayList<>();
        //Initializing DB
        SQLiteDatabase db = this.getReadableDatabase();

        //Fetch query
        String select_query = "SELECT * FROM " + Params.TABLE_NAME;
        Cursor cursor = db.rawQuery(select_query, null);

        //Iterate through task array
        if(cursor.moveToFirst()) {
            do {
                Task task = new Task();
                task.setID(Integer.parseInt(cursor.getString(0)));
                task.setText(cursor.getString(1));
                taskList.add(task);
            }
            while (cursor.moveToNext());
        }
        //Closing DB
        db.close();

        return taskList;
    }

    public int updateTask(Task task) {
        //Initializing DB
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        //Updating with new variables
        values.put(Params.COL_TEXT, task.getText());

        //Update query
        int rows = db.update(Params.TABLE_NAME, values, Params.KEY_ID + "=?", new String[]{String.valueOf(task.getID())});

        //Closing DB
        db.close();

        return rows;
    }

    public void deleteTask(int id) {
        //Initializing DB
        SQLiteDatabase db = this.getWritableDatabase();

        //Delete query
        db.delete(Params.TABLE_NAME, Params.KEY_ID + "=?", new String[]{String.valueOf(id)});

        //Closing DB
        db.close();
    }

    //Delete all tasks
    public void reset() {
        //Initializing DB
        SQLiteDatabase db = this.getWritableDatabase();

        //Delete query
       db.delete(Params.TABLE_NAME, null, null);

        //Closing DB
        db.close();
    }


}
