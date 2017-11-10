
package Node.src;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.*;
import java.net.InetAddress;

import java.util.Scanner;
import java.util.concurrent.ThreadLocalRandom;
import java.util.Random;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.File;
import java.util.*;
import java.util.logging.*;

public class Node implements Runnable {
    private DatagramSocket s, node2, node3;
    private static Thread mainThread, stdReadThread;
    private String ip_address = "";
    private String server_ip = "localhost";
    byte[] buf = new byte[1000];
    int bs_port = 55555;
    int node_port = 5001; //if cli port argument is not given this port is used for node node comm
    String node_name = "n1";
    int maxHops = 3; //This is the maximum hops count for a request

    InetAddress hostAddress;
    DatagramPacket dp;

    HashMap<String, File> filesToStore = new HashMap<String, File>();
    HashMap<String, String> addressHistory = new HashMap<String, String>();
    List<Neighbour> joinedNodes = new ArrayList<Neighbour>();
    List<String> nodeFiles = new ArrayList<String>();
    private final static Logger fLogger = Logger.getLogger(Node.class.getName());

    static Thread joinThread;

    public Node() throws Exception {
        s = new DatagramSocket();
        InetAddress IP = InetAddress.getLocalHost();
        ip_address = IP.getHostAddress();
        echo("IP address: " + ip_address);
        hostAddress = InetAddress.getByName(server_ip);
        try {
            dp = new DatagramPacket(buf, buf.length);
            //initiate files
            initializeFiles();
        } catch (Exception e) {

        }
    }

    public void setName(String name) {
        node_name = name;
    }

    public void setIP(String ip) {
        ip_address = ip;
    }

    public void setPort(int port) {
        node_port = port;
    }

    public int getPort() {
        return node_port;
    }

    //simple function to echo data to terminal
    public void echo(String msg) {
        System.out.println(msg);
    }

    //File Searching
    public void searchFile(String fileName) {

    }

    public void serialize() {
        try (OutputStream file = new FileOutputStream("addresses.ser");
                OutputStream buffer = new BufferedOutputStream(file);
                ObjectOutput output = new ObjectOutputStream(buffer);) {
            output.writeObject(addressHistory);
        } catch (IOException ex) {
            fLogger.log(Level.SEVERE, "Cannot perform output.", ex);
        }
    }

    public void deserialize() {
        try (InputStream file = new FileInputStream("addresses.ser");
                InputStream buffer = new BufferedInputStream(file);
                ObjectInput input = new ObjectInputStream(buffer);) {
            //deserialize the List
            List<String> recoveredQuarks = (List<String>) input.readObject();
            //display its data
            for (String quark : recoveredQuarks) {
                System.out.println("Recovered Quark: " + quark);
            }
        } catch (ClassNotFoundException e) {
            fLogger.log(Level.SEVERE, "Cannot perform input. Class not found.", e);
        } catch (FileNotFoundException e) {
            fLogger.log(Level.SEVERE, "Cannot perform input. File not found.", e);
        } catch (IOException e) {
            fLogger.log(Level.SEVERE, "Cannot perform input. IO exception.", e);
        }
    }

    //Randomly pick two files from the file list.
    public void initializeFiles() {

        HashMap<String, File> allFiles = new HashMap<String, File>();
        allFiles.put("Lord_of_the_Rings", new File("G:\\Films\\LR\\Lord_of_the_Rings.mov"));
        allFiles.put("Harry_Porter_1", new File("G:\\Films\\HP\\Harry_Porter_1.mov"));
        allFiles.put("Fast_and_Furious", new File("G:\\Films\\FF\\Fast_and_Furious.mov"));
        allFiles.put("La_La_Land", new File("G:\\Films\\LR\\La_La_Land.mov"));
        allFiles.put("Transformers", new File("G:\\Films\\Transformers\\Transformers.mov"));
        allFiles.put("Spider_Man_1", new File("G:\\Films\\SP\\Spider_Man_1.mov"));
        allFiles.put("XXX", new File("G:\\Films\\XXX\\XXX.mov"));

        //generate 3 random indices to pick files from hashmap
        int[] randomIndices = new Random().ints(1, 7).distinct().limit(3).toArray();

        //pick files randomly
        ArrayList<String> keysAsArray = new ArrayList<String>(allFiles.keySet());
        for (int fileIndex : randomIndices) {
            filesToStore.put(keysAsArray.get(fileIndex), allFiles.get(keysAsArray.get(fileIndex)));
            System.out.println(keysAsArray.get(fileIndex));
        }

        //filesToStore.put("Lord_of_the_Rings", new File("G:\\Films\\LR\\Lord_of_the_Rings.mov"));

    }

