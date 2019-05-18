package model;

import java.io.Serializable;

public class Book implements Serializable {
    private static final long id = 1L;

    private String bookTitle;
    private String bookAuthor;
    private String bookIMDB;
    private String publisher;
    private int pages;
    private String Isbn;

    public Book() {
    }

    public Book(String bookTitle, String bookAuthor, String bookIMDB,String publisher, int pages, String Isbn) {
        this.bookTitle = bookTitle;
        this.bookAuthor = bookAuthor;
        this.bookIMDB = bookIMDB;
        this.publisher = publisher;
        this.pages = pages;
        this.Isbn = Isbn;
    }

    public String getBookTitle() {
        return bookTitle;
    }

    public void setBookTitle(String bookTitle) {
        this.bookTitle = bookTitle;
    }

    public String getBookAuthor() {
        return bookAuthor;
    }

    public void setBookAuthor(String bookAuthor) {
        this.bookAuthor = bookAuthor;
    }

    public String getBookIMDB() {
        return bookIMDB;
    }

    public void setBookIMDB(String bookIMDB) {
        this.bookIMDB = bookIMDB;
    }

    //For the Medium book poster
    public String getMediumPoster() {
        return "http://covers.openlibrary.org/b/olid/" + bookIMDB + "-M.jpg?default=false";
    }

    //For the Large book poster
    public String getLargePoster(){
        return "http://covers.openlibrary.org/b/olid/" + bookIMDB + "-L.jpg?default=false";
    }
    public String getIsbn(){
        return Isbn;
    }
    public void setIsbn(String Isbn){
        this.Isbn = Isbn;

    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public int getPages() {
        return pages;
    }

    public void setPages(int pages) {
        this.pages = pages;
    }
}
