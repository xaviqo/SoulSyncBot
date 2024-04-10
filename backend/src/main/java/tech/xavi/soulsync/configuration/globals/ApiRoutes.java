package tech.xavi.soulsync.configuration.globals;

import org.springframework.http.HttpMethod;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

public class ApiRoutes {
    public static final String API_VERSION = "/v2";
    public static final String API_ROOT = API_VERSION + "/api";

    public static final String EP_HEALTH_CHECK = API_ROOT + "/health";

    // CFG
    public static final String EP_CONFIGURATION_PATH = API_ROOT + "/cfg";
    public static final String EP_INITIAL_SETUP = EP_CONFIGURATION_PATH + "/initial-setup";
    public static final String EP_IS_INSTALLED = EP_CONFIGURATION_PATH + "/is-installed";

    // ACCOUNT
    public static final String EP_ACCOUNT = API_ROOT + "/account";
    public static final String EP_ACC_SIGN_IN = EP_ACCOUNT + "/sign-in";

    // PLAYLIST
    public static final String EP_PLAYLIST = API_ROOT + "/playlist";

    public static final RequestMatcher[] SECURED_EPS = {
            new AntPathRequestMatcher(EP_PLAYLIST, HttpMethod.POST.name())
    };

}
