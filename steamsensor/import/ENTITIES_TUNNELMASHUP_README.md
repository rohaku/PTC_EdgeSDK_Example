# examples/steamsensor/import/Entities_TunnelMashup.xml

Entities_TunnelMashup.xml contains all the entities that are required on the ThingWorx platform to test a remote session using the SteamSensor Thing.  


Refer to the following document regarding the steps necessary to perform the edge SDK remote tunnel test.   
Remote Tunnel Testing  
https://thingworx.jira.com/wiki/spaces/EDGE/pages/214991888/Remote+Tunnel+Testing  


To use this Entities_TunnelMashup.xml:  
- Start a new platform docker container from thingworx-java-sdk/src/test/resources/docker/docker-compose.yml. 
- For details see   thingworx-java-sdk/src/test/resources/docker/README.md.  
- It is assumed that this container will not have any configuration or entities.  
- Log into the ThingWorx platform and import Entities_TunnelMashup.xml.  


After importing the Entities_TunnelMashup.xml into the platform:  

1. Modify the IP of the target machine depending on your setup:    

- Go to the SteamSensor Thing   
- Under the "Configuration" tab you should see three tunnel endpoints "vnc", "rdp", and "ssh".  
- Modify the "Host" to the IP of the machine hosting the respective tunneling protocol means targeting machine.  

2. Modify the TunnelSubsystem public port used for tunnels to 8443.  

- Navigate to the configuration of the "Home>System>{Search for TunnelSubSystem}>Configuration" subsystem  
- Ensure that the "public port used for tunnels" is the same port your TWX server is hosted on or change 443 to 8443.    



