package tech.xavi.soulsync.configuration.constants;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

@Configuration
public class ConfigurationFinals {

    public static final String TOKEN_PREFIX = "Bearer ";

    public final static Integer[] BIT_RATES = new Integer[]{8, 16, 24, 32, 40, 48, 56, 64, 80, 96, 112, 128, 160, 192, 224, 256, 320};

    public static final RequestMatcher[] UNFILTERED_JWT_FILTER_ROUTES = {
            new AntPathRequestMatcher(EndPoint.LOGIN, HttpMethod.POST.name())
    };

    public static final RequestMatcher[] UNFILTERED_CFG_FILTER_ROUTES = {
            new AntPathRequestMatcher(EndPoint.LOGIN, HttpMethod.POST.name()),
            new AntPathRequestMatcher(EndPoint.CONFIGURATION+"/**", HttpMethod.POST.name()),
            new AntPathRequestMatcher(EndPoint.CONFIGURATION+"/**", HttpMethod.GET.name())
    };

    public static String DEFAULT_ADMIN_USER = "admin";
    public static String DEFAULT_ADMIN_PASS = "admin";

}
