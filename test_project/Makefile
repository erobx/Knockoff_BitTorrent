PKG := test_project

# OUT_DIR := out

# SRCS := $(wildcard *.java)

# CLS := $(SRCS:$(SRC_DIR)/%.java=$(OUT_DIR)/%.class)

JC := javac
# JCFLAGS := -d $(OUT_DIR)/ -cp $(DIR)/

all:
	$(JC) PeerInfo.java peerProcess.java

# $(CLS): $(OUT_DIR)/%.class: $(SRC_DIR)/%.java
# 	$(JC) $(JCFLAGS) $<

clean:
	rm *.class

run:
	java -cp .. $(PKG).peerProcess 1001

# .PHONY: all clean

# .SUFFIXES: .java