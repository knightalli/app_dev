import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;

public class MoodStorage {

    private final Path storageFile;

    public MoodStorage() {
        storageFile = Paths.get(
                System.getProperty("user.home"),
                ".mood-pulse",
                "history.tsv"
        );
    }

    public List<MoodEntry> load() {
        List<MoodEntry> entries = new ArrayList<>();

        if (!Files.exists(storageFile)) {
            return entries;
        }

        try {
            for (String line : Files.readAllLines(storageFile, StandardCharsets.UTF_8)) {
                if (line.isBlank()) {
                    continue;
                }

                String[] parts = line.split("\t", 3);

                if (parts.length < 3) {
                    continue;
                }

                try {
                    LocalDateTime timestamp =
                            LocalDateTime.parse(parts[0]);

                    int score =
                            Integer.parseInt(parts[1]);

                    String note =
                            new String(
                                    Base64.getDecoder().decode(parts[2]),
                                    StandardCharsets.UTF_8
                            );

                    if (score >= 1 && score <= 10) {
                        entries.add(
                                new MoodEntry(
                                        timestamp,
                                        score,
                                        note
                                )
                        );
                    }

                } catch (Exception ignored) {
                }
            }

        } catch (IOException ignored) {
        }

        entries.sort(
                Comparator.comparing(
                        MoodEntry::getTimestamp
                )
        );

        return entries;
    }

    public void saveOrUpdateToday(
            int score,
            String note
    ) throws IOException {

        saveOrUpdateDate(
                LocalDate.now(),
                score,
                note
        );
    }

    public void saveOrUpdateDate(
            LocalDate date,
            int score,
            String note
    ) throws IOException {

        if (score < 1 || score > 10) {
            throw new IllegalArgumentException(
                    "Оценка должна быть от 1 до 10"
            );
        }

        if (date.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException(
                    "Нельзя изменить будущий день"
            );
        }

        List<MoodEntry> entries = load();

        LocalDateTime preservedTimestamp = null;

        for (MoodEntry entry : entries) {
            if (
                    entry.getTimestamp()
                            .toLocalDate()
                            .equals(date)
            ) {
                preservedTimestamp =
                        entry.getTimestamp();

                break;
            }
        }

        entries.removeIf(
                entry ->
                        entry.getTimestamp()
                                .toLocalDate()
                                .equals(date)
        );

        LocalDateTime timestamp;

        if (preservedTimestamp != null) {
            timestamp =
                    preservedTimestamp;
        } else if (date.equals(LocalDate.now())) {
            timestamp =
                    LocalDateTime.now();
        } else {
            timestamp =
                    LocalDateTime.of(
                            date,
                            LocalTime.NOON
                    );
        }

        entries.add(
                new MoodEntry(
                        timestamp,
                        score,
                        note
                )
        );

        entries.sort(
                Comparator.comparing(
                        MoodEntry::getTimestamp
                )
        );

        write(entries);
    }

    private void write(
            List<MoodEntry> entries
    ) throws IOException {

        Files.createDirectories(
                storageFile.getParent()
        );

        List<String> lines =
                new ArrayList<>();

        for (MoodEntry entry : entries) {
            String encodedNote =
                    Base64.getEncoder()
                            .encodeToString(
                                    entry.getNote()
                                            .getBytes(
                                                    StandardCharsets.UTF_8
                                            )
                            );

            lines.add(
                    entry.getTimestamp()
                    + "\t"
                    + entry.getScore()
                    + "\t"
                    + encodedNote
            );
        }

        Files.write(
                storageFile,
                lines,
                StandardCharsets.UTF_8
        );
    }
}
