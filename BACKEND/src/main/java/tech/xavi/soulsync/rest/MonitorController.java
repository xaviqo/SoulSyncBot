package tech.xavi.soulsync.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tech.xavi.soulsync.configuration.constants.EndPoint;
import tech.xavi.soulsync.entity.Song;
import tech.xavi.soulsync.service.configuration.MonitorService;

import java.util.List;

@RequiredArgsConstructor
@RestController
public class MonitorController {

    private final MonitorService monitorService;

    @GetMapping(EndPoint.MONITOR_GET_LOGS)
    public ResponseEntity<List<String>> getConfigurationFields(@RequestParam(required = false) Integer lines){
        return ResponseEntity.ok(monitorService.getLastLines(lines));
    }

    @GetMapping(EndPoint.MONITOR_GET_QUEUE)
    public ResponseEntity<List<Song>> getSongsQueue(){
        return ResponseEntity.ok(monitorService.getSongsInQueue());
    }

}
