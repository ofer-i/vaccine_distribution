import java.sql.Connection;

public class Distributor extends AUser {

  Distributor(String username, Connection conn) {
    super(username, conn);
  }

  public void updateStock(String manName,int quantity){

  }

}
