javac Server/src/Neighbour.java  Server/src/BootstrapServer.java -d Server
#javac Node/src/Node.java -d nodes
echo 'Server and Node compiled! server will execute now'
cd Server && java server.BootstrapServer
