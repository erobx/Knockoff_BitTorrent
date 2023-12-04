all:
	javac peerProcess.java StartLocalPeers.java
	javac util/*.java peers/*.java messages/*.java

run:
	java StartLocalPeers

clean:
	rm -rf *.class
	rm -rf util/*.class peers/*.class messages/*.class

test:
	rm -rf *.class
	rm -rf util/*.class peers/*.class messages/*.class
	javac peerProcess.java StartLocalPeers.java
	javac util/*.java peers/*.java messages/*.java
	java StartLocalPeers

start:
	javac peerProcess.java StartLocalPeers.java
	javac util/*.java peers/*.java messages/*.java
	java StartLocalPeers

compileForRemote:
	javac -source 11 -target 11 peerProcess.java

moveFiles:
	scp -r ../src vboudreau1@lin114-05.cise.ufl.edu:~
	scp -r ../src vboudreau1@lin114-07.cise.ufl.edu:~
	scp -r ../src vboudreau1@lin114-08.cise.ufl.edu:~
	scp -r ../src vboudreau1@lin114-09.cise.ufl.edu:~
	scp -r ../src vboudreau1@lin114-10.cise.ufl.edu:~
	scp -r ../src vboudreau1@lin114-10.cise.ufl.edu:~
	