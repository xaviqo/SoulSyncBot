package tech.xavi.soulsync.dto.downloadlist;

import lombok.Getter;
import tech.xavi.soulsync.dto.configuration.SearchPolicyDto;
import tech.xavi.soulsync.entity.db.DownloadList;

@Getter
public class DownloadListDto extends DownloadList {
    SearchPolicyDto newSearchPolicy;
}
