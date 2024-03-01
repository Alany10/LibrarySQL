package alany.labb.model;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

public class Book {
    private final int bookId;
    private final String isbn;
    private final String title;
    private final Date published;
    private final Genre genre;
    private int rating;
    private final List<Author> authors;

    public Book(int bookId, String isbn, String title, Date published, Genre genre, int rating, List<Author> authors) {
        this.bookId = bookId;
        this.isbn = isbn;
        this.title = title;
        this.published = published;
        this.genre = genre;
        this.rating = rating;
        this.authors = new ArrayList<>();
        this.authors.addAll(authors);
    }
    public Book(int bookId, String isbn, String title, Date published, Genre genre, int rating) {
        this.bookId = bookId;
        this.isbn = isbn;
        this.title = title;
        this.published = published;
        this.genre = genre;
        this.rating = rating;
        this.authors = new ArrayList<>();
    }

    public int getBookId() { return bookId; }
    public String getIsbn() { return isbn; }
    public String getTitle() { return title; }
    public Date getPublished() { return published; }
    public Genre getGenre() {
        return genre;
    }
    public int getRating() {
        return rating;
    }
    public void setRating(int rating) {
        this.rating = rating;
    }
    public List<Author> getAuthors() {
        return new ArrayList<>(authors);
    }
    public void addAuthor(Author author){
        for (Author a: authors){
            if (a.getAuthorId() == author.getAuthorId()){
                return;
            }
        }
        authors.add(author);
    }

    @Override
    public String toString() {
        return title + ", " + isbn + ", " + published.toString() + ", " + genre + ", " + rating;
    }
}