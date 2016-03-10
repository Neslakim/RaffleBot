package no.sebmik;

import java.util.Timer;
import java.util.TimerTask;

class Raffle {
    private final boolean multi;
    private final int points;
    private int duration;
    private final Controller controller;

    public Raffle(boolean multi, int points, int duration, Controller controller) {
        this.multi = multi;
        this.points = points;
        this.duration = duration;
        this.controller = controller;
    }

    public void start() {
        String p = String.valueOf(points);
        if (points >= 10000 && points % 1000 == 0) {
            p = String.format("%sk", points / 1000);
        }
        if (multi) {
            controller.updateRaffleText(String.format("Multi-raffle for %s points", p));
        } else {
            controller.updateRaffleText(String.format("Raffle for %s points", p));
        }
        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                controller.updateRaffleTime(duration);
                if (duration > 0) {
                    duration--;
                } else {
                    controller.updateRaffleText("No raffle is running");
                    cancel();
                }
            }
        };
        timer.schedule(task, 0, 1000);
    }
}
