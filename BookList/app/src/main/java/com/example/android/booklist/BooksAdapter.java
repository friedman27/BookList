package com.example.android.booklist;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;


// custom ArrayAdapter that holds CityAttraction objects
public class BooksAdapter extends ArrayAdapter<Book> {

    // provides a simple name for log entries
    private static final String LOG_TAG = BooksAdapter.class.getSimpleName();

    public BooksAdapter(Activity context, ArrayList<Book> books) {
        super(context, 0, books);
    }

    public BooksAdapter(Activity context) {
        super(context, 0);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Check if the existing view is being reused, otherwise inflate the view
        View listItemView = convertView;
        if(listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(
                    R.layout.list_item, parent, false);
        }

        Book currentBook = getItem(position);

        //////////////////////// TITLE //////////////////////////////////////////////////
        TextView titleTextView = (TextView) listItemView.findViewById(R.id.title);
        titleTextView.setText(currentBook.getTitle());

        //////////////////////// AUTHOR /////////////////////////////////////////////////
        TextView authorTextView = (TextView) listItemView.findViewById(R.id.author);
        authorTextView.setText(currentBook.getAuthor());

        return listItemView;
    }

}

