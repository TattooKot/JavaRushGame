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
import java.util.regex.Pattern;

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

        //Filter
        boolean filter = filter(name, title, race,profession,after,before,banned,minExperience,maxExperience,minLevel,maxLevel);
        if(!filter)
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

        //sort
        if(order == null) order = PlayerOrder.ID;
        sort(order);

        //Paging
        if(pageSize == null) pageSize = 3;
        if(pageNumber == null) pageNumber = 0;

        int indexFrom = pageNumber == 0 ? 0 : pageSize * pageNumber;
        int indexTo = Math.min(allPlayers.size(), (pageSize * (pageNumber + 1)));

        return new ResponseEntity<>(allPlayers.subList(indexFrom, indexTo), HttpStatus.OK);
    }

    @RequestMapping(value = "/count", method = RequestMethod.GET)
    public ResponseEntity<Integer> count(@RequestParam(required = false) String name, @RequestParam(required = false) String title,
                                               @RequestParam(required = false) Race race, @RequestParam(required = false) Profession profession,
                                               @RequestParam(required = false) Long after, @RequestParam(required = false) Long before,
                                               @RequestParam(required = false) Boolean banned, @RequestParam(required = false) Integer minExperience,
                                               @RequestParam(required = false) Integer maxExperience, @RequestParam(required = false) Integer minLevel,
                                               @RequestParam(required = false) Integer maxLevel)
    {
        List<Player> players = this.playerService.getAll();

        //Filter

        if(name != null)players.removeIf(player -> !player.getName().toLowerCase().contains(name.toLowerCase()));
        if(title != null)players.removeIf(player -> !player.getTitle().toLowerCase().contains(title.toLowerCase()));
        if(race != null)players.removeIf(player -> !player.getRace().toLowerCase().equalsIgnoreCase(race.name()));
        if(profession != null)players.removeIf(player -> !player.getProfession().toLowerCase().equalsIgnoreCase(profession.name()));
        if(banned != null) {
            if(banned)players.removeIf(player -> !player.getBanned());
            else players.removeIf(Player::getBanned);
        }

        if(after != null)players.removeIf(player -> player.getBirthday().getTime() < after);
        if(before != null)players.removeIf(player -> player.getBirthday().getTime() > before);
        if(before != null && after !=null)
            if(before<after)
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

        if(minExperience != null)players.removeIf(player -> player.getExperience() < minExperience);
        if(maxExperience != null)players.removeIf(player -> player.getExperience() > maxExperience);
        if(minExperience != null && maxExperience !=null)
            if(maxExperience<minExperience)
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

        if(minLevel != null)players.removeIf(player -> player.getLevel()< minLevel);
        if(maxLevel!= null)players.removeIf(player -> player.getLevel()> maxLevel);
        if(minLevel != null && maxLevel !=null)
            if(maxLevel<minLevel)
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

            return new ResponseEntity<>(players.size(), HttpStatus.OK);
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

    @PostMapping
    public ResponseEntity<Player> createPlayer(@RequestBody Player player){

        if(player.getName() == null || player.getTitle() == null || player.getBirthday() == null
                || player.getExperience() == null || player.getRace() == null || player.getProfession() == null)
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

        if(player.getName().length() > 12 || player.getTitle().length() > 30 || player.getName().equals("")
                || player.getBirthday().getTime() < 0 || player.getBirthday().getTime() > Calendar.getInstance().getTimeInMillis()
                || player.getExperience() > 10000000)
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);


        player.setLevel(player.calculateLevel(player.getExperience()));
        player.setUntilNextLevel(player.howMuchToNextLevel(player.getExperience(),player.getLevel()));

        return new ResponseEntity<>(this.playerService.savePlayer(player), HttpStatus.OK);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Player> getPlayer(@PathVariable("id") String id){

        if(id == null)
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);

        if (Pattern.matches("[a-zA-Z]+", id) || Integer.parseInt(id) <= 0)
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

        int playerId = Integer.parseInt(id);

        if(this.playerService.checkById(playerId)) {
            Player player = this.playerService.getById(Integer.parseInt(id));
            return new ResponseEntity<>(player, HttpStatus.OK);
        }

        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PostMapping("/{id}")
    public ResponseEntity<Player> updatePlayer(@PathVariable("id") String id, @RequestBody Player updatedPlayer){

        if(id == null)
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);

        if (Pattern.matches("[a-zA-Z]+", id) || Integer.parseInt(id) <= 0)
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

        Player player = getPlayer(id).getBody();

        if(player == null)
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);

        if(updatedPlayer == null)
            return new ResponseEntity<>(player, HttpStatus.OK);


//
//        Player player = getPlayer(id).getBody();
//
//        if(player == null)
//            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
//
//        if(updatedPlayer.getName().isEmpty())
//            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

        if(updatedPlayer.getName() != null) {
            if(updatedPlayer.getName().isEmpty())
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            player.setName(updatedPlayer.getName());
        }
        if(updatedPlayer.getTitle() != null)
            player.setTitle(updatedPlayer.getTitle());

        if(updatedPlayer.getProfession() != null)
            player.setProfession(updatedPlayer.getProfession());

        if(updatedPlayer.getRace() != null)
            player.setRace(updatedPlayer.getRace());

        if(updatedPlayer.getBirthday() != null) {
            if (updatedPlayer.getBirthday().getTime() < 0)
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            player.setBirthday(updatedPlayer.getBirthday());
        }

        if(updatedPlayer.getBanned() != null)
            player.setBanned(updatedPlayer.getBanned());

        if(updatedPlayer.getExperience() != null) {
            if (updatedPlayer.getExperience() < 0 || updatedPlayer.getExperience() > 10_000_000)
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            player.setExperience(updatedPlayer.getExperience());
        }

        this.playerService.savePlayer(player);

        return new ResponseEntity<>(player, HttpStatus.OK);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<HttpStatus>deletePlayer(@PathVariable("id") String id){
        if(id == null)
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);

        if (Pattern.matches("[a-zA-Z]+", id) || Integer.parseInt(id) <= 0)
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

        int playerId = Integer.parseInt(id);

        if(this.playerService.checkById(playerId)){
            this.playerService.deleteById(playerId);
            return new ResponseEntity<>(HttpStatus.OK, HttpStatus.OK);
        }

        return new ResponseEntity<>(HttpStatus.NOT_FOUND, HttpStatus.NOT_FOUND);
    }
}
