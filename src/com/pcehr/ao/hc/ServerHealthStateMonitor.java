package com.pcehr.ao.hc;

import weblogic.management.mbeanservers.domainruntime.DomainRuntimeServiceMBean;
import weblogic.management.runtime.ServerRuntimeMBean;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import javax.naming.Context;
//import weblogic.management.jmx.MBeanServerInvocationHandler;
import java.util.Hashtable;
import java.io.IOException;
import java.net.MalformedURLException;
import weblogic.management.runtime.JDBCDataSourceRuntimeMBean;
import javax.management.*;
import javax.naming.*;


/** 
 * Java class to monitor the status of the weblogic servers
 * @author d.das
 *
 */

public class ServerHealthStateMonitor {
	
	private static MBeanServerConnection connection;
	private static JMXConnector connector;
	private static final ObjectName service;
	private static String combea = "com.bea:Name=";
	private static String service1 = "DomainRuntimeService,Type=weblogic.management.mbeanservers.domainruntime.DomainRuntimeServiceMBean";
	private static String service2 = "RuntimeService,Type=weblogic.management.mbeanservers.runtime.RuntimeServiceMBean";
	//- See more at: http://middlewaremagic.com/weblogic/?p=2851#sthash.rsAUluII.dpuf
	
	
	  /*
	   * Initialize connection to the Domain Runtime MBean Server.
	   */
	public static void initConnection(String hostname, String portString,String username, String password) throws IOException,MalformedURLException {
		String protocol = "t3";
	    Integer portInteger = Integer.valueOf(portString);
	    int port = portInteger.intValue();
	    String jndiroot = "/jndi/";
	    String mserver = "weblogic.management.mbeanservers.domainruntime";
	    JMXServiceURL serviceURL = new JMXServiceURL(protocol, hostname, port, jndiroot + mserver);
	    Hashtable h = new Hashtable();
	    h.put(Context.SECURITY_PRINCIPAL, username);
	    h.put(Context.SECURITY_CREDENTIALS, password);
	    h.put(JMXConnectorFactory.PROTOCOL_PROVIDER_PACKAGES, "weblogic.management.remote");
	    h.put("jmx.remote.x.request.waiting.timeout", new Long(10000));
	    connector = JMXConnectorFactory.connect(serviceURL, h);
	    connection = connector.getMBeanServerConnection();
	}   
	
	
	
	public void printNameAndState() throws Exception{
		ObjectName arr[]=getServerRuntimes();
		for(ObjectName temp : arr)
			System.out.println("\n\t servers: "+temp);
			ObjectName domain = (ObjectName) connection.getAttribute(service,"DomainConfiguration");
			System.out.println("Domain: " + domain.toString());
			ObjectName[] servers = (ObjectName[]) connection.getAttribute(domain,"Servers");
			for (ObjectName server : servers){
				String aName = (String) connection.getAttribute(server,"Name");
				try{
					ObjectName ser= new ObjectName("com.bea:Name="+aName+",Location="+aName+",Type=ServerRuntime");
					String serverState=(String) connection.getAttribute(ser,"State");
					System.out.println("\n\t Server: "+aName+"\t State: "+serverState);
					weblogic.health.HealthState serverHealthState=( weblogic.health.HealthState) connection.getAttribute(ser,"HealthState");
					int hState=serverHealthState.getState();
					if(hState==weblogic.health.HealthState.HEALTH_OK)
						System.out.println("\t Server: "+aName+"\t State Health: HEALTH_OK");
					if(hState==weblogic.health.HealthState.HEALTH_WARN)
						System.out.println("\t Server: "+aName+"\t State Health: HEALTH_WARN");
					if(hState==weblogic.health.HealthState.HEALTH_CRITICAL)
						System.out.println("\t Server: "+aName+"\t State Health: HEALTH_CRITICAL");
					if(hState==weblogic.health.HealthState.HEALTH_FAILED)
						System.out.println("\t Server: "+aName+"\t State Health: HEALTH_FAILED");
					if(hState==weblogic.health.HealthState. HEALTH_OVERLOADED)
						System.out.println("\t Server: "+aName+"\t State Health: HEALTH_OVERLOADED");
				}
				catch(javax.management.InstanceNotFoundException e){
					System.out.println("\n\t Server: "+aName+"\t State: SHUTDOWN (or Not Reachable)");
				}
			}
	}
   
	   
	static {
		try {
		  	service = new ObjectName(combea + service1);
		}catch (MalformedObjectNameException e) {
			throw new AssertionError(e.getMessage());
		}
	}
		  // - See more at: http://middlewaremagic.com/weblogic/?p=2851#sthash.rsAUluII.9D8fUfjy.dpuf
	   
    public static ObjectName[] getServerRuntimes() throws Exception {
    	return (ObjectName[]) connection.getAttribute(service,"ServerRuntimes");
    }
    
    
	   
    public static void main(String[] args) throws Exception {
    	String hostname = "10.81.145.19";
    	String portString = "7001";
    	String username = "weblogic";
    	String password = "Welcome123";
    	ServerHealthStateMonitor c= new ServerHealthStateMonitor();
    	initConnection(hostname, portString, username, password);
    	c.printNameAndState();
    	connector.close();
   }
	
	
}

