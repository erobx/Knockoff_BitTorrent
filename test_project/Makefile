PKG := test_project

# OUT_DIR := out

SRCS := $(wildcard *.java)

PEER := 1002

# CLS := $(SRCS:$(SRC_DIR)/%.java=$(OUT_DIR)/%.class)

JC := javac
# JCFLAGS := -d $(OUT_DIR)/ -cp $(DIR)/

all:
	$(JC) $(SRCS)

# $(CLS): $(OUT_DIR)/%.class: $(SRC_DIR)/%.java
# 	$(JC) $(JCFLAGS) $<

clean:
	rm *.class

run:
	java -cp .. $(PKG).peerProcess $(PEER)

# .PHONY: all clean

# .SUFFIXES: .java