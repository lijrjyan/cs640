JC = javac
JFLAGS = 

all: Iperfer.class

Iperfer.class: Iperfer.java
	$(JC) $(JFLAGS) Iperfer.java

clean:
	rm -f *.class

run-server:
	java Iperfer -s -p 5001

run-client:
	java Iperfer -c -h localhost -p 5001 -t 10