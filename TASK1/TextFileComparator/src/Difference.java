
public class Difference {
    private final int lineNumber;
    private final String line1;
    private final String line2;

    public Difference(int lineNumber, String line1, String line2) {
        this.lineNumber = lineNumber;
        this.line1 = line1;
        this.line2 = line2;
    }


    public int getLineNumber() {
        return lineNumber;
    }
}