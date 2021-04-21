import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;

public final class Citizen extends AUser {

  private ArrayList<Integer> availableAppointments;
  private String ssn;
  private int clinic;

  Citizen(String ssn) throws SQLException {
    super();
    availableAppointments = new ArrayList<>();
    this.ssn = ssn;
    clinic = getClinic();
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


  //gets the name of the staff member given the id
  public int getClinic() throws SQLException {
    String query = "{? = CALL get_assigned_clinic(?)}";
    CallableStatement getStmt = connect.prepareCall(query);
    getStmt.registerOutParameter(1, Types.INTEGER);
    getStmt.setString(2, this.ssn);
    getStmt.execute();
    int output = getStmt.getInt(1);
    return output;
  }

  public void viewAppointmentsAtClinic() throws SQLException {
    String query = "{CALL view_available_appointment(?)}";
    CallableStatement createStmt = connect.prepareCall(query);
    createStmt.setInt(1, this.clinic);
    ResultSet result = createStmt.executeQuery();
    System.out.println(result);

    if (result == null) {
      System.out.println("no available appointments at this clinic");
    } else {
      while (result.next())
        System.out.println("Appointment ID: " + result.getInt(1) +
            ", " + "Appointment length: " + result.getInt(2) + "Date and Time:" + ", "
            + result.getDate(3) + ", "
            + "Staff name: " + getStaffName(result.getInt(7)));
    }
  }

  public void viewMyAppointments() throws SQLException {
    String query = "{CALL view_my_appointments(?)}";
    CallableStatement createStmt = connect.prepareCall(query);
    createStmt.registerOutParameter(1,Types.VARCHAR, this.ssn);
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

  public void makeAppointment(int apptID){

  }
}
