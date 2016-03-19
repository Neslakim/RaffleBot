package no.sebmik;

class Bet {
    private final Log log = new Log(getClass().getSimpleName());
    private double win;
    private double lose;
    boolean running;

    public Bet() {
        win = 0;
        lose = 0;
    }

    void win(double w) {
        win += w;
    }

    void lose(double l) {
        lose += l;
    }

    private String winRatio() {
        if (win == 0 && lose == 0) {
            return "0";
        }
        double percent = (win * 100) / (win + lose);
        return String.format("%.2f", percent);
    }

    private String loseRatio() {
        if (win == 0 && lose == 0) {
            return "0";
        }
        double percent = (lose * 100) / (win + lose);
        return String.format("%.2f", percent);
    }

    double winRate() {
        if (win == 0 && lose == 0) {
            return 0;
        }
        return (win * 100) / (win + lose);
    }

    double loseRate() {
        if (win == 0 && lose == 0) {
            return 0;
        }
        return (lose * 100) / (win + lose);
    }

    double getTotalWinPoints() {
        return win;
    }

    public double getTotalLosePoints() {
        return lose;
    }

    public void start() {
        log.d("Betting started", false);
        this.running = true;
    }

    public void stop() {
        log.d("Betting done", false);
        this.running = false;
    }

    public String toString() {
        return String.format("Win: %s (%s), Lose: %s (%s)", winRatio(), (int) win, loseRatio(), (int) lose);
    }
}
