package fi.metatavu.acgbridge.server.security;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class AuthenticationWhitelistController {
  
  @Inject
  private Logger logger;

  private List<String> whitelist;
  
  @PostConstruct
  public void init() {
    ObjectMapper objectMapper = new ObjectMapper();
    
    try {
      whitelist = objectMapper.readValue(getClass().getClassLoader().getResourceAsStream("authentication-whitelist.json"), new TypeReference<List<String>>() { });
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Failed to read whitelist", e);
      whitelist = Collections.emptyList();
    } 
  }
  
  public boolean isWhitelisted(String path) {
    return whitelist.contains(path);
  }
  
}
