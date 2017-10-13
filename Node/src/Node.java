import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.StringTokenizer;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;

public class Node{

    public static void main(String args[]) throws Exception{
        DatagramSocket s=new DatagramSocket();
        byte[] buf = new byte[1000];
        int port=55555;

        try{
            DatagramPacket dp = new DatagramPacket(buf, buf.length);

            InetAddress hostAddress = InetAddress.getByName("localhost");
            while (true) {
                BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
                String outMessage = stdin.readLine();

                if (outMessage.equals("bye"))
                    break;
                String outString = outMessage;
                buf = outString.getBytes();

                DatagramPacket out = new DatagramPacket(buf, buf.length, hostAddress, port);
                s.send(out);

                s.receive(dp);
                String rcvd = "rcvd from " + dp.getAddress() + ", " + dp.getPort() + ": "
                        + new String(dp.getData(), 0, dp.getLength());
                System.out.println(rcvd);
            }
        }
        //catch(IOException e){
        catch(Exception e){
            echo("IO Exception");
        }
    }

    //simple function to echo data to terminal
    public static void echo(String msg)
    {
        System.out.println(msg);
    }
}
