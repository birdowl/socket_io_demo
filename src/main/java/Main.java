/**
 * Created by bird on 12/3/15.
 */
public class Main {
    public static void main(String[] args) {
        WebServer server = new WebServer();

        try {

            server.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
