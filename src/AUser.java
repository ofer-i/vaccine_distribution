import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Types;

public abstract class  AUser implements IUser {
  protected Connection connect;
  private boolean loggedIn;

  AUser(Connection conn) {
    this.connect = conn;
    loggedIn = false;
  }

  //creates an account
  @Override
  public void createAccount(String username, String password,String type) throws SQLException {
    String query = "{CALL add_user(?, ?, ?)}";
    CallableStatement createStmt = connect.prepareCall(query);
    createStmt.setString(1, username);
    createStmt.setString(2, password);
    createStmt.setString(3, type);
    createStmt.executeQuery();
  }

  //starting the user session
  @Override
  public void startSession(String username) throws SQLException {
    if(!this.loggedIn) {
      CallableStatement addStmt = connect.prepareCall("{CALL add_user_session(?, ?)}");
      addStmt.setString(1, username);
      Date d1 = new Date(System.currentTimeMillis());
      addStmt.setDate(2, d1);

      addStmt.execute();
      this.loggedIn = true;
    } else{
      throw new IllegalStateException("user already is in session");
    }
  }

  //makes sure the login username and password are correct.
  @Override
  public boolean correctLogInInformation(String username,String password) throws SQLException {
    CallableStatement correctStmt = connect.prepareCall("{? = CALL passwordMatches(?, ?)}");

    correctStmt.registerOutParameter(1, Types.BOOLEAN);
    correctStmt.setString(2, username);
    correctStmt.setString(3, password);

    correctStmt.execute();
    Boolean output = correctStmt.getBoolean(1);
    return output;
  }

  @Override
  public void logOut(int session) throws SQLException {
    String query = "{CALL update_user_session(?, ?)}";
    CallableStatement endStmt = connect.prepareCall(query);
    endStmt.setInt(1, session);
    Date d1 =new Date(System.currentTimeMillis());
    endStmt.setDate(2, d1);
    endStmt.execute();
    loggedIn = false;
    connect.close();
    System.exit(0);
  }

  @Override
  public int getCurrentSession(String username) throws SQLException {
    if(loggedIn) {
      CallableStatement existStmt = connect.prepareCall("{? = CALL userExists(?)}");
      existStmt.registerOutParameter(1, Types.INTEGER);
      existStmt.setString(2, username);
      existStmt.execute();
      int currentSession = existStmt.getInt(1);
      return currentSession;
    }
    else{
      throw new IllegalStateException("user not logged in");
    }
  }

  public String lookUpClinic(int clinicNo) {
    try {
      String query = "{? = CALL get_clinic_name_by_no(?)}";
      CallableStatement getStmt = connect.prepareCall(query);
      getStmt.registerOutParameter(1, Types.VARCHAR);
      getStmt.setInt(2, clinicNo);
      getStmt.execute();
      return getStmt.getString(1);
    } catch (SQLException e) {
      System.out.println("ERROR: Could not schedule appointment.");
    }
    return null;
  }

  public String lookUpApptDateTime(int apptId) {
    try {
      String query = "{? = CALL get_appt_date_time(?)}";
      CallableStatement getStmt = connect.prepareCall(query);
      getStmt.registerOutParameter(1, Types.VARCHAR);
      getStmt.setInt(2, apptId);
      getStmt.execute();
      return getStmt.getString(1);
    } catch (SQLException e) {
      System.out.println("ERROR: Could not schedule appointment.");
    }
    return null;
  }
}
