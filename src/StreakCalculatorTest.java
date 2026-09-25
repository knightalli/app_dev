import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class StreakCalculatorTest {

    private static MoodEntry entryOn(LocalDate date) {
        return new MoodEntry(date.atTime(12, 0), 5, "");
    }

    @Test
    @DisplayName("Обычный случай: записи сегодня, вчера и позавчера — серия равна 3")
    void countsConsecutiveDaysEndingToday() {
        LocalDate today = LocalDate.now();
        List<MoodEntry> entries = List.of(
                entryOn(today),
                entryOn(today.minusDays(1)),
                entryOn(today.minusDays(2))
        );

        assertEquals(3, StreakCalculator.daysInARow(entries));
    }

    @Test
    @DisplayName("Граничный случай: история пуста — серия равна 0")
    void emptyListReturnsZero() {
        assertEquals(0, StreakCalculator.daysInARow(List.of()));
    }

    @Test
    @DisplayName("Ошибочный случай: запись с датой в будущем — IllegalArgumentException")
    void futureEntryThrowsIllegalArgument() {
        List<MoodEntry> entries = List.of(
                entryOn(LocalDate.now().plusDays(1))
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> StreakCalculator.daysInARow(entries)
        );
    }
}