    public void initializecommSocket(int port) { // initiating the listening for the port
        echo("listening to " + port);
        try {
            node2 = new DatagramSocket(port);
        } catch (Exception e) {
            //echo("****** another node running in the same port!\n please enter a different port");

        }
    }

    public void joinListener() {
        byte[] buffer = new byte[65536];
        DatagramPacket incoming = new DatagramPacket(buffer, buffer.length);
        initializecommSocket(node_port);
        while (true) {
            try {
                node2.receive(incoming);
            } catch (Exception e) {

            }
            byte[] data = incoming.getData();
            String str = new String(data, 0, incoming.getLength());
            echo(incoming.getAddress().getHostAddress() + " : " + incoming.getPort() + " - " + str);

            StringTokenizer st = new StringTokenizer(str, " ");
            String command = "", length = "";
            String ip = incoming.getAddress().getHostAddress();
            int port = incoming.getPort();

            try {
                length = st.nextToken();
                command = st.nextToken();
                echo("command: " + command);

                if (command.equals("JOIN")) {
                    String reply = " JOINOK 0";
                    reply = "00" + (reply.length() + 2) + reply;

                    initializecommSocket(port);
                    sendJoinReq(reply, ip, port);
                    Neighbour tempNeighbour = new Neighbour(ip, port, "neighbour");
                    joinedNodes.add(tempNeighbour);
                    // echo(Integer.toString(joinedNodes.size()));

                } else if (command.equals("JOINOK")) {
                    Neighbour tempNeighbour = new Neighbour(ip, port, "neighbour");
                    joinedNodes.add(tempNeighbour);
                    // echo(Integer.toString(joinedNodes.size()));
                } else if (command.equals("SER")) {

                    String originatorIP = st.nextToken();
                    int originatorPort = Integer.parseInt(st.nextToken());
                    String searchFile = st.nextToken();
                    int hops = Integer.parseInt(st.nextToken());

                    if (hops < 0) {
                        //handle not found situation
                        String searchResultNotFoundCommand = " SEROK 0 "+ip_address+" "+node_port+" "+(maxHops-hops);
                        searchResultNotFoundCommand = "00" + (searchResultNotFoundCommand.length() + 4) + searchResultNotFoundCommand;

                        //send message to client who generated the search query
                        sendMessage(searchResultNotFoundCommand, originatorIP, String.valueOf(originatorPort));

                    } else {
                        int totalResults = 0;
                        ArrayList<String> searchResults = new ArrayList<String>();

                        for (String fileNames : filesToStore.keySet()) {
                            System.out.println(fileNames+" "+searchFile);
                            if (fileNames.contains(searchFile)) {
                                totalResults++;
                                searchResults.add(fileNames);
                            }
                        }
                        //sending search results to originator
                        if(totalResults > 0) {
                            --hops;
                            String searchResultOkCommand = " SEROK "+totalResults+ " "+ip_address+" "+node_port+" "+(maxHops-hops);

                            //calculate message length and append resultant file names to message
                            for(String fileName: searchResults) {
                                searchResultOkCommand += " "+fileName;
                            }

                            if(searchResultOkCommand.length() < 96) {
                                searchResultOkCommand = "00" + (searchResultOkCommand.length() + 4) + searchResultOkCommand;
                            } else {
                                searchResultOkCommand = "0" + (searchResultOkCommand.length() + 4) + searchResultOkCommand;
                            }

                            //send message to client who generated the search query
                            sendMessage(searchResultOkCommand, originatorIP, String.valueOf(originatorPort));

                        }else if(totalResults == 0) {
                            //select random node from neighbours
                            Random r = new Random();
                            Neighbour randomSuccessor = null;

                            while (true) {
                                randomSuccessor = joinedNodes.get(r.nextInt(joinedNodes.size()));

                                if (!(randomSuccessor.getIp().equals(incoming.getAddress().getHostAddress())
                                        && randomSuccessor.getPort() == incoming.getPort())) {
                                    break;
                                }
                            }

                            //send search message to picked neighbour
                            String searchCommand = " SER " + originatorIP + " " + originatorPort + " Lord_of_the_Rings " + --hops;

                            searchCommand = "00" + (searchCommand.length() + 4) + searchCommand;
                            sendMessage(searchCommand, randomSuccessor.getIp(),
                                    String.valueOf(randomSuccessor.getPort()));

                            System.out.println("Request is forwareded!!!");
                        }
                    }
                } else if(command.equals("SEROK")) {
                    int totalResults = Integer.parseInt(st.nextToken());
                    String respondedNodeIP = st.nextToken();
                    int respondedNodePort = Integer.parseInt(st.nextToken());
                    int hops = Integer.parseInt(st.nextToken());

                    System.out.println("Responded Node IP: " + respondedNodeIP);
                    System.out.println("Responded Node Port: " + respondedNodePort);
                    System.out.println("Total No. of Results: " + totalResults);
                    System.out.println("No of Hops request went through: " + hops);
                    for(int i = 0; i < totalResults; i++) {
                        System.out.println(st.nextToken());
                    }
                }
            } catch (Exception e) {
                System.out.println(e);
                for (int i = 0; i < 3; i++) {   //reconnecting on the port if an eception occurs
                    initializecommSocket(node_port);
                }
            }
        }

    }

