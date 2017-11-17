javac Node/src/Neighbour.java Node/src/Node.java 
echo 'Node build and now running..'

for i in {1..5}
do
	xterm -hold -e java Node/src/Node &
done

java Node/src/Node
