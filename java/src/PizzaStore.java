/*
 * Template JAVA User Interface
 * =============================
 *
 * Database Management Systems
 * Department of Computer Science &amp; Engineering
 * University of California - Riverside
 *
 * Target DBMS: 'Postgres'
 *
 */


import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ArrayList;
import java.lang.Math;

/**
 * This class defines a simple embedded SQL utility class that is designed to
 * work with PostgreSQL JDBC drivers.
 *
 */
public class PizzaStore {

   // reference to physical database connection.
   private Connection _connection = null;

   // handling the keyboard inputs through a BufferedReader
   // This variable can be global for convenience.
   static BufferedReader in = new BufferedReader(
                                new InputStreamReader(System.in));

   /**
    * Creates a new instance of PizzaStore
    *
    * @param hostname the MySQL or PostgreSQL server hostname
    * @param database the name of the database
    * @param username the user name used to login to the database
    * @param password the user login password
    * @throws java.sql.SQLException when failed to make a connection.
    */
   public PizzaStore(String dbname, String dbport, String user, String passwd) throws SQLException {

      System.out.print("Connecting to database...");
      try{
         // constructs the connection URL
         String url = "jdbc:postgresql://localhost:" + dbport + "/" + dbname;
         System.out.println ("Connection URL: " + url + "\n");

         // obtain a physical connection
         this._connection = DriverManager.getConnection(url, user, passwd);
         System.out.println("Done");
      }catch (Exception e){
         System.err.println("Error - Unable to Connect to Database: " + e.getMessage() );
         System.out.println("Make sure you started postgres on this machine");
         System.exit(-1);
      }//end catch
   }//end PizzaStore

   /**
    * Method to execute an update SQL statement.  Update SQL instructions
    * includes CREATE, INSERT, UPDATE, DELETE, and DROP.
    *
    * @param sql the input SQL string
    * @throws java.sql.SQLException when update failed
    */
   public void executeUpdate (String sql) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the update instruction
      stmt.executeUpdate (sql);

      // close the instruction
      stmt.close ();
   }//end executeUpdate

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and outputs the results to
    * standard out.
    *
    * @param query the input query string
    * @return the number of rows returned
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int executeQueryAndPrintResult (String query) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the query instruction
      ResultSet rs = stmt.executeQuery (query);

      /*
       ** obtains the metadata object for the returned result set.  The metadata
       ** contains row and column info.
       */
      ResultSetMetaData rsmd = rs.getMetaData ();
      int numCol = rsmd.getColumnCount ();
      int rowCount = 0;

      // iterates through the result set and output them to standard out.
      boolean outputHeader = true;
      while (rs.next()){
		 if(outputHeader){
			for(int i = 1; i <= numCol; i++){
			System.out.print(rsmd.getColumnName(i) + "\t");
			}
			System.out.println();
			outputHeader = false;
		 }
         for (int i=1; i<=numCol; ++i)
            System.out.print (rs.getString (i) + "\t");
         System.out.println ();
         ++rowCount;
      }//end while
      stmt.close();
      return rowCount;
   }//end executeQuery

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and returns the results as
    * a list of records. Each record in turn is a list of attribute values
    *
    * @param query the input query string
    * @return the query result as a list of records
    * @throws java.sql.SQLException when failed to execute the query
    */
   public List<List<String>> executeQueryAndReturnResult (String query) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the query instruction
      ResultSet rs = stmt.executeQuery (query);

      /*
       ** obtains the metadata object for the returned result set.  The metadata
       ** contains row and column info.
       */
      ResultSetMetaData rsmd = rs.getMetaData ();
      int numCol = rsmd.getColumnCount ();
      int rowCount = 0;

      // iterates through the result set and saves the data returned by the query.
      boolean outputHeader = false;
      List<List<String>> result  = new ArrayList<List<String>>();
      while (rs.next()){
        List<String> record = new ArrayList<String>();
		for (int i=1; i<=numCol; ++i)
			record.add(rs.getString (i));
        result.add(record);
      }//end while
      stmt.close ();
      return result;
   }//end executeQueryAndReturnResult

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and returns the number of results
    *
    * @param query the input query string
    * @return the number of rows returned
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int executeQuery (String query) throws SQLException {
       // creates a statement object
       Statement stmt = this._connection.createStatement ();

       // issues the query instruction
       ResultSet rs = stmt.executeQuery (query);

       int rowCount = 0;

       // iterates through the result set and count nuber of results.
       while (rs.next()){
          rowCount++;
       }//end while
       stmt.close ();
       return rowCount;
   }

   /**
    * Method to fetch the last value from sequence. This
    * method issues the query to the DBMS and returns the current
    * value of sequence used for autogenerated keys
    *
    * @param sequence name of the DB sequence
    * @return current value of a sequence
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int getCurrSeqVal(String sequence) throws SQLException {
	Statement stmt = this._connection.createStatement ();

	ResultSet rs = stmt.executeQuery (String.format("Select currval('%s')", sequence));
	if (rs.next())
		return rs.getInt(1);
	return -1;
   }

   /**
    * Method to close the physical connection if it is open.
    */
   public void cleanup(){
      try{
         if (this._connection != null){
            this._connection.close ();
         }//end if
      }catch (SQLException e){
         // ignored.
      }//end try
   }//end cleanup

   /**
    * The main execution method
    *
    * @param args the command line arguments this inclues the <mysql|pgsql> <login file>
    */
   public static void main (String[] args) {
      if (args.length != 3) {
         System.err.println (
            "Usage: " +
            "java [-classpath <classpath>] " +
            PizzaStore.class.getName () +
            " <dbname> <port> <user>");
         return;
      }//end if

      Greeting();
      PizzaStore esql = null;
      try{
         // use postgres JDBC driver.
         Class.forName ("org.postgresql.Driver").newInstance ();
         // instantiate the PizzaStore object and creates a physical
         // connection.
         String dbname = args[0];
         String dbport = args[1];
         String user = args[2];
         esql = new PizzaStore (dbname, dbport, user, "");

         boolean keepon = true;
         while(keepon) {
            // These are sample SQL statements
            System.out.println("MAIN MENU");
            System.out.println("---------");
            System.out.println("1. Create user");
            System.out.println("2. Log in");
            System.out.println("9. < EXIT");
            String authorisedUser = null;
            switch (readChoice()){
               case 1: CreateUser(esql); break;
               case 2: authorisedUser = LogIn(esql); break;
               case 9: keepon = false; break;
               default : System.out.println("Unrecognized choice!"); break;
            }//end switch
            if (authorisedUser != null) {
              boolean usermenu = true;
              while(usermenu) {
                System.out.println("MAIN MENU");
                System.out.println("---------");
                System.out.println("1. View Profile");
                System.out.println("2. Update Profile");
                System.out.println("3. View Menu");
                System.out.println("4. Place Order"); //make sure user specifies which store
                System.out.println("5. View Full Order ID History");
                System.out.println("6. View Past 5 Order IDs");
                System.out.println("7. View Order Information"); //user should specify orderID and then be able to see detailed information about the order
                System.out.println("8. View Stores"); 

                //**the following functionalities should only be able to be used by drivers & managers**
                System.out.println("9. Update Order Status");

                //**the following functionalities should ony be able to be used by managers**
                System.out.println("10. Update Menu");
                System.out.println("11. Update User");

                System.out.println(".........................");
                System.out.println("20. Log out");
                switch (readChoice()){
                   case 1: viewProfile(esql, authorisedUser); break;
                   case 2: updateProfile(esql, authorisedUser); break;
                   case 3: viewMenu(esql); break;
                   case 4: placeOrder(esql, authorisedUser); break;
                   case 5: viewAllOrders(esql); break;
                   case 6: viewRecentOrders(esql); break;
                   case 7: viewOrderInfo(esql); break;
                   case 8: viewStores(esql); break;
                   case 9: updateOrderStatus(esql); break;
                   case 10: updateMenu(esql); break;
                   case 11: updateUser(esql); break;



                   case 20: usermenu = false; break;
                   default : System.out.println("Unrecognized choice!"); break;
                }
              }
            }
         }//end while
      }catch(Exception e) {
         System.err.println (e.getMessage ());
      }finally{
         // make sure to cleanup the created table and close the connection.
         try{
            if(esql != null) {
               System.out.print("Disconnecting from database...");
               esql.cleanup ();
               System.out.println("Done\n\nBye !");
            }//end if
         }catch (Exception e) {
            // ignored.
         }//end try
      }//end try
   }//end main

   public static void Greeting(){
      System.out.println(
         "\n\n*******************************************************\n" +
         "              User Interface      	               \n" +
         "*******************************************************\n");
   }//end Greeting

   /*
    * Reads the users choice given from the keyboard
    * @int
    **/
   public static int readChoice() {
      int input;
      // returns only if a correct value is given.
      do {
         System.out.print("Please make your choice: ");
         try { // read the integer, parse it and break.
            input = Integer.parseInt(in.readLine());
            break;
         }catch (Exception e) {
            System.out.println("Your input is invalid!");
            continue;
         }//end try
      }while (true);
      return input;
   }//end readChoice

   /*
    * Creates a new user
    **/
   public static void CreateUser(PizzaStore esql){
      String login = null;
      String password = null;
      String confirmPassword = null;
      String phoneNum = null;
      String favoriteItems = null;
      String role = "customer";

      try {
         while(true) {
            System.out.println("Please enter a username! (Maxiumum 50 characters)");
            login = in.readLine();
            if(login.trim().isEmpty()) {
               System.out.println("Username cannot be empty. Please try again.");
            }
            else if(login.length() > 50) {
               System.out.println("Username cannot be over 50 characters. Please try again.");
            }
            else {
               break;
            }
         }
         while(true) {
            System.out.println("Enter a password: ");
            password = in.readLine();
            if(password.trim().isEmpty()) {
               System.out.println("Password cannot be empty. Please try again.");
               continue;
            }
            System.out.println("Confirm your password: ");
            confirmPassword = in.readLine();

            if(password.equals(confirmPassword)) {
               break;
            }
            else {
               System.out.println("The passwords do not match, please try again");
            }
         }
         while(true) {
            System.out.println("Enter your phone number: ");
            phoneNum = in.readLine();
            if(phoneNum.trim().isEmpty()) {
               System.out.println("Phone number cannot be empty. Please try again.");
            }
            else {
               break;
            }
         }
      }
      catch (Exception e) {
        // Handle any exceptions (like input issues)
        System.err.println("Error creating user: " + e.getMessage());
      }
      try{
         String query = String.format("INSERT INTO Users (login, password, role, favoriteItems, phoneNum) " +
                                     "VALUES ('%s', '%s', '%s', '%s', '%s');",
                                     login, password, role, favoriteItems, phoneNum);
         esql.executeUpdate(query);
         esql.executeQueryAndPrintResult("SELECT favoriteItems FROM users WHERE login = 'allie';");
         System.out.println("User created successfully!");
      }
      catch (Exception e) {
         System.err.println (e.getMessage ());
      }
   }

   /*
    * Check log in credentials for an existing user
    * @return User login or null is the user does not exist
    **/
   public static String LogIn(PizzaStore esql){
      String enteredUser;
      String enteredPassword;

      try {
         while(true) {
            System.out.println("Username: ");
            enteredUser = in.readLine();
            System.out.println("Password: ");
            enteredPassword = in.readLine();

            String query = String.format("SELECT login FROM Users WHERE login = '%s' AND password = '%s';",
            enteredUser, enteredPassword
            );
            List<List<String>> result = esql.executeQueryAndReturnResult(query);
            if (!result.isEmpty()) {
               System.out.println("Login successful! Welcome, " + enteredUser);
               return enteredUser;  // Return username on successful login
            } else {
               System.out.println("Invalid username or password. Please try again.");
            }
         }
      }
      catch (Exception e) {
         System.err.println (e.getMessage ());
         return null;
      }
   }//end

// Rest of the functions definition go in here

   public static void viewProfile(PizzaStore esql, String loggedInUser) {
      if(loggedInUser == null) {
         System.out.println("Error: No user is logged in");
         return;
      }
      try {
         String query = String.format("SELECT favoriteItems, phoneNum FROM Users WHERE login = '%s';", loggedInUser
         );

         List<List<String>> result = esql.executeQueryAndReturnResult(query);
         if (result.isEmpty()) {
            System.out.println("Error: User profile not found.");
         }
         System.out.println("Fetching " + loggedInUser + "'s profile");
         String favoriteItems = result.get(0).get(0);
         String phoneNum = result.get(0).get(1);

         if (favoriteItems == null || favoriteItems.trim().isEmpty()) {
            favoriteItems = "(empty)";
         }
         System.out.println("\n---- Profile Info ----");
         System.out.println("Username: " + loggedInUser);
         System.out.println("Phone Number: " + phoneNum);
         System.out.println("Favorite Items: " + favoriteItems);      
      }

      catch (Exception e) {
         System.err.println (e.getMessage ());
         return;
      }
   }
   public static void updateProfile(PizzaStore esql, String loggedInUser) {
      if (loggedInUser == null) {
         System.out.println("Error: No user is logged in");
         return;
      }
      boolean updatingMenu = true;

      while (updatingMenu == true) {
        System.out.println("\n---- Update Profile ----");
        System.out.println("1. Update Password");
        System.out.println("2. Update Phone Number");
        System.out.println("3. Update Favorite Items");
        System.out.println("4. Go Back");
        
        switch (readChoice()) {
         case 1: updatePassword(esql, loggedInUser); break;
         case 2: updateNumber(esql, loggedInUser); break;
         case 3: updateFavItems(esql, loggedInUser); break;
         case 4: updatingMenu = false; break;
         default: System.out.println("Invalid choice.");
        }
      }
   }
   public static void updatePassword(PizzaStore esql, String loggedInUser) {
      String newPassword;
      String confirmPassword;
      try {
         while (true) {
            System.out.println("Enter your new password: ");
            newPassword = in.readLine();

            if(newPassword.trim().isEmpty()) {
               System.out.println("New password cannot be empty. Please try again.");
               continue;
            }
            System.out.println("Confirm your new password: ");
            confirmPassword = in.readLine();
            if(newPassword.equals(confirmPassword)) {
               break;
            }
            else {
               System.out.println("The passwords do not match, please try again");
            }
         }

         String query = String.format("UPDATE Users SET password = '%s' WHERE login = '%s';",
         newPassword, loggedInUser);
         esql.executeUpdate(query);
         System.out.println("Password updated!");
      }
      catch (Exception e) {
         System.err.println (e.getMessage ());
         return;
      }
   }

   public static void updateNumber(PizzaStore esql, String loggedInUser) {
      String newNumber;
      try {
         while (true) {
            System.out.println("Enter your new phone number: ");
            newNumber = in.readLine();

            if(newNumber.trim().isEmpty()) {
               System.out.println("New phone number cannot be empty. Please try again.");
               continue;
            }
            break;
         }

         String query = String.format("UPDATE Users SET phoneNum = '%s' WHERE login = '%s';",
         newNumber, loggedInUser);
         esql.executeUpdate(query);
         System.out.println("Phone number updated!");
      }
      catch (Exception e) {
         System.err.println (e.getMessage ());
         return;
      }
   }
   public static void updateFavItems(PizzaStore esql, String loggedInUser) {
      String newFavItems;
      try {
         System.out.println("Enter your new favorite items: ");
         newFavItems = in.readLine();

         String query = String.format("UPDATE Users SET favoriteItems = '%s' WHERE login = '%s';",
         newFavItems, loggedInUser);
         esql.executeUpdate(query);
         System.out.println("Favorite items updated!");
      }
      catch (Exception e) {
         System.err.println (e.getMessage ());
         return;
      }
   }


   public static void viewMenu(PizzaStore esql) {
      boolean viewingMenu = true;

      while (viewingMenu == true) {
        System.out.println("\n---- Browse Menu ----");
        System.out.println("1. View all items");
        System.out.println("2. Filter by item type");
        System.out.println("3. Filter by item price");
        System.out.println("4. Sort by price (ascending)");
        System.out.println("5. Sort by price (descending)");
        System.out.println("6. Go Back");
        
        switch (readChoice()) {
         case 1: showAllItems(esql); break;
         case 2: filterType(esql); break;
         case 3: filterPrice(esql); break;
         case 4: sortPrice(esql, "ASC"); break;
         case 5: sortPrice(esql, "DESC"); break;
         case 6: viewingMenu = false; break;
         default: System.out.println("Invalid choice.");
        }
      }
   }

   public static void showAllItems(PizzaStore esql){ 
      try {
         String query = String.format("SELECT itemName AS Name, price AS Price, description AS Description FROM Items;");

         List<List<String>> menuItems = esql.executeQueryAndReturnResult(query);
         for (List<String> row : menuItems) {
            System.out.println(row);
         }
      }
      catch (Exception e) {
         System.err.println (e.getMessage ());
         return;
      }
   }
   public static void filterType(PizzaStore esql){ 
      String itemType;
      try {
         System.out.println("Enter type to filter by: ");
         itemType = in.readLine();
         String query = String.format("SELECT itemName AS Name, price AS Price, description AS Description FROM Items " +
            "WHERE TRIM(LOWER(typeOfItem)) = LOWER('%s');", 
         itemType);
         List<List<String>> menuItems = esql.executeQueryAndReturnResult(query);
         for (List<String> row : menuItems) {
            System.out.println(row);
         }
      }
      catch (Exception e) {
         System.err.println (e.getMessage ());
         return;
      }
   }

   public static void filterPrice(PizzaStore esql){ 
      double priceLimit;
      try {
         System.out.println("Enter maximum price of item: ");
         priceLimit = Double.parseDouble(in.readLine().trim());
         String query = String.format("SELECT itemName AS Name, price AS Price, description AS Description FROM Items WHERE price <= %.2f;",
         priceLimit);
         List<List<String>> menuItems = esql.executeQueryAndReturnResult(query);
         for (List<String> row : menuItems) {
            System.out.println(row);
         }
      }
      catch (Exception e) {
         System.err.println (e.getMessage ());
         return;
      }
   }

   public static void sortPrice(PizzaStore esql, String order) {
      try {
         String query = String.format("SELECT itemName AS Name, price AS Price, description AS Desc FROM items ORDER BY price %s;",
         order);

         esql.executeQueryAndPrintResult(query);
      }
      catch (Exception e) {
         System.err.println (e.getMessage ());
         return;
      }
   }

   public static void placeOrder(PizzaStore esql, String loggedInUser) {
      if (loggedInUser == null) {
        System.out.println("Error: No user is logged in.");
        return;
      }
      try {
         int storeID = -1;
         boolean validStore = false;

         while (!validStore) {
            System.out.print("Enter the StoreID of the desired store: ");
            storeID = Integer.parseInt(in.readLine().trim());

            // Check if store exists
            String storeQuery = String.format("SELECT storeID FROM Store WHERE storeID = %d;", storeID);
            List<List<String>> storeResult = esql.executeQueryAndReturnResult(storeQuery);

            if (!storeResult.isEmpty()) {
                validStore = true;  // Store found, proceed
            } else {
                System.out.println("Store ID not found. Please enter a valid store.");
            }
         }
         List<String> itemNames = new ArrayList<>();
         List<Integer> quantities = new ArrayList<>();

         double basketPrice = 0.0;

         boolean ordering = true;

         while(ordering) {
            System.out.println("Enter item name (or type 'done' to finish ordering): ");
            String currItem = in.readLine().trim();

            if (currItem.equalsIgnoreCase("done")) {
               break;
            } 

            System.out.println("Enter desired quantity: ");
            int quantity = Integer.parseInt(in.readLine().trim());

            String priceQuery = String.format("SELECT price FROM items WHERE itemName = '%s';",
            currItem);

            List<List<String>> priceResult = esql.executeQueryAndReturnResult(priceQuery);
            if (priceResult.isEmpty()) {
                System.out.println("System was unable to locate item or price, please check input and try again!");
                continue;
            }
            double currPrice = Double.parseDouble(priceResult.get(0).get(0));
            basketPrice += currPrice * quantity;

            itemNames.add(currItem);
            quantities.add(quantity);
         }
         if (itemNames.isEmpty()) {
            System.out.println("Order cancelled. No items were selected.");
            return;
         }

         int orderID = 1; 
         String getOrderIDQuery = "SELECT MAX(orderID) FROM FoodOrder;";
         List<List<String>> lastUsedID = esql.executeQueryAndReturnResult(getOrderIDQuery);
         if (!lastUsedID.isEmpty() && lastUsedID.get(0).get(0) != null) {
            orderID = Integer.parseInt(lastUsedID.get(0).get(0)) + 1; 
         }
   

         String insertOrder = String.format("INSERT INTO foodorder (orderID, login, storeID, totalPrice, orderTimestamp, orderStatus) " +
         "VALUES (%d, '%s', %d, %.2f, NOW(), 'Pending');",
         orderID, loggedInUser, storeID, basketPrice
         );
         esql.executeUpdate(insertOrder);

        for (int i = 0; i < itemNames.size(); i++) {
            String insertItemQuery = String.format(
                "INSERT INTO ItemsInOrder (orderID, itemName, quantity) VALUES (%d, '%s', %d);",
                orderID, itemNames.get(i), quantities.get(i)
            );
            esql.executeUpdate(insertItemQuery);
        }
         System.out.println("\n Order placed successfully!");
         System.out.println("Order ID: " + orderID);
         System.out.println("Store ID: " + storeID);
         System.out.println("Total Price: $" + String.format("%.2f", basketPrice));
         System.out.println("Items Ordered:");
         for (int i = 0; i < itemNames.size(); i++) {
            System.out.println("- " + itemNames.get(i) + " x" + quantities.get(i));
         }
      }
      catch (Exception e) {
         System.err.println (e.getMessage ());
         return;
      }
   }
   public static void viewAllOrders(PizzaStore esql) {}
   public static void viewRecentOrders(PizzaStore esql) {}
   public static void viewOrderInfo(PizzaStore esql) {}
   public static void viewStores(PizzaStore esql) {}
   public static void updateOrderStatus(PizzaStore esql) {}
   public static void updateMenu(PizzaStore esql) {}
   public static void updateUser(PizzaStore esql) {}


}//end PizzaStore

