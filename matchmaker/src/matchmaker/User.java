/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package matchmaker;

/**
 *
 * @author Vitor Castro
 */
public class User {

    private String username;
    private String password;
    private int ranking;

    public User(String user, String pass) {
        username = user;
        password = pass;
        ranking = 0;
    }

    public synchronized String getUsername() {
        return username;
    }

    public synchronized String getPassword() {
        return password;
    }

    public synchronized int getRanking() {
        return ranking;
    }

    public synchronized void increaseRanking() {
        if (ranking < 9) {
            ranking++;
        }
    }

    public synchronized void decreaseRanking() {
        if (ranking > 0) {
            ranking--;
        }
    }
}
