import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.Scanner;

public class TextFileComparator {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("----------------------------------------");
        System.out.println("       КОМПАРАТОР ТЕКСТОВЫХ ФАЙЛОВ      ");
        System.out.println("----------------------------------------");
        System.out.println();

        try {
            // Запрос путей к файлам
            System.out.print("Введите путь к первому файлу: ");
            String file1Path = scanner.nextLine().trim();

            System.out.print("Введите путь ко второму файлу: ");
            String file2Path = scanner.nextLine().trim();

            System.out.println();
            System.out.println("Настройки сравнения:");

            System.out.print("Игнорировать пробелы? (y/n): ");
            boolean ignoreWhitespace = scanner.nextLine().trim().equalsIgnoreCase("y");

            System.out.print("Игнорировать регистр? (y/n): ");
            boolean ignoreCase = scanner.nextLine().trim().equalsIgnoreCase("y");

            System.out.print("Показывать контекст? (y/n): ");
            boolean showContext = scanner.nextLine().trim().equalsIgnoreCase("y");

            int contextLines = 3;
            if (showContext) {
                System.out.print("Сколько строк контекста показывать? (по умолчанию 3): ");
                String contextInput = scanner.nextLine().trim();
                if (!contextInput.isEmpty()) {
                    try {
                        contextLines = Integer.parseInt(contextInput);
                    } catch (NumberFormatException e) {
                        System.out.println("Неверный формат, использую значение по умолчанию: 3");
                    }
                }
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

            ComparisonResult result = compareFiles(file1Path, file2Path, ignoreWhitespace, ignoreCase);

            printComparisonResult(result, showContext, contextLines);

        } catch (IOException e) {
            System.out.println("Ошибка при чтении файлов: " + e.getMessage());
        } finally {
            scanner.close();
        }

        System.out.println();
        System.out.println("Работа программы завершена");
    }

    public static ComparisonResult compareFiles(String file1Path, String file2Path,
                                                boolean ignoreWhitespace, boolean ignoreCase) throws IOException {
        List<String> lines1 = readFile(file1Path, ignoreWhitespace, ignoreCase);
        List<String> lines2 = readFile(file2Path, ignoreWhitespace, ignoreCase);

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

    private static List<String> readFile(String filePath, boolean ignoreWhitespace, boolean ignoreCase) throws IOException {

        List<String> lines = Files.readAllLines(Paths.get(filePath));

        List<String> processedLines = new ArrayList<>();

        for (String line : lines) {
            String processedLine = line;

            if (ignoreWhitespace) {
                processedLine = processedLine.trim().replaceAll("\\s+", " ");
            }

            if (ignoreCase) {
                processedLine = processedLine.toLowerCase();
            }

            processedLines.add(processedLine);
        }

        return processedLines;
    }

    private static boolean areLinesEqual(String line1, String line2) {
        if (line1 == null && line2 == null) return true;

        if (line1 == null || line2 == null) return false;

        return line1.equals(line2);
    }

    private static void printComparisonResult(ComparisonResult result, boolean showContext, int contextLines) {
        System.out.println("РЕЗУЛЬТАТЫ СРАВНЕНИЯ:");
        System.out.println("----------------------------------------");

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

        for (Difference diff : result.getDifferences()) {
            printDifference(diff, originalLines1, originalLines2, showContext, contextLines);
        }
    }

    private static void printDifference(Difference diff, List<String> originalLines1,
                                        List<String> originalLines2, boolean showContext, int contextLines) {
        System.out.println("Различие в строке " + diff.getLineNumber() + ":");

        if (showContext) {
            printContext(diff.getLineNumber(), originalLines1, originalLines2, contextLines);
        } else {
            String line1 = diff.getLine1() != null ? diff.getLine1() : "//КОНЕЦ ФАЙЛА>";
            String line2 = diff.getLine2() != null ? diff.getLine2() : "//КОНЕЦ ФАЙЛА>";

            System.out.println("  Файл 1: " + line1);
            System.out.println("  Файл 2: " + line2);
        }

        System.out.println("----------------------------------------");
    }

    private static void printContext(int lineNumber, List<String> lines1, List<String> lines2, int contextLines) {
        int start = Math.max(1, lineNumber - contextLines);
        int end = Math.min(Math.max(lines1.size(), lines2.size()),
                lineNumber + contextLines);

        for (int i = start; i <= end; i++) {
            String marker = (i == lineNumber) ? ">>> " : "    ";

            String line1 = i <= lines1.size() ? lines1.get(i - 1) : "//КОНЕЦ ФАЙЛА";
            String line2 = i <= lines2.size() ? lines2.get(i - 1) : "//КОНЕЦ ФАЙЛА";

            if (i == lineNumber) {
                System.out.printf("%s|РАЗЛИЧИЕ| Строка %d:%n", marker, i);
                System.out.printf("%s  Файл 1: %s%n", marker, line1);
                System.out.printf("%s  Файл 2: %s%n", marker, line2);
            } else {
                System.out.printf("%sСтрока %d:%n", marker, i);
                System.out.printf("%s  Файл 1: %s%n", marker, line1);
                System.out.printf("%s  Файл 2: %s%n", marker, line2);
            }

            if (i != end) System.out.println();
        }
    }

    private static List<String> readOriginalFile(String filePath) {
        try {
            return Files.readAllLines(Paths.get(filePath));
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }
}