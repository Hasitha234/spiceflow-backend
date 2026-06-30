import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
public class HashTest {
    public static void main(String[] args) {
        BCryptPasswordEncoder e = new BCryptPasswordEncoder();
        System.out.println(e.encode("password"));
    }
}
