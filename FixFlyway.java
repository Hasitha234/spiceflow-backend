import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class FixFlyway {
    public static void main(String[] args) throws Exception {
        String url = "jdbc:h2:file:./data/spiceflow_dev;DB_CLOSE_DELAY=-1;MODE=PostgreSQL";
        String user = "sa";
        String password = "password";

        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            try (PreparedStatement check = conn.prepareStatement("SELECT * FROM \"flyway_schema_history\" WHERE \"version\" = '37'")) {
                ResultSet rs = check.executeQuery();
                if (rs.next()) {
                    System.out.println("Found V37 row. Description: " + rs.getString("description"));
                }
            }
            
            try (PreparedStatement pstmt = conn.prepareStatement("DELETE FROM \"flyway_schema_history\" WHERE \"version\" = '37'")) {
                int rows = pstmt.executeUpdate();
                System.out.println("Deleted " + rows + " rows from flyway_schema_history.");
            }
        }
    }
}
