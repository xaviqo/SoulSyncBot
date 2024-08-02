package tech.xavi.soulsync.service.search;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import tech.xavi.soulsync.configuration.globals.ProcessStatus;
import tech.xavi.soulsync.dto.gateway.slskd.SlskdFile;
import tech.xavi.soulsync.entity.datafile.SearchPolicy;
import tech.xavi.soulsync.entity.db.Artist;
import tech.xavi.soulsync.entity.db.SlskdRequest;
import tech.xavi.soulsync.entity.db.SpotifySong;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileFinderServiceTest {

    private ObjectMapper objectMapper;
    @InjectMocks
    private FileFinderService fileFinderService;
    private SearchInputService searchInputService;
    private List<SlskdFile> files;

    @Mock
    private SearchPolicyService searchPolicyService;

    @BeforeEach
    void setUp() {
        searchInputService = new SearchInputService();
        MockitoAnnotations.openMocks(this);
        objectMapper = new ObjectMapper();
    }

    @Test
    void whenAlbumNotPresent_strictFindFails()  {
        //given
        SearchPolicy searchPolicy = getTestingSearchPolicy();
        SlskdRequest slskdRequest = getTestingSlskdRequest();
        SlskdFile slskdFile = getTestingSlskdFile();

        //when
        Mockito.when(searchPolicyService.getPolicyByRequest(searchPolicy.getId()))
                .thenReturn(searchPolicy);

        //then
        assertFalse(fileFinderService.strictFileFind(slskdRequest,slskdFile));
    }

    @Test
    void whenLiveIsAvoided_shouldReturnFalse() {
        SearchPolicy searchPolicy = getTestingSearchPolicy();
        SlskdRequest slskdRequest = getTestingSlskdRequest();
        SlskdFile slskdFile = getTestingSlskdFile();

        //when
        Mockito.when(searchPolicyService.getPolicyByRequest(slskdRequest))
                .thenReturn(searchPolicy);

        //then
        assertFalse(fileFinderService.flexibleFileFind(slskdRequest,slskdFile));
    }

    @Test
    void whenLiveIsAvoided_butWeAreLookingForLiveVersion_shouldReturnTrue() {
        SearchPolicy searchPolicy = getTestingSearchPolicy();
        SlskdRequest slskdRequest = getTestingSlskdRequest();
        slskdRequest.getSpotifySong().setName("Coffee & TV Live");
        slskdRequest.setSearchInput("coffee tv live");
        SlskdFile slskdFile = getTestingSlskdFile();

        //when
        Mockito.when(searchPolicyService.getPolicyByRequest(slskdRequest))
                .thenReturn(searchPolicy);

        //then
        assertTrue(fileFinderService.flexibleFileFind(slskdRequest,slskdFile));
    }

    private SlskdFile getTestingSlskdFile() {
        return SlskdFile.builder()
                .bitRate(320)
                .size(15074235)
                .filename("@@user\\\\Música\\\\B\\\\Blur\\\\2012 - 21 The Box\\\\DVD2 The Singles Night at Wembley Arena, 11 Dec 1999\\\\20 - Coffee & TV (Live).mp3")
                .id(UUID.randomUUID().toString())
                .state("state")
                .username("username")
                .build();
    }

    private SlskdRequest getTestingSlskdRequest() {
        SpotifySong spotifySong = SpotifySong.builder()
                .spotifyId(UUID.randomUUID().toString())
                .name("Coffee & TV")
                .album("13")
                .artists(List.of(Artist.builder().id("1234").name("Blur").build()))
                .build();
        return SlskdRequest.builder()
                .id(1L)
                .spotifySong(spotifySong)
                .searchInput(searchInputService.getSearchInput(spotifySong,getTestingSearchPolicy()))
                .searchId(UUID.randomUUID())
                .status(ProcessStatus.WAITING)
                .build();
    }

    private SearchPolicy getTestingSearchPolicy() {
        String searchPolicyJSON = "{\"id\":\"Default\",\"name\":\"Default\",\"fileFormatsByComa\":\"mp3\",\"minCharsPerWord\":3,\"wordsToRemoveByComa\":\"the,a,an,and,feat,with,of,in,on,at,by,for,to,featuring\",\"minimumMp3Bitrate\":320,\"minimumMinutesPerRetry\":60,\"maxRetries\":0,\"avoidRemix\":true,\"avoidLive\":true,\"avoidRadioEdit\":true,\"avoidMixedTrack\":true,\"inputStrategy\":\"STANDARD_STRATEGY\",\"downloadPriority\":\"NORMAL\"}";
        try {
            return objectMapper.readValue(searchPolicyJSON, SearchPolicy.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

}