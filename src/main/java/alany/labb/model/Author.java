package alany.labb.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Author {
    private final int authorId;
    private final String firstName;
    private final String lastName;
    private final LocalDate birthDay;
    private final List<Book> books;

    public Author(int id, String firstName, String lastName, LocalDate birthDay) {
        this.authorId = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.birthDay = birthDay;
        this.books = new ArrayList<>();
    }

    public int getAuthorId() {
        return authorId;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public LocalDate getBirthDay() {
        return birthDay;
    }

    public List<Book> getBooks() {
        return new ArrayList<>(books);
    }

    public void addBook(Book book){
        for (Book b: books){
            if (b.getBookId() == book.getBookId()){
                return;
            }
        }
        books.add(book);
    }

    @Override
    public String toString() {
        return firstName + " " + lastName;
    }
}