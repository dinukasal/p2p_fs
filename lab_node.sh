/home/student/Desktop/jdk1.8.0_151/bin/javac Node/src/Neighbour.java Node/src/Node.java

# ifconfig  | grep 'inet addr:'| grep -v '127.0.0.1' | cut -d: -f2 | awk '{ print $1}'
ifconfig  | grep 'inet addr:'| grep -v '127.0.0.1' | cut -d: -f2 | sed -i 's/192.168.44.236/$1' Node/src/Node.java

/home/student/Desktop/jdk1.8.0_151/bin/java Node/src/Node
