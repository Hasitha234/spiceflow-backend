import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class TestDb {
    public static void main(String[] args) throws Exception {
        Class.forName("org.h2.Driver");
        String url = "jdbc:h2:file:C:/Users/bdils/OneDrive/Desktop/Sysco/spice_business/spiceflow-backend/data/spiceflow_dev;AUTO_SERVER=TRUE";
        Connection conn = DriverManager.getConnection(url, "sa", "password");
        Statement stmt = conn.createStatement();
        
        System.out.println("--- USERS ---");
        ResultSet rs = stmt.executeQuery("SELECT id, email, user_type, tenant_id FROM users");
        while(rs.next()) {
            System.out.println(rs.getInt("id") + " | " + rs.getString("email") + " | " + rs.getString("user_type") + " | " + rs.getString("tenant_id"));
        }
        conn.close();
    }
}
