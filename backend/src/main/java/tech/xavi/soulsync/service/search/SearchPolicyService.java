package tech.xavi.soulsync.service.search;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import tech.xavi.soulsync.dto.configuration.SearchPolicyDto;
import tech.xavi.soulsync.entity.datafile.SearchPolicy;
import tech.xavi.soulsync.exception.SoulSyncError;
import tech.xavi.soulsync.exception.SoulSyncException;
import tech.xavi.soulsync.repository.datafile.SearchPolicyRepository;

import java.util.Set;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class SearchPolicyService {

    private final SearchPolicyRepository searchPolicyRepository;

    public SearchPolicyDto upsert(SearchPolicyDto dto) {
        if (dto.getId() == null || dto.getId().isBlank())
            dto.setId(UUID.randomUUID().toString());
        saveEntity(dto);
        return dto;
    }

    public SearchPolicy getPolicyById(String id) {
        return searchPolicyRepository.get(id);
    }

    public Set<SearchPolicy> getAllPolicies() {
        return searchPolicyRepository
                .getAll();
    }

    public boolean containPolicyById(String id) {
        return searchPolicyRepository.contains(id);
    }

    public void deletePolicyById(String id) {
        if (getAllPolicies().size() <= 1)
            throw new SoulSyncException(
                    SoulSyncError.MINIMUM_SEARCH_POLICY,
                    HttpStatus.FORBIDDEN
            );
        searchPolicyRepository.delete(id);
    }

    public SearchPolicy saveEntity(
            SearchPolicy searchPolicyConfiguration
    ) {
        return searchPolicyRepository.save(searchPolicyConfiguration);
    }


}
