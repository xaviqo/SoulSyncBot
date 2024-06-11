package tech.xavi.soulsync.service.search;

import lombok.Getter;
import org.springframework.stereotype.Service;
import tech.xavi.soulsync.dto.gateway.slskd.SlskdFile;
import tech.xavi.soulsync.dto.gateway.slskd.SlskdSearchResponse;
import tech.xavi.soulsync.entity.datafile.SearchPolicy;
import tech.xavi.soulsync.entity.db.Artist;
import tech.xavi.soulsync.entity.db.SlskdRequest;
import tech.xavi.soulsync.service.configuration.ConfigurationFieldService;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.stream.Stream;

@Service
public class FileFinderService {

    private final SearchPolicyService searchPolicyService;
    private final ConfigurationFieldService configurationFieldService;
    @Getter private final List<BiConsumer<SlskdRequest, SlskdSearchResponse>> findingModes;

    public FileFinderService(
            SearchPolicyService searchPolicyService,
            ConfigurationFieldService configurationFieldService
    ) {
        this.searchPolicyService = searchPolicyService;
        this.configurationFieldService = configurationFieldService;
        this.findingModes = List.of(this::strictFind,this::flexibleFind);
    }

    public void flexibleFind(SlskdRequest request, SlskdSearchResponse response) {
        genericFind(request, response, this::flexibleFileFind);
    }

    public void strictFind(SlskdRequest request, SlskdSearchResponse response) {
        genericFind(request, response, this::strictFileFind);
    }

    private void genericFind(
            SlskdRequest request,
            SlskdSearchResponse response,
            BiPredicate<SlskdRequest, SlskdFile> fileFindCriteria
    ) {
        if (!isRequestReady(request)) {
            sortFilesByBitRate(response.files());
            for (SlskdFile file : response.files()) {
                if (fileFindCriteria.test(request, file)) {
                    request.setSharedBy(response.username());
                    request.setFilename(file.filename());
                    request.setSize(file.size());
                    request.setBitRate(file.bitRate());
                    return;
                }
            }
        }
    }

    private boolean flexibleFileFind(SlskdRequest request, SlskdFile slskdFile) {
        return isDistinctFromLastAttempt(request, slskdFile)
                && isDesiredFormat(request, slskdFile)
                && isMp3BitRateOk(request, slskdFile)
                && isNotRemix(request, slskdFile)
                && isNotLive(request, slskdFile);
    }

    private boolean strictFileFind(SlskdRequest request, SlskdFile slskdFile) {
        return containsAllOriginalSongKeywords(request, slskdFile)
                && flexibleFileFind(request, slskdFile);
    }


    private boolean containsAllOriginalSongKeywords(SlskdRequest request, SlskdFile file) {
        String[] fileAndDirs = file.filename().split("\\\\");
        String fileName = fileAndDirs[fileAndDirs.length -1];
        Stream<String> keywords = Stream.concat(
                request
                        .getSpotifySong()
                        .getArtists()
                        .stream()
                        .map(Artist::getName),
                Stream.of(request.getSearchInput().split(" "))
        );
        return keywords.allMatch(kw ->
                fileName.toLowerCase().contains(kw.toLowerCase())
        );
    }

    private boolean isDistinctFromLastAttempt(SlskdRequest request, SlskdFile first) {
        return !Objects.nonNull(request.getFilename()) || !first.filename().equals(request.getFilename());
    }

    private boolean isDesiredFormat(SlskdRequest request, SlskdFile slskdFile) {
        String fileFormat = getFileFormat(slskdFile);
        return searchPolicyService
                .getPolicyById(request)
                .acceptedFormats()
                .stream()
                .anyMatch( f -> f.equalsIgnoreCase(fileFormat) );
    }

    private boolean isMp3BitRateOk(SlskdRequest request, SlskdFile file) {
        SearchPolicy policy = searchPolicyService
                .getPolicyById(request);
        return policy.isMp3Accepted()
                && getFileFormat(file).equalsIgnoreCase("mp3")
                && policy.getMinimumMp3Bitrate() >= file.bitRate();
    }

    private boolean isNotLive(SlskdRequest request, SlskdFile file) {
        boolean shouldAvoidLive = searchPolicyService
                .getPolicyById(request)
                .isAvoidLive();
        boolean isRequestLive = request
                .getSearchInput()
                .toLowerCase()
                .contains("live");
        if (shouldAvoidLive && isRequestLive) {
            String filenameLower = file.filename().toLowerCase();
            return !filenameLower.contains("live");
        }
        return true;
    }

    private boolean isNotRemix(SlskdRequest request, SlskdFile file) {
        boolean shouldAvoidRemix = searchPolicyService
                .getPolicyById(request)
                .isAvoidRemix();
        boolean isRequestRemix = request
                .getSearchInput()
                .toLowerCase()
                .contains("remix");
        if (shouldAvoidRemix && isRequestRemix) {
            String filenameLower = file.filename().toLowerCase();
            return !filenameLower.contains("remix")
                    && !filenameLower.contains("rmx");
        }
        return true;
    }

    private String getFileFormat(SlskdFile file){
        String[] split = file.filename().split("\\.");
        if (split.length > 0)
            return split[split.length - 1].toLowerCase();
        return "";
    }

    private boolean isRequestReady(SlskdRequest request) {
        return !Objects.isNull(request.getSharedBy());
    }

    private void sortFilesByBitRate(List<SlskdFile> slskdFiles) {
        slskdFiles.sort(Comparator.comparingInt(SlskdFile::bitRate));
    }

}