    public void sendJoinReq(String outString, String outAddress, int outPort) {
        try {
            buf = outString.getBytes();
            DatagramPacket out = new DatagramPacket(buf, buf.length, InetAddress.getByName(outAddress), outPort);

            System.out.println("SENDING... => " + outString + " to " + outPort);
            node2.send(out);
        } catch (Exception e) {

        }
    }

    public void listener() {

        byte[] buffer = new byte[65536];
        DatagramPacket incoming = new DatagramPacket(buffer, buffer.length);
        try {
            s.receive(incoming);
        } catch (Exception e) {

        }

        byte[] data = incoming.getData();
        String str = new String(data, 0, incoming.getLength());
        echo(incoming.getAddress().getHostAddress() + " : " + incoming.getPort() + " - " + str);

        StringTokenizer st = new StringTokenizer(str, " ");
        String length = "", command = "";
        try {
            length = st.nextToken();
            command = st.nextToken();
        } catch (Exception e) {

        }

        if (command.equals("REGOK")) {

            int no_nodes = Integer.parseInt(st.nextToken());

            // Loop twice if no_nodes == 2
            while (no_nodes > 0 && no_nodes<21) {

                String join_ip = st.nextToken();
                String join_port = st.nextToken();

                if (Integer.parseInt(join_port) != node_port) {
                    // Send JOIN request => 'length JOIN IP_address port_no'
                    String join = " JOIN " + join_ip + " " + join_port;
                    String join_msg = "00" + (join.length() + 4) + join;

                    sendJoinReq(join_msg, join_ip, Integer.parseInt(join_port));
                    no_nodes -= 1;
                }
            }

            if(no_nodes==9999){
                echo("There's an error in the command");
            }else if(no_nodes==9998){
                unreg();
                doREG();
            }

        }
        // ?????????????????
        else if (command.equals("JOIN")) {
            echo("JOINED");
            //joinedNodes.add(new Neighbour(ip, port, username));
        }
    }

