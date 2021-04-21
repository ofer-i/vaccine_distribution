import java.sql.SQLException;

public interface IUser {

  public void createAccount(String username,String password, String type) throws SQLException;

  public void startSession(String username) throws SQLException;

  public void connect();

  public boolean userExists(String username) throws SQLException;

  public void logOut(int session) throws SQLException;

  public boolean correctLogInInformation(String username,String password) throws SQLException;

  public int getCurrentSession(String username) throws SQLException;

}
