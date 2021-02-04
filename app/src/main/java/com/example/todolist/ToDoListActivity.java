package com.example.todolist;

import android.app.ListActivity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

import com.example.todolist.DataBase.DataBaseHelper;
import com.example.todolist.DataBase.ToDoItemContract;
import com.example.todolist.ToDoItem.Priority;
import com.example.todolist.ToDoItem.Status;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.ParseException;
import java.util.Date;

public class ToDoListActivity extends ListActivity {

    private static final String TAG = ToDoListActivity.class.getSimpleName();
    private static final String FILE_NAME = "TodoManagerActivityData.txt";
    private static final int ADD_TODO_ITEM_REQUEST = 0;
    private static final int Update_TODO_ITEM_REQUEST = 1;
    private static  String Update_ID ;
    private DataBaseHelper dataBaseHelper ;
    private SQLiteDatabase database;
    SQLiteDatabase dbReadable ;
    // IDs for menu items
    private static final int MENU_DELETE = Menu.FIRST;
    private static final int MENU_DUMP = Menu.FIRST + 1;

    private ToDoListAdapter mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_todolist);
        dataBaseHelper = new DataBaseHelper(this);
         dbReadable = dataBaseHelper.getReadableDatabase();
        database= dataBaseHelper.getWritableDatabase();
        dbReadable = dataBaseHelper.getReadableDatabase();

        // Create a new TodoListAdapter for this ListActivity's ListView
        mAdapter = new ToDoListAdapter(getApplicationContext());
        mAdapter.setOnUpdateDeleteListener(new ToDoListAdapter.OnUpdateDeleteListener() {
            @Override
            public void onDelete(int postion) {
               int rowNum = database.delete(ToDoItemContract.ToDoItemTable.TABLE_NAME, ToDoItemContract.ToDoItemTable.COLUMN_NAME_TITLE +" LIKE ?",new String []{((ToDoItem)mAdapter.getItem(postion)).getTitle()});
               if(rowNum >0)
                {
                    Toast.makeText(ToDoListActivity.this,"Deletes Succussfully" , Toast.LENGTH_SHORT).show();
                }
                mAdapter.clear_item(postion);
            }

            @Override
            public void onUpdate(int postion) {
                Cursor cursor = dbReadable.query(ToDoItemContract.ToDoItemTable.TABLE_NAME,new String[]{ToDoItemContract.ToDoItemTable._ID }, ToDoItemContract.ToDoItemTable.COLUMN_NAME_TITLE +" LIKE ? ", new String[] {((ToDoItem)mAdapter.getItem(postion)).getTitle()}, null , null ,null);
                if(cursor != null && cursor.getCount()>0){
                    cursor.moveToNext();
                    Update_ID = cursor.getString(cursor.getColumnIndexOrThrow(ToDoItemContract.ToDoItemTable._ID));
                }

                Intent intent = new Intent(getApplicationContext(), AddToDoActivity.class);
                intent.putExtra(ToDoItem.TITLE, ((ToDoItem)mAdapter.getItem(postion)).getTitle());
                intent.putExtra(ToDoItem.PRIORITY, ((ToDoItem)mAdapter.getItem(postion)).getPriority().toString());
                intent.putExtra(ToDoItem.STATUS, ((ToDoItem)mAdapter.getItem(postion)).getStatus().toString());
                intent.putExtra(ToDoItem.DATE,ToDoItem.FORMAT.format(((ToDoItem)mAdapter.getItem(postion)).getDate()));
                intent.putExtra("POSITION",postion);
                startActivityForResult(intent, Update_TODO_ITEM_REQUEST);

            }
        });

       // loadItems();
        // Put divider between ToDoItems and FooterView
        getListView().setFooterDividersEnabled(true);

        // TODO - Inflate footerView for footer_view.xml file
        TextView footerView = (TextView) getLayoutInflater().inflate(R.layout.footer_view,null);

        // TODO - Add footerView to ListView
        this.getListView().addFooterView(footerView);



        footerView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "Entered footerView.OnClickListener.onClick()");
                // TODO - Implement OnClick().
                Intent intent = new Intent(getApplicationContext(), AddToDoActivity.class);
                startActivityForResult(intent, ADD_TODO_ITEM_REQUEST);


            }
        });

        // TODO - Attach the adapter to this ListActivity's ListView
          setListAdapter(mAdapter);




    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i(TAG, "Entered onActivityResult()");
        // TODO - Check result code and request code
        //  if user submitted a new ToDoItem
        //  Create a new ToDoItem from the data Intent
        //  and then add it to the adapter
        if(requestCode == ADD_TODO_ITEM_REQUEST){
            if(resultCode == RESULT_OK){
                    ToDoItem todoItem = new ToDoItem(data);
                    mAdapter.add(todoItem);
                    saveItems(todoItem);
            }

        }
        else if(requestCode == Update_TODO_ITEM_REQUEST){
            if(requestCode==RESULT_OK){
                ToDoItem todoItem = new ToDoItem(data);
                int position = data.getExtras().getInt("POSITION");
                if( position != -1){
                    mAdapter.update_item(position , todoItem);
                    updateItems(todoItem);
                    mAdapter.notifyDataSetChanged();
            }

            }


        }


    }

    /****************************************************
     * Do not modify below here
     ***************************************************/

    @Override
    public void onResume() {
        super.onResume();

        // Load saved ToDoItems, if necessary
        if (mAdapter.getCount() == 0) {
            loadItems();
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        // Save ToDoItems
  //      saveItems();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(Menu.NONE, MENU_DELETE, Menu.NONE, "Delete all");
        menu.add(Menu.NONE, MENU_DUMP, Menu.NONE, "Dump to log");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_DELETE:
                int rowNum = database.delete(ToDoItemContract.ToDoItemTable.TABLE_NAME,null,null);
                if(rowNum >0)
                {
                    Toast.makeText(ToDoListActivity.this,"Deletes Succussfully" , Toast.LENGTH_SHORT).show();
                }
                mAdapter.clear();
                return true;
            case MENU_DUMP:
                dump();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void dump() {
        for (int i = 0; i < mAdapter.getCount(); i++) {
            String data = ((ToDoItem) mAdapter.getItem(i)).toLog();
            assert ToDoItem.ITEM_SEP != null;
            Log.i(TAG, "Item " + i + ": " + data.replace(ToDoItem.ITEM_SEP, ","));
        }
    }

    // Load stored ToDoItems
    private void loadItems() {
        Cursor cursor=dbReadable.query(ToDoItemContract.ToDoItemTable.TABLE_NAME,null,null,null,null,null,null);
        Date date=new Date();
        String title =null;
        String priority =null;
        String status =null;
        while(cursor.moveToNext()){
             title = cursor.getString(cursor.getColumnIndexOrThrow(ToDoItemContract.ToDoItemTable.COLUMN_NAME_TITLE));
             priority =cursor.getString(cursor.getColumnIndexOrThrow(ToDoItemContract.ToDoItemTable.COLUMN_NAME_PRIORITY));
             status = cursor.getString(cursor.getColumnIndexOrThrow(ToDoItemContract.ToDoItemTable.COLUMN_NAME_STATUS));
          try {
                 date = ToDoItem.FORMAT.parse(cursor.getString(cursor.getColumnIndexOrThrow(ToDoItemContract.ToDoItemTable.COLUMN_NAME_DATE)));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            mAdapter.add(new ToDoItem(title, Priority.valueOf(priority), Status.valueOf(status), date));

        }
    }

    // Save ToDoItems to file
    private void saveItems(ToDoItem todoItem) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(ToDoItemContract.ToDoItemTable.COLUMN_NAME_TITLE, todoItem.getTitle());
        contentValues.put(ToDoItemContract.ToDoItemTable.COLUMN_NAME_PRIORITY, todoItem.getPriority().toString());
        contentValues.put(ToDoItemContract.ToDoItemTable.COLUMN_NAME_STATUS, todoItem.getStatus().toString());
        contentValues.put(ToDoItemContract.ToDoItemTable.COLUMN_NAME_DATE, ToDoItem.FORMAT.format(todoItem.getDate()));

        long recordId = database.insert(ToDoItemContract.ToDoItemTable.TABLE_NAME , ToDoItemContract.ToDoItemTable.COLUMN_NAME_TITLE, contentValues);
        if(recordId != -1)
        {
            Toast.makeText(ToDoListActivity.this,"Added Succussfully" , Toast.LENGTH_SHORT).show();
        }


    }
    private void updateItems(ToDoItem todoItem) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(ToDoItemContract.ToDoItemTable.COLUMN_NAME_TITLE, todoItem.getTitle());
        contentValues.put(ToDoItemContract.ToDoItemTable.COLUMN_NAME_PRIORITY, todoItem.getPriority().toString());
        contentValues.put(ToDoItemContract.ToDoItemTable.COLUMN_NAME_STATUS, todoItem.getStatus().toString());
        contentValues.put(ToDoItemContract.ToDoItemTable.COLUMN_NAME_DATE, ToDoItem.FORMAT.format(todoItem.getDate()));
        System.out.println("ID*****************"+Update_ID);
        long recordId = database.update(ToDoItemContract.ToDoItemTable.TABLE_NAME , contentValues , ToDoItemContract.ToDoItemTable._ID+" LIKE ?" , new String[]{Update_ID} );
        if(recordId != -1)
        {
            Toast.makeText(ToDoListActivity.this,"Added Succussfully" , Toast.LENGTH_SHORT).show();
        }


    }

}


 //   PrintWriter writer = null;
//        try {
//            FileOutputStream fos = openFileOutput(FILE_NAME, MODE_PRIVATE);
//            writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(fos)));
//            for (int idx = 0; idx < mAdapter.getCount(); idx++) {
//                writer.println(mAdapter.getItem(idx));
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        } finally {
//            if (null != writer) {
//                writer.close();
//            }
//        }

/*
*   BufferedReader reader = null;
        try {
            FileInputStream fis = openFileInput(FILE_NAME);
            reader = new BufferedReader(new InputStreamReader(fis));
            String title = null;
            String priority = null;
            String status = null;
            Date date = null;
            while (null != (title = reader.readLine())) {
                priority = reader.readLine();
                status = reader.readLine();
                date = ToDoItem.FORMAT.parse(reader.readLine());
                mAdapter.add(new ToDoItem(title, Priority.valueOf(priority), Status.valueOf(status), date));
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        } finally {
            if (null != reader) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }*/