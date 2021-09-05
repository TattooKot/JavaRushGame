package com.game.entity;


import javax.persistence.*;
import java.sql.Date;

@Table(name = "player")
@Entity
public class Player {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "name", length = 12)
    private String name;

    @Column(name = "title", length = 30)
    private String title;

    @Column(name = "race", length = 20)
    private String race;

    @Column(name = "profession", length = 20)
    private String profession;

    @Column(name = "birthday")
    private Date birthday;

    @Column(name = "banned")
    private Boolean banned;

    @Column(name = "experience")
    private Integer experience;

    @Column(name = "level")
    private Integer level;

    @Column(name = "untilNextLevel")
    private Integer untilNextLevel;

    public Player() {
    }

    public Player(String name, String title, String race, String profession, Date birthday, Boolean banned, Integer experience) {
        this.name = name;
        this.title = title;
        this.race = race;
        this.profession = profession;
        this.birthday = birthday;
        this.banned = banned;
        this.experience = experience;
        this.level = calculateLevel(experience);
        this.untilNextLevel = howMuchToNextLevel(experience, level);
    }

    @Override
    public String toString() {
        return "Player{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", title='" + title + '\'' +
                ", race='" + race + '\'' +
                ", profession='" + profession + '\'' +
                ", birthday=" + birthday +
                ", banned=" + banned +
                ", experience=" + experience +
                ", level=" + level +
                ", untilNextLevel=" + untilNextLevel +
                '}';
    }

    public int calculateLevel(int experience){
        return (int) (Math.sqrt(2500+200*experience)-50)/100;
    }

    public int howMuchToNextLevel(int experience, int level){
        return 50*(level+1)*(level+2)-experience;
    }

    public Integer getUntilNextLevel() {
        return untilNextLevel;
    }

    public void setUntilNextLevel(Integer untilNextLevel) {
        this.untilNextLevel = untilNextLevel;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public Integer getExperience() {
        return experience;
    }

    public void setExperience(Integer experience) {
        this.experience = experience;
        this.level = calculateLevel(experience);
        this.untilNextLevel = howMuchToNextLevel(experience,level);
    }

    public Boolean getBanned() {
        return banned;
    }

    public void setBanned(Boolean banned) {
        this.banned = banned;
    }

    public Date getBirthday() {
        return birthday;
    }

    public void setBirthday(Date birthday) {
        this.birthday = birthday;
    }

    public String getProfession() {
        return profession;
    }

    public void setProfession(String profession) {
        this.profession = profession;
    }

    public String getRace() {
        return race;
    }

    public void setRace(String race) {
        this.race = race;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

}