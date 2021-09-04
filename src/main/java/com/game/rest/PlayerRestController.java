package com.game.rest;

import com.game.controller.PlayerOrder;
import com.game.entity.Player;
import com.game.entity.Profession;
import com.game.entity.Race;
import com.game.service.PlayerService;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/rest/players")
public class PlayerRestController {
    private final PlayerService playerService;
    private List<Player> allPlayers;
    public PlayerRestController(PlayerService playerService) {
        this.playerService = playerService;
    }

    @GetMapping
    public ResponseEntity<List<Player>> getAll(@RequestParam(required = false) String name, @RequestParam(required = false) String title,
                                               @RequestParam(required = false) Race race, @RequestParam(required = false) Profession profession,
                                               @RequestParam(required = false) Long after, @RequestParam(required = false) Long before,
                                               @RequestParam(required = false) Boolean banned, @RequestParam(required = false) Integer minExperience,
                                               @RequestParam(required = false) Integer maxExperience, @RequestParam(required = false) Integer minLevel,
                                               @RequestParam(required = false) Integer maxLevel, @RequestParam(required = false) PlayerOrder order,
                                               @RequestParam(required = false) Integer pageNumber, @RequestParam(required = false) Integer pageSize
                                               ){
        this.allPlayers = this.playerService.getAll();

        //Paging
        if(pageSize == null) pageSize = 3;
        if(pageNumber == null) pageNumber = 0;

        int indexFrom = pageNumber == 0 ? 0 : pageSize * pageNumber;
        int indexTo = Math.min(allPlayers.size(), (pageSize * (pageNumber + 1)));

        //Filter
        boolean filter = filter(name, title, race,profession,after,before,banned,minExperience,maxExperience,minLevel,maxLevel);
        if(!filter)
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

        //sort
        if(order == null) order = PlayerOrder.ID;
        sort(order);

        return new ResponseEntity<>(allPlayers.subList(indexFrom, indexTo), HttpStatus.OK);
    }

    private boolean filter(String name, String title, Race race, Profession profession, Long after, Long before,
                       Boolean banned, Integer minExperience, Integer maxExperience, Integer minLevel, Integer maxLevel ){

        boolean ok = true;
        //Filter

        if(name != null) allPlayers.removeIf(player -> !player.getName().toLowerCase().contains(name.toLowerCase()));
        if(title != null) allPlayers.removeIf(player -> !player.getTitle().toLowerCase().contains(title.toLowerCase()));
        if(race != null) allPlayers.removeIf(player -> !player.getRace().toLowerCase().equalsIgnoreCase(race.name()));
        if(profession != null) allPlayers.removeIf(player -> !player.getProfession().toLowerCase().equalsIgnoreCase(profession.name()));
        if(banned != null) {
            if(banned) allPlayers.removeIf(player -> !player.getBanned());
            else allPlayers.removeIf(Player::getBanned);
        }

        if(after != null) allPlayers.removeIf(player -> player.getBirthday().getTime() < after);
        if(before != null) allPlayers.removeIf(player -> player.getBirthday().getTime() > before);
        if(before != null && after !=null)
            if(before<after)
                ok = false;

        if(minExperience != null) allPlayers.removeIf(player -> player.getExperience() < minExperience);
        if(maxExperience != null) allPlayers.removeIf(player -> player.getExperience() > maxExperience);
        if(minExperience != null && maxExperience !=null)
            if(maxExperience<minExperience)
                ok = false;

        if(minLevel != null) allPlayers.removeIf(player -> player.getLevel()< minLevel);
        if(maxLevel!= null) allPlayers.removeIf(player -> player.getLevel()> maxLevel);
        if(minLevel != null && maxLevel !=null)
            if(maxLevel<minLevel)
                ok = false;
        return ok;
    }

    private void sort(PlayerOrder order){

        if(order.equals(PlayerOrder.ID)) {
            Comparator<Player> byId = Comparator.comparing(Player::getId);
            allPlayers.sort(byId);
        } else if(order.equals(PlayerOrder.NAME)) {
            Comparator<Player> byName = Comparator.comparing(Player::getName);
            allPlayers.sort(byName);
        } else if(order.equals(PlayerOrder.LEVEL)) {
            Comparator<Player> byLevel = Comparator.comparing(Player::getLevel);
            allPlayers.sort(byLevel);
        } else if(order.equals(PlayerOrder.BIRTHDAY)) {
            Comparator<Player> byBirthday = Comparator.comparing(Player::getBirthday);
            allPlayers.sort(byBirthday);
        } else if(order.equals(PlayerOrder.EXPERIENCE)) {
            Comparator<Player> byExperience = Comparator.comparing(Player::getExperience);
            allPlayers.sort(byExperience);
        }
    }

    @RequestMapping(value = "/count", method = RequestMethod.GET)
    public ResponseEntity<Integer> count(){
        return new ResponseEntity<>(this.allPlayers.size(), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<Player> createPlayer(@RequestBody Player player){

        if(player.getName() == null || player.getTitle() == null || player.getBirthday() == null
                || player.getExperience() == null || player.getRace() == null || player.getProfession() == null)
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

        if(player.getName().length() > 12 || player.getTitle().length() > 30 || player.getName().equals("")
                || player.getBirthday().getTime() < 0 || player.getBirthday().getTime() > Calendar.getInstance().getTimeInMillis())
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

        player.setLevel(player.calculateLevel(player.getExperience()));
        player.setUntilNextLevel(player.howMuchToNextLevel(player.getExperience(),player.getLevel()));

        return new ResponseEntity<>(this.playerService.savePlayer(player), HttpStatus.OK);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Player> getPlayer(@PathVariable("id") Long id){

        if(id < 0)
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

        Player player = this.playerService.getById(id);

        if(player == null)
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);

        return  new ResponseEntity<>(player,HttpStatus.OK);
    }

    @PostMapping("/{id}")
    public ResponseEntity<Player> updatePlayer(@PathVariable("id") long id, @RequestBody Player updatedPlayer){
        if(id<0)
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

        Player player = getPlayer(id).getBody();

        if(player == null)
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);

        if(updatedPlayer.getName() != null)
            player.setName(updatedPlayer.getName());

        if(updatedPlayer.getTitle() != null)
            player.setTitle(updatedPlayer.getTitle());

        if(updatedPlayer.getProfession() != null)
            player.setProfession(updatedPlayer.getProfession());

        if(updatedPlayer.getRace() != null)
            player.setRace(updatedPlayer.getRace());

        if(updatedPlayer.getBirthday() != null)
            player.setBirthday(updatedPlayer.getBirthday());

        if(updatedPlayer.getBanned() != null)
            player.setBanned(updatedPlayer.getBanned());

        if(updatedPlayer.getExperience() != null)
            player.setExperience(updatedPlayer.getExperience());

        this.playerService.savePlayer(player);

        return new ResponseEntity<>(player, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public void deletePlayer(@PathVariable("id") long playerId){
        this.playerService.deleteById(playerId);
    }
}