package SeaTurtle.ui;

import SeaTurtle.model.Article;
import SeaTurtle.model.Book;
import SeaTurtle.model.Tag;
import SeaTurtle.dao.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;
import org.apache.commons.validator.UrlValidator;

public class TextUI {

    private BookDao bookDao;
    private ArticleDao articleDao;
    private TagDao tagDao;
    private ArrayList<Book> books;
    private ArrayList<Article> articles;
    private Scanner s;

    public TextUI(Scanner s, BookDao bookDao, ArticleDao articleDao, TagDao tagDao) {
        this.bookDao = bookDao;
        this.articleDao = articleDao;
        this.tagDao = tagDao;
        this.s = s;

        updateBooks();
        updateArticles();
    }

    public void run() {

        while (true) {
            this.help();

            System.out.println("Anna komento:");
            String input = s.nextLine();

            switch (input) {
                case "k":
                    this.addBook(s);
                    break;
                case "a":
                    this.addArticle(s);
                    break;
                case "m":
                    this.updateBookmark(s);
                    break;
                case "l":
                    this.listBooks();
                    break;
                case "e":
                    this.find(s);
                    break;
                case "h":
                    this.help();
                    break;
                case "q":
                    this.exit();
                    return;
                case "f":  //testing purpose only
                    this.addTag(s);
                    break;
                default:
                    System.out.println(ConsoleColors.RED + "komentoa ei tunnistettu." + ConsoleColors.RESET);
                    this.help();
                    break;
            }
        }
    }

    public void addBook(Scanner s) {

        System.out.println("kirjan nimi: ");
        String title = s.nextLine();
        while (title.trim().isEmpty()) {
            System.out.println("anna kirjan nimi:");
            title = s.nextLine();
        }
        Book newBook = new Book(title);

        System.out.println("kirjan kirjoittaja: ");
        String author = s.nextLine();
        if (!author.trim().isEmpty()) {
            newBook.setAuthor(author);
        }

        while (true) {
            System.out.println("kirjan sivumäärä: ");
            String pageCount = s.nextLine();
            if (pageCount.isEmpty()) {
                break;
            } 
            else if (pageCount.matches("\\d+") && Integer.parseInt(pageCount) >= 0) {
                newBook.setPageCount(pageCount);
                System.out.println(
                        "paina [m] jos haluat lisätä kirjaan kirjanmerkin tai mitä tahansa muuta näppäintä tallentaaksesi kirjavinkin");
                String addBookmark = s.nextLine();
                if (addBookmark.equals("m")) {
                    addBookmark(newBook);
                }
                break;
            }
            System.out.println("anna sivumäärä positiivisena numerona tai paina enter, jos haluat jättää kentän tyhjäksi");
        }

        try {
            bookDao.create(newBook);
        } catch (SQLException e) {
            System.out.print(e.getMessage());
        }
        updateBooks();
        
        while (true) {
            System.out.println("paina [t] jos haluat lisätä tageja tai enter, jos et halua");
            String addTag = s.nextLine();
            if (addTag.isEmpty()) {
                break;
            }
            else if (addTag.equals("t")) {
                System.out.println("anna tagi:");
                String tag = s.nextLine();
                String bookId = Integer.toString(books.size());
                try {
                    tagDao.create(new Tag(tag, bookId));
                } catch (SQLException e) {
                    System.err.println(e);
                }
            }
        }

        System.out.println(ConsoleColors.GREEN + "kirjavinkki lisätty" + ConsoleColors.RESET);

        listBooks();

        while (true) {
            System.out.println("[k] lisää uusi kirjavinkki\n" + "[v] palaa valikkoon\n");
            String choice = s.nextLine();
            if (choice.equals("k")) {
                addBook(s);
                break;
            }

            else if (choice.equals("v")) {
                return;
            }
        }
    }

    public boolean listBooks() {
        System.out.println("");
        System.out.println("kaikki kirjavinkit:");
        Collections.sort(books);

        if (books.isEmpty()) {
            System.out.println("ei kirjavinkkejä");
            System.out.println("");
            return false;
        } else {
            for (Book book : books) {
                System.out.println((books.indexOf(book) + 1) + ") " + book.toString());
            }
        }
        System.out.println("");
        return true;
    }

    public void updateBooks() {
        try {
            books = bookDao.list();
        } catch (SQLException e) {
            System.out.println(e);
        }
    }

    public void updateBookmark(Scanner s) {
        if (listBooks()) {
            while (true) {
                System.out.println(
                        "anna sen kirjan numero, jolle haluat asettaa kirjanmerkin, tai paina enter palataksesi valikkoon: ");
                String selectBook = s.nextLine();
                if (selectBook.isEmpty()) {
                    break;
                } else if (selectBook.matches("\\d+")) {
                    int bookIndex = Integer.parseInt(selectBook);
                    if (bookIndex <= books.size() + 1 && bookIndex > 0) {
                        Book book = books.get(bookIndex - 1);
                        if (book.getPageCount() != null) {
                            addBookmark(book);
                            try {
                                bookDao.updateBookmark(book);
                            } catch (SQLException e) {
                                System.out.print(e.getMessage());
                            }
                            updateBooks();
                            listBooks();
                            break;
                        } else {
                            System.out.println("kirjalla ei ole sivumäärää, joten kirjanmerkkiä ei voida lisätä");
                            System.out.println("");
                        }
                    } else {
                        System.out.println("väärä kirjan numero");
                        System.out.println("");
                    }
                }
            }
        }
    }

    public void addBookmark(Book book) {
        while (true) {
            System.out.println("kirjanmerkin sivunumero: ");
            String bookmark = s.nextLine();
            if (bookmark.isEmpty()) {
                break;
            } else if (bookmark.matches("\\d+")
                    && Integer.parseInt(bookmark) <= Integer.parseInt(book.getPageCount())
                    && Integer.parseInt(bookmark) >= 0) {
                book.setBookmark(bookmark);
                break;
            }
            System.out.println(
                    "sivunumero ei saa olla negatiivinen tai sivumäärää suurempi. paina enter, jos haluat jättää kentän tyhjäksi.");
        }
    }

    public void addArticle(Scanner s) {

        System.out.println("artikkelin otsikko: ");
        String title = s.nextLine();
        while (title.trim().isEmpty()) {
            System.out.println("anna artikkelin otsikko:");
            title = s.nextLine();
        }
        Article newArticle = new Article(title);

        while (true) {
            System.out.println("artikkelin URL-osoite (tai tyhjä): ");
            String url = s.nextLine();
            String[] schemes = {"http","https"};
            UrlValidator urlValidator = new UrlValidator(schemes);
            if (url.trim().isEmpty()) {
                break;
            } else if (urlValidator.isValid(url.trim())) {
                newArticle.setUrl(url);
                break;
            }
            System.out.println("URL-osoite oli virheellinen. anna oikeanmuotoinen URL-osoite (esim. https://www.hs.fi). paina enter, jos haluat jättää kentän tyhjäksi.");
        }

        try {
            articleDao.create(newArticle);
        } catch (SQLException e) {
            System.out.print(e.getMessage());
        }
        updateArticles();

        System.out.println(ConsoleColors.GREEN + "artikkelivinkki lisätty" + ConsoleColors.RESET);

        listArticles();

        while (true) {
            System.out.println("[a] lisää uusi artikkelivinkki\n" + "[v] palaa valikkoon\n");
            String choice = s.nextLine();
            if (choice.equals("a")) {
                addArticle(s);
                break;
            }

            else if (choice.equals("v")) {
                return;
            }
        }
    }

    public boolean listArticles() {
        System.out.println("");
        System.out.println("kaikki artikkelivinkit:");
        Collections.sort(articles);

        if (articles.isEmpty()) {
            System.out.println("ei artikkelivinkkejä");
            System.out.println("");
            return false;
        } else {
            for (Article article : articles) {
                System.out.println((articles.indexOf(article) + 1) + ") " + article.toString());
            }
        }
        System.out.println("");
        return true;
    }

    public void updateArticles() {
        try {
            articles = articleDao.list();
        } catch (SQLException e) {
            System.out.println(e);
        }
    }

    public void find(Scanner s) {
        ArrayList<Book> bookSearchResults = new ArrayList<>();
        ArrayList<Article> articleSearchResults = new ArrayList<>();
//        ArrayList<Tag> tagSearchResults = new ArrayList<>();

        while (true) {
            System.out.println("\n[k] hae vain kirjavinkeistä\n" 
            + "[a] hae vain artikkelivinkeistä\n"
            + "[ak] hae kaikista vinkeistä\n"
            + "[t] hae tageista\n"
            + "[v] poistu valikkoon\n");
            String searchArea = s.nextLine();
            
            if (searchArea.equals("v")) {
                return;
            }

            while (true) {
                System.out.println("anna hakutermi (tyhjällä takaisin edelliseen näkymään):");
                String searchTerm = s.nextLine();

                if (searchTerm.equals(""))
                    break;

                if (searchTerm.equals("t")) {
                    /*
                    try {
                        tagSearchResults = tagDao.findAndList(searchTerm);
                    } catch (SQLException e) {
                        System.err.println(e);
                    }
                    */
                }

                if (searchArea.contains("k")) {
                    try {
                        bookSearchResults = bookDao.findAndList(searchTerm);
                    } catch (SQLException e) {
                        System.err.println(e);
                    }
                }
                
                if (searchArea.contains("a")) {
                    try {
                        articleSearchResults = articleDao.findAndList(searchTerm);
                    } catch (SQLException e) {
                        System.err.println(e);
                    }
                }
                
                System.out.println("Löydetyt lukuvinkit:");
                bookSearchResults.forEach(System.out::println);
                articleSearchResults.forEach(System.out::println);
//                tagSearchResults.forEach(System.out::println);
                System.out.println();
                bookSearchResults.clear();
                articleSearchResults.clear();
//                tagSearchResults.clear();

            }
        }
    }

    public void addTag(Scanner s) {

        System.out.println("anna uusi tag");
        String tag = s.nextLine();


        try {
            bookDao.list().forEach(System.out::println);
            System.out.println("anna kirjan id");
            String bookId = s.nextLine();
            tagDao.create(new Tag(tag, bookId));
             System.out.println("---");
            tagDao.list().forEach(System.out::println);
        } catch (SQLException e) {
            System.err.println(e);
        }


    }



    public void help() {
        System.out.println("\n" + "Käytettävissä olevat komennot:\n" 
        + "[k] lisää uusi kirjavinkki\n"
        + "[a] lisää uusi artikkelivinkki\n" 
        + "[m] lisää tai päivitä kirjanmerkki\n"        
        + "[l] listaa kaikki kirjavinkit\n" 
        + "[e] etsi lukuvinkki\n" 
        + "---\n" 
        + "[h] listaa komennot\n"
        + "[q] poistu ohjelmasta\n");
    }

    public void exit() {
        System.out.println("\nHei hei!\n");
        return;
    }

}
