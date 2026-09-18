import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        askMoodAndMotivate();
    }

    public static void askMoodAndMotivate() {
        Scanner scanner = new Scanner(System.in);
        int score;

        while (true) {
            System.out.print("Как настроение по 10-тибалльной шкале? ");

            if (!scanner.hasNextInt()) {
                System.out.println("Пожалуйста, введите целое число от 1 до 10.");
                scanner.next();
                continue;
            }

            score = scanner.nextInt();

            if (score >= 1 && score <= 10) {
                break;
            }

            System.out.println("Оценка должна быть от 1 до 10.");
        }

        switch (score) {
            case 1:
                System.out.println("Даже один балл — это начало. Сделай сегодня один маленький шаг вперёд.");
                break;
            case 2:
                System.out.println("Ты уже держишься. Маленький шаг сегодня создаст движение завтра.");
                break;
            case 3:
                System.out.println("Начни с самого простого действия — и постепенно станет легче.");
                break;
            case 4:
                System.out.println("У тебя уже есть силы расти. Продолжай двигаться в своём темпе.");
                break;
            case 5:
                System.out.println("Хорошая середина: сохрани спокойствие и сделай ещё один уверенный шаг.");
                break;
            case 6:
                System.out.println("Уже есть заметный подъём. Используй его для небольшого важного дела.");
                break;
            case 7:
                System.out.println("Отличный настрой! Пусть он поможет тебе приблизиться к цели.");
                break;
            case 8:
                System.out.println("Ты на хорошем уровне. Действуй смело — у тебя всё получится.");
                break;
            case 9:
                System.out.println("Почти максимум! Сохрани эту энергию и поделись ею с другими.");
                break;
            case 10:
                System.out.println("Прекрасное настроение! Пусть сегодня всё получается легко и с удовольствием.");
                break;
            default:
                System.out.println("Ничего страшного, если настроение отличается от стандартного, может так даже лучше.");
                break;
        }

        scanner.close();
    }
}
