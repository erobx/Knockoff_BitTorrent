all:
	javac peerProcess.java StartLocalPeers.java
	javac util/*.java peers/*.java messages/*.java

run:
	java StartLocalPeers

clean:
	rm -rf *.class
	rm -rf util/*.class peers/*.class messages/*.class
	rm -rf *.log

test:
	rm -rf *.class
	rm -rf util/*.class peers/*.class messages/*.class
	javac peerProcess.java StartLocalPeers.java
	javac util/*.java peers/*.java messages/*.java
	java StartLocalPeers
	