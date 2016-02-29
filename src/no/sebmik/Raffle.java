package no.sebmik;

import java.util.Timer;
import java.util.TimerTask;

class Raffle {
    private final String name;
    private final boolean multi;
    private final int points;
    private int duration;
    private final Controller controller;

    public Raffle(boolean multi, int points, int duration, String name, Controller controller) {
        this.multi = multi;
        this.points = points;
        this.duration = duration;
        this.name = name;
        this.controller = controller;
    }

    public void start() {
        if (multi) {
            controller.updateRaffleText(String.format("Multi raffle for %s points", points), name);
        } else {
            controller.updateRaffleText(String.format("Raffle for %s points", points), name);
        }
        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                controller.updateRaffleTime(duration, name);
                if (duration > 0) {
                    duration--;
                } else {
                    controller.updateRaffleText("No raffle is running", name);
                    cancel();
                }
            }
        };
        timer.schedule(task, 0, 1000);
    }
}
