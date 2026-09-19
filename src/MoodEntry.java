import java.time.LocalDateTime;

public class MoodEntry {
    private final LocalDateTime timestamp;
    private final int score;
    private final String note;

    public MoodEntry(LocalDateTime timestamp, int score, String note) {
        this.timestamp = timestamp;
        this.score = score;
        this.note = note == null ? "" : note;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public int getScore() {
        return score;
    }

    public String getNote() {
        return note;
    }
}
