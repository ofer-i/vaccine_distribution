import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;

public class ClinicStaff extends AUser {

  int clinicId;

  ClinicStaff(String username, Connection conn, int clinicId, boolean alreadyExisted) {
    super(username, conn);
    this.clinicId = clinicId;
    if (!alreadyExisted) {
      this.updateEmployment();
    }
  }

  public void updateEmployment() {
    try {
      String query = "{CALL update_employment_table(?, ?)}";
      CallableStatement getStmt = connect.prepareCall(query);
      getStmt.setString(1, this.username);
      getStmt.setInt(2, this.clinicId);
      getStmt.execute();
    } catch (SQLException e) {
      System.out.println("ERROR: Could not update staff employment.");
    }
  }

  public void getClinicInformation() {
    try {
      CallableStatement addStmt = connect.prepareCall("{CALL view_clinic_information_by_no(?)}");
      addStmt.setInt(1, this.clinicId);
      boolean hasResults = addStmt.execute();

      while (hasResults) {
        ResultSet resultSet = addStmt.getResultSet();
        while (resultSet.next()) {
          System.out.println("Clinic number: " + resultSet.getInt("clinic_no") +
              ", " + "Clinic Name: " + resultSet.getString("clinic_name") + ", Street: "
              + resultSet.getString("street") + ", "
              + "City: " + resultSet.getString("city") + ", "
              + "Zip Code: " + resultSet.getString("zip_code"));
        }
        hasResults = addStmt.getMoreResults();
      }
    } catch (SQLException e) {
      System.out.println("ERROR: Could not get clinic information.");
    }
  }

  public Integer getEmployeeId() {
    Integer output = null;
    try {
      String query = "{? = CALL get_staff_id_from_user(?)}";
      CallableStatement getStmt = connect.prepareCall(query);
      getStmt.registerOutParameter(1, Types.INTEGER);
      getStmt.setString(2, this.username);
      getStmt.execute();
      output = getStmt.getInt(1);
    } catch (SQLException e) {
      System.out.println("ERROR: Could not retrieve employee id");
    }
    return output;
  }

  public boolean addAppointmentAvailability(String apptDateTime, int empId) {
    try {
      CallableStatement apptExists = connect.prepareCall("{? = CALL does_appt_exist(?, ?)}");
      apptExists.setString(2, apptDateTime);
      apptExists.setInt(3, empId);
      apptExists.registerOutParameter(1, Types.BOOLEAN);
      apptExists.execute();
      if (!apptExists.getBoolean(1)) {
        CallableStatement addStmt = connect.prepareCall("{CALL add_appt_availability(?, ?, ?)}");
        addStmt.setInt(1, this.clinicId);
        addStmt.setString(2, apptDateTime);
        addStmt.setInt(3, empId);
        addStmt.execute();
        System.out.println("Appointment added successfully.");
        return true;
      }
      System.out.println("Appointment already exists in database.");
    } catch (SQLException e) {
      System.out.println("ERROR: Could not add new appointment availability.");
    }
    return false;
  }

  public void deletePatient(String ssn) {
    try {
      CallableStatement deletePatient = connect.prepareCall("{CALL delete_patient(?)}");
      deletePatient.setString(1, ssn);
      deletePatient.execute();
      System.out.println("Patient successfully deleted.");
    } catch (SQLException e) {
      System.out.println("ERROR: Could not delete patient from database.");
    }
  }

  public void updatePatientInformation(String ssn) {

  }

  public boolean getUpcomingAppointments() throws SQLException {
    String query = "{CALL view_staffs_appts(?)}";
    CallableStatement createStmt = connect.prepareCall(query);
    createStmt.setInt(1, this.getEmployeeId());
    boolean hasResults = createStmt.execute();
    while (hasResults) {
      ResultSet resultSet = createStmt.getResultSet();
      if (!resultSet.next()) {
        System.out.println(
            "You have no upcoming appointments.");
        return false;
      }
      System.out
          .println("Your upcoming appointments are:");
      System.out.println("| appointment_id | length_in_min | date_and_time | citizen_no |\n" +
          "*********************************************************************");
      int apptNo = resultSet.getInt("appointment_id");
      int apptLength = resultSet.getInt("length_in_min");
      Date date = resultSet.getDate("date_and_time");
      String citizenSSN = resultSet.getString("citizen_no");
      System.out.println("| " + apptNo + " | " + apptLength + " | "
          + date + " | " + citizenSSN + " |");
      while (resultSet.next()) {
        apptNo = resultSet.getInt("appointment_id");
        apptLength = resultSet.getInt("length_in_min");
        date = resultSet.getDate("date_and_time");
        citizenSSN = resultSet.getString("citizen_no");
        System.out.println("| " + apptNo + " | " + apptLength + " | "
            + date + " | " + citizenSSN + " |");
      }
      hasResults = createStmt.getMoreResults();
    }
    return true;
  }

}
