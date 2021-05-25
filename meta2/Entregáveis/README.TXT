Hello! Thanks for using our app!

META 1:
Here are some quick instructions:
on Windows -> click the .bat files to execute each component of the project.
on other OS -> write "java -jar name_of_the_file.jar" on a command line and press ENTER. Example: "java -jar RMIServer.jar"


Notes:
->The RMI Server only becomes operational when two instances of the RMIServer are executed.
->The config folder MUST stay in the same directory as the VotingTerminal.jar or the MulticastServer.jar for them to work.
->The address, port and voting station id in the config files may be changed.
->The lib folder must be in the same directory as the .jar files for them to work.

META 2:
/eVoting contains the intelliJ project for this goal. Note that you probably need to change the PORT that the Tomcat runs since 1099 (default) is occupied by our RMI Server
/RMIServer contains the poject for our new RMIServer. We suggest you use this instead of the one from META 1
/War contains the War file 