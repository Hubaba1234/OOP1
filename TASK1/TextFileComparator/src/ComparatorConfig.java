
public class ComparatorConfig {
    private boolean ignoreWhitespace = true;
    private boolean ignoreCase = true;
    private boolean showContext = true;
    private int contextLines = 3;


    public boolean isIgnoreWhitespace() {
        return ignoreWhitespace;
    }

    public void setIgnoreWhitespace(boolean ignoreWhitespace) {
        this.ignoreWhitespace = ignoreWhitespace;
    }

    public boolean isIgnoreCase() {
        return ignoreCase;
    }

    public void setIgnoreCase(boolean ignoreCase) {
        this.ignoreCase = ignoreCase;
    }

    public boolean isShowContext() {
        return showContext;
    }

    public void setShowContext(boolean showContext) {
        this.showContext = showContext;
    }

    public int getContextLines() {
        return contextLines;
    }

    public void setContextLines(int contextLines) {
        this.contextLines = contextLines;
    }
}