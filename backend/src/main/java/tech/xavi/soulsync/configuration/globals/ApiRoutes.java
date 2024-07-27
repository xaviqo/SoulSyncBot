package tech.xavi.soulsync.configuration.globals;

import jakarta.servlet.http.HttpServletRequest;
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
    public static final String EP_INIT_SETUP_APIS = EP_INITIAL_SETUP + "/apis";
    public static final String EP_INIT_SETUP_ADMIN = EP_INITIAL_SETUP + "/admin";
    public static final String EP_IS_INSTALLED = EP_CONFIGURATION_PATH + "/is-installed";
    public static final String EP_FIELDS = EP_CONFIGURATION_PATH + "/fields";
    public static final String EP_FIELD = EP_CONFIGURATION_PATH + "/field";

    // ACCOUNT
    public static final String EP_ACCOUNT = API_ROOT + "/account";
    public static final String EP_ACC_CREATE_USER= EP_ACCOUNT + "/create";
    public static final String EP_ACC_DELETE_USER= EP_ACCOUNT + "/delete";
    public static final String EP_ACC_SIGN_IN = EP_ACCOUNT + "/sign-in";
    public static final String EP_ACC_GET_USERS = EP_ACCOUNT + "/users";

    // PLAYLIST
    public static final String EP_PLAYLIST = API_ROOT + "/playlist";
    public static final String EP_SEARCH_POLICY = EP_PLAYLIST + "/search-policy";
    public static final String EP_PLAYLIST_DISCOGRAPHY = EP_PLAYLIST + "/{id}/parent-playlists";
    public static final String EP_PLAYLIST_DOWNLOADS = EP_PLAYLIST + "/{playlistId}/download-lists";
    public static final String EP_PLAYLIST_SONGS = EP_PLAYLIST + "/{id}/songs";
    public static final String EP_PLAYLIST_FORCE_UPDATE = EP_PLAYLIST + "/{playlistId}/force-update";

    // DOWNLOAD LIST
    public static final String EP_DOWNLOAD_LIST = API_ROOT + "/download-list";
    public static final String EP_DOWNLOAD_LIST_TRACKS = EP_DOWNLOAD_LIST + "/{downloadListId}/tracks";
    public static final String EP_DOWNLOAD_LIST_PAUSE = EP_DOWNLOAD_LIST + "/{downloadListId}/pause";
    public static final String EP_DOWNLOAD_LIST_BY_POLICY = EP_DOWNLOAD_LIST + "/by-policy/{searchPolicyId}";

    public static final RequestMatcher[] NO_JWT_FILTER_EPS = {
            new AntPathRequestMatcher(EP_HEALTH_CHECK, HttpMethod.GET.name()),
            new AntPathRequestMatcher(EP_IS_INSTALLED, HttpMethod.GET.name()),
            new AntPathRequestMatcher(EP_INITIAL_SETUP, HttpMethod.GET.name()),
            new AntPathRequestMatcher(EP_INIT_SETUP_APIS, HttpMethod.POST.name()),
            new AntPathRequestMatcher(EP_INIT_SETUP_ADMIN,HttpMethod.POST.name()),
            new AntPathRequestMatcher(EP_ACC_SIGN_IN,HttpMethod.POST.name())
    };

    public static final RequestMatcher[] NO_AVAILABLE_FOR_DEMO_USER = {
            new AntPathRequestMatcher(EP_FIELDS,HttpMethod.POST.name()),
            new AntPathRequestMatcher(EP_ACC_GET_USERS,HttpMethod.GET.name()),
            new AntPathRequestMatcher(EP_ACC_CREATE_USER,HttpMethod.POST.name()),
            new AntPathRequestMatcher(EP_ACC_DELETE_USER,HttpMethod.DELETE.name()),
    };

    public static boolean isApiRequest(HttpServletRequest request){
        return request
                .getRequestURI()
                .startsWith(ApiRoutes.API_ROOT);
    }

}
