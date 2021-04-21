import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

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

  public boolean addAppointmentAvailability(String apptDateTime, int empId){
    try {
      CallableStatement available = connect.prepareCall("{? = CALL does_appt_exist(?, ?)}");
      available.setString(2, apptDateTime);
      available.setInt(3, empId);
      available.registerOutParameter(1, Types.BOOLEAN);
      available.execute();
      if (available.getBoolean(1)) {
        CallableStatement addStmt = connect.prepareCall("{CALL add_appt_availability(?, ?, ?)}");
        addStmt.setInt(1, this.clinicId);
        addStmt.setString(2, apptDateTime);
        addStmt.setInt(3, empId);
        addStmt.execute();
        return true;
      }
      System.out.println("Appointment added successfuly");
    } catch (SQLException e) {
      System.out.println("ERROR: Could not add new appointment availability.");
    }
    System.out.println("Appointment already exists in database.");
    return false;
  }



  public void updatePatientInformation(String ssn){

  }


}
