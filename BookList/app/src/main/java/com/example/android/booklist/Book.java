package com.example.android.booklist;


public class Book {
    // author name
    public String Author;
    // book title
    public String Title;

    public Book(String bookAuthor, String bookTitle) {
        Author = bookAuthor;
        Title = bookTitle;
    }

    public String getAuthor() {
        return Author;
    }

    public String getTitle() {
        return Title;
    }
}
