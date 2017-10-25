javac Node/src/Neighbour.java Node/src/Node.java -d Node
echo 'Node build and now running..'
cd Node
for i in {1..5}
do
	xterm -hold -e java Node &
done

java Node
