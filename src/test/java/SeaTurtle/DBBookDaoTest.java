package SeaTurtle;

import SeaTurtle.dao.DBBookDao;
import SeaTurtle.model.Book;
import SeaTurtle.ui.TextUI;
import io.cucumber.messages.internal.com.google.gson.internal.bind.SqlDateTypeAdapter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.Scanner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import SeaTurtle.dao.DBBookDao;

public class DBBookDaoTest {

    private DBBookDao dbBookDao;

    @Before
    public void setUp() {
        dbBookDao = new DBBookDao("jdbc:sqlite:seaturtletest.db");
    }

    @After
    public void emptyDb() throws SQLException {
        dbBookDao.dropTable();
    }

    public ArrayList<Book> exampleBooks() {
        ArrayList<Book> books = new ArrayList<>();        
        books.add(new Book("test", "tester", "666"));
        books.add(new Book("history of apple", "someone at apple", "70"));
        books.add(new Book("computers r cool", "dijkstra", "1000"));
        return books;
    }

    @Test
    public void createsABookAndRetrievesFromDB() throws Exception {
        Book testBook = exampleBooks().get(0);
        dbBookDao.create(testBook);
        
        assertTrue(dbBookDao.list().contains(testBook));
    }

    @Test
    public void createsMultipleBooksAndRetrievesFromDB() throws SQLException{
        ArrayList<Book> expectedBooks = exampleBooks();
        for (int i = 0; i < expectedBooks.size(); i++) {
            dbBookDao.create(expectedBooks.get(i));
        }
    
        assertEquals(expectedBooks, dbBookDao.list());
    }



}
