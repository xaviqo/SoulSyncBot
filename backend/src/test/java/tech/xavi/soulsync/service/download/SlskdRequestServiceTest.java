package tech.xavi.soulsync.service.download;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import tech.xavi.soulsync.dto.gateway.slskd.SlskdDownloadStatusResult;
import tech.xavi.soulsync.repository.db.SlskdRequestRepository;
import tech.xavi.soulsync.service.integration.SlskdGatewayService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SlskdRequestServiceTest {

    @Mock
    private SlskdRequestRepository slskdRequestRepository;
    @Mock
    private SlskdGatewayService slskdGatewayService;
    @InjectMocks
    private SlskdRequestService slskdRequestService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        objectMapper = new ObjectMapper();
    }

    @Test
    void shouldGet3StuckDownloads() throws JsonProcessingException {
        String dwsJson = "[\n" +
                "  {\n" +
                "    \"username\": \"testUser1\",\n" +
                "    \"directories\": [\n" +
                "      {\n" +
                "        \"directory\": \"@@qbjcj\\\\\\\\Music\\\\\\\\from itunes\\\\\\\\Beethoven\\\\\\\\Symphony No. 9\",\n" +
                "        \"fileCount\": 3,\n" +
                "        \"files\": [\n" +
                "          {\n" +
                "            \"bitRate\": 320,\n" +
                "            \"size\": 7340032,\n" +
                "            \"filename\": \"@@qbjcj\\\\\\\\Music\\\\\\\\from itunes\\\\\\\\Beethoven\\\\\\\\Symphony No. 9\\\\\\\\01_Allegro_ma_non_troppo.mp3\",\n" +
                "            \"id\": \"abcd1234-efgh-5678-ijkl-9012mnop3456\",\n" +
                "            \"state\": \"Completed, Succeeded\",\n" +
                "            \"username\": \"testUser1\"\n" +
                "          },\n" +
                "          {\n" +
                "            \"bitRate\": 320,\n" +
                "            \"size\": 10485760,\n" +
                "            \"filename\": \"@@qbjcj\\\\\\\\Music\\\\\\\\from itunes\\\\\\\\Beethoven\\\\\\\\Symphony No. 9\\\\\\\\02_Molto_vivace.mp3\",\n" +
                "            \"id\": \"mnop3456-abcd-1234-efgh-5678ijkl9012\",\n" +
                "            \"state\": \"Completed, Errored\",\n" +
                "            \"username\": \"testUser1\"\n" +
                "          },\n" +
                "          {\n" +
                "            \"bitRate\": 320,\n" +
                "            \"size\": 15728640,\n" +
                "            \"filename\": \"@@qbjcj\\\\\\\\Music\\\\\\\\from itunes\\\\\\\\Beethoven\\\\\\\\Symphony No. 9\\\\\\\\03_Adagio_molto_e_cantabile.mp3\",\n" +
                "            \"id\": \"ijkl9012-mnop-3456-abcd-1234efgh5678\",\n" +
                "            \"state\": \"Completed, Cancelled\",\n" +
                "            \"username\": \"testUser1\"\n" +
                "          }\n" +
                "        ]\n" +
                "      }\n" +
                "    ]\n" +
                "  },\n" +
                "  {\n" +
                "    \"username\": \"testUser2\",\n" +
                "    \"directories\": [\n" +
                "      {\n" +
                "        \"directory\": \"@@qbjcj\\\\\\\\Music\\\\\\\\from itunes\\\\\\\\Led Zeppelin\\\\\\\\IV\",\n" +
                "        \"fileCount\": 2,\n" +
                "        \"files\": [\n" +
                "          {\n" +
                "            \"bitRate\": 192,\n" +
                "            \"size\": 5242880,\n" +
                "            \"filename\": \"@@qbjcj\\\\\\\\Music\\\\\\\\from itunes\\\\\\\\Led Zeppelin\\\\\\\\IV\\\\\\\\01_Black_Dog.mp3\",\n" +
                "            \"id\": \"7890qrst-uvwx-1234-yzab-5678cdef9012\",\n" +
                "            \"state\": \"Queued, Remotely\",\n" +
                "            \"username\": \"testUser2\"\n" +
                "          },\n" +
                "          {\n" +
                "            \"bitRate\": 192,\n" +
                "            \"size\": 7340032,\n" +
                "            \"filename\": \"@@qbjcj\\\\\\\\Music\\\\\\\\from itunes\\\\\\\\Led Zeppelin\\\\\\\\IV\\\\\\\\02_Rock_and_Roll.mp3\",\n" +
                "            \"id\": \"cdef9012-7890-qrst-uvwx-1234yzab5678\",\n" +
                "            \"state\": \"Completed, Succeeded\",\n" +
                "            \"username\": \"testUser2\"\n" +
                "          }\n" +
                "        ]\n" +
                "      }\n" +
                "    ]\n" +
                "  }\n" +
                "]";
        List<SlskdDownloadStatusResult> downloadStatusResults = objectMapper.readValue(
                dwsJson,
                new TypeReference<List<SlskdDownloadStatusResult>>(){}
        );
        Mockito.when(slskdGatewayService.getSlskdDownloads())
                .thenReturn(downloadStatusResults.stream());

        assertEquals(3, slskdRequestService.getAllStuckDownloads().toList().size());
    }

}