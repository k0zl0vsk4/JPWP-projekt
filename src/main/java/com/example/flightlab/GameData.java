package com.example.flightlab;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class GameData {

    private static final String FILE_NAME = "flightlab_save.dat";
    private static GameData instance;

    private ObservableList<Player> players;
    private Player currentPlayer;

    private GameData() {
        players = FXCollections.observableArrayList();
        loadData();
    }

    public static GameData getInstance() {
        if (instance == null) {
            instance = new GameData();
        }
        return instance;
    }

    public ObservableList<Player> getPlayers() {
        return players;
    }

    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    //Creating or login of a player
    public void login(String name) {
        for (Player p : players) {
            if (p.getName().equalsIgnoreCase(name)) {
                currentPlayer = p; //Loading an existing player
                return;
            }
        }
        //If player not found - create a new one
        Player newPlayer = new Player(name);
        players.add(newPlayer);
        currentPlayer = newPlayer;
        saveData();
    }

    public void saveData() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_NAME))) {

        //Saving the list of players as a regular ArrayList
            oos.writeObject(new ArrayList<>(players));
            System.out.println("Zapisano dane.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    public void loadData() {
        File file = new File(FILE_NAME);
        if (file.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                List<Player> loadedList = (List<Player>) ois.readObject();
                players.setAll(loadedList);
                System.out.println("Wczytano graczy: " + players.size());
            } catch (IOException | ClassNotFoundException e) {
                System.err.println("Błąd odczytu danych: " + e.getMessage());
            }
        }
    }
}
