package tech.xavi.soulsync.service.relocate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.TextNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import tech.xavi.soulsync.entity.db.SlskdRequest;
import tech.xavi.soulsync.service.configuration.ConfigurationFieldService;

import static org.mockito.ArgumentMatchers.any;

class RelocationServiceTest {

    @Mock
    private ConfigurationFieldService configurationFieldService;
    @InjectMocks
    private RelocationService relocationService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        objectMapper = new ObjectMapper();
    }

    @Test
    void test() throws JsonProcessingException {
        String json = "{\n" +
                "    \"searchInput\": \"Some search input\",\n" +
                "    \"spotifySong\": {\n" +
                "      \"spotifyId\": \"spotify123\",\n" +
                "      \"name\": \"Test Song\",\n" +
                "      \"album\": \"Test Album\",\n" +
                "      \"artists\": [\n" +
                "        {\n" +
                "          \"name\": \"Test Artist 1\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"name\": \"Test Artist 2\"\n" +
                "        }\n" +
                "      ]\n" +
                "    },\n" +
                "    \"playlist\": {\n" +
                "      \"name\": \"Test Playlist\"\n" +
                "    },\n" +
                "    \"copyRoute\": \"/some/path/to/file\",\n" +
                "    \"filename\": \"test_song.mp3\"\n" +
                "  }}";

        Mockito.when(configurationFieldService.getValue(any()))
                .thenReturn(TextNode.valueOf("PLAYLIST"));

        SlskdRequest slskdRequest = objectMapper.readValue(json, SlskdRequest.class);

        System.out.println(relocationService.getRelocationFilePath(slskdRequest));
    }

}