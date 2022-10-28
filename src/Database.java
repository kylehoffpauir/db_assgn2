import java.io.*;
import java.sql.*;
import java.util.*;

public class Database {
    private static final String CATALOG = "dbfiles.db";
    private static final String CATALOG_PATH = "dbfiles/";
    private static final boolean DEBUG = false;
    public static ArrayList<String> TO_DELETE = new ArrayList<String>();

    public static void main(String[] args) throws IOException, SQLException, ClassNotFoundException {
        String user = "";
        String password = "";
        String url = "jdbc:ucanaccess://stuff.accdb";
        Connection connection = DriverManager.getConnection(url, user, password);
        System.out.println(connection.toString());
        //a database stuff.accdb has to be first created in acess and closed before the program can access it
        //run the program now that the dbFiles loaded
        run(connection);
    } // end main

    public static void run(Connection database) throws IOException {
        //scanner object to prompt for user input and to pass to functions
        Scanner in = new Scanner(System.in);
        String option;
        //continue prompting user until they hit 5 to exit
        boolean done = false;
        while(!done) {
            printMenu();
            option = in.nextLine().toUpperCase();
            System.out.println();
            switch (option) {
                case "A":
                    createTable(database, in);
                    break;
                case "B":
                    insertEntry(database, in);
                    break;
                case "C":
                    removeEntry(database, in);
                    break;
                case "D":
                    printTableContents(database, in);
                    break;
                case "1":
                    includeNewBook(database, in);
                    break;
                case "2":
                    includeNewUser(database, in);
                    break;
                case "3":
                    listUsers(database, in);
                    break;
                case "4":
                    checkout(database, in);
                    break;
                case "5":
                    returnBook(database, in);
                    break;
                case "6":
                    done = true;
                    break;
                default:
                    System.out.println("Error! Enter a valid option");
            }//end switch
            System.out.println("\n");
        }//end infinite while loop
        return;
    }

    public static void printMenu() {
        System.out.println("---------DATABASE-------");
        String[] options = {
            "A - Create a table",
            "B - Include an entry",
            "C - Delete an entry/entries",
            "D - List a table",
            "1 - Include a new book in the collection",
            "2 - Include a new user",
            "3 - List library users with books they checked out",
            "4 - Check out a book",
            "5 - Return a book",
            "6 - Exit"
        };
        for (String option : options){
            System.out.println(option);
        }
        System.out.print("Choose your option : ");
    }


    //create a table and store it in the dbfiles
    public static void createTable(Connection database, Scanner in) throws IOException {
        boolean keepReading = true;
        try {
            Statement stmt = database.createStatement();
            //if table already exists, kick user out
            System.out.print("Enter table name: ");
            String tableName = in.nextLine();
            System.out.println();
            if (tableExistsSQL(database, tableName)) {
                System.err.println("Error! cannot create table " + tableName + " as it already exists!");
                return;
            }
            //take in and store column info
            while(keepReading) {
                System.out.print("Enter column name and it's length: ");
                String line = in.nextLine();
                System.out.println();
                if (line.equals(""))
                    keepReading = false;
                else {
                    String[] lineSplit = line.split(" ");
                    String name = lineSplit[0];
                    int size = Integer.parseInt(line.split(" ")[1]);
                    // ensure single word colNames
                    if (name.contains(" ")) {
                        System.err.println("Error! Db columns must be single words");
                        table.delete();
                        System.err.println("Table deleted.");
                        return;
                    }
                    // add to lists
                    while (name.length() - 1 != size)
                        name += " ";
                    colNames.add(name);
                    colLengths.add(size);
                }
            }
        } catch (Exception e) {
                System.err.println("ERROR - error creating table");
                return;
        }
        System.out.println("Done collecting!");
        //save column and table info to disk
        try {
            //dump(table, colNames, colLengths);
            //TODO write to db
            System.out.println("Wrote to disk!");
        } catch(Exception e) {
            System.err.println("Error writing to disk. Table not saved.");
            if(DEBUG)
                System.err.println(e.toString());
        }

        return;
    }//end createTable

    //adapted from https://www.baeldung.com/jdbc-check-table-exists
    private static boolean tableExistsSQL(Connection connection, String tableName) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement("SELECT count(*) "
            + "FROM information_schema.tables "
            + "WHERE table_name = ?"
            + "LIMIT 1;");
        preparedStatement.setString(1, tableName);

        ResultSet resultSet = preparedStatement.executeQuery();
        resultSet.next();
        return resultSet.getInt(1) != 0;
    }


    private static String getDataType(Scanner in) {

    }


    private static void dump() throws IOException {

    }//end dump


    //insert into an existing table
    public static void insertEntry(Connection database, Scanner in) throws IOException {
        return;
    }//end insert


    //mark db entry for removal
    public static void removeEntry(Connection database, Scanner in) {
       return;
    }//end remove


    //print contents of dbfile
    public static void printTableContents(Connection database, Scanner in) {

    }//end printFile

    private static void returnBook(Connection database, Scanner in) {
    }

    private static void checkout(Connection database, Scanner in) {
    }

    private static void listUsers(Connection database, Scanner in) {
    }

    private static void includeNewUser(Connection database, Scanner in) {
    }

    private static void includeNewBook(Connection database, Scanner in) {
    }

}//end Main class