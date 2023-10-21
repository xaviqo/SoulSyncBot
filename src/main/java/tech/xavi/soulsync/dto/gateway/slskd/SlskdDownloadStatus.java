package tech.xavi.soulsync.dto.gateway.slskd;


import lombok.Getter;

import java.util.List;

@Getter
public class SlskdDownloadStatus {
    String username;
    List<SlskdDirectory> directories;

    @Getter
    public static class SlskdDirectory {
        String directory;
        List<SlskdDirectoryFile> files;
    }

    @Getter
    public static class SlskdDirectoryFile {
        String id;
        String filename;
        String state;
    }
}
