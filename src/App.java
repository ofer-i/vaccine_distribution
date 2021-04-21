import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Properties;
import java.util.Scanner;

public class App {

  /**
   * The name of the MySQL account to use (or empty for anonymous)
   */
  private String databaseUserName;

  /**
   * The password for the MySQL account (or empty for anonymous)
   */
  private String databasePassword;

  /**
   * The name of the computer running MySQL
   */
  private final String serverName = "localhost";

  /**
   * The port of the MySQL server (default is 3306)
   */
  private final int portNumber = 3306;

  /**
   * The name of the database we are testing with (this default is installed with MySQL)
   */
  private final String dbName = "vaccine_distribution";

  private String characterName;

  private ResultSet characters;

  public String username;
  public String password;
  public Type type = null;

  /**
   * Get a new database connection
   *
   * @return
   * @throws SQLException
   */
  public Connection getConnection() throws SQLException {
    Connection conn;
    Properties connectionProps = new Properties();
    connectionProps.put("user", this.databaseUserName);
    connectionProps.put("password", this.databasePassword);

    conn = DriverManager.getConnection("jdbc:mysql://"
            + this.serverName + ":" + this.portNumber + "/" + this.dbName
            + "?characterEncoding=UTF-8&useSSL=false",
        connectionProps);

    return conn;
  }

  void getDatabaseUser() {
    Scanner scan = new Scanner(System.in);  // Create a Scanner object
    System.out.println("Enter database username:");

    this.databaseUserName = scan.nextLine();  // Read user input
    System.out.println("Database username is: " + this.databaseUserName);  // Output user input

    System.out.println("Enter database password:");
    this.databasePassword = scan.nextLine();
    System.out.println("Database password is: " + this.databasePassword);
  }

  void login(Scanner scan, Connection conn) {
    while (true) {
      System.out.println("Please enter your username:");
      username = scan.next();
      try {
        CallableStatement userExists = conn.prepareCall("{? = CALL userExists(?)}");
        userExists.registerOutParameter(1, Types.BOOLEAN);
        userExists.setString(2, username);
        userExists.execute();
        if (userExists.getBoolean(1)) {
          break;
        }
        System.out.println("User does not exist in database. Try again.");
      } catch (SQLException e) {
        System.out.println("ERROR: Could not check if user exists in database.");
      }
    }

    while (true) {
      System.out.println("Please enter your password:");
      password = scan.next();
      try {
        CallableStatement passwordMatches = conn.prepareCall("{? = CALL passwordMatches(?, ?)}");
        passwordMatches.registerOutParameter(1, Types.BOOLEAN);
        passwordMatches.setString(2, username);
        passwordMatches.setString(3, password);
        passwordMatches.execute();
        if (passwordMatches.getBoolean(1)) {
          break;
        }
        System.out.println("Password does not match username. Try again.");
      } catch (SQLException e) {
        System.out.println("ERROR: Could not check if password matches user in database.");
      }
    }

    try {
      CallableStatement userType = conn.prepareCall("{? = CALL getUserType(?)}");
      userType.registerOutParameter(1, Types.VARCHAR);
      userType.setString(2, username);
      userType.execute();
      switch (userType.getString(1)) {
        case "vaccine provider":
          type = Type.VACCINE_PROVIDER;
          break;
        case "clinic administrator":
        case "health care provider":
          type = Type.CLINIC_STAFF;
          break;
        case "citizen":
          type = Type.CITIZEN;
          break;
        case "government official":
          type = Type.GOV_OFFICIAL;
          break;
        default:
          System.out.println("ERROR: Unsupported user type.");
      }
    } catch (SQLException e) {
      System.out.println("ERROR: Could not check for user type in database.");
    }

    System.out.println("You have logged into the following account:");
    System.out.print(String.format("Username: %s\nPassword: %s\nPermissions: %s\n",
        username, password, type.getStrType()));
  }

  void makeNewUser(Scanner scan, Connection conn) {
    loginCodeLoop:
    while (true) {
      System.out.println("Please enter your provided login code:");
      String loginCode = scan.next();
      switch (loginCode) {
        case "1":
          type = Type.VACCINE_PROVIDER;
          break loginCodeLoop;
        case "2":
          type = Type.CLINIC_STAFF;
          break loginCodeLoop;
        case "3":
          type = Type.CITIZEN;
          break loginCodeLoop;
        case "4":
          type = Type.GOV_OFFICIAL;
          break loginCodeLoop;
        default:
          System.out.println("You have provided an invalid login code. Try again.");
      }
    }

    System.out.println(String.format("You are creating a %s account.", type.getStrType()));

    while (true) {
      System.out.println("Please enter your desired username:");
      username = scan.next();
      try {
        CallableStatement userExists = conn.prepareCall("{? = CALL userExists(?)}");
        userExists.registerOutParameter(1, Types.BOOLEAN);
        userExists.setString(2, username);
        userExists.execute();
        if (!userExists.getBoolean(1)) {
          break;
        }
        System.out.println("User already exists in database. Choose a different username.");
      } catch (SQLException e) {
        System.out.println("ERROR: Could not check if user exists in database.");
      }
    }

    System.out.println("Please enter your desired password:");
    password = scan.next();

    try {
      CallableStatement addNewUser = conn.prepareCall("{CALL add_user(?, ?, ?)}");
      addNewUser.setString(1, username);
      addNewUser.setString(2, password);
      addNewUser.setString(3, type.getStrType());
      addNewUser.execute();
      System.out.print(
          "You have successfully created your account.\nBelow is your login information. Please store it in a secure location.\n");
      System.out.print(String.format("Username: %s\nPassword: %s\nPermissions: %s\n",
          username, password, type.getStrType()));
    } catch (SQLException e) {
      System.out.println("ERROR: Could not add new user to database.");
    }
  }

  public void run() {
    Connection conn;
    try {
      this.getDatabaseUser();
      conn = this.getConnection();
      System.out.println("Connected to database.");
    } catch (SQLException e) {
      System.out.println("ERROR: Could not connect to the database.");
      e.printStackTrace();
      return;
    }

    Scanner scan = new Scanner(System.in);
    boolean hasAccount;

    aa:
    while (true) {
      System.out.println("Do you have an account already? Enter 'Y' or 'N':");

      String input = scan.next();
      switch (input) {
        case "yes":
        case "'y'":
        case "y":
        case "Yes":
        case "'Y'":
        case "Y":
          hasAccount = true;
          System.out.println("You have indicated that you already have an account.");
          break aa;
        case "no":
        case "'n'":
        case "n":
        case "No":
        case "'N'":
        case "N":
          hasAccount = false;
          System.out.println("You have indicated that you do not yet have an account.");
          break aa;
        default:
          System.out.println("Invalid option entered! Please try again.");
      }
    }

    if (hasAccount) {
      this.login(scan, conn);
    } else {
      this.makeNewUser(scan, conn);
    }

    IController controller = Factory.createController(type);
    controller.run(scan, conn, username);
  }

  public static void main(String[] args) {
    App app = new App();
    app.run();
  }
}

