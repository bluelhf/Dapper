package io.github.bluelhf.dappertest;

import io.github.bluelhf.dapper.DapperPlayer;

import java.util.concurrent.locks.LockSupport;

public class DapperTest {
    public static void main(String[] args) {
        DapperPlayer player = new DapperPlayer(DapperTest.class.getResourceAsStream("/song.mp3"));
        System.out.println("Starting Dapper demonstration with Caramelldansen");
        player.play();
        LockSupport.parkNanos(4000000000L);
        System.out.println("Pausing Dapper.");
        player.pause();
        LockSupport.parkNanos(1000000000L);
        System.out.println("Resuming Dapper.");
        player.resume();
        LockSupport.parkNanos(20000000000L);
        System.out.println("Closing Dapper.");
        player.close();
        System.out.println("Dapper Demonstration over. Bye bye!");
    }
}
