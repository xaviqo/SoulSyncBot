package tech.xavi.soulsync.gateway;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SlskdGateway extends Gateway {

    public SlskdGateway(
            @Value("${tech.xavi.soulsync.gateway.scheme.slsk}") String scheme
    ) {
        this.SCHEME = scheme;
    }
}
