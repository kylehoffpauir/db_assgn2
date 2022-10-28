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
            ArrayList<String> variables = new ArrayList<String>();
            //take in and store column info
            System.out.println("Reading variables - to continue, simply hit enter on all three fields");
            while(keepReading) {
                String var = getVariableInformation(in);
                if(!var.equals(""))
                    variables.add(var);
                else
                    keepReading = false;
            }
            //build string for create
            String sqlCreate = "CREATE TABLE " + tableName + " (";
            for(int i = 0; i < variables.size()-1; i++) {
                sqlCreate += variables.get(i) + ", ";
            }
            sqlCreate += variables.get(variables.size()-1);

            System.out.println("Specify primary key(s)? (Y / N)");
            String option = in.nextLine();
            // if we have a primary key add the , PRIMARY KEY (var))
            switch(option.toUpperCase()){
                case "Y":
                    System.out.println("Specify number of primary keys");
                    int numKeys = in.nextInt();
                    in.nextLine();
                    String key = "";
                    for(int i = 0; i < numKeys; i++) {
                        System.out.println("Enter the variable would you like to set as the primary key:");
                        key += in.nextLine() + ", ";

                    }
                    key = key.substring(0, key.length()-2);
                    sqlCreate += ", PRIMARY KEY ( " + key + " ))";
                    break;
                case "N":
                default:
                    sqlCreate += ")";
                    break;
            }
            //send create statement to the DB
            stmt.executeUpdate(sqlCreate);
        } catch (Exception e) {
                System.err.println("ERROR - error creating table");
                e.printStackTrace();
                return;
        }
        System.out.println("Wrote table to SQL database");
        return;
    }//end createTable

    //adapted from https://www.baeldung.com/jdbc-check-table-exists
    private static boolean tableExistsSQL(Connection connection, String tableName) throws SQLException {
        DatabaseMetaData meta = connection.getMetaData();
        ResultSet resultSet = meta.getTables(null, null, tableName, new String[] {"TABLE"});
        return resultSet.next();
    }


    private static String getVariableInformation(Scanner in) {
        String varName = "";
        String varType = "";
        String varLength = "";
        System.out.println("What would you like the variable to be named?");
        varName = in.nextLine();
        System.out.println("What would you like the type of the data to be?");
        varType = in.nextLine();
        System.out.println("What would you like the size of the data to be?\n" +
            "If this datatype does not take size, simply hit enter to skip.");
        varLength = in.nextLine();
        if((varLength + varName + varType).equals(""))
            return "";

        if(varLength.equals("")) {
            return varName + " " + varType.toUpperCase();
        } else {
            if (varLength.contains("(")) {
                return varName + " " + varType.toUpperCase() + varLength;
            } else {
                return varName + " " + varType.toUpperCase() + "(" + varLength + ")";
            }
        }
    }

    //insert into an existing table
    public static void insertEntry(Connection database, Scanner in) throws IOException {
        String sqlInsert = "";
        try {
            Statement stmt = database.createStatement();
            //if table already exists, kick user out
            System.out.print("Enter table name: ");
            String tableName = in.nextLine();
            System.out.println();
            if (!tableExistsSQL(database, tableName)) {
                System.err.println("Error! cannot add to table " + tableName + " as it does not exist!");
                return;
            }
            ArrayList<String> variables = new ArrayList<String>();
            //take in and store column info
            //System.out.println("Reading variables - to continue, simply hit enter on all three fields");
            DatabaseMetaData metadata = database.getMetaData();
            ResultSet resultSet = metadata.getColumns(null, null, tableName, null);
            String varNames = "(";
            while (resultSet.next()) {
                String name = resultSet.getString("COLUMN_NAME");
                String type = resultSet.getString("TYPE_NAME");
                int size = resultSet.getInt("COLUMN_SIZE");
                System.out.println("ENTER VALUE FOR " + name + " " + type + "(" + size + "):");
                String value = in.nextLine();
                if(type == "VARCHAR" || type == "CHAR") {
                    value = "\"" + value + "\"";
                }
                variables.add(value);
                varNames += name + ", ";
            }
            //build string for create
            sqlInsert += "INSERT INTO " + tableName + " " + varNames.substring(0, varNames.length()-2) + ") VALUES (";
            for(int i = 0; i < variables.size()-1; i++) {
                sqlInsert += variables.get(i) + ", ";
            }
            sqlInsert += variables.get(variables.size()-1) + ")";

            //send create statement to the DB
            stmt.executeUpdate(sqlInsert);
        } catch (Exception e) {
            System.err.println("ERROR - error inserting into table");
            System.err.println(sqlInsert);
            e.printStackTrace();
            return;
        }
        System.out.println("Wrote entry to SQL database");
        return;
    }//end insert


    //mark db entry for removal
    public static void removeEntry(Connection database, Scanner in) {
        boolean keepReading = true;
        String sqlRemove = "";
        try {
            Statement stmt = database.createStatement();
            //if table already exists, kick user out
            System.out.print("Enter table name: ");
            String tableName = in.nextLine();
            System.out.println();
            if (!tableExistsSQL(database, tableName)) {
                System.err.println("Error! cannot add to table " + tableName + " as it does not exist!");
                return;
            }
            ArrayList<String> variables = new ArrayList<String>();
            //take in and store column info
            //System.out.println("Reading variables - to continue, simply hit enter on all three fields");
            DatabaseMetaData metadata = database.getMetaData();
            ResultSet resultSet = metadata.getColumns(null, null, tableName, null);
            System.out.println("Enter name of column you would like to specify a value to delete from");
            String colToDelete = in.nextLine();
            while (resultSet.next()) {
                String name = resultSet.getString("COLUMN_NAME");
                if(!colToDelete.equals(name))
                    continue;
                String type = resultSet.getString("TYPE_NAME");
                int size = resultSet.getInt("COLUMN_SIZE");
                System.out.println("ENTER VALUE FOR " + name + " " + type + "(" + size + "):");
                String value = in.nextLine();
                if(type == "VARCHAR" || type == "CHAR") {
                    value = "\"" + value + "\"";
                }
                variables.add(value);
                //varNames += name + ", ";
            }
            //build string for create
            sqlRemove = "DELETE FROM " + tableName + " WHERE " + colToDelete + " = " +  variables.get(0);
            //send create statement to the DB
            stmt.executeUpdate(sqlRemove);
        } catch (Exception e) {
            System.err.println("ERROR - error inserting into table");
            System.err.println(sqlRemove);
            e.printStackTrace();
            return;
        }
        System.out.println("Deleted row from SQL database");
        return;
    }//end remove


    //print contents of dbfile
	//TODO FIX THE WAY THIS PRINTS -- SHOULD PRINT THE NAME OF PEOPLE WHO CHECKED OUT WITH AUTHOR TITLE
	//TODO IN A SEPERATE PRINT BELOW THE MAIN, PRINT EVERYONE WHO HAS NOT CHECKED OUT A BOOK
    public static void printTableContents(Connection database, Scanner in) {
        try {
            Statement stmt = database.createStatement();
            //if table already exists, kick user out
            System.out.print("Enter table name: ");
            String tableName = in.nextLine();
            System.out.println();
            if (!tableExistsSQL(database, tableName)) {
                System.err.println("Error! cannot add to table " + tableName + " as it does not exist!");
                return;
            }
            ArrayList<String> variables = new ArrayList<String>();
            //take in and store column info
            //System.out.println("Reading variables - to continue, simply hit enter on all three fields");
            DatabaseMetaData metadata = database.getMetaData();
            ResultSet resultSet = stmt.executeQuery( "select * from " + tableName);
            ResultSetMetaData rsmd = resultSet.getMetaData();
            int numCol = rsmd.getColumnCount();

            //PRINTING THE TABLE
            for (int i = 1; i <= numCol; i++) {
                if (i > 1) System.out.print(",\t");
                String columnName = rsmd.getColumnName(i);
                System.out.print(columnName);
            }
            System.out.println("");
            while (resultSet.next()) {
                for (int i = 1; i <= numCol; i++) {
                    if (i > 1) System.out.print(",\t");
                    String columnValue = resultSet.getString(i);
                    System.out.print(columnValue);
                }
                System.out.println("");
            }

            stmt.close();

        } catch (Exception e) {
            System.err.println("ERROR - error printing table");
            e.printStackTrace();
            return;
        }
        return;
    }//end printFile

    private static void returnBook(Connection database, Scanner in) {
    }

    private static void checkout(Connection database, Scanner in) {
    }

    private static void listUsers(Connection database, Scanner in) {
    }

    private static void includeNewEntry(Connection database, Scanner in, String tableName) {
        boolean keepReading = true;
        String sqlInsert = "";
        try {
            while(keepReading) {
                sqlInsert = "";
                int totalLengthOfVars = 0;
                Statement stmt = database.createStatement();
                System.out.println();
                if (!tableExistsSQL(database, tableName)) {
                    System.err.println("Error! cannot add to table " + tableName + " as it does not exist!");
                    return;
                }
                ArrayList<String> variables = new ArrayList<String>();
                //take in and store column info
                //System.out.println("Reading variables - to continue, simply hit enter on all three fields");
                DatabaseMetaData metadata = database.getMetaData();
                ResultSet resultSet = metadata.getColumns(null, null, tableName, null);
                String varNames = "(";
                while (resultSet.next()) {
                    String name = resultSet.getString("COLUMN_NAME");
                    String type = resultSet.getString("TYPE_NAME");
                    int size = resultSet.getInt("COLUMN_SIZE");
                    System.out.println("ENTER VALUE FOR " + name + " " + type + "(" + size + "):");
                    String value = in.nextLine();
                    if ((type == "VARCHAR" || type == "CHAR") && value.length() != 0) {
                        value = "\"" + value + "\"";
                    }
                    variables.add(value);
                    totalLengthOfVars += value.length();
                    varNames += name + ", ";
                }
                if (totalLengthOfVars == 0)
                    return;
                //build string for create
                sqlInsert += "INSERT INTO " + tableName + " " + varNames.substring(0, varNames.length() - 2) + ") VALUES (";
                for (int i = 0; i < variables.size() - 1; i++) {
                    sqlInsert += variables.get(i) + ", ";
                }
                sqlInsert += variables.get(variables.size() - 1) + ")";

                //send create statement to the DB
                stmt.executeUpdate(sqlInsert);
                System.out.println("Wrote entry to SQL database");
            }
        } catch (Exception e) {
            System.err.println("ERROR - error inserting into table");
            System.err.println(sqlInsert);
            e.printStackTrace();
            return;
        }
        return;
    }

    private static void includeNewUser(Connection database, Scanner in) {
        includeNewEntry(database, in, "people");
        return;
    }

    private static void includeNewBook(Connection database, Scanner in) {
        includeNewEntry(database, in, "books");
        return;
    }

}//end Main class