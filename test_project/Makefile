JC := javac

SRCS := $(wildcard *.java)

CLS := $(SRCS:%.java=%.class)

all: $(CLS)

$(CLS): %.class: %.java
	$(JC) $<

clean:
	rm *.class

run:
	java peerProcess.java 1001

.PHONY: all clean

.SUFFIXES: .java