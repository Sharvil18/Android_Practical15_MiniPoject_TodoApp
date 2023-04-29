package com.example.practical15_miniproject_todo;

import static android.content.Intent.getIntent;

import static androidx.core.content.ContextCompat.startActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {
    //Application Context member variable
    private Activity context;
    //Array list of all the tasks member variable
    private List<Task> taskList;
    private DbHandler db;
    private AlertDialog.Builder builder;

    //Constructor initializing member variables
    public TaskAdapter(Activity context, List<Task> taskList) {
        this.context = context;
        this.taskList = taskList;
    }

    //Command which view to render
    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //Initialize view
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.task_list_row,parent,false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        //Init task
        Task task = taskList.get(position);

        //Init DB
        db = new DbHandler(context);

        //Set text in text view
        holder.textView.setText(task.getText());

        //Get Reminder
        String reminder = db.getReminder(task);

        //Set Reminder in text view
        if (reminder.equals("00:00:00"))
            holder.textViewReminder.setText(" No reminder");
        else
            holder.textViewReminder.setText(reminder);

        holder.btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Init task
                Task t =  taskList.get(holder.getAdapterPosition());

                //Get id
                int ID = t.getID();

                //Get text
                String text = t.getText();

                //Init dialogue
                Dialog dialog = new Dialog(context);

                //Set content view
                dialog.setContentView(R.layout.dialog_update);

                //Get width and height
                int width = WindowManager.LayoutParams.MATCH_PARENT;
                int height = WindowManager.LayoutParams.WRAP_CONTENT;

                //Get window
                dialog.getWindow().setLayout(width, height);

                //show dialog
                dialog.show();

                //Assign variables
                EditText editText = dialog.findViewById(R.id.editTxt);
                Button btnUpdate = dialog.findViewById(R.id.btn_update);

                //Set text for edit text
                editText.setText(text);

                btnUpdate.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //dismiss dialog
                        dialog.dismiss();

                        //Get updated text from edit text
                        String updatedText = editText.getText().toString().trim();

                        //Update text in DB
                        Task task1 = new Task();
                        task1.setID(ID);
                        task1.setText(updatedText);
                        db.updateTask(task1);

                        //Notify when data is updated
                        taskList.clear();
                        taskList.addAll(db.getAllTasks());
                        notifyDataSetChanged();
                    }
                });
            }
        });

        holder.btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                builder = new AlertDialog.Builder(view.getContext());

                //Setting message mannually
                builder.setMessage("Are you sure you want to delete this task?").setCancelable(false).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //Get task
                        Task t = taskList.get(holder.getAdapterPosition());

                        //Delete text from DB
                        db.deleteTask(t.getID());

                        //Notify when task is deleted
                        int position = holder.getAdapterPosition();
                        taskList.remove(position);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, taskList.size());
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //Cancel dialog
                        dialogInterface.cancel();
                    }
                });
                //Init dialog box
                AlertDialog alertDialog = builder.create();
                //Setting title
                alertDialog.setTitle("Delete Confirmation");
                alertDialog.show();
            }
        });


        holder.btnReminder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Init task
                Task t =  taskList.get(holder.getAdapterPosition());

                //Get id
                int ID = t.getID();

                //Get text
                String text = t.getText();

                //Init dialogue
                Dialog dialog = new Dialog(context);

                //Set content view
                dialog.setContentView(R.layout.dialog_reminder);

                //Get width and height
                int width = WindowManager.LayoutParams.MATCH_PARENT;
                int height = WindowManager.LayoutParams.WRAP_CONTENT;

                //Get window
                dialog.getWindow().setLayout(width, height);

                //show dialog
                dialog.show();

                //Assign variables
                TimePicker timePicker = (TimePicker) dialog.findViewById(R.id.reminderTime);
                Button btnSetReminder = dialog.findViewById(R.id.btnSetReminder);

                btnSetReminder.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //dismiss dialog
                        dialog.dismiss();

                        //Get Time
                        int hour = timePicker.getCurrentHour();
                        int min = timePicker.getCurrentMinute();

                        StringBuilder sbTime = new StringBuilder();
                        sbTime.append(hour).append(":").append(min);

                        SimpleDateFormat simpleDateFormat24 = new SimpleDateFormat("HH:mm");
                        SimpleDateFormat simpleDateFormat12 = new SimpleDateFormat("hh:mm a");
                        String time12;
                        try {
                             Date time24 = (Date) simpleDateFormat24.parse(sbTime.toString());
                             time12 = simpleDateFormat12.format(time24);
                        } catch (ParseException e) {
                            throw new RuntimeException(e);
                        }

                        Toast.makeText(context, "Reminder successfully set on " + time12, Toast.LENGTH_LONG).show();

                        //Update text in DB
                        Task task1 = new Task();
                        task1.setID(ID);
                        task1.setText(text);
                        task1.setReminder(time12);
                        task1.setIsRemainder(0);
                        db.addReminder(task1);



                        //TODO: Mechanism to call configure reminder everytime reminder is changed
                        String reminder = db.getReminder(task1);
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

                            Log.d("Notification Test 3", reminder + "\n" + date + "\n" + time24 + "\n" + localTime + "\n" + localDate + "\n" + localDateTime + "\n" + finalDate);

                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }

                        Timer timer = new Timer();
                        timer.schedule(new MyTimeTask2(task1, finalDate), finalDate);

                        db.setIsReminder(task1);





                        //Notify when data is updated
                        taskList.clear();
                        taskList.addAll(db.getAllTasks());
                        Log.d("Reload test", taskList.toString());
                        notifyDataSetChanged();

                    }
                });
            }
        });
    }

    class MyTimeTask2 extends TimerTask {
        private Task task;
        private Date date;
        public MyTimeTask2(Task task, Date date) {
            this.task = task;
            this.date = date;
        }

        @Override
        public void run() {
            NotificationManager mNotificationManager;

            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(context, "notify" + String.valueOf(task.getID()));
            Intent ii = new Intent(context, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, ii, 0);

            NotificationCompat.BigTextStyle bigText = new NotificationCompat.BigTextStyle();
            bigText.bigText("Reminder to start the task in 5 minutes!");
            bigText.setBigContentTitle(task.getText());


            mBuilder.setContentIntent(pendingIntent);
            mBuilder.setSmallIcon(R.drawable.ic_alarm);
            mBuilder.setContentTitle(task.getText());
            mBuilder.setContentText("Task Reminder");
            mBuilder.setPriority(Notification.PRIORITY_MAX);
            mBuilder.setStyle(bigText);

            mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            {
                String channelId = "channelID"  + String.valueOf(task.getID());
                NotificationChannel channel = new NotificationChannel(
                        channelId,
                        "Channel human readable title",
                        NotificationManager.IMPORTANCE_HIGH);
                mNotificationManager.createNotificationChannel(channel);
                mBuilder.setChannelId(channelId);
            }
            mNotificationManager.notify(task.getID(), mBuilder.build());

            Log.d("Notification Test", "Notification test working properly " + date);
        }
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    public class TaskViewHolder extends RecyclerView.ViewHolder {
        //initialize variables
        TextView textView, textViewReminder;
        ImageView btnEdit, btnDelete, btnReminder;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.txtView);
            textViewReminder = itemView.findViewById(R.id.txtViewReminder);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            btnReminder = itemView.findViewById(R.id.btnReminder);
        }
    }
}
