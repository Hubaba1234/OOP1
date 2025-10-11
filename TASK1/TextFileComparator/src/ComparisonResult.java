import java.util.List;

public class ComparisonResult {
    private final String file1Path;
    private final String file2Path;
    private final List<Difference> differences;
    private final int file1LineCount;
    private final int file2LineCount;

    public ComparisonResult(String file1Path, String file2Path,
                            List<Difference> differences, int file1LineCount, int file2LineCount) {
        this.file1Path = file1Path;
        this.file2Path = file2Path;
        this.differences = differences;
        this.file1LineCount = file1LineCount;
        this.file2LineCount = file2LineCount;
    }

    public String getFile1Path() { return file1Path; }
    public String getFile2Path() { return file2Path; }
    public List<Difference> getDifferences() { return differences; }
    public int getFile1LineCount() { return file1LineCount; }
    public int getFile2LineCount() { return file2LineCount; }
}