package tech.xavi.soulsync.repository.datafile;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Repository;
import tech.xavi.soulsync.entity.datafile.SearchPolicy;

import java.util.Map;

@Repository
public class SearchPolicyRepository extends KeyValueJsonRepository<String, SearchPolicy> {

    public SearchPolicyRepository(ObjectMapper mapper) {
        super(mapper, "search-policy");
    }

    public synchronized SearchPolicy save(SearchPolicy spc){
        synchronized (this.data) { put(spc.getId(), spc); }
        return spc;
    }

    public synchronized SearchPolicy get(String id){
        return this.get(id, SearchPolicy.class);
    }

    public synchronized boolean contains(String id){
        return this.containsKey(id);
    }

    public synchronized void delete(String id){
        this.remove(id);
    }

    @Override
    protected TypeReference<Map<String, SearchPolicy>> getTypeReference() {
        return new TypeReference<Map<String, SearchPolicy>>() {};
    }
}
