/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package matchmaker;

import java.io.BufferedReader;
import java.io.IOException;

/**
 *
 * @author Vitor Castro
 */
public class ClientListener implements Runnable {

    BufferedReader in = null;

    public ClientListener(BufferedReader input) {

        in = input;
    }

    /**
     * Used to continuously read the messages broadcasted
     */
    @Override
    public void run() {
        String message;
        try {
            while ((message = in.readLine()) != null) {
                System.out.println(message);
            }
        } catch (IOException e) {
        }
    }

}
