package tech.xavi.soulsync.configuration.globals;

import tech.xavi.soulsync.entity.db.DownloadList;

public enum DownloadPriority {
    HIGHEST,
    HIGH,
    ABOVE_NORMAL,
    NORMAL,
    BELOW_NORMAL,
    LOW,
    LOWEST;

    public static int compare(DownloadList dl1, DownloadList dl2) {
        return dl1.getPriorityRank() < dl2.getPriorityRank()
                ? 1
                : -1;
    }
}
