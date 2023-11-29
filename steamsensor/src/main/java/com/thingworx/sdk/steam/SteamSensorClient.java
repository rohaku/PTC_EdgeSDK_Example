package com.thingworx.sdk.steam;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import com.thingworx.communications.client.ClientConfigurator;
import com.thingworx.communications.client.ConnectedThingClient;
import com.thingworx.communications.client.things.VirtualThing;
import com.thingworx.communications.client.things.VirtualThingPropertyChangeEvent;
import com.thingworx.communications.client.things.VirtualThingPropertyChangeListener;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SteamSensorClient extends ConnectedThingClient {
    private static final Logger LOG = LoggerFactory.getLogger(SteamSensorClient.class);
    static String hostName = "";
    static int port = 8080;
    static int logLevel = 2;
    static String thingworxUri = "";

    public SteamSensorClient(ClientConfigurator config) throws Exception {
        super(config);
    }

    public static void main(String[] args) throws Exception {

        Options options = new Options();
        options.addOption("t", true, "ThingName defaults to SteamSensor");
        options.addOption("c", true, "Count, number of Steam sensors to create.");
        options.addOption("h", true, "Hostname of ThingWorx Platform");
        options.addOption("d", false, "Disable HTTPS error checking");
        options.addOption("f", false, "Force use of HTTPS on selected port");
        options.addOption("p", true, "Port used by ThingWorx Platform (Defaults to 8080)");
        options.addOption("k", true, "appKey used to authenticate");
        options.addOption("l", true, "Logging level 1=TRACE,(2=DEBUG),3=INFO,4=WARN,5=ERROR,6=FORCE,7=AUDIT");
        options.addOption("g", true, "Gateway Name (If not given, no gateway will be created)");
        options.addOption("j", true, "Proxy Hostname (If not provided, no proxy will be used)");
        options.addOption("q", true, "Proxy Password (Optional)");
        options.addOption("r", true, "Proxy Port (Optional)");
        options.addOption("u", true, "Proxy User (Optional)");
        options.addOption("n", false, "Enable Tunneling (Optional, Defaults to false)");

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse( options, args);

        if (args.length == 0 || !cmd.hasOption("h")|| !cmd.hasOption("k")) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("SteamSensor", options);
            System.exit(-1);
        }

        // Build URI
        thingworxUri=null;
        if(cmd.hasOption("p")){
            port = Integer.parseInt(cmd.getOptionValue("p"));
        }
        if(cmd.hasOption("f")||port==443||port==8443){
            thingworxUri="wss://"+cmd.getOptionValue("h")+":"+port+"/Thingworx/WS";
        } else {
            thingworxUri="ws://"+cmd.getOptionValue("h")+":"+port+"/Thingworx/WS";
        }

        // Set the required configuration information
        ClientConfigurator config = new ClientConfigurator();
        config.setUri(thingworxUri);

        // Reconnect every 15 seconds if a disconnect occurs or if initial connection cannot be made
        config.setReconnectInterval(15);

        // Enable tunneling if requested
        if(cmd.hasOption("n")) {
            config.tunnelsEnabled(true);
        }

        // Configure Proxy if required
        if(cmd.hasOption("j")) {
            config.setProxyHost(cmd.getOptionValue("j"));
            config.setProxyPort(Integer.parseInt(cmd.getOptionValue("r")));
            config.setProxyUser(cmd.getOptionValue("u"));
            
            if(cmd.hasOption("q")) {
            	config.setProxyPassCallback( new SamplePasswordCallback(cmd.getOptionValue("q")) );
            }
        }
        
        // Override default log levels
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        //StatusPrinter.print(loggerContext);
        ch.qos.logback.classic.Logger logger = loggerContext.getLogger("ROOT");
        Appender<ILoggingEvent> fileLogAppender = logger.getAppender("ROLLING");
        if(fileLogAppender!=null){
            System.out.println("***** Detaching rolling appender.");
            logger.detachAppender("ROLLING");
        }

        if(cmd.hasOption("l")) {
            int level = 0;
            try {
                level = Integer.parseInt(cmd.getOptionValue("l"));
            } catch (NumberFormatException e){ }
            switch(level) {
                case 1:
                    logger.setLevel(Level.TRACE);
                    break;
                case 2:
                    logger.setLevel(Level.DEBUG);
                    break;
                case 3:
                    logger.setLevel(Level.INFO);
                    break;
                case 4:
                    logger.setLevel(Level.WARN);
                    break;
                case 5:
                    logger.setLevel(Level.ERROR);
                    break;
                default: {
                    logger.setLevel(Level.ALL);
                    LOG.warn("Log Level has been set to ALL.");
                }
            }
        }

        // Set the security using an Application Key
        config.setSecurityClaims( new SamplePasswordCallback( cmd.getOptionValue("k") ) );
        

        // Set the name of the gateway
        if(cmd.hasOption("g")) {
            config.setName(cmd.getOptionValue("g"));
            config.setAsSDKType();
        } else {
            config.setName(null);
        }

        // This will allow us to test against a server using a self-signed certificate.
        // This should be removed for production systems.
        if(cmd.hasOption("d")) {
            config.ignoreSSLErrors(true); // All self signed certs
        }

        // Get the scan rate (milliseconds) that is specific to this example
        // The example will execute the processScanRequest of the VirtualThing
        // based on this scan rate
        int scanRate = 3000; // 3 seconds

        // decide how many things will be created and create a latch to use
        // to wait for each bind notification to complete
        int startSensor = 0;
        int nSensors = 1;
        if(cmd.hasOption("c")){
            nSensors=Integer.parseInt(cmd.getOptionValue("c"));
        }

        // Create the client passing in the configuration from above
        SteamSensorClient client = new SteamSensorClient(config);

        String thingBaseName = "SteamSensor";
        if(cmd.hasOption("t")) {
            thingBaseName = cmd.getOptionValue("t");
        }

        for (int sensor = 0; sensor < nSensors; sensor++) {
            int sensorID = startSensor + sensor;
            String thingName = thingBaseName + sensorID;
            if(1==nSensors)
                thingName = thingBaseName;
            final SteamThing steamSensorThing =
                    new SteamThing(thingName, "Steam Sensor #" + sensorID, null, client,fileLogAppender);
            client.bindThing(steamSensorThing);

            steamSensorThing.addPropertyChangeListener(new VirtualThingPropertyChangeListener() {
                @Override
                public void propertyChangeEventReceived(VirtualThingPropertyChangeEvent evt) {
                    if ("TemperatureLimit".equals(evt.getPropertyDefinition().getName())) {
                        System.out.println(String.format("Temperature limit on %s has been changed to %s°.", steamSensorThing.getName(),
                            evt.getPrimitiveValue().getValue()));
                    }
                }
            });
        }

        try {
            // Start the client
            client.start();
        } catch (Exception eStart) {
            System.out.println("Initial Start Failed : " + eStart.getMessage());
        }

        // As long as the client has not been shutdown, continue
        while (!client.isShutdown()) {
            // Only process the Virtual Things if the client is connected
            if (client.isConnected()) {
                // Loop over all the Virtual Things and process them
                for (VirtualThing thing : client.getThings().values()) {
                    try {
                        thing.processScanRequest();
                    } catch (Exception eProcessing) {
                        System.out.println("Error Processing Scan Request for [" + thing.getName() + "] : " + eProcessing.getMessage());
                    }
                }
            }
            // Suspend processing at the scan rate interval
            Thread.sleep(scanRate);
        }
    }
}
