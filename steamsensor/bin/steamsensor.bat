REM Batch script to execute the steamsensor from the SDK distribution

set MY_ARGS=%*

java -Dlogback.configurationFile=%~dp0\logback.xml -jar %~dp0\steamsensor.jar %MY_ARGS%
