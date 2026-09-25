import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class StreakCalculator {

    private StreakCalculator() {
    }

    public static int daysInARow(List<MoodEntry> entries) {
        LocalDate today = LocalDate.now();
        Set<LocalDate> days = new HashSet<>();

        for (MoodEntry entry : entries) {
            LocalDate day = entry.getTimestamp().toLocalDate();
            if (day.isAfter(today)) {
                throw new IllegalArgumentException(
                        "Запись из будущего: " + day
                );
            }
            days.add(day);
        }

        LocalDate cursor =
                days.contains(today) ? today : today.minusDays(1);

        int streak = 0;
        while (days.contains(cursor)) {
            streak++;
            cursor = cursor.minusDays(1);
        }

        return streak;
    }
}
