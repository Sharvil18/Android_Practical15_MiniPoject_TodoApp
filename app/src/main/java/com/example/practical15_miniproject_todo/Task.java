package com.example.practical15_miniproject_todo;

import java.sql.Time;

public class Task {
    private int ID;
    private String text;
    private String reminder;
    private int isRemainder;

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getReminder() {
        return reminder;
    }

    public void setReminder(String reminder) {
        this.reminder = reminder;
    }

    public int getIsRemainder() {
        return isRemainder;
    }

    public void setIsRemainder(int isRemainder) {
        this.isRemainder = isRemainder;
    }
}
