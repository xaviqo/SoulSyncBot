package tech.xavi.soulsync.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tech.xavi.soulsync.configuration.globals.ApiRoutes;
import tech.xavi.soulsync.dto.configuration.SearchPolicyDto;
import tech.xavi.soulsync.entity.datafile.SearchPolicy;
import tech.xavi.soulsync.service.search.SearchPolicyService;

import java.util.Collection;

@RestController
@RequiredArgsConstructor
public class SearchPolicyController {

    private final SearchPolicyService searchPolicyService;

    @GetMapping(ApiRoutes.EP_SEARCH_POLICY)
    public ResponseEntity<Collection<SearchPolicy>> getSearchPolicyConfiguration(){
        return ResponseEntity.ok(searchPolicyService.getAllPolicies());
    }

    @GetMapping(ApiRoutes.EP_SEARCH_POLICY + "/{id}")
    public ResponseEntity<SearchPolicy> getSearchPolicyById(@PathVariable String id){
        return ResponseEntity.ok(searchPolicyService.getPolicyByRequest(id));
    }

    @PostMapping(ApiRoutes.EP_SEARCH_POLICY)
    public ResponseEntity<SearchPolicyDto> upsertSearchPolicy(@RequestBody SearchPolicyDto searchPolicyReq){
        return ResponseEntity.ok(searchPolicyService.upsert(searchPolicyReq));
    }

    @DeleteMapping(ApiRoutes.EP_SEARCH_POLICY + "/{id}")
    public ResponseEntity<Void> deleteSearchPolicy(@PathVariable String id){
        searchPolicyService.deletePolicyById(id);
        return ResponseEntity.noContent().build();
    }

}
