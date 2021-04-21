import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Scanner;

public class ControllerClinic implements IController {

  ClinicStaff clinicStaff;

  private void addStaffInfo(Scanner scan, Connection conn, String username) {
    System.out.println("Please provide your employee number:");
    int empNo;
    empNo = scan.nextInt();

    scan.nextLine();
    System.out.println("Please provide your full name:");
    String fullName;
    fullName = scan.nextLine();

    String jobDesc = "";
    System.out.println("Would you like to add your job description?");

    boolean addJobDesc;
    String input = scan.next();
    scan.nextLine();
    switch (input) {
      case "yes":
      case "'y'":
      case "y":
      case "Yes":
      case "'Y'":
      case "Y":
        addJobDesc = true;
        System.out.println("You have indicated that you would like to add your job description.");
        System.out.println("Please enter it below:");
        jobDesc += scan.nextLine();
        break;
      case "no":
      case "'n'":
      case "n":
      case "No":
      case "'N'":
      case "N":
        addJobDesc = false;
        System.out.println("You have indicated that you would not like to add your job description.");
        break;
      default:
        System.out.println("Invalid option entered! Please try again.");
    }

    try {
      Statement statement = conn.createStatement();
      String stringToExecute = String.format(
          "INSERT INTO staff (emp_no, emp_name, user_id, job_description) "
              + "VALUES (%d, '%s', '%s', '%s')", empNo, fullName, username, jobDesc);
      statement.executeUpdate(stringToExecute);
    } catch (SQLException e) {
      System.out.println("ERROR: Could not add staff info to database.");
    }
  }

  private boolean staffUserExists(Scanner scan, String username, Connection conn) {
    try {
      CallableStatement staffWithUserExists = conn
          .prepareCall("{? = CALL check_clinic_staff_user_exists(?)}");
      staffWithUserExists.registerOutParameter(1, Types.BOOLEAN);
      staffWithUserExists.setString(2, username);
      staffWithUserExists.execute();
      if (!staffWithUserExists.getBoolean(1)) {
        this.addStaffInfo(scan, conn, username);
        return false;
      }
      return true;
    } catch (SQLException e) {
      System.out.println("ERROR: Could not fetch staff info to database.");
    }
    return false;
  }

  private Integer getUserClinic(String username, Connection conn) {
    try {
      CallableStatement getUserClinic = conn
          .prepareCall("{? = CALL get_staff_clinic(?)}");
      getUserClinic.registerOutParameter(1, Types.VARCHAR);
      getUserClinic.setString(2, username);
      getUserClinic.execute();
      return getUserClinic.getInt(1);
    } catch (SQLException e) {
      System.out.println("ERROR: Could not fetch staff info to database.");
    }
    return null;
  }

  @Override
  public void run(Scanner scan, Connection conn, String username) {

    boolean alreadyExisted = this.staffUserExists(scan, username, conn);

    ClinicStaff clinic;
    Integer selectedClinic;
    boolean userHasClinic; //= userHasClinic(username, conn);
    if (getUserClinic(username, conn) == 0) {
      userHasClinic = false;
    } else {
      userHasClinic = true;
    }

    if (!userHasClinic) {
      checkClinicLoop:
      while (true) {
        System.out.println("Please enter the clinic you work at:");
        try {
          selectedClinic = Integer.parseInt(scan.next());
        } catch (NumberFormatException e) {
          System.out.println("You have entered an invalid clinic id. Please try again.");
          continue;
        }
        try {
          String checkClinicExistsStr = "{? = CALL does_clinic_exist(?)}";
          CallableStatement checkClinicExists = conn.prepareCall(checkClinicExistsStr);
          checkClinicExists.registerOutParameter(1, Types.BOOLEAN);
          checkClinicExists.setInt(2, selectedClinic);
          checkClinicExists.execute();
          if (checkClinicExists.getBoolean(1)) {
            clinic = new ClinicStaff(username, conn, selectedClinic, alreadyExisted);
            clinicStaff = clinic;
            System.out.println("You have successfully joined clinic with id " + selectedClinic + ".");
            break checkClinicLoop;
          } else {
            System.out.println(
                "The clinic you have selected does not exist. Please try again.");
          }
        } catch (SQLException e) {
          System.out.println("ERROR: Could not select a clinic.");
        }
      }
    } else {
      clinic = new ClinicStaff(username, conn, getUserClinic(username, conn), alreadyExisted);
      clinicStaff = clinic;
    }

    while (true) {
      System.out.println("Option menu:");
      System.out.println("> Enter 1 to view all clinic information");
      System.out.println("> Enter 2 to add new available appointment");

      int choice = scan.nextInt();
      switch (choice) {
        case 1:
          try {
            clinic.getClinicInformation();
          } catch (Exception e) {
            System.out.println("Clinic information is not available!");
          }
          break;
        case 2:
          String dateTimeFormat = "yyyy-MM-dd HH:mm:00";
          String apptDateTime;
          while (true) {
            System.out.println("Please provide the appointment date and time (yyyy-MM-dd HH:mm):");
            apptDateTime = scan.next();
            try {
              new SimpleDateFormat(dateTimeFormat).parse(apptDateTime);
              break;
            } catch (ParseException e) {
              System.out.println("You have provided an invalid appointment time. Please try again.");
            }
          }

          int empId = this.clinicStaff.getEmployeeId();

          this.clinicStaff.addAppointmentAvailability(apptDateTime, empId);

      }
    }


  }
}
