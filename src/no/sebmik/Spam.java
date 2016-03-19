package no.sebmik;

class Spam {
    private final Thread thread;
    boolean running;

    public Spam(String message, String var, Listener listener) {
        thread = new Thread(() -> {
            boolean b = true;
            while (running) {
                long wait = listener.getHistory().getTimeToWait();
                try {
                    Thread.sleep(wait);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (running) {
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
}
