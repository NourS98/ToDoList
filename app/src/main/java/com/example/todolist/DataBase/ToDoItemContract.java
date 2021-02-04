package com.example.todolist.DataBase;

import android.provider.BaseColumns;

public class ToDoItemContract {

    private ToDoItemContract(){

    }

    public  static class ToDoItemTable implements BaseColumns {

        public static final String TABLE_NAME ="ToDoItem";
        public static final String COLUMN_NAME_TITLE ="title";
        public static final String COLUMN_NAME_PRIORITY ="priority";
        public static final String COLUMN_NAME_STATUS ="status";
        public static final String COLUMN_NAME_DATE ="date";

        public static final String CREATE= "CREATE TABLE " + TABLE_NAME
                +"("+_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                +COLUMN_NAME_TITLE + " TEXT,"
                +COLUMN_NAME_PRIORITY + " TEXT,"
                +COLUMN_NAME_STATUS + " TEXT,"
                +COLUMN_NAME_DATE + " TEXT"
                +")";
        public static final String DROP="DROP TABLE IF EXIST" +TABLE_NAME;
    }


}
