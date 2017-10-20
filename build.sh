javac Server/src/Neighbour.java  Server/src/BootstrapServer.java -d server
javac Node/src/Node.java -d nodes
cd server && java server/BootstrapServer
