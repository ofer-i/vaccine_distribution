import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

public class ControllerCitizen implements IController {

  ControllerCitizen() {

  }

  private void addCitizenInfo(Scanner scan, Connection conn, String username) {
    System.out.println("Please provide your social security number:");
    String ssn;
    while (true) {
      ssn = scan.next();
      for (int i = 0; i < ssn.length(); i++) {
        try {
          Integer.parseInt(String.valueOf(ssn.charAt(i)));
        } catch (NumberFormatException e) {
          System.out.println(
              "Invalid social security number. SSN must be a 10-digit number.\nPlease re-enter your social security number:");
          break;
        }
      }
      break;
    }

    scan.nextLine();
    System.out.println("Please provide your full name:");
    String fullName;
    fullName = scan.nextLine();

    String dateFormat = "yyyy-MM-dd";
    String dob;
    while (true) {
      System.out.println("Please provide your date of birth (yyyy-MM-dd):");
      dob = scan.next();
      try {
        new SimpleDateFormat(dateFormat).parse(dob);
        break;
      } catch (ParseException e) {
        System.out.println("You have provided an invalid date.");
      }
    }

    String gender;
    genderLoop:
    while (true) {
      System.out.println(
          "Please enter your gender:\nSelect (1) for 'female'\nSelect (2) for 'male'\nSelect (3) for 'prefer not to say'");
      int selection;
      selection = scan.nextInt();
      switch (selection) {
        case 1:
          gender = "female";
          break genderLoop;
        case 2:
          gender = "male";
          break genderLoop;
        case 3:
          gender = "prefer not to say";
          break genderLoop;
        default:
          System.out.println("You have entered an invalid selection.");
      }
    }

    String healthHistory = "";
    System.out.println("Do you have any relevant health history you'd like to add?");

    boolean addHistory;
    String input = scan.next();
    scan.nextLine();
    switch (input) {
      case "yes":
      case "'y'":
      case "y":
      case "Yes":
      case "'Y'":
      case "Y":
        addHistory = true;
        System.out.println("You have indicated that you would like to add health history.");
        System.out.println("Please enter your health history:");
        healthHistory += scan.nextLine();
        break;
      case "no":
      case "'n'":
      case "n":
      case "No":
      case "'N'":
      case "N":
        addHistory = false;
        System.out.println("You have indicated that you would not like to add health history.");
        break;
      default:
        System.out.println("Invalid option entered! Please try again.");
    }

    try {
      Statement statement = conn.createStatement();
      String stringToExecute = String.format(
            "INSERT INTO citizen (user_id, ssn, citizen_name, dob, gender, health_history)"
              + "VALUES ('%s', '%s', '%s', '%s', '%s', '%s')", username, ssn, fullName, dob, gender,
          healthHistory);
      statement.executeUpdate(stringToExecute);
      //statement.executeUpdate(String.format(
        //  "INSERT INTO citizen (user_id, ssn, citizen_name, dob, gender, health_history)"
          //    + "VALUES (%s, %s, %s, %s, %s, %s)", username, ssn, fullName, dob, gender,
          //healthHistory));
    } catch (SQLException e) {
      System.out.println("ERROR: Could not add citizen info to database.");
    }
  }

  @Override
  public void run(Scanner scan, Connection conn, String username) {

    try {
      CallableStatement citizenWithUserExists = conn
          .prepareCall("{? = CALL check_citizen_user_exists(?)}");
      citizenWithUserExists.registerOutParameter(1, Types.BOOLEAN);
      citizenWithUserExists.setString(2, username);
      citizenWithUserExists.execute();
      if (!citizenWithUserExists.getBoolean(1)) {
        this.addCitizenInfo(scan, conn, username);
      }
    } catch (SQLException e) {
      System.out.println("ERROR: Could not add citizen info to database.");
    }

/*
    while (true) {
      System.out.println("press 1 for viewing all available appointment at a clinic");
      System.out.println("press 2 for viewing my appointment");
      System.out.println("press 3 to log out");

      int choice = scan.nextInt();

      if (choice == 1) {
        citizen.viewAppointmentsAtClinic();
      }

      if (choice == 2) {
        citizen.viewMyAppointments();
      }
      if (choice == 3) {
        int session = citizen.getCurrentSession(username);
        citizen.logOut(session);
        System.out.println("Successfully logged Out!");
        break;
      } else {
        System.out.println("Invalid choice!");
      }


    }
*/

  }
}
