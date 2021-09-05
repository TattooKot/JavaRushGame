package com.game.service;

import com.game.entity.Player;
import com.game.repository.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
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
        if (this.playerRepository.existsById(id)) {
            return this.playerRepository.findById(id).get();
        }
        return null;
    }

    public List<Player> getAll(){
        return playerRepository.findAll();
    }

    public Player savePlayer(Player player){
        return playerRepository.save(player);
    }

    public void deleteById(long id){
        if (this.playerRepository.existsById(id)) {
            this.playerRepository.deleteById(id);
        }
    }

    public boolean checkById(long id) {
        return playerRepository.existsById(id);
    }


}
