package no.sebmik;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Spam {
    private final Thread thread;
    boolean running;

    public Spam(String message, String var, Listener listener, History history) {
        thread = new Thread(() -> {
            boolean b = true;
            while (running) {
                long wait = history.getTimeToWait();
                System.out.println(String.format("[%s] %s", getTime(), wait));
                try {
                    Thread.sleep(wait);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (running && listener != null) {
                    if (b) {
                        listener.sendMessageWithoutWait(message + " " + var);
                    } else {
                        listener.sendMessageWithoutWait(message);
                    }
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
        Spam spam = new Spam("asd", ".", null, new History(20));
        spam.start();
    }

    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    public String getTime() {
        Calendar cal = Calendar.getInstance();
        return dateFormat.format(cal.getTime());
    }
}
