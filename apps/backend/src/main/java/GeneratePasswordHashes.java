import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class GeneratePasswordHashes {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(10);
        
        System.out.println("-- Generated BCrypt hashes (strength 10):");
        System.out.println("-- Password: admin123");
        System.out.println("INSERT admin hash: '" + encoder.encode("admin123") + "'");
        System.out.println();
        System.out.println("-- Password: recruiter123");
        System.out.println("INSERT recruiter hash: '" + encoder.encode("recruiter123") + "'");
        System.out.println();
        System.out.println("-- Password: hm123");
        System.out.println("INSERT hm hash: '" + encoder.encode("hm123") + "'");
    }
}
