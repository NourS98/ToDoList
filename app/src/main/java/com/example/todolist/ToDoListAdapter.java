package com.example.todolist;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.todolist.DataBase.DataBaseHelper;
import com.example.todolist.DataBase.ToDoItemContract;

public class ToDoListAdapter extends BaseAdapter {


    public interface OnUpdateDeleteListener {
        void onDelete(int postion);
        void onUpdate(int postion);
    }
    OnUpdateDeleteListener onUpdateDeleteListener;
    private static final String TAG = ToDoListAdapter.class.getSimpleName();
    private final List<ToDoItem> mItems = new ArrayList<>();
    private final Context mContext;



    public ToDoListAdapter(Context context) {
        mContext = context;

    }

    // Add a ToDoItem to the adapter
    // Notify observers that the data set has changed
    public void add(ToDoItem item) {
        mItems.add(item);
        notifyDataSetChanged();
    }

    // Clears the list adapter of all items.
    public void clear() {
        mItems.clear();
        notifyDataSetChanged();
    }
    public void clear_item(int position) {
        mItems.remove(position);
        notifyDataSetChanged();
    }
    public void update_item(int position ,ToDoItem toDoItem) {
        ToDoItem mtoDoItem =mItems.get(position);
        mtoDoItem.setTitle(toDoItem.getTitle());
        mtoDoItem.setDate(toDoItem.getDate());
        mtoDoItem.setPriority(toDoItem.getPriority());
        mtoDoItem.setStatus(toDoItem.getStatus());
    }

    // Returns the number of ToDoItems
    @Override
    public int getCount() {
        return mItems.size();
    }

    // Retrieve the number of ToDoItems
    @Override
    public Object getItem(int pos) {
        return mItems.get(pos);
    }

    // Get the ID for the ToDoItem
    // In this case it's just the position

    @Override
    public long getItemId(int pos) {
        return pos;
    }

    // Create a View for the ToDoItem at specified position
    // Remember to check whether convertView holds an already allocated View
    // before created a new View.
    // Consider using the ViewHolder pattern to make scrolling more efficient
    // See:
    // http://developer.android.com/training/improving-layouts/smooth-scrolling.html

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        // TODO - Get the current ToDoItem
        final ToDoItem toDoItem = (ToDoItem) getItem(position);

        final ViewHolder viewHolder;

        if (convertView == null) {

            // TODO - Inflate the View for this ToDoItem from todo_item.xml
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.todo_item,parent,false);

            // TODO - Fill in specific ToDoItem data
            // Remember that the data that goes in this View
            // corresponds to the user interface elements defined
            // in the layout file

            viewHolder = new ViewHolder();
            viewHolder.titleView = convertView.findViewById(R.id.titleView);
            viewHolder.statusView = convertView.findViewById(R.id.statusCheckBox);
            viewHolder.priorityView = convertView.findViewById(R.id.priorityView);
            viewHolder.dateView = convertView.findViewById(R.id.dateView);
            viewHolder.deleteImageView = convertView.findViewById(R.id.delete_imgeView);
            viewHolder.updateImageView = convertView.findViewById(R.id.update_imgeView);

            // TODO - set up an OnCheckedChangeListener, which is called when the user toggles the status checkbox
                viewHolder.statusView.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked){
                            toDoItem.setStatus(ToDoItem.Status.DONE);
                        }
                        else
                            toDoItem.setStatus(ToDoItem.Status.NOT_DONE);
                    }
                });
                viewHolder.updateImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(onUpdateDeleteListener !=null){
                            onUpdateDeleteListener.onUpdate((Integer) v.getTag());
                        }
                    }
                });
                viewHolder.deleteImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(onUpdateDeleteListener !=null){
                            onUpdateDeleteListener.onDelete((Integer) v.getTag());
                        }
                    }
                });

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }


        // TODO - Display Title in TextView

            viewHolder.titleView.setText(toDoItem.getTitle());

        // TODO - Set up Status CheckBox
        viewHolder.statusView.setChecked(toDoItem.getStatus() == ToDoItem.Status.DONE);

        // TODO - Display Priority in a TextView
        viewHolder.priorityView.setText(toDoItem.getPriority().toString());


        // TODO - Display Time and Date.
        // Hint - use ToDoItem.FORMAT.format(toDoItem.getDate()) to get date and
        // time String
        viewHolder.dateView.setText(ToDoItem.FORMAT.format(toDoItem.getDate()));

        // Return the View you just created
        viewHolder.deleteImageView.setTag(position);
        viewHolder.updateImageView.setTag(position);
        return convertView;

    }

    public void setOnUpdateDeleteListener(OnUpdateDeleteListener onUpdateDeleteListener) {
        this.onUpdateDeleteListener = onUpdateDeleteListener;
    }

    static class ViewHolder {
        TextView titleView;
        CheckBox statusView;
        TextView priorityView;
        TextView dateView;
        ImageView deleteImageView;
        ImageView updateImageView;
    }

}
