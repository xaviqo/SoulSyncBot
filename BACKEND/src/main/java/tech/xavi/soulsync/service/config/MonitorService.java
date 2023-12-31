package tech.xavi.soulsync.service.config;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import tech.xavi.soulsync.entity.Song;
import tech.xavi.soulsync.repository.SongRepository;
import tech.xavi.soulsync.service.task.QueueManagerService;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MonitorService {

    private final String LOG_FILE_ROUTE = "./logs/soulsync-log.log";
    private final Resource currentLogFile;
    private final QueueManagerService queueManagerService;
    private final SongRepository songRepository;

    public MonitorService(QueueManagerService queueManagerService, SongRepository songRepository) {
        this.queueManagerService = queueManagerService;
        this.songRepository = songRepository;
        currentLogFile = new FileSystemResource(LOG_FILE_ROUTE);
    }

    public List<Song> getSongsInQueue(){
        List<Song> songsInQueue = new ArrayList<>();
        queueManagerService.getSongsInQueueIds().forEach(id -> {
            songRepository
                    .findBySpotifyId(id)
                    .ifPresent(songsInQueue::add);
        });
        return songsInQueue;
    }

    public List<String> getLastLines(Integer numLines) {
        if (numLines == null) numLines = 0;
        try {
            InputStreamReader inputStreamReader = new InputStreamReader(currentLogFile.getInputStream());
            BufferedReader reader = new BufferedReader(inputStreamReader);
            List<String> lines = reader.lines().collect(Collectors.toList());
            return (lines.size() <= numLines)
                    ? lines
                    : lines.subList(lines.size() - numLines, lines.size());
        } catch (Exception exception){
            exception.printStackTrace();
            return null;
        }
    }
}
