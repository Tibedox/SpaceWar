package ru.spacewar;

public class Player {
    String name = "Noname";
    int score;
    int kills;

    public void clone(Player player){
        name = player.name;
        score = player.score;
        kills = player.kills;
    }
}
