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
    int bs_port=55555;
    int node_port=5001;
    String node_name="n1";

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
    public void setName(String name){
        node_name=name;
    }
    public void setIP(String ip){
        ip_address=ip;
    }
    public void setPort(int port){
        node_port=port;
    }

    public static void main(String args[]) throws Exception {
        // for(String s:args){
            
        // }
        Node n1=new Node();
        try{
            n1.setName(args[0]);
            //n1.setPort((args[1]));
            n1.setIP(args[1]);

        }catch(Exception e){
            n1.echo("Enter the arguments as `java Node <node name> <ip address>");
        }

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
        DatagramPacket out = new DatagramPacket(buf, buf.length, hostAddress, bs_port);
        try{
            s.send(out);
            receive();
        }catch(Exception e){
            echo("Send error!");
        }
    }

    public void doReg(){
        String reg=" REG "+ip_address+" 5001 "+node_name;
        reg= "00"+(reg.length()+4)+ reg ;
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
