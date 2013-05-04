cls
SET JAVA_HOME="C:\Program Files\Java\jdk1.7.0_17"
SET M2_REPO=C:\Workspace\Development\java\localrepo

%JAVA_HOME%\bin\java -cp .\target\classes;%M2_REPO%\io\netty\netty\3.6.3.Final\netty-3.6.3.Final.jar; org.elasticsoftware.sip.SipServer