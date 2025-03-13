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
                   case 1: viewProfile(esql); break;
                   case 2: updateProfile(esql); break;
                   case 3: viewMenu(esql); break;
                   case 4: placeOrder(esql); break;
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

   public static void viewProfile(PizzaStore esql) {
    System.out.print("Enter your username: ");
    String loggedInUser;
    try {
        loggedInUser = in.readLine().trim();
        // calling helper implementation
        viewProfileHelper(esql, loggedInUser);
    } catch (Exception e) {
        System.err.println(e.getMessage());
    }
}

public static void viewProfileHelper(PizzaStore esql, String loggedInUser) {
   if(loggedInUser == null) {
      System.out.println("Error: No user is logged in");
      return;
   }
   try {
      String query = String.format("SELECT favoriteItems, phoneNum FROM Users WHERE login = '%s';", loggedInUser);

      List<List<String>> result = esql.executeQueryAndReturnResult(query);
      if (result.isEmpty()) {
         System.out.println("Error: User profile not found.");
         return;
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
      System.err.println(e.getMessage());
      return;
   }
}

   public static void updateProfile(PizzaStore esql) {
      System.out.print("Enter your username: ");
      String loggedInUser;
      try {
         loggedInUser = in.readLine().trim();
         updateProfileHelper(esql, loggedInUser);
      } catch (Exception e) {
         System.err.println(e.getMessage());
      }
   }

   public static void updateProfileHelper(PizzaStore esql, String loggedInUser) {
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
   public static void placeOrder(PizzaStore esql) {
      System.out.print("Enter your username: ");
      String loggedInUser;
      try {
         loggedInUser = in.readLine().trim();
         placeOrderHelper(esql, loggedInUser);
      } catch (Exception e) {
         System.err.println(e.getMessage());
      }
   }
   public static void placeOrderHelper(PizzaStore esql, String loggedInUser) {
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
   public static void viewAllOrders(PizzaStore esql) {
   try {
      // Get current logged in user
      System.out.print("Enter your username: ");
      String loggedInUser = in.readLine().trim();
      
      // Check user's role first
      String roleQuery = "SELECT role FROM Users WHERE login = '" + loggedInUser + "';";
      List<List<String>> roleResult = esql.executeQueryAndReturnResult(roleQuery);
      
      if (roleResult.isEmpty()) {
         System.out.println("User not found!");
         return;
      }
      
      String userRole = roleResult.get(0).get(0).trim();
      String targetUser = loggedInUser; // Default to viewing own orders
      
      // Only managers and drivers can view other users' orders
      if (userRole.equals("manager") || userRole.equals("driver")) {
         System.out.print("Enter username to view their orders (or press Enter to view your own): ");
         String enteredUser = in.readLine().trim();
         
         if (!enteredUser.isEmpty()) {
            // Verify the user exists
            String userCheckQuery = "SELECT login FROM Users WHERE login = '" + enteredUser + "';";
            List<List<String>> userResult = esql.executeQueryAndReturnResult(userCheckQuery);
            
            if (userResult.isEmpty()) {
               System.out.println("User not found!");
               return;
            }
            
            targetUser = enteredUser;
         }
      }
      
      // Query to get all orders for the target user
      String ordersQuery = String.format(
         "SELECT o.orderID, o.totalPrice, o.orderTimestamp, o.orderStatus, s.storeID, s.address " +
         "FROM FoodOrder o JOIN Store s ON o.storeID = s.storeID " +
         "WHERE o.login = '%s' " +
         "ORDER BY o.orderTimestamp DESC;",
         targetUser
      );
      
      int rowCount = esql.executeQueryAndPrintResult(ordersQuery);
      
      if (rowCount == 0) {
         System.out.println("No orders found for user: " + targetUser);
      } else {
         System.out.println("\nFound " + rowCount + " orders for user: " + targetUser);
      }
   } catch (Exception e) {
      System.err.println(e.getMessage());
   }
}
 public static void viewRecentOrders(PizzaStore esql) {
   try {
      // Get current logged in user
      System.out.print("Enter your username: ");
      String loggedInUser = in.readLine().trim();
      
      // Check user's role first
      String roleQuery = "SELECT role FROM Users WHERE login = '" + loggedInUser + "';";
      List<List<String>> roleResult = esql.executeQueryAndReturnResult(roleQuery);
      
      if (roleResult.isEmpty()) {
         System.out.println("User not found!");
         return;
      }
      
      String userRole = roleResult.get(0).get(0).trim();
      String targetUser = loggedInUser; // Default to viewing own orders
      
      // Only managers and drivers can view other users' recent orders
      if (userRole.equals("manager") || userRole.equals("driver")) {
         System.out.print("Enter username to view their recent orders (or press Enter to view your own): ");
         String enteredUser = in.readLine().trim();
         
         if (!enteredUser.isEmpty()) {
            // Verify the user exists
            String userCheckQuery = "SELECT login FROM Users WHERE login = '" + enteredUser + "';";
            List<List<String>> userResult = esql.executeQueryAndReturnResult(userCheckQuery);
            
            if (userResult.isEmpty()) {
               System.out.println("User not found!");
               return;
            }
            
            targetUser = enteredUser;
         }
      }
      
      // Query to get the 5 most recent orders for the target user
      String ordersQuery = String.format(
         "SELECT o.orderID, o.totalPrice, o.orderTimestamp, o.orderStatus, s.storeID, s.address " +
         "FROM FoodOrder o JOIN Store s ON o.storeID = s.storeID " +
         "WHERE o.login = '%s' " +
         "ORDER BY o.orderTimestamp DESC " +
         "LIMIT 5;",
         targetUser
      );
      
      int rowCount = esql.executeQueryAndPrintResult(ordersQuery);
      
      if (rowCount == 0) {
         System.out.println("No recent orders found for user: " + targetUser);
      } else {
         System.out.println("\nFound " + rowCount + " recent orders for user: " + targetUser);
      }
   } catch (Exception e) {
      System.err.println(e.getMessage());
   }
}
public static void viewOrderInfo(PizzaStore esql) {
   try {
      // Get current logged in user
      System.out.print("Enter your username: ");
      String loggedInUser = in.readLine().trim();
      
      // Check user's role first
      String roleQuery = "SELECT role FROM Users WHERE login = '" + loggedInUser + "';";
      List<List<String>> roleResult = esql.executeQueryAndReturnResult(roleQuery);
      
      if (roleResult.isEmpty()) {
         System.out.println("User not found!");
         return;
      }
      
      String userRole = roleResult.get(0).get(0).trim();
      
      // Get the order ID to view
      System.out.print("Enter Order ID to view details: ");
      String orderIDStr = in.readLine().trim();
      int orderID;
      
      try {
         orderID = Integer.parseInt(orderIDStr);
      } catch (NumberFormatException e) {
         System.out.println("Invalid Order ID! Please enter a numeric value.");
         return;
      }
      
      // Check if the order exists and get the owner
      String orderCheckQuery = String.format(
         "SELECT orderID, login FROM FoodOrder WHERE orderID = %d;",
         orderID
      );
      
      List<List<String>> orderResult = esql.executeQueryAndReturnResult(orderCheckQuery);
      
      if (orderResult.isEmpty()) {
         System.out.println("Order not found!");
         return;
      }
      
      // Get the user who placed the order
      String orderOwner = orderResult.get(0).get(1);
      
      // Check permissions - only allow if it's the user's own order OR they are manager/driver
      if (!orderOwner.equals(loggedInUser) && !userRole.equals("manager") && !userRole.equals("driver")) {
         System.out.println("You don't have permission to view this order!");
         return;
      }
      
      // Get basic order information
      String orderInfoQuery = String.format(
         "SELECT o.orderID, o.login, o.totalPrice, o.orderTimestamp, o.orderStatus, " +
         "s.storeID, s.address, s.city, s.state " +
         "FROM FoodOrder o JOIN Store s ON o.storeID = s.storeID " +
         "WHERE o.orderID = %d;",
         orderID
      );
      
      System.out.println("\n---- Order Information ----");
      esql.executeQueryAndPrintResult(orderInfoQuery);
      
      // Get items in the order
      String orderItemsQuery = String.format(
         "SELECT io.itemName, io.quantity, i.price, (i.price * io.quantity) AS subtotal " +
         "FROM ItemsInOrder io JOIN Items i ON io.itemName = i.itemName " +
         "WHERE io.orderID = %d;",
         orderID
      );
      
      System.out.println("\n---- Items in Order ----");
      esql.executeQueryAndPrintResult(orderItemsQuery);
      
   } catch (Exception e) {
      System.err.println(e.getMessage());
   }
}
    public static void viewStores(PizzaStore esql) {
      try {
         System.out.println("\n---- Available Stores ----");
         
         String storesQuery = 
            "SELECT storeID, address, city, state, isOpen, reviewScore " +
            "FROM Store " +
            "ORDER BY reviewScore DESC;";
         
         int rowCount = esql.executeQueryAndPrintResult(storesQuery);
         
         System.out.println("\nTotal stores: " + rowCount);
      } catch (Exception e) {
         System.err.println(e.getMessage());
      }
   }
   public static void updateOrderStatus(PizzaStore esql) {
   try {
      // Get current user
      System.out.print("Enter your username: ");
      String username = in.readLine().trim();
      
      // Check user role
      String roleQuery = String.format(
         "SELECT role FROM Users WHERE login = '%s';",
         username
      );
      
      List<List<String>> roleResult = esql.executeQueryAndReturnResult(roleQuery);
      
      if (roleResult.isEmpty()) {
         System.out.println("User not found!");
         return;
      }
      
      String role = roleResult.get(0).get(0).trim();
      
      // Only allow managers and drivers to update order status
      if (!role.equals("manager") && !role.equals("driver")) {
         System.out.println("You don't have permission to update order status!");
         return;
      }
      
      // If we reach here, user has permission - proceed with order status update
      System.out.print("Enter Order ID to update: ");
      String orderIDStr = in.readLine().trim();
      int orderID;
      
      try {
         orderID = Integer.parseInt(orderIDStr);
      } catch (NumberFormatException e) {
         System.out.println("Invalid Order ID! Please enter a numeric value.");
         return;
      }
      
      // Check if the order exists
      String orderCheckQuery = String.format(
         "SELECT orderID, orderStatus FROM FoodOrder WHERE orderID = %d;",
         orderID
      );
      
      List<List<String>> orderResult = esql.executeQueryAndReturnResult(orderCheckQuery);
      
      if (orderResult.isEmpty()) {
         System.out.println("Order not found!");
         return;
      }
      
      String currentStatus = orderResult.get(0).get(1).trim();
      System.out.println("Current status: " + currentStatus);
      
      // Get new status
      System.out.println("Select new status:");
      System.out.println("1. Pending");
      System.out.println("2. Preparing");
      System.out.println("3. Ready");
      System.out.println("4. Out for Delivery");
      System.out.println("5. Delivered");
      System.out.println("6. Cancelled");
      
      int choice = readChoice();
      String newStatus;
      
      switch (choice) {
         case 1: newStatus = "Pending"; break;
         case 2: newStatus = "Preparing"; break;
         case 3: newStatus = "Ready"; break;
         case 4: newStatus = "Out for Delivery"; break;
         case 5: newStatus = "Delivered"; break;
         case 6: newStatus = "Cancelled"; break;
         default: 
            System.out.println("Invalid choice!");
            return;
      }
      
      // Update order status
      String updateQuery = String.format(
         "UPDATE FoodOrder SET orderStatus = '%s' WHERE orderID = %d;",
         newStatus, orderID
      );
      
      esql.executeUpdate(updateQuery);
      System.out.println("Order status updated successfully!");
      
   } catch (Exception e) {
      System.err.println(e.getMessage());
   }
}
 public static void updateMenu(PizzaStore esql) {
   try {
      // Get current user
      System.out.print("Enter your username: ");
      String username = in.readLine().trim();
      
      // Check user role
      String roleQuery = String.format(
         "SELECT role FROM Users WHERE login = '%s';",
         username
      );
      
      List<List<String>> roleResult = esql.executeQueryAndReturnResult(roleQuery);
      
      if (roleResult.isEmpty()) {
         System.out.println("User not found!");
         return;
      }
      
      String role = roleResult.get(0).get(0).trim();
      
      // Only allow managers to update menu items
      if (!role.equals("manager")) {
         System.out.println("You don't have permission to update menu items!");
         return;
      }
      
      // If we reach here, user is a manager - proceed with menu management
      System.out.println("\n---- Menu Management ----");
      System.out.println("1. Add new item");
      System.out.println("2. Update existing item");
      System.out.println("3. Delete item");
      System.out.println("4. Back to main menu");
      
      int choice = readChoice();
      
      switch (choice) {
         case 1: addMenuItem(esql); break;
         case 2: updateMenuItem(esql); break;
         case 3: deleteMenuItem(esql); break;
         case 4: return;
         default: 
            System.out.println("Invalid choice!");
            return;
      }
      
   } catch (Exception e) {
      System.err.println(e.getMessage());
   }
}
   public static void updateUser(PizzaStore esql) {
      try {
         // Get current user
         System.out.print("Enter your username: ");
         String username = in.readLine().trim();
         
         // Check user role
         String roleQuery = String.format(
            "SELECT role FROM Users WHERE login = '%s';",
            username
         );
         
         List<List<String>> roleResult = esql.executeQueryAndReturnResult(roleQuery);
         
         if (roleResult.isEmpty()) {
            System.out.println("User not found!");
            return;
         }
         
         String role = roleResult.get(0).get(0).trim();
         
         if (!role.equals("manager")) {
            System.out.println("You don't have permission to update user information!");
            return;
         }
         
         // Get username to update
         System.out.print("Enter username to update: ");
         String targetUser = in.readLine().trim();
         
         // Check if target user exists
         String targetCheckQuery = String.format(
            "SELECT * FROM Users WHERE login = '%s';",
            targetUser
         );
         
         List<List<String>> targetResult = esql.executeQueryAndReturnResult(targetCheckQuery);
         
         if (targetResult.isEmpty()) {
            System.out.println("Target user not found!");
            return;
         }
         
         // Display current user details
         System.out.println("\nCurrent user details:");
         esql.executeQueryAndPrintResult(targetCheckQuery);
         
         // Update options
         System.out.println("\nSelect field to update:");
         System.out.println("1. Password");
         System.out.println("2. Role");
         System.out.println("3. Favorite Items");
         System.out.println("4. Phone Number");
         
         int choice = readChoice();
         String updateQuery = "";
         
         switch (choice) {
            case 1:
               System.out.print("Enter new password: ");
               String password = in.readLine().trim();
               updateQuery = String.format(
                  "UPDATE Users SET password = '%s' WHERE login = '%s';",
                  password, targetUser
               );
               break;
               
            case 2:
               System.out.println("Select new role:");
               System.out.println("1. customer");
               System.out.println("2. driver");
               System.out.println("3. manager");
               
               int roleChoice = readChoice();
               String newRole;
               
               switch (roleChoice) {
                  case 1: newRole = "customer"; break;
                  case 2: newRole = "driver"; break;
                  case 3: newRole = "manager"; break;
                  default: 
                     System.out.println("Invalid choice!");
                     return;
               }
               
               updateQuery = String.format(
                  "UPDATE Users SET role = '%s' WHERE login = '%s';",
                  newRole, targetUser
               );
               break;
               
            case 3:
               System.out.print("Enter new favorite items: ");
               String favoriteItems = in.readLine().trim();
               updateQuery = String.format(
                  "UPDATE Users SET favoriteItems = '%s' WHERE login = '%s';",
                  favoriteItems, targetUser
               );
               break;
               
            case 4:
               System.out.print("Enter new phone number: ");
               String phoneNum = in.readLine().trim();
               updateQuery = String.format(
                  "UPDATE Users SET phoneNum = '%s' WHERE login = '%s';",
                  phoneNum, targetUser
               );
               break;
               
            default:
               System.out.println("Invalid choice!");
               return;
         }
         
         esql.executeUpdate(updateQuery);
         System.out.println("User information updated successfully!");
         
      } catch (Exception e) {
         System.err.println(e.getMessage());
      }
   }

private static void addMenuItem(PizzaStore esql) {
   try {
      String itemName, ingredients, typeOfItem, description;
      double price;
      
      System.out.println("\n---- Add New Menu Item ----");
      
      // Get item details
      System.out.print("Enter item name: ");
      itemName = in.readLine().trim();
      
      // Check if item already exists
      String checkQuery = String.format(
         "SELECT itemName FROM Items WHERE itemName = '%s';",
         itemName
      );
      
      List<List<String>> checkResult = esql.executeQueryAndReturnResult(checkQuery);
      
      if (!checkResult.isEmpty()) {
         System.out.println("Item already exists! Please use update option instead.");
         return;
      }
      
      System.out.print("Enter ingredients (comma separated): ");
      ingredients = in.readLine().trim();
      
      System.out.print("Enter type of item (e.g., pizza, drink, dessert): ");
      typeOfItem = in.readLine().trim();
      
      System.out.print("Enter price: ");
      try {
         price = Double.parseDouble(in.readLine().trim());
      } catch (NumberFormatException e) {
         System.out.println("Invalid price! Please enter a numeric value.");
         return;
      }
      
      System.out.print("Enter description: ");
      description = in.readLine().trim();
      
      // Insert new item
      String insertQuery = String.format(
         "INSERT INTO Items (itemName, ingredients, typeOfItem, price, description) " +
         "VALUES ('%s', '%s', '%s', %.2f, '%s');",
         itemName, ingredients, typeOfItem, price, description
      );
      
      esql.executeUpdate(insertQuery);
      System.out.println("Menu item added successfully!");
      
   } catch (Exception e) {
      System.err.println(e.getMessage());
   }
}

private static void updateMenuItem(PizzaStore esql) {
   try {
      String itemName;
      
      System.out.println("\n---- Update Menu Item ----");
      
      // Get item name
      System.out.print("Enter item name to update: ");
      itemName = in.readLine().trim();
      
      // Check if item exists
      String checkQuery = String.format(
         "SELECT * FROM Items WHERE itemName = '%s';",
         itemName
      );
      
      List<List<String>> checkResult = esql.executeQueryAndReturnResult(checkQuery);
      
      if (checkResult.isEmpty()) {
         System.out.println("Item not found!");
         return;
      }
      
      // Display current item details
      System.out.println("\nCurrent item details:");
      esql.executeQueryAndPrintResult(checkQuery);
      
      // Update options
      System.out.println("\nSelect field to update:");
      System.out.println("1. Ingredients");
      System.out.println("2. Type of item");
      System.out.println("3. Price");
      System.out.println("4. Description");
      
      int choice = readChoice();
      String updateQuery = "";
      
      switch (choice) {
         case 1:
            System.out.print("Enter new ingredients: ");
            String ingredients = in.readLine().trim();
            updateQuery = String.format(
               "UPDATE Items SET ingredients = '%s' WHERE itemName = '%s';",
               ingredients, itemName
            );
            break;
            
         case 2:
            System.out.print("Enter new type: ");
            String typeOfItem = in.readLine().trim();
            updateQuery = String.format(
               "UPDATE Items SET typeOfItem = '%s' WHERE itemName = '%s';",
               typeOfItem, itemName
            );
            break;
            
         case 3:
            System.out.print("Enter new price: ");
            try {
               double price = Double.parseDouble(in.readLine().trim());
               updateQuery = String.format(
                  "UPDATE Items SET price = %.2f WHERE itemName = '%s';",
                  price, itemName
               );
            } catch (NumberFormatException e) {
               System.out.println("Invalid price! Please enter a numeric value.");
               return;
            }
            break;
            
         case 4:
            System.out.print("Enter new description: ");
            String description = in.readLine().trim();
            updateQuery = String.format(
               "UPDATE Items SET description = '%s' WHERE itemName = '%s';",
               description, itemName
            );
            break;
            
         default:
            System.out.println("Invalid choice!");
            return;
      }
      
      esql.executeUpdate(updateQuery);
      System.out.println("Menu item updated successfully!");
      
   } catch (Exception e) {
      System.err.println(e.getMessage());
   }
}

private static void deleteMenuItem(PizzaStore esql) {
   try {
      String itemName;
      
      System.out.println("\n---- Delete Menu Item ----");
      
      // Get item name
      System.out.print("Enter item name to delete: ");
      itemName = in.readLine().trim();
      
      // Check if item exists
      String checkQuery = String.format(
         "SELECT * FROM Items WHERE itemName = '%s';",
         itemName
      );
      
      List<List<String>> checkResult = esql.executeQueryAndReturnResult(checkQuery);
      
      if (checkResult.isEmpty()) {
         System.out.println("Item not found!");
         return;
      }
      
      // Check if item is used in any orders
      String orderCheckQuery = String.format(
         "SELECT COUNT(*) FROM ItemsInOrder WHERE itemName = '%s';",
         itemName
      );
      
      List<List<String>> orderCheckResult = esql.executeQueryAndReturnResult(orderCheckQuery);
      int orderCount = Integer.parseInt(orderCheckResult.get(0).get(0));
      
      if (orderCount > 0) {
         System.out.println("Warning: This item is used in " + orderCount + " orders.");
         System.out.print("Deleting this item will affect order history. Continue? (y/n): ");
         String confirm = in.readLine().trim().toLowerCase();
         
         if (!confirm.equals("y")) {
            System.out.println("Deletion cancelled.");
            return;
         }
      }
      
      // Delete item
      String deleteQuery = String.format(
         "DELETE FROM Items WHERE itemName = '%s';",
         itemName
      );
      
      esql.executeUpdate(deleteQuery);
      System.out.println("Menu item deleted successfully!");
      
   } catch (Exception e) {
      System.err.println(e.getMessage());
   }
}
}//end PizzaStore
