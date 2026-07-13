import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ClearBusinessData {
    public static void main(String[] args) {
        Set<String> preservedTables = new HashSet<>(Arrays.asList(
            "tenants",
            "users",
            "roles",
            "permissions",
            "role_permissions",
            "refresh_tokens",
            "password_reset_tokens",
            "platform_admins",
            "business_types",
            "flyway_schema_history"
        ));

        List<String[]> targets = new ArrayList<>();
        if (System.getenv("DB_URL") != null && !System.getenv("DB_URL").trim().isEmpty()) {
            targets.add(new String[]{System.getenv("DB_URL"), System.getenv("DB_USERNAME"), System.getenv("DB_PASSWORD")});
        }
        targets.add(new String[]{"jdbc:h2:file:./data/spiceflow_dev;DB_CLOSE_DELAY=-1;MODE=PostgreSQL", "sa", "password"});
        targets.add(new String[]{"jdbc:postgresql://localhost:5432/spiceflow", "spiceflow_user", "spiceflow_pass"});

        for (String[] target : targets) {
            String url = target[0];
            String user = target[1];
            String pass = target[2];
            System.out.println("\n=======================================================");
            System.out.println("Checking Database: " + url);
            try (Connection conn = DriverManager.getConnection(url, user, pass);
                 Statement stmt = conn.createStatement()) {

                boolean isH2 = url.toLowerCase().contains("h2:");
                String query = isH2
                    ? "SELECT table_name FROM information_schema.tables WHERE table_schema = 'PUBLIC' AND table_type = 'BASE TABLE'"
                    : "SELECT table_name FROM information_schema.tables WHERE table_schema = 'public' AND table_type = 'BASE TABLE'";

                ResultSet rs = stmt.executeQuery(query);
                List<String> tablesToClear = new ArrayList<>();

                while (rs.next()) {
                    String tableName = rs.getString("table_name");
                    if (!preservedTables.contains(tableName.toLowerCase())) {
                        tablesToClear.add(tableName);
                    } else {
                        System.out.println(" -> PRESERVING ACCOUNT/SYSTEM TABLE: " + tableName);
                    }
                }

                if (!tablesToClear.isEmpty()) {
                    System.out.println(" -> Found " + tablesToClear.size() + " business tables to clear: " + tablesToClear);
                    if (isH2) {
                        stmt.execute("SET REFERENTIAL_INTEGRITY FALSE;");
                        for (String tbl : tablesToClear) {
                            stmt.execute("TRUNCATE TABLE " + tbl + ";");
                        }
                        stmt.execute("SET REFERENTIAL_INTEGRITY TRUE;");
                        System.out.println(" -> SUCCESS: Cleared " + tablesToClear.size() + " business tables in H2 while preserving accounts!");
                    } else {
                        StringBuilder truncateQuery = new StringBuilder("TRUNCATE TABLE ");
                        for (int i = 0; i < tablesToClear.size(); i++) {
                            if (i > 0) truncateQuery.append(", ");
                            truncateQuery.append(tablesToClear.get(i));
                        }
                        truncateQuery.append(" CASCADE;");
                        stmt.execute(truncateQuery.toString());
                        System.out.println(" -> SUCCESS: Cleared " + tablesToClear.size() + " business tables in PostgreSQL while preserving accounts!");
                    }
                } else {
                    System.out.println(" -> No business tables found to clear in this database.");
                }

            } catch (Exception e) {
                System.out.println(" -> Skipped/Error for " + url + ": " + e.getMessage());
            }
        }
        System.out.println("=======================================================\n");
    }
}
