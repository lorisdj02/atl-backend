package com.atl.backend.service;

import org.springframework.stereotype.Service;

import com.atl.backend.model.Player;
import com.atl.backend.repository.PlayerRepository;


@Service
public class PlayerService {
    
    private final PlayerRepository playerRepository;

    public PlayerService(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    public Player getPlayerById(Integer id) {
        return playerRepository.findById(id).orElse(null);
    }

    public Player savePlayer(Player player) {
        return playerRepository.save(player);
    }

    public void deletePlayer(Integer id) {
        playerRepository.deleteById(id);
    }
}
