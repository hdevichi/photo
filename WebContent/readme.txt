Change Tomcat config to release locks on JARs when undeploying, otherwise
mail api jar won't be removed.

antiJARLocking="true" in context.xml of tomcat

TODO how to configure SSL
