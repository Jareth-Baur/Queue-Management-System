import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class GetIPAddresses {

    public static void main(String[] args) {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();
                Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress address = addresses.nextElement();
                    String ipAddress = address.getHostAddress();
                    System.out.println("Interface: " + networkInterface.getDisplayName() + ", IP Address: " + ipAddress);
                }
            }
        } catch (SocketException e) {
            System.err.println("Could not get IP address: " + e.getMessage());
        }
    }
}