    public static void main(String args[]) throws Exception {

        Node n1 = new Node();

        
        // n1.setPort();
        /*
        try {
            n1.setName(args[0]);
            n1.setIP(args[1]);

            // n1.setPort(Integer.parseInt(args[2]));
            n1.initializecommSocket(n1.getPort());
            //n1.setIP("localhost");

        } catch (Exception e) {
            n1.echo("Enter the arguments as `java Node <node name> <ip address> <port>");
        }
        */

        mainThread = new Thread(n1);
        stdReadThread = new Thread(new Runnable() {
            public void run() {
                System.out.println("std listener started...");
                n1.readStdin();
            }
        });

        Thread listnerThread = new Thread(new Runnable() { //thread which listens on the joining

            public void run() {
                System.out.println("** join listener on port " + n1.getPort() + " started..");
                n1.joinListener();
            }
        });

        Runtime.getRuntime().addShutdownHook(new Thread() {     //unregister on ctrl+c or exit
            public void run() {
                n1.unreg();
            }
        });

        mainThread.start();
        stdReadThread.start();
        listnerThread.start();
    }

    public void run() {
        try {
            startNode();
        } catch (Exception e) {
            echo("Cannot start node!");
        }
    }

    public void readStdin() { //get input from command line
        BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
        try {
            while (true) {
                String outMessage = stdin.readLine();

                if (outMessage.equals("bye")) {

                    System.exit(1);
                } else if (outMessage.equals("join")) {

                    sendMessage("test from n1", "127.0.1.1", "5001");

                } else if (outMessage.contains("ser")) {
                    String searchQuery = outMessage.split(" ")[1];

                    //select random node from neighbours
                    Random r = new Random();
                    Neighbour randomSuccessor = joinedNodes.get(r.nextInt(joinedNodes.size()));

                    //send search message to picked neighbour
                    String searchCommand = " SER " + ip_address + " " + node_port + " " + searchQuery +" "+maxHops;
                    searchCommand = "00" + (searchCommand.length() + 4) + searchCommand;
                    sendMessage(searchCommand, randomSuccessor.getIp(), String.valueOf(randomSuccessor.getPort()));

                } else {
                    echo("Enter valid command");
                }
            }
        } catch (Exception e) {

        }

    }

    public void startNode() throws Exception {
        try {

            doREG();

            while (true) {

                ////////////////////////////////////////////////////////////////////////////

                //sendMessage(outMessage, server_ip, Integer.toString(bs_port) );        // outMessage == UNREG?
                ///////////////////////////////////////////////////////////////////////////
                listener();

            }
        }
        //catch(IOException e){
        catch (Exception e) {
            echo("IO Exception");
        }
    }

    public void doREG() {
        Random r = new Random();
        node_port= Math.abs(r.nextInt())%6000+3000;
        
        String reg = " REG " + ip_address + " " + node_port + " " + node_name; //node_port?
        reg = "00" + (reg.length() + 4) + reg;

        sendMessage(reg, server_ip, Integer.toString(bs_port));
    }

    public void unreg() {
        String reg = " UNREG " + ip_address + " " + node_port + " " + node_name; //node_port?
        reg = "00" + (reg.length() + 4) + reg;
        sendMessage(reg, server_ip, Integer.toString(bs_port));
    }

    public void sendMessage(String outString, String outAddress, String outPort) {
        try {

            buf = outString.getBytes();
            DatagramPacket out = new DatagramPacket(buf, buf.length, InetAddress.getByName(outAddress),
                    Integer.parseInt(outPort));

            System.out.println("SENDING... => " + outString + " to " + outPort);
            s.send(out);
            //receive();
        } catch (Exception e) {
            echo("Send error!");
        }
    }

    // recieveReplyMessage ?
    public void receive() {
        try {
            s.receive(dp);
        } catch (Exception e) {
            echo("revc error!");
        }

        String rcvd = "rcvd from " + dp.getAddress() + ":" + dp.getPort() + " => "
                + new String(dp.getData(), 0, dp.getLength());
        System.out.println(rcvd);
    }

}