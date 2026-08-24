package com.blinkit.clone.services;

import com.blinkit.clone.dtos.request.DarkStoreRequest;
import com.blinkit.clone.entities.DarkStore;
import com.blinkit.clone.repositories.DarkStoreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DarkStoreService {

    @Autowired
    private DarkStoreRepository darkStoreRepository;

    public List<DarkStore> getAllActive() {
        return darkStoreRepository.findByActiveTrue();
    }

    public DarkStore create(DarkStoreRequest request) {
        DarkStore store = new DarkStore();
        store.setStoreName(request.getStoreName());
        store.setLatitude(request.getLatitude());
        store.setLongitude(request.getLongitude());
        store.setActive(true);
        return darkStoreRepository.save(store);
    }
}
