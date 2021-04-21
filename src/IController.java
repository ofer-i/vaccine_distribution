import java.sql.Connection;
import java.sql.SQLException;
import java.util.Scanner;

public interface IController {

  public void run(Scanner scan, Connection conn, String username);
}
