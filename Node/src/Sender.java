package Node.src; /**
 * Created by nadunindunil on 10/13/17.
 */
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

//from  j  a  va2s.c o  m
public class Sender {
    public static void main(String[] argv) throws Exception {
        InetAddress dst = InetAddress.getLocalHost();
        int port = 55555;


        String string = "0336 sender";

        byte[] outBuf = string.getBytes(StandardCharsets.UTF_8);

        System.out.println(outBuf);
        String string2 = new String(outBuf, Charset.forName("ASCII"));
        System.out.println(string2);

        int len = outBuf.length;


        DatagramPacket request = new DatagramPacket(outBuf, len, dst, port);
        DatagramSocket socket = new DatagramSocket();
        socket.send(request);


        System.out.println("packet was sent!");
    }
}
