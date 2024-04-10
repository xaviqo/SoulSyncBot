package tech.xavi.soulsync.configuration.globals;

public enum GatewayName {
    SLSKD,
    SPOTIFY;

    public static GatewayName getGatewayName(String name){
        for (GatewayName gatewayName : values())
            if (gatewayName.name().equalsIgnoreCase(name))
                return gatewayName;
        return SLSKD;
    }
}
