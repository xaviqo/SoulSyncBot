package tech.xavi.soulsync.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tech.xavi.soulsync.configuration.globals.ApiRoutes;
import tech.xavi.soulsync.dto.configuration.SearchPolicyDto;
import tech.xavi.soulsync.dto.playlist.AddPlaylistDto;
import tech.xavi.soulsync.dto.playlist.AddResponseDto;
import tech.xavi.soulsync.entity.datafile.SearchPolicy;
import tech.xavi.soulsync.service.playlist.AddPlaylistService;
import tech.xavi.soulsync.service.search.SearchPolicyService;

@RestController @RequiredArgsConstructor
public class PlaylistController {

    private final AddPlaylistService addPlaylistService;
    private final SearchPolicyService searchPolicyService;

    @PostMapping(ApiRoutes.EP_PLAYLIST)
    public ResponseEntity<AddResponseDto> addPlaylist(@RequestBody AddPlaylistDto addPlaylistRequest){
        return ResponseEntity.ok(addPlaylistService.handleAddPlaylistRequest(addPlaylistRequest));
    }

    @GetMapping(ApiRoutes.EP_SEARCH_POLICY)
    public ResponseEntity<?> getSearchPolicyConfiguration(){
        return ResponseEntity.ok(searchPolicyService.getAllPolicies());
    }

    @GetMapping(ApiRoutes.EP_SEARCH_POLICY+ "/{id}")
    public ResponseEntity<SearchPolicy> getSearchPolicyById(@PathVariable String id){
        return ResponseEntity.ok(searchPolicyService.getPolicyById(id));
    }

    @PostMapping(ApiRoutes.EP_SEARCH_POLICY)
    public ResponseEntity<SearchPolicyDto> upsertSearchPolicy(@RequestBody SearchPolicyDto searchPolicyReq){
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(searchPolicyService.upsert(searchPolicyReq));
    }

    @DeleteMapping(ApiRoutes.EP_SEARCH_POLICY + "/{id}")
    public ResponseEntity<Void> deleteSearchPolicy(@PathVariable String id){
        searchPolicyService.deletePolicyById(id);
        return ResponseEntity.noContent().build();
    }

}
