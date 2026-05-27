package tools;

public interface Tool {
    String getName();
    String getDescription();
    String execute(String args) throws Exception;
}
