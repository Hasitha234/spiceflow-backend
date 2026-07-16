import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class FixH2 {
    public static void main(String[] args) {
        try {
            Class.forName("org.h2.Driver");
            Connection conn = DriverManager.getConnection("jdbc:h2:file:./data/spiceflow_dev", "sa", "password");
            Statement stmt = conn.createStatement();
            stmt.executeUpdate("DELETE FROM \"flyway_schema_history\" WHERE \"version\" = '40'");
            System.out.println("Flyway checksum repaired.");
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
