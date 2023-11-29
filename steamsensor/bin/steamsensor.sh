#Shell script to execute the steamsensor from the SDK distribution

export MY_ARGS="$@"
java -Dlogback.configurationFile=./logback.xml -jar steamsensor.jar $MY_ARGS