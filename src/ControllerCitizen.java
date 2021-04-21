import static java.lang.System.exit;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;

public class ControllerCitizen implements IController {

  private Citizen citizen;

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

    Integer zipCode = null;
    System.out.println("Please provide your zip code:");
    while (true) {
      String zip = scan.next();
      try {
        zipCode = Integer.parseInt(String.valueOf(zip));
      } catch (NumberFormatException e) {
        System.out.println(
            "Invalid zip code. SSN must be a 5-digit number.\nPlease re-enter your zip code:");
        break;
      }
      break;
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
          "INSERT INTO citizen (user_id, ssn, citizen_name, dob, gender, health_history, zip_code)"
              + "VALUES ('%s', '%s', '%s', '%s', '%s', '%s', %d)", username, ssn, fullName, dob,
          gender,
          healthHistory, zipCode);
      statement.executeUpdate(stringToExecute);
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

    try {
      CallableStatement citizenSSN = conn.prepareCall("{? = CALL get_ssn_by_user(?)}");
      citizenSSN.registerOutParameter(1, Types.VARCHAR);
      citizenSSN.setString(2, username);
      citizenSSN.execute();
      CallableStatement citizenZip = conn.prepareCall("{? = CALL get_zip_code_by_user(?)}");
      citizenZip.registerOutParameter(1, Types.VARCHAR);
      citizenZip.setString(2, username);
      citizenZip.execute();
      citizen = new Citizen(citizenSSN.getString(1), citizenZip.getInt(1), conn);
    } catch (SQLException e) {
      System.out.println("ERROR: Could not create a citizen object for this user.");
    }

    try {
      CallableStatement citizenClinic = conn.prepareCall("{? = CALL get_clinic_by_user(?)}");
      citizenClinic.registerOutParameter(1, Types.VARCHAR);
      citizenClinic.setString(2, username);
      citizenClinic.execute();
      if (citizenClinic.getInt(1) == 0) {
        this.findAClinic(conn, scan);
      } else {
        this.citizen.assignClinicLocal(citizenClinic.getInt(1));
      }
    } catch (SQLException e) {
      System.out.println("ERROR: Could not retrieve this user's clinic.");
    }

    Integer numAppts;
    try {
      CallableStatement getNumAppts = conn.prepareCall("{? = CALL num_citizen_appts(?)}");
      getNumAppts.registerOutParameter(1, Types.INTEGER);
      getNumAppts.setString(2, this.citizen.getSSN());
      getNumAppts.execute();
      numAppts = getNumAppts.getInt(1);
      if (numAppts == 0) {
        System.out.println("It's time to make your first appointment!");
      } else if (numAppts == 1) {
        System.out.println("It's time to make your second appointment!");
      } else {
        System.out
            .println("You have already completed your vaccination! No further action is needed.");
        exit(0);
      }
    } catch (SQLException e) {
      System.out.println("ERROR: Could not check this user's appointments.");
    }

    boolean apptsAvailable = false;
    System.out.println("Loading available appointments...");
    try {
      apptsAvailable = citizen.viewAppointmentsAtClinic();
    } catch (SQLException e) {
      System.out.println("ERROR: Could not load appointments at this clinic.");
    }

    if (apptsAvailable) {
      System.out.println(
          "Please select from the above available appointments.\nInput your desired appt_id:");
      Integer selectedAppt;
      apptLoop:
      while (true) {
        try {
          selectedAppt = Integer.parseInt(scan.next());
        } catch (NumberFormatException e) {
          System.out.println("You have entered an invalid appointment id. Please try again.");
          continue;
        }
        try {
          String checkApptAvailable = "{? = CALL check_appt_available(?)}";
          CallableStatement checkApptAvailableStmt = conn.prepareCall(checkApptAvailable);
          checkApptAvailableStmt.registerOutParameter(1, Types.BOOLEAN);
          checkApptAvailableStmt.setInt(2, selectedAppt);
          checkApptAvailableStmt.execute();
          if (checkApptAvailableStmt.getBoolean(1)) {
            this.citizen.makeAppointment(selectedAppt);
            break apptLoop;
          } else {
            System.out.println(
                "The appointment you have selected is currently unavailable. Please select another appointment.");
          }
        } catch (SQLException e) {
          System.out.println("ERROR: Could not schedule an appointment.");
        }


      }
    }
  }

  private void findAClinic(Connection conn, Scanner scan) {
    ArrayList<Integer> clinicsToChooseFrom = new ArrayList<>();

    System.out.println("Loading your clinic options...");
    try {
      CallableStatement clinicOptions = conn.prepareCall("{CALL find_clinic_by_zip_code(?)}");
      clinicOptions.setInt(1, citizen.zipCode);
      boolean hasResults = clinicOptions.execute();

      while (hasResults) {
        ResultSet resultSet = clinicOptions.getResultSet();

        if (!resultSet.next()) {
          System.out.println("There are currently no available clinics. Please try again later.");
          exit(1);
        }
        System.out
            .println("Your clinic options are:");
        System.out.println("| clinic_no | clinic_name | street | city | zip_code |\n" +
            "*********************************************************************");
        int clinicNo = resultSet.getInt("clinic_no");
        clinicsToChooseFrom.add(clinicNo);
        String clinic_name = resultSet.getString("clinic_name");
        String street = resultSet.getString("street");
        String city = resultSet.getString("city");
        int zipCode = resultSet.getInt("zip_code");
        System.out.println("| " + clinicNo + " | " + clinic_name + " | "
            + street + " | " + city + " | " + zipCode + " |");
        while (resultSet.next()) {
          clinicNo = resultSet.getInt("clinic_no");
          clinicsToChooseFrom.add(clinicNo);
          clinic_name = resultSet.getString("clinic_name");
          street = resultSet.getString("street");
          city = resultSet.getString("city");
          zipCode = resultSet.getInt("zip_code");
          System.out.println("| " + clinicNo + " | " + clinic_name + " | "
              + street + " | " + city + " | " + zipCode + " |");
        }
        hasResults = clinicOptions.getMoreResults();
      }
    } catch (SQLException e) {
      System.out.println("ERROR: Could not fetch clinic options for this user.");
    }

    System.out.println("\nPlease select your desired clinic_no (column 1):");
    int value;
    while (true) {
      try {
        value = Integer.parseInt(scan.next());
        if (!clinicsToChooseFrom.contains(value)) {
          throw new IllegalArgumentException();
        }
        break;
      } catch (IllegalArgumentException e) {
        System.out
            .println("You have not entered a valid input. Please select a valid clinic option:");
      }
    }
    this.citizen.assignClinic(value);

  }
}
