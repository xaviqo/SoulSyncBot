package tech.xavi.soulsync.service.search;

import lombok.Getter;
import org.springframework.stereotype.Service;
import tech.xavi.soulsync.dto.gateway.slskd.SlskdFile;
import tech.xavi.soulsync.dto.gateway.slskd.SlskdSearchResponse;
import tech.xavi.soulsync.entity.datafile.SearchPolicy;
import tech.xavi.soulsync.entity.db.SlskdRequest;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;

@Service
public class FileFinderService {

    private final SearchPolicyService searchPolicyService;
    @Getter private final List<BiConsumer<SlskdRequest, SlskdSearchResponse>> findingModes;
    @Getter private final AtomicLong totalFoundStrict;
    @Getter private final AtomicLong totalFoundFlexible;

    public FileFinderService(SearchPolicyService searchPolicyService) {
        this.searchPolicyService = searchPolicyService;
        this.findingModes = List.of(this::strictFind,this::flexibleFind);
        this.totalFoundStrict = new AtomicLong(0);
        this.totalFoundFlexible = new AtomicLong(0);
    }

    public void flexibleFind(SlskdRequest request, SlskdSearchResponse response) {
        genericFind(request, response, this::flexibleFileFind);
    }

    public void strictFind(SlskdRequest request, SlskdSearchResponse response) {
        genericFind(request, response, this::strictFileFind);
    }

    public boolean flexibleFileFind(SlskdRequest request, SlskdFile slskdFile) {
        boolean isFound = isDistinctFromLastAttempt(request, slskdFile)
                && isDesiredFormat(request, slskdFile)
                && isMp3BitRateOk(request, slskdFile)
                && notContainsAvoidedWords(request, slskdFile);
        if (isFound) totalFoundFlexible.incrementAndGet();
        return isFound;
    }

    public boolean strictFileFind(SlskdRequest request, SlskdFile slskdFile) {
        boolean isFound = containsAllOriginalSongKeywords(request, slskdFile)
                && flexibleFileFind(request, slskdFile);
        if (isFound) totalFoundStrict.incrementAndGet();
        return isFound;
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

    public boolean containsAllOriginalSongKeywords(SlskdRequest request, SlskdFile file) {
        String sharedPath = file.filename().toLowerCase();
        String[] fileAndDirs = sharedPath.split("\\\\");
        return checkContainsAlbum(request, fileAndDirs) &&
                checkContainsArtists(request, fileAndDirs) &&
                checkContainsSongName(request, fileAndDirs);
    }

    private boolean checkContainsAlbum(SlskdRequest request, String[] fileAndDirs) {
        String albumName = request.getSpotifySong().getAlbum().toLowerCase();
        for (String fileDir : fileAndDirs)
            if (fileDir.contains(albumName)) return true;
        return false;
    }

    private boolean checkContainsArtists(SlskdRequest request, String[] fileAndDirs) {
        String[] artists = request.getSpotifySong()
                .getArtists()
                .stream()
                .map(a -> a.getName().toLowerCase())
                .toArray(String[]::new);

        for (String artist : artists) {
            boolean matchFound = false;
            for (String fileDir : fileAndDirs)
                if (fileDir.contains(artist)) {
                    matchFound = true;
                    break;
                }
            if (!matchFound) return false;
        }
        return true;
    }

    private boolean checkContainsSongName(SlskdRequest request, String[] fileAndDirs) {
        String songName = request.getSpotifySong().getName().toLowerCase();
        String fileName = fileAndDirs[fileAndDirs.length -1];
        return fileName.contains(songName);
    }

    private boolean isDistinctFromLastAttempt(SlskdRequest request, SlskdFile first) {
        return !Objects.nonNull(request.getFilename()) || !first.filename().equals(request.getFilename());
    }

    private boolean isDesiredFormat(SlskdRequest request, SlskdFile slskdFile) {
        String fileFormat = getFileFormat(slskdFile);
        return searchPolicyService
                .getPolicyByRequest(request)
                .acceptedFormats()
                .stream()
                .anyMatch( f -> f.equalsIgnoreCase(fileFormat) );
    }

    private boolean isMp3BitRateOk(SlskdRequest request, SlskdFile file) {
        SearchPolicy policy = searchPolicyService
                .getPolicyByRequest(request);
        return policy.isMp3Accepted()
                && getFileFormat(file).equalsIgnoreCase("mp3")
                && policy.getMinimumMp3Bitrate() >= file.bitRate();
    }

    private boolean notContainsAvoidedWords(SlskdRequest request, SlskdFile file){
        String searchInput = request.getSearchInput()
                .toLowerCase();
        boolean isRequestAnyOfTheCases = searchPolicyService
                .getPolicyByRequest(request)
                .getAvoidValues()
                .stream()
                .anyMatch(searchInput::contains);
        if (isRequestAnyOfTheCases) return true;
        String fileName = file.filename().toLowerCase();
        return searchPolicyService
                .getPolicyByRequest(request)
                .getAvoidValues()
                .stream()
                .noneMatch(fileName::contains);
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
