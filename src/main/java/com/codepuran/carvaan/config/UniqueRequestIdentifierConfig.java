package com.codepuran.carvaan.config;

import com.codepuran.carvaan.dto.UniqueRequestIdentifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.web.context.WebApplicationContext;

import java.util.UUID;

@Configuration
public class UniqueRequestIdentifierConfig {

  @Bean(name = "requestIdentifier")
  @Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
  UniqueRequestIdentifier createUniqueIdentifier() {
    return UniqueRequestIdentifier.builder().value(UUID.randomUUID().toString()).build();
  }
}