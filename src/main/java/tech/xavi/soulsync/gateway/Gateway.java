package tech.xavi.soulsync.gateway;

import org.springframework.web.reactive.function.client.WebClient;

public class Gateway {
    protected String SCHEME;
    protected final String BASIC_PREFIX = "Basic ";
    protected final String BEARER_PREFIX = "Bearer ";
    protected WebClient webClient;
}
