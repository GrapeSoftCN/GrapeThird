package Test;

import httpServer.booter;
import nlogger.nlogger;

public class TestThird {
    public static void main(String[] args) {
        booter booter = new booter();
        try {
            System.out.println("GrapeThird");
            System.setProperty("AppName", "GrapeThird");
            booter.start(1006);
        } catch (Exception e) {
            nlogger.logout(e);
        }
    }
}
