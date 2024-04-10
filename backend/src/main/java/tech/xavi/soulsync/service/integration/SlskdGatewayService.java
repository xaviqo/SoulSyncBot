package tech.xavi.soulsync.service.integration;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tech.xavi.soulsync.repository.gateway.SlskdGateway;
import tech.xavi.soulsync.service.configuration.ConfigurationFieldService;

@Service @RequiredArgsConstructor
public class SlskdGatewayService {

    private final SlskdGateway slskdGateway;
    private final ConfigurationFieldService configurationFieldService;



}
