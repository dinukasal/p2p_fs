//package Node.src;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.*;
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

    HashMap<String, File> filesToStore = new HashMap<String, File>();

    public Node() throws Exception{
        s=new DatagramSocket();
        InetAddress IP=InetAddress.getLocalHost();
        ip_address=IP.getHostAddress();
        echo("IP address: "+ip_address);
        hostAddress = InetAddress.getByName(server_ip);
        dp = new DatagramPacket(buf, buf.length);

        List<Neighbour> joinedNodes = new ArrayList<Neighbour>();
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

    //simple function to echo data to terminal
    public void echo(String msg) {
        System.out.println(msg);
    }

    //File Searching
    public void searchFile(String fileName){

    }

    //Randomly pick two files from the file list.
    public void initializeFiles() {

        HashMap<String,File> allFiles=new HashMap<String,File>();
        allFiles.put("Lord Of the Rings", new File("G:\\Films\\LR\\Lord of the Rings.mov"));
        allFiles.put("Harry Porter 1", new File("G:\\Films\\HP\\Harry Porter 1.mov"));
        allFiles.put("Fast and Furious", new File("G:\\Films\\FF\\Fast and Furious.mov"));
        allFiles.put("La La Land", new File("G:\\Films\\LR\\La La Land.mov"));
        allFiles.put("Transformers", new File("G:\\Films\\Transformers\\Transformers.mov"));
        allFiles.put("Spider Man 1", new File("G:\\Films\\SP\\Spider Man 1.mov"));
        allFiles.put("XXX", new File("G:\\Films\\XXX\\XXX.mov"));

        //generate 3 random indices to pick files from hashmap
        int[] randomIndices = new Random().ints(1, 7).distinct().limit(3).toArray();

        //pick files randomly
        ArrayList<String> keysAsArray = new ArrayList<String>(allFiles.keySet());
        for (int fileIndex : randomIndices) {
            filesToStore.put(keysAsArray.get(fileIndex), allFiles.get(keysAsArray.get(fileIndex)));
        }

    }


    public static void main(String args[]) throws Exception {
        // for(String s:args){  
        // }

        Node n1=new Node();
        n1.initializeFiles();
        try{
            n1.setName(args[0]);
            n1.setIP(args[1]);

            n1.setPort( Integer.parseInt(args[2]) );
            //n1.setIP("localhost");

        }catch(Exception e){
            n1.echo("Enter the arguments as `java Node <node name> <ip address> <port>");
        }

        t1=new Thread(n1);
        t2=new Thread(new Runnable(){
            public void run(){
                System.out.println("t2 started...");
                n1.readStdin();
            }
        });
        t1.start();
        t2.start();

    }

    public void run(){
        echo("thread started...");
        try{

            startNode();
                    
        }catch(Exception e){
            echo("Cannot start node!");
        }
    }
    public void readStdin(){
        BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
        try{
            while(true){
                String outMessage = stdin.readLine();
                if (outMessage.equals("bye"))
                    System.exit(1);
                else
                    echo("Enter valid command");
            }
        }catch(Exception e){

        }

    }

    public void startNode() throws Exception{
        try{

            doREG();

            while (true) {

                ////////////////////////////////////////////////////////////////////////////

                //sendMessage(outMessage, server_ip, Integer.toString(bs_port) );        // outMessage == UNREG?
                ///////////////////////////////////////////////////////////////////////////


                byte[] buffer = new byte[65536];
                DatagramPacket incoming = new DatagramPacket(buffer, buffer.length);
                s.receive(incoming);

                byte[] data = incoming.getData();
                String str = new String(data, 0, incoming.getLength());
                echo(incoming.getAddress().getHostAddress() + " : " + incoming.getPort() + " - " + str);

                StringTokenizer st = new StringTokenizer(str, " ");
                String length="",command="";
                try{
                    length = st.nextToken();
                    command = st.nextToken();
                }catch(Exception e){

                }


                if (command.equals("REGOK")) {

                    int no_nodes = Integer.parseInt(st.nextToken());

                    // Loop twice if no_nodes == 2
                    while(no_nodes>0) {

                        String join_ip = st.nextToken();
                        String join_port = st.nextToken();

                        // Send JOIN request => 'length JOIN IP_address port_no'
                        String join =" JOIN "+join_ip+" "+join_port;
                        String join_msg= "00"+(join.length()+4)+ join ;
                        
                        sendMessage(join_msg, join_ip, join_port);
                        no_nodes -= 1;
                    }

                }
                // ?????????????????
                else if(command.equals("JOIN")){
                    echo("JOINED");
                    //joinedNodes.add(new Neighbour(ip, port, username));
                }


            }
        }
        //catch(IOException e){
        catch(Exception e){
            echo("IO Exception");
        }
    }

    public void doREG(){
        String reg=" REG "+ip_address+" " + node_port +" "+node_name; //node_port?
        reg= "00"+(reg.length()+4)+ reg ;
        sendMessage(reg, server_ip, Integer.toString(bs_port));
    }

    public void sendMessage(String outString, String outAddress, String outPort){
        try{

            buf = outString.getBytes();
            DatagramPacket out = new DatagramPacket(buf, buf.length, InetAddress.getByName(outAddress), Integer.parseInt(outPort));
        
            System.out.println("SENDING... => " + outString);
            s.send(out);
            receive();
        }catch(Exception e){
            echo("Send error!");
        }
    }

    // recieveReplyMessage ?
    public void receive(){
        try{
            s.receive(dp);
        }catch(Exception e){
            echo("revc error!");
        }

        String rcvd = "rcvd from " + dp.getAddress() + ":" + dp.getPort() + " => "
                + new String(dp.getData(), 0, dp.getLength());
        System.out.println(rcvd);
    }

}