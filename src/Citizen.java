import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;

public final class Citizen extends AUser {

  private ArrayList<Integer> availableAppointments;
  private String ssn;
  private Integer clinic;
  int zipCode;

  Citizen(String ssn, int zipCode, Connection conn) throws SQLException {
    super(conn);
    availableAppointments = new ArrayList<>();
    this.ssn = ssn;
  }

  //gets the name of the staff member given the id
  public String getStaffName(int id) throws SQLException {
    String query = "{? = CALL get_staff_name(?)}";
    CallableStatement getStmt = connect.prepareCall(query);
    getStmt.registerOutParameter(1, Types.VARCHAR);
    getStmt.setInt(2, id);
    getStmt.execute();
    String output = getStmt.getString(1);
    return output;
  }

  public void assignClinic(int clinic_no) {
    this.clinic = clinic_no;
    try {
      String query = "{CALL update_patient_clinic(?, ?)}";
      CallableStatement getStmt = connect.prepareCall(query);
      getStmt.setString(1, this.ssn);
      getStmt.setInt(2, clinic_no);
      getStmt.execute();
      CallableStatement clinicName = connect.prepareCall("{? = CALL get_clinic_name_by_no(?)}");
      clinicName.registerOutParameter(1, Types.VARCHAR);
      clinicName.setInt(2, clinic_no);
      clinicName.execute();
      System.out.println("Your assigned clinic is now " + clinicName.getString(1) + ".");
    } catch (SQLException e) {
      System.out.println("ERROR: Could not update patient clinic.");
    }
  }

  public void assignClinicLocal(int clinic_no) {
    this.clinic = clinic_no;
  }

  //gets the name of the staff member given the id
  public Integer getClinic() {
    return this.clinic;
  }

  public void viewAppointmentsAtClinic() throws SQLException {
    String query = "{CALL view_available_appointment(?)}";
    CallableStatement createStmt = connect.prepareCall(query);
    createStmt.setInt(1, this.clinic);
    ArrayList<Integer> apptsToChooseFrom = new ArrayList<>();
    boolean hasResults = createStmt.execute();
    while (hasResults) {
      ResultSet resultSet = createStmt.getResultSet();
      if (!resultSet.next()) {
        System.out.println("There are currently no available appointments at your clinic. Please try again later.");
        return;
      }
      System.out
          .println("Your appointment options are:");
      System.out.println("| appt_id | appt_length | date_and_time | staff_name |\n" +
          "*********************************************************************");
      int apptNo = resultSet.getInt(1);
      apptsToChooseFrom.add(apptNo);
      int apptLength = resultSet.getInt(2);
      Date date = resultSet.getDate(3);
      String staffName = resultSet.getString(7);
      System.out.println("| " + apptNo + " | " + apptLength + " | "
          + date + " | " + staffName + " |");
      while (resultSet.next()) {
        apptNo = resultSet.getInt(1);
        apptsToChooseFrom.add(apptNo);
        apptLength = resultSet.getInt(2);
        date = resultSet.getDate(3);
        staffName = resultSet.getString(7);
        System.out.println("| " + apptNo + " | " + apptLength + " | "
            + date + " | " + staffName + " |");
      }
      hasResults = createStmt.getMoreResults();
    }
  }

  public void viewMyAppointments() throws SQLException {
    String query = "{CALL view_my_appointments(?)}";
    CallableStatement createStmt = connect.prepareCall(query);
    createStmt.registerOutParameter(1, Types.VARCHAR, this.ssn);
    ResultSet result = createStmt.executeQuery();

    while (result.next()) {
      System.out.println("Appointment ID: " + result.getInt(1) +
          ", " + "Appointment length: " + result.getInt(2) + "Date and Time:" + ", "
          + result.getDate(3) + ", "
          + "Clinic name: " + result.getString(4)
          + "Staff name: " + getStaffName(result.getInt(5))
          + ", " + "City: " + result.getString(6)
          + ", " + "Street: " + result.getString(7) +
          ", " + "Zip Code: " + result.getString(8));
    }
  }

  public void makeAppointment(int apptID) {

  }
}
