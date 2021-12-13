all: JndiLookup.jar

JndiLookup.jar: JndiLookup.java
	javac JndiLookup.java
	jar cmf JndiLookup.mf JndiLookup.jar JndiLookup*.class
