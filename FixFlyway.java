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
            try (PreparedStatement check = conn.prepareStatement("SELECT * FROM \"flyway_schema_history\" ORDER BY \"installed_rank\"")) {
                ResultSet rs = check.executeQuery();
                while (rs.next()) {
                    System.out.println("V" + rs.getString("version") + " | " + rs.getString("description") + " | Success: " + rs.getBoolean("success"));
                }
            }
        }
    }
}
