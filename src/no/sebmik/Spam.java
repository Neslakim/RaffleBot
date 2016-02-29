package no.sebmik;

import org.pircbotx.Channel;

class Spam {
    private final Thread thread;
    boolean running;

    public Spam(String message, Channel channel, History history) {
        thread = new Thread(() -> {
            int c = 1;
            boolean b = true;
            while (running) {
                long wait = history.getTimeToWait();
                try {
                    Thread.sleep(wait);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
//                channel.send().message(message + " " + c++);
                if (b) {
                    channel.send().message(message + " .");
                    b = !b;
                } else {
                    channel.send().message(message);
                    b = !b;
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

    public static void main(String[] args) {
        Spam spam = new Spam("asd", null, new History(20));
        spam.start();
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        spam.stop();
    }
}
