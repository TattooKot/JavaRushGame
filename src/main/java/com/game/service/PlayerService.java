package com.game.service;

import com.game.entity.Player;
import com.game.repository.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class PlayerService {

    private final PlayerRepository playerRepository;

    @Autowired
    public PlayerService(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    public Player getById(long id){
        return playerRepository.findById(id).orElseThrow(null);
    }

    public List<Player> getAll(){
        return playerRepository.findAll();
    }

    public Player savePlayer(Player player){
        return playerRepository.save(player);
    }

    public void deleteById(long id){
        playerRepository.deleteById(id);
    }



}
