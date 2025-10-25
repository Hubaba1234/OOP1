import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

public class TextFileComparator {
    private ComparatorConfig config;

    public TextFileComparator() {
        this.config = new ComparatorConfig();
    }

    public void run() {
        Scanner scanner = new Scanner(System.in);

        System.out.println("========================================");
        System.out.println("        КОМПАРАТОР ТЕКСТОВЫХ ФАЙЛОВ     ");
        System.out.println("           Объектная версия            ");
        System.out.println("========================================");
        System.out.println();

        try {
            System.out.print("Введите путь к первому файлу: ");
            String file1Path = scanner.nextLine().trim();

            System.out.print("Введите путь ко второму файлу: ");
            String file2Path = scanner.nextLine().trim();

            System.out.println();

            showCurrentConfig();
            System.out.print("Изменить настройки сравнения? (y/n): ");
            boolean changeConfig = scanner.nextLine().trim().equalsIgnoreCase("y");

            if (changeConfig) {
                configureSettings(scanner);
            }

            System.out.println();
            System.out.println("Сравниваю файлы...");
            System.out.println();

            if (!Files.exists(Paths.get(file1Path))) {
                System.out.println("Ошибка: файл '" + file1Path + "' не существует");
                return;
            }
            if (!Files.exists(Paths.get(file2Path))) {
                System.out.println("Ошибка: файл '" + file2Path + "' не существует");
                return;
            }

            ComparisonResult result = compareFiles(file1Path, file2Path);

            printComparisonResult(result);

        } catch (IOException e) {
            System.out.println("Ошибка при чтении файлов: " + e.getMessage());
        } finally {
            scanner.close();
        }

        System.out.println();
        System.out.println("Работа программы завершена");
    }


    private void showCurrentConfig() {
        System.out.println("----------------------------------------");
        System.out.println("           ТЕКУЩИЕ НАСТРОЙКИ           ");
        System.out.println("----------------------------------------");
        System.out.printf("  • Игнорировать пробелы: %s%n", (config.isIgnoreWhitespace() ? "ДА" : "НЕТ"));
        System.out.printf("  • Игнорировать регистр: %s%n", (config.isIgnoreCase() ? "ДА" : "НЕТ"));
        System.out.printf("  • Показывать контекст:  %s%n", (config.isShowContext() ? "ДА" : "НЕТ"));
        if (config.isShowContext()) {
            System.out.printf("  • Строк контекста:    %d%n", config.getContextLines());
        }
        System.out.println("----------------------------------------");
        System.out.println();
    }


    private void configureSettings(Scanner scanner) {
        System.out.println();
        System.out.println("----------------------------------------");
        System.out.println("           НАСТРОЙКА ПАРАМЕТРОВ         ");
        System.out.println("----------------------------------------");
        System.out.println();

        System.out.print("Игнорировать пробелы? (y/n, по умолчанию y): ");
        String whitespaceInput = scanner.nextLine().trim();
        if (!whitespaceInput.isEmpty()) {
            config.setIgnoreWhitespace(whitespaceInput.equalsIgnoreCase("y"));
        }

        System.out.print("Игнорировать регистр? (y/n, по умолчанию y): ");
        String caseInput = scanner.nextLine().trim();
        if (!caseInput.isEmpty()) {
            config.setIgnoreCase(caseInput.equalsIgnoreCase("y"));
        }

        System.out.print("Показывать контекст? (y/n, по умолчанию y): ");
        String contextInput = scanner.nextLine().trim();
        if (!contextInput.isEmpty()) {
            config.setShowContext(contextInput.equalsIgnoreCase("y"));
        }

        if (config.isShowContext()) {
            System.out.print("Сколько строк контекста? (по умолчанию " + config.getContextLines() + "): ");
            String contextLinesInput = scanner.nextLine().trim();
            if (!contextLinesInput.isEmpty()) {
                try {
                    config.setContextLines(Integer.parseInt(contextLinesInput));
                } catch (NumberFormatException e) {
                    System.out.println("Неверный формат, использую: " + config.getContextLines());
                }
            }
        }
    }


    public ComparisonResult compareFiles(String file1Path, String file2Path) throws IOException {
        List<String> lines1 = readFile(file1Path);
        List<String> lines2 = readFile(file2Path);

        List<Difference> differences = new ArrayList<>();
        int maxLines = Math.max(lines1.size(), lines2.size());

        for (int i = 0; i < maxLines; i++) {
            String line1 = i < lines1.size() ? lines1.get(i) : null;
            String line2 = i < lines2.size() ? lines2.get(i) : null;

            if (!areLinesEqual(line1, line2)) {
                differences.add(new Difference(i + 1, line1, line2));
            }
        }

        return new ComparisonResult(file1Path, file2Path, differences, lines1.size(), lines2.size());
    }


