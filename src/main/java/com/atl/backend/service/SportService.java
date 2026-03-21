package com.atl.backend.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.atl.backend.model.Sport;
import com.atl.backend.repository.SportRepository;

@Service
public class SportService {
    final SportRepository sportRepository;

    public SportService(SportRepository sportRepository) {
        this.sportRepository = sportRepository;
    }

    public void saveSport(com.atl.backend.model.Sport sport) {
        sportRepository.save(sport);
    }

    public Optional<Sport> getSportById(Integer id) {
        return sportRepository.findById(id);
    }

    public Optional<Sport> getSportByName(String name) {
        return sportRepository.findByName(name);
    }

    public List<Sport> getAllSports() {
        return sportRepository.findAll();
    }

    public void deleteAllSports() {
        sportRepository.deleteAll();
    }
}
