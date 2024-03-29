package org.acme.config;

import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.config.inject.ConfigProperty;

@Path("/greeting")
public class GreetingResource {

    //@ConfigProperty(name = "greeting.message")
    @Inject
    @Named("greeting.message")
    String message;

//    @ConfigProperty(name = "greeting.suffix", defaultValue="!")
//    String suffix;
//
//    @ConfigProperty(name = "greeting.name")
//    Optional<String> name;


    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String hello() {
        return message ;
    }
}
