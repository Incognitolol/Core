package rip.alpha.core.voter;

import java.io.IOException;

/**
 * @author Moose1301
 * @date 4/18/2022
 */
public class Main {
    public static void main(String[] args) {
        try {
            new VotifierCore(Integer.parseInt(args[0]));
        } catch (Exception ex) {
            System.out.println("Error dumbass wrong port!");
        }
    }
}
