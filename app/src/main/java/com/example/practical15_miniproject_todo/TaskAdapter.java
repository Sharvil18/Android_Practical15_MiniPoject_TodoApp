package com.example.practical15_miniproject_todo;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

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
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    public class TaskViewHolder extends RecyclerView.ViewHolder {
        //initialize variables
        TextView textView;
        ImageView btnEdit, btnDelete;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.txtView);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
