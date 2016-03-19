package no.sebmik;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

class Bingo {
    private final Log log = new Log(getClass().getSimpleName());
    private final Thread thread;
    private final Random r;
    private boolean running;

    public Bingo(List<String> emotes, Listener listener) {
        r = new Random();
        thread = new Thread(() -> {
            List<String> tempEmotes = new ArrayList<>(emotes);
            while (running) {
                long wait = listener.getHistory().getTimeToWait();
                try {
                    Thread.sleep(wait);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (running && tempEmotes.size() > 0) {
                    log.d(tempEmotes.remove(r.nextInt(tempEmotes.size())), false);
//                    listener.sendMessageWithoutWait(tempEmotes.remove(r.nextInt(tempEmotes.size())));
                }
            }
        });
    }

    public void start() {
        running = true;
        thread.start();
    }

    public void stop() {
        running = false;
    }
}
