package tech.xavi.soulsync.service.task.download;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import lombok.extern.log4j.Log4j2;
import tech.xavi.soulsync.entity.datafile.DownloadList;
import tech.xavi.soulsync.entity.db.SlskdDownload;
import tech.xavi.soulsync.service.task.AbstractProcessHandler;

@Setter @Getter
@SuperBuilder
@Log4j2
public class DownloadProcess extends AbstractProcessHandler {

    private final DownloadList downloadList;
    private final SlskdDownload slskdDownload;

    @Override
    public String getTaskName() {
        return "DOWNLOAD_PROCESS";
    }

    @Override
    public String getTaskType() {
        return "DOWNLOAD_PROCESS";
    }

    @Override
    public int getOrder() {
        return 0;
    }

}