    private List<String> readFile(String filePath) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(filePath));
        List<String> processedLines = new ArrayList<>();

        for (String line : lines) {
            String processedLine = line;

            if (config.isIgnoreWhitespace()) {
                processedLine = processedLine.trim().replaceAll("\\s+", " ");
            }

            if (config.isIgnoreCase()) {
                processedLine = processedLine.toLowerCase();
            }

            processedLines.add(processedLine);
        }

        return processedLines;
    }


    private boolean areLinesEqual(String line1, String line2) {
        if (line1 == null && line2 == null) return true;
        if (line1 == null || line2 == null) return false;
        return line1.equals(line2);
    }


    private void printComparisonResult(ComparisonResult result) {
        System.out.println("========================================");
        System.out.println("           РЕЗУЛЬТАТЫ СРАВНЕНИЯ        ");
        System.out.println("========================================");
        System.out.println();

        System.out.println("Файл 1: " + result.getFile1Path() + " (" + result.getFile1LineCount() + " строк)");
        System.out.println("Файл 2: " + result.getFile2Path() + " (" + result.getFile2LineCount() + " строк)");
        System.out.println();

        if (result.getDifferences().isEmpty()) {
            System.out.println("Файлы идентичны!");
            return;
        }

        System.out.println("Найдено различий: " + result.getDifferences().size());

        int totalLines = Math.max(result.getFile1LineCount(), result.getFile2LineCount());
        int matchingLines = totalLines - result.getDifferences().size();
        double matchPercentage = totalLines > 0 ? (matchingLines * 100.0) / totalLines : 100.0;

        System.out.printf("Совпадение: %.1f%%%n", matchPercentage);
        System.out.println();

        List<String> originalLines1 = readOriginalFile(result.getFile1Path());
        List<String> originalLines2 = readOriginalFile(result.getFile2Path());

        if (config.isShowContext()) {
            printAllDifferencesWithContext(result, originalLines1, originalLines2);
        } else {
            printDifferencesList(result, originalLines1, originalLines2);
        }
    }


    private void printAllDifferencesWithContext(ComparisonResult result,
                                                List<String> lines1, List<String> lines2) {
        System.out.println("----------------------------------------");
        System.out.println("         СРАВНЕНИЕ С КОНТЕКСТОМ         ");
        System.out.println("----------------------------------------");
        System.out.println();

        Set<Integer> differentLines = result.getDifferences().stream()
                .map(Difference::getLineNumber)
                .collect(Collectors.toSet());

        int maxLines = Math.max(lines1.size(), lines2.size());

        for (int i = 1; i <= maxLines; i++) {
            boolean isDifferent = differentLines.contains(i);
            String marker = isDifferent ? ">>> " : "    ";
            String line1 = i <= lines1.size() ? lines1.get(i - 1) : "<КОНЕЦ ФАЙЛА>";
            String line2 = i <= lines2.size() ? lines2.get(i - 1) : "<КОНЕЦ ФАЙЛА>";

            System.out.printf("%sСтрока %d:%n", marker, i);
            System.out.printf("%s  Файл 1: %s%n", marker, line1);
            System.out.printf("%s  Файл 2: %s%n", marker, line2);

            if (isDifferent) {
                System.out.printf("%s  [РАЗЛИЧИЕ]%n", marker);
            }

            if (i != maxLines) {
                System.out.println();
            }
        }
    }


    private void printDifferencesList(ComparisonResult result,
                                      List<String> lines1, List<String> lines2) {
        System.out.println("----------------------------------------");
        System.out.println("             СПИСОК РАЗЛИЧИЙ            ");
        System.out.println("----------------------------------------");
        System.out.println();

        for (int i = 0; i < result.getDifferences().size(); i++) {
            Difference diff = result.getDifferences().get(i);
            int lineIndex = diff.getLineNumber() - 1;

            String line1 = lineIndex < lines1.size() ? lines1.get(lineIndex) : "<КОНЕЦ ФАЙЛА>";
            String line2 = lineIndex < lines2.size() ? lines2.get(lineIndex) : "<КОНЕЦ ФАЙЛА>";

            System.out.printf("Различие #%d (строка %d):%n", i + 1, diff.getLineNumber());
            System.out.println("   Файл 1: " + line1);
            System.out.println("   Файл 2: " + line2);

            if (i != result.getDifferences().size() - 1) {
                System.out.println();
                System.out.println("   ----------------------------------------");
                System.out.println();
            }
        }
    }


    private List<String> readOriginalFile(String filePath) {
        try {
            return Files.readAllLines(Paths.get(filePath));
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

}