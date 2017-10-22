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

public class Node implements Runnable{
    private DatagramSocket s;
    private static Thread t1,t2;
    private String ip_address="";
    private String server_ip="localhost";
    byte[] buf = new byte[1000];
    int port=55555;
    InetAddress hostAddress;
    DatagramPacket dp;

    public void run(){
        echo("thread started...");
        try{

            startNode();        
        }catch(Exception e){
            echo("Cannot start node!");
        }
    }

    public Node() throws Exception{
        s=new DatagramSocket();
        InetAddress IP=InetAddress.getLocalHost();
        ip_address=IP.getHostAddress();
        echo("IP address: "+ip_address);
        hostAddress = InetAddress.getByName(server_ip);
        dp = new DatagramPacket(buf, buf.length);
        
    }

    public static void main(String args[]) throws Exception {
        Node n1=new Node();
        t1=new Thread(n1);
        t2=new Thread(n1);
        t1.start();


    }

    //simple function to echo data to terminal
    public void echo(String msg) {
        System.out.println(msg);

    }
    
    public void sendMessage(String outString){
        buf = outString.getBytes();
        DatagramPacket out = new DatagramPacket(buf, buf.length, hostAddress, port);
        try{
            s.send(out);
            receive();
        }catch(Exception e){
            echo("Send error!");
        }
    }

    public void doReg(){
        String reg=" REG 127.0.1.1 5001 cl1";
        reg= reg.length()+2+ reg ;
        echo(reg);
        sendMessage(reg);
    }

    public void receive(){
        try{
            s.receive(dp);
        }catch(Exception e){
            echo("revc error!");
        }
        String rcvd = "rcvd from " + dp.getAddress() + ", " + dp.getPort() + ": "
                + new String(dp.getData(), 0, dp.getLength());
        System.out.println(rcvd);
    }

    public void startNode() throws Exception{
        try{

            doReg();

            while (true) {
                BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
                String outMessage = stdin.readLine();

                if (outMessage.equals("bye"))
                    break;

                sendMessage(outMessage);


            }
        }
        //catch(IOException e){
        catch(Exception e){
            echo("IO Exception");
        }
    }



}
