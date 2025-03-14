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
                System.out.println("4. Place Order");
                System.out.println("5. View Order History");
                System.out.println("6. View Recent Orders");
                System.out.println("7. View Order Information");
                System.out.println("8. View Stores"); 
                
                // Check if user is manager or driver to show appropriate menu option
                String roleQuery = String.format("SELECT role FROM Users WHERE login = '%s';", authorisedUser);
                List<List<String>> roleResult = esql.executeQueryAndReturnResult(roleQuery);
                String userRole = roleResult.get(0).get(0).trim();
                
                if (userRole.equals("manager")) {
                    System.out.println("9. Manager Access");
                } else if (userRole.equals("driver")) {
                    System.out.println("9. Driver Access");
                }

                System.out.println(".........................");
                System.out.println("20. Log out");
                switch (readChoice()){
                   case 1: viewProfile(esql, authorisedUser); break;
                   case 2: updateProfile(esql, authorisedUser); break;
                   case 3: viewMenu(esql); break;
                   case 4: placeOrder(esql, authorisedUser); break;
                   case 5: viewOwnOrders(esql, authorisedUser); break;
                   case 6: viewOwnRecentOrders(esql, authorisedUser); break;
                   case 7: viewOrderInfo(esql, authorisedUser); break;
                   case 8: viewStores(esql); break;
                   case 9: managerMenu(esql, authorisedUser); break;
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
      System.out.println("\n---- Create User ----");
      System.out.println("At any prompt, type 'exit' to return to main menu\n");
      
      while(true) {
         System.out.println("Please enter a username! (Maximum 50 characters)");
         login = in.readLine();
         
         // Check for exit command
         if(login.equalsIgnoreCase("exit")) {
            System.out.println("Returning to main menu...");
            return;
         }
         
         if(login.trim().isEmpty()) {
            System.out.println("Username cannot be empty. Please try again.");
         }
         else if(login.length() > 50) {
            System.out.println("Username cannot be over 50 characters. Please try again.");
         }
         else {
            // Check if username already exists
            String checkUserQuery = String.format("SELECT login FROM Users WHERE login = '%s';", login);
            List<List<String>> result = esql.executeQueryAndReturnResult(checkUserQuery);
            if (!result.isEmpty()) {
               System.out.println("Username already exists! Please choose a different one.");
               continue;  // Go back to username entry
            }
            break;
         }
      }
      
      while(true) {
         System.out.println("Enter a password: ");
         password = in.readLine();
         
         // Check for exit command
         if(password.equalsIgnoreCase("exit")) {
            System.out.println("Returning to main menu...");
            return;
         }
         
         if(password.trim().isEmpty()) {
            System.out.println("Password cannot be empty. Please try again.");
            continue;
         }
         System.out.println("Confirm your password: ");
         confirmPassword = in.readLine();
         
         // Check for exit command
         if(confirmPassword.equalsIgnoreCase("exit")) {
            System.out.println("Returning to main menu...");
            return;
         }

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
         
         // Check for exit command
         if(phoneNum.equalsIgnoreCase("exit")) {
            System.out.println("Returning to main menu...");
            return;
         }
         
         if(phoneNum.trim().isEmpty()) {
            System.out.println("Phone number cannot be empty. Please try again.");
         }
         else {
            break;
         }
      }
   
      try {
         String query = String.format("INSERT INTO Users (login, password, role, favoriteItems, phoneNum) " +
                                  "VALUES ('%s', '%s', '%s', '%s', '%s');",
                                  login, password, role, favoriteItems, phoneNum);
         esql.executeUpdate(query);
         System.out.println("User created successfully!");
         System.out.println("Tip: After reviewing our menu, you can add your favorite items to your profile");
         System.out.println("by selecting the 'Update Profile' option from the main menu.");
      }
      catch (Exception e) {
         if (e.getMessage().contains("duplicate key value")) {
            System.out.println("Error: This username already exists. Please choose another.");
         } else {
            System.err.println("Database Error: " + e.getMessage());
         }
      }
   }
   catch (Exception e) {
      // Handle any exceptions (like input issues)
      System.err.println("Error creating user: " + e.getMessage());
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
   
   public static void viewOwnOrders(PizzaStore esql, String loggedInUser) {
      if (loggedInUser == null) {
        System.out.println("Error: No user is logged in.");
        return;
      }
      try {
         String ordersQuery = String.format("SELECT o.orderID, o.totalPrice, o.orderTimestamp, o.orderStatus, s.storeID, s.address " + 
         "FROM FoodOrder o JOIN Store s ON o.storeID = s.storeID " +
         "WHERE o.login = '%s' " +
         "ORDER BY o.orderTimestamp DESC; ",
         loggedInUser);

         List<List<String>> orders = esql.executeQueryAndReturnResult(ordersQuery);

         int rowCount = orders.size();
         if (rowCount == 0) {
            System.out.println("No orders found for user: " + loggedInUser);
         }
         else {
            System.out.println("\nFound " + rowCount + " orders for user: " + loggedInUser);
         }

         for (List<String> row : orders) {
            System.out.println(row);
         }
      }
      catch (Exception e) {
         System.err.println (e.getMessage ());
         return;
      }
   }
   
   public static void viewOrders(PizzaStore esql) {
      try {
         System.out.println("Which user would you like to view orders for?" );
         String targetUser = in.readLine();

         String ordersQuery = String.format("SELECT o.orderID, o.totalPrice, o.orderTimestamp, o.orderStatus, s.storeID, s.address " + 
            "FROM FoodOrder o JOIN Store s ON o.storeID = s.storeID " +
            "WHERE o.login = '%s' " +
            "ORDER BY o.orderTimestamp DESC;",
            targetUser);

         List<List<String>> orders = esql.executeQueryAndReturnResult(ordersQuery);

         int rowCount = orders.size();
         if (rowCount == 0) {
            System.out.println("No orders found for user: " + targetUser);
         }
         else {
            System.out.println("\nFound " + rowCount + " orders for user: " + targetUser);
         }

         for (List<String> row : orders) {
            System.out.println(row);
         }
      }
      catch (Exception e) {
         System.err.println (e.getMessage ());
         return;
      }
   }
   
   public static void viewOwnRecentOrders(PizzaStore esql, String loggedInUser) {
      if (loggedInUser == null) {
        System.out.println("Error: No user is logged in.");
        return;
      }
   try {
         String ordersQuery = String.format("SELECT o.orderID, o.totalPrice, o.orderTimestamp, o.orderStatus, s.storeID, s.address " + 
         "FROM FoodOrder o JOIN Store s ON o.storeID = s.storeID " +
         "WHERE o.login = '%s' " +
         "ORDER BY o.orderTimestamp DESC " +
         "LIMIT 5;",
         loggedInUser);

         List<List<String>> orders = esql.executeQueryAndReturnResult(ordersQuery);

         int rowCount = orders.size();
         if (rowCount == 0) {
            System.out.println("No recent orders found for user: " + loggedInUser);
         }
         else {
            System.out.println("\nFound " + rowCount + " recent orders for user: " + loggedInUser);
         }

         for (List<String> row : orders) {
            System.out.println(row);
         }
      }
      catch (Exception e) {
         System.err.println (e.getMessage ());
         return;
      }
   }
   
   public static void viewRecentOrders(PizzaStore esql) {
      try {
         System.out.println("Which user would you like to view recent orders for?" );
         String targetUser = in.readLine();

         String ordersQuery = String.format("SELECT o.orderID, o.totalPrice, o.orderTimestamp, o.orderStatus, s.storeID, s.address " + 
         "FROM FoodOrder o JOIN Store s ON o.storeID = s.storeID " +
         "WHERE o.login = '%s' " +
         "ORDER BY o.orderTimestamp DESC " +
         "LIMIT 5;",
         targetUser);

         List<List<String>> orders = esql.executeQueryAndReturnResult(ordersQuery);

         int rowCount = orders.size();
         if (rowCount == 0) {
            System.out.println("No recent orders found for user: " + targetUser);
         }
         else {
            System.out.println("\nFound " + rowCount + " recent orders for user: " + targetUser);
         }

         for (List<String> row : orders) {
            System.out.println(row);
         }
      }
      catch (Exception e) {
         System.err.println (e.getMessage ());
         return;
      }
   }

   public static void viewOrderInfo(PizzaStore esql, String loggedInUser) {
      if (loggedInUser == null) {
           System.out.println("Error: No user is logged in.");
           return;
      }
      try {
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
   
   public static void updateUserRole(PizzaStore esql) {
      try {
         System.out.println("\n---- Update User Role ----");
         
         // Get username to update
         System.out.print("Enter username to change role: ");
         String targetUser = in.readLine().trim();
         
         // Check if target user exists
         String userCheckQuery = String.format(
            "SELECT login, role FROM Users WHERE login = '%s';", 
            targetUser
         );
         
         List<List<String>> userResult = esql.executeQueryAndReturnResult(userCheckQuery);
         
         if (userResult.isEmpty()) {
            System.out.println("User not found!");
            return;
         }
         
         String currentRole = userResult.get(0).get(1).trim();
         System.out.println("Current role for " + targetUser + ": " + currentRole);
         
         // Get new role
         System.out.println("Select new role:");
         System.out.println("1. Customer");
         System.out.println("2. Driver");
         System.out.println("3. Manager");
         
         int choice = readChoice();
         String newRole;
         
         switch (choice) {
            case 1: newRole = "customer"; break;
            case 2: newRole = "driver"; break;
            case 3: newRole = "manager"; break;
            default:
               System.out.println("Invalid choice!");
               return;
         }
         
         if (newRole.equals(currentRole)) {
            System.out.println("User already has this role. No change needed.");
            return;
         }
         
         // Update role
         String updateQuery = String.format(
            "UPDATE Users SET role = '%s' WHERE login = '%s';",
            newRole, targetUser
         );
         
         esql.executeUpdate(updateQuery);
         System.out.println(targetUser + " has been updated to role: " + newRole);
         
      } catch (Exception e) {
         System.err.println(e.getMessage());
      }
   }
   
   public static void updateUserProfile(PizzaStore esql) {
      try {
         System.out.println("\n---- Update User Profile ----");
         
         // Get username to update
         System.out.print("Enter username to update profile: ");
         String targetUser = in.readLine().trim();
         
         // Check if target user exists
         String userCheckQuery = String.format(
            "SELECT * FROM Users WHERE login = '%s';", 
            targetUser
         );
         
         List<List<String>> userResult = esql.executeQueryAndReturnResult(userCheckQuery);
         
         if (userResult.isEmpty()) {
            System.out.println("User not found!");
            return;
         }
         
         // Display current user details
         System.out.println("\nCurrent user details:");
         esql.executeQueryAndPrintResult(userCheckQuery);
         
         // Update options
         System.out.println("\nSelect field to update:");
         System.out.println("1. Username");
         System.out.println("2. Password");
         System.out.println("3. Phone Number");
         System.out.println("4. Favorite Items");
         System.out.println("5. Go Back");
         
         int choice = readChoice();
         
         switch (choice) {
            case 1:
   // Update username (login)
   System.out.print("Enter new username: ");
   String newLogin = in.readLine().trim();
   
   // Check if the new username already exists
   String checkLoginQuery = String.format(
      "SELECT login FROM Users WHERE login = '%s';",
      newLogin
   );
   
   List<List<String>> loginResult = esql.executeQueryAndReturnResult(checkLoginQuery);
   
   if (!loginResult.isEmpty()) {
      System.out.println("Username already exists! Please choose a different username.");
      return;
   }
   
   try {
      // Begin by updating the orders
      String updateOrdersQuery = String.format(
         "UPDATE FoodOrder SET login = '%s' WHERE login = '%s';",
         newLogin, targetUser
      );
      esql.executeUpdate(updateOrdersQuery);
      
      // Then update the user
      String updateLoginQuery = String.format(
         "UPDATE Users SET login = '%s' WHERE login = '%s';",
         newLogin, targetUser
      );
      esql.executeUpdate(updateLoginQuery);
      
      System.out.println("Username updated successfully from '" + targetUser + "' to '" + newLogin + "'");
   } catch (Exception e) {
      if (e.getMessage().contains("duplicate key value")) {
         // Check for unique constraint violation 
         System.out.println("Error: This username already exists. Please choose another.");
      } else {
         System.err.println("Database Error: " + e.getMessage());
      }
   }
   break;
               
            case 2:
               // Update password
               System.out.print("Enter new password: ");
               String newPassword = in.readLine().trim();
               
               // Update password
               String updatePasswordQuery = String.format(
                  "UPDATE Users SET password = '%s' WHERE login = '%s';",
                  newPassword, targetUser
               );
               
               esql.executeUpdate(updatePasswordQuery);
               System.out.println("Password updated successfully for user: " + targetUser);
               break;
               
            case 3:
               // Update phone number
               System.out.print("Enter new phone number: ");
               String newPhoneNum = in.readLine().trim();
               
               // Update phone number
               String updatePhoneQuery = String.format(
                  "UPDATE Users SET phoneNum = '%s' WHERE login = '%s';",
                  newPhoneNum, targetUser
               );
               
               esql.executeUpdate(updatePhoneQuery);
               System.out.println("Phone number updated successfully for user: " + targetUser);
               break;
               
            case 4:
               // Update favorite items
               System.out.print("Enter new favorite items: ");
               String newFavoriteItems = in.readLine().trim();
               
               // Update favorite items
               String updateFavoritesQuery = String.format(
                  "UPDATE Users SET favoriteItems = '%s' WHERE login = '%s';",
                  newFavoriteItems, targetUser
               );
               
               esql.executeUpdate(updateFavoritesQuery);
               System.out.println("Favorite items updated successfully for user: " + targetUser);
               break;
               
            case 5:
               return;
               
            default:
               System.out.println("Invalid choice!");
               return;
         }
         
      } catch (Exception e) {
         System.err.println(e.getMessage());
      }
   }
   
   public static void managerMenu(PizzaStore esql, String loggedInUser) {
      if (loggedInUser == null) {
         System.out.println("Error: No user is logged in");
         return;
      }
      
      try {
         // Check if the logged-in user is a manager or driver
         String roleQuery = String.format(
            "SELECT role FROM Users WHERE login = '%s';",
            loggedInUser
         );
         
         List<List<String>> roleResult = esql.executeQueryAndReturnResult(roleQuery);
         
         if (roleResult.isEmpty()) {
            System.out.println("User not found!");
            return;
         }
         
         String userRole = roleResult.get(0).get(0).trim();
         
         if (!userRole.equals("manager") && !userRole.equals("driver")) {
            System.out.println("Access Denied: You do not have permission to access this menu.");
            return;
         }
         
         // If we get here, user is either a manager or driver
         boolean managerMenuActive = true;
         
         while (managerMenuActive) {
            if (userRole.equals("manager")) {
               System.out.println("\n---- Manager Menu ----");
               // Manager options
               System.out.println("1. View Orders for Any User");
               System.out.println("2. View Recent Orders for Any User");
               System.out.println("3. Update Order Status");
               System.out.println("4. Manage Menu Items");
               System.out.println("5. Update User Role");
               System.out.println("6. Update User Profile");
            } else {
               System.out.println("\n---- Driver Menu ----");
               // Driver options 
               System.out.println("1. View Orders for Any User");
               System.out.println("2. View Recent Orders for Any User");
               System.out.println("3. Update Order Status");
            }
            
            System.out.println("9. Back to Main Menu");
            
            int choice = readChoice();
            
            if (userRole.equals("manager")) {
               // Manager options handling
               switch (choice) {
                  case 1: viewOrders(esql); break;
                  case 2: viewRecentOrders(esql); break;
                  case 3: updateOrderStatus(esql); break;
                  case 4: updateMenu(esql); break;
                  case 5: updateUserRole(esql); break;
                  case 6: updateUserProfile(esql); break;
                  case 9: managerMenuActive = false; break;
                  default: System.out.println("Invalid choice!"); break;
               }
            } else {
               // Driver options handling
               switch (choice) {
                  case 1: viewOrders(esql); break;
                  case 2: viewRecentOrders(esql); break;
                  case 3: updateOrderStatus(esql); break;
                  case 9: managerMenuActive = false; break;
                  default: System.out.println("Invalid choice!"); break;
               }
            }
         }
      } catch (Exception e) {
         System.err.println(e.getMessage());
      }
   }
}//end PizzaStore