javac Server/src/Neighbour.java  Server/src/BootstrapServer.java
#javac Node/src/Node.java -d nodes
echo 'Server and Node compiled! server will execute now'
java Server/src/BootstrapServer
