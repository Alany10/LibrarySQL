package alany.labb.model;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class BooksDb implements IBooksDb {
    private final List<Book> books;
    private final List<Author> authors;
    private final String url;
    private final String user;
    private final String password;

    public BooksDb(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
        this.books = new ArrayList<>();
        this.authors = new ArrayList<>();
    }

    @Override
    public List<Book> getBooks() {
        return new ArrayList<>(books);
    }

    @Override
    public List<Author> getAuthors() {
        return new ArrayList<>(authors);
    }

    @Override
    public boolean connect() throws BooksDbException {
        try {
            retrieveBooks();
            retrieveAuthors();
            retrieveRelations();
            return true; // Indicate successful connection
        } catch (BooksDbException e) {
            throw new BooksDbException("Failed to connect to the database", e);
        }
    }

    @Override
    public void disconnect() throws BooksDbException {
        try (Connection connection = DriverManager.getConnection(url, user, password)) {
            // Clear existing relations before reseting all my books and authors.
            String deleteAllRelationsQuery = "DELETE FROM author_book";
            try (PreparedStatement deleteStatement = connection.prepareStatement(deleteAllRelationsQuery)) {
                deleteStatement.executeUpdate();
            }

            updateBooks(connection);
            updateAuthors(connection);
            updateRelation(connection);

        } catch (SQLException e) {
            throw new BooksDbException("Failed to disconnect from the database", e);
        }
    }

    private void retrieveBooks() throws BooksDbException {
        try (Connection connection = DriverManager.getConnection(url, user, password);
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM book");
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                int bookId = resultSet.getInt("id");
                String isbn = resultSet.getString("ISBN");
                String title = resultSet.getString("title");
                Date published = resultSet.getDate("published");
                Genre genre = Genre.valueOf(resultSet.getString("genre"));
                int rating = resultSet.getInt("rating");

                books.add(new Book(bookId, isbn, title, published, genre, rating));
            }
        } catch (Exception e) {
            throw new BooksDbException("Failed to fetch books from the database", e);
        }
    }

    private void retrieveAuthors() throws BooksDbException {
        try (Connection connection = DriverManager.getConnection(url, user, password);
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM author");
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String firstName = resultSet.getString("firstName").toLowerCase();
                String lastName = resultSet.getString("lastName").toLowerCase();
                Date birthDay = resultSet.getDate("birthDay");
                authors.add(new Author(id, firstName, lastName, birthDay.toLocalDate()));
            }
        } catch (SQLException e) {
            throw new BooksDbException("Failed to fetch authors from the database", e);
        }
    }

    private void retrieveRelations() throws BooksDbException{
        try (Connection connection = DriverManager.getConnection(url, user, password);
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM author_book");
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                int authorId = resultSet.getInt("author_id");
                int bookId = resultSet.getInt("book_id");

                for (Book book: books){
                    if (book.getBookId() == bookId){
                        for (Author author: authors){
                            if (author.getAuthorId() == authorId){
                                book.addAuthor(author);
                                author.addBook(book);
                            }
                        }
                    }
                }

            }
        } catch (SQLException e) {
            throw new BooksDbException("Failed to fetch authors from the database", e);
        }
    }

    private void updateBooks(Connection connection) throws SQLException {
        // First, delete all existing books
        String deleteBooksQuery = "DELETE FROM book";

        try (PreparedStatement deleteStatement = connection.prepareStatement(deleteBooksQuery)) {
            deleteStatement.executeUpdate();
        }

        // Then, reset auto-increment to start from 1
        String resetAutoIncrementQuery = "ALTER TABLE book AUTO_INCREMENT = 1";

        try (PreparedStatement resetAutoIncrementStatement = connection.prepareStatement(resetAutoIncrementQuery)) {
            resetAutoIncrementStatement.executeUpdate();
        }

        // Now, insert the new books from the Java list
        String insertBookQuery = "INSERT INTO book (isbn, title, published, genre, rating) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement insertStatement = connection.prepareStatement(insertBookQuery)) {
            for (Book book : books) {
                insertStatement.setString(1, book.getIsbn());
                insertStatement.setString(2, book.getTitle());
                insertStatement.setDate(3, book.getPublished());
                insertStatement.setString(4, book.getGenre().toString());
                insertStatement.setInt(5, book.getRating());
                insertStatement.executeUpdate();
            }
        }
    }

    private void updateAuthors(Connection connection) throws SQLException {
        // First, delete all existing authors
        String deleteAuthorsQuery = "DELETE FROM author";

        try (PreparedStatement deleteStatement = connection.prepareStatement(deleteAuthorsQuery)) {
            deleteStatement.executeUpdate();
        }

        // Then, reset auto-increment to start from 1
        String resetAutoIncrementQuery = "ALTER TABLE author AUTO_INCREMENT = 1";

        try (PreparedStatement resetAutoIncrementStatement = connection.prepareStatement(resetAutoIncrementQuery)) {
            resetAutoIncrementStatement.executeUpdate();
        }

        // Now, insert or update the authors from the Java list
        String insertAuthorQuery = "INSERT INTO author (firstName, lastName, birthDay) VALUES (?, ?, ?)";

        try (PreparedStatement insertStatement = connection.prepareStatement(insertAuthorQuery)) {
            for (Author author : authors) {
                insertStatement.setString(1, author.getFirstName());
                insertStatement.setString(2, author.getLastName());
                insertStatement.setDate(3, Date.valueOf(author.getBirthDay()));
                insertStatement.executeUpdate();
            }
        }
    }

    private void updateRelation(Connection connection) throws SQLException {
        // Insert updated relations
        String insertRelationsQuery = "INSERT INTO author_book (author_id, book_id) VALUES (?, ?)";
        try (PreparedStatement insertStatement = connection.prepareStatement(insertRelationsQuery)) {
            for (Book book : books) {
                for (Author author : book.getAuthors()) {
                    insertStatement.setInt(1, author.getAuthorId());
                    insertStatement.setInt(2, book.getBookId());
                    insertStatement.executeUpdate();
                }
            }
        }
    }

    @Override
    public List<Book> searchBooksByTitle(String searchTitle) throws BooksDbException {
        List<Book> result = new ArrayList<>();
        searchTitle = searchTitle.toLowerCase();
        for (Book book : books) {
            if (book.getTitle().toLowerCase().contains(searchTitle)) {
                result.add(book);
            }
        }
        return result;
    }

    @Override
    public List<Book> searchBooksByISBN(String isbn) throws BooksDbException {
        List<Book> result = new ArrayList<>();
        for (Book book : books) {
            if (book.getIsbn().contains(isbn)) {
                result.add(book);
            }
        }
        return result;
    }

    @Override
    public List<Book> searchBooksByAuthor(String name) throws BooksDbException {
        List<Book> result = new ArrayList<>();
        name = name.toLowerCase();
        for (Book book : books) {
            for (Author author: book.getAuthors()){
                if ((author.getFirstName() + " " + author.getLastName()).contains(name)) {
                    result.add(book);
                }

            }
        }
        return result;
    }

    @Override
    public List<Book> searchBooksByRating(String ratingStr) throws BooksDbException {
        List<Book> result = new ArrayList<>();
        int rating = Integer.parseInt(ratingStr);
        for (Book book : books) {
                if (book.getRating() == rating){
                    result.add(book);
            }
        }
        return result;
    }

    @Override
    public List<Book> searchBooksByGenre(String genreStr) throws BooksDbException {
        List<Book> result = new ArrayList<>();
        Genre genre = Genre.valueOf(genreStr);
        for (Book book : books) {
            if (book.getGenre() == genre){
                result.add(book);
            }
        }
        return result;
    }

    @Override
    public void rateBook(Book book, int rating) {
        for (Book b: books){
            if (b.getBookId() == book.getBookId()){
                b.setRating(rating);
                break;
            }
        }
    }

    @Override
    public void createBook(String title, String isbn, String genre, String rating){
        for (Book book: books){
            if (book.getIsbn().equals(isbn)){
                return;
            }
        }
        Book book = new Book(highestBookId(),
                isbn, title,
                Date.valueOf(LocalDate.now()),
                Genre.valueOf(genre),
                Integer.parseInt(rating));
        books.add(book);
    }

    @Override
    public void createAuthor(String firstName, String lastName, LocalDate birthDay){
        Author author = new Author(highestAuthorId(), firstName, lastName, birthDay);
        authors.add(author);
    }

    @Override
    public boolean createRelation(Book book, Author author){
        for (Book b: books){
            if (b.getBookId() == book.getBookId()){
                for (Author a: b.getAuthors()){
                    if (a.getAuthorId() == author.getAuthorId()){
                        return false;
                    }
                }
            }
        }
        book.addAuthor(author);
        author.addBook(book);
        return true;
    }

    @Override
    public void updateDb() throws BooksDbException {
        try (Connection connection = DriverManager.getConnection(url, user, password)) {
            // Clear existing relations before reseting all my books and authors.
            String deleteAllRelationsQuery = "DELETE FROM author_book";
            try (PreparedStatement deleteStatement = connection.prepareStatement(deleteAllRelationsQuery)) {
                deleteStatement.executeUpdate();
            }

            updateBooks(connection);
            updateAuthors(connection);
            updateRelation(connection);

        } catch (SQLException e) {
            throw new BooksDbException("Failed to disconnect from the database", e);
        }
    }
    private int highestBookId(){
        int highestId = 1;
        for (Book book: books){
            if (book.getBookId() > highestId){
                highestId = book.getBookId();
            }
        }
        return highestId;
    }

    private int highestAuthorId(){
        int highestId = 1;
        for (Author author: authors){
            if (author.getAuthorId() > highestId){
                highestId = author.getAuthorId();
            }
        }
        return highestId;
    }
}