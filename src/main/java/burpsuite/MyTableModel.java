package burpsuite;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.http.message.HttpRequestResponse;
import burp.api.montoya.proxy.http.InterceptedResponse;
import javax.swing.table.AbstractTableModel;
import java.util.*;


public class MyTableModel extends AbstractTableModel {

    private final MontoyaApi api;
    private final List<LogEntry> log;

    private static class LogEntry {
        private final Object message;
        private final Boolean cache;


        public LogEntry(Object message, Boolean cache) {
            this.message = message;
            this.cache = cache;

        }
        public Object getMessage() {
            return message;
        }

        public boolean isHttpRequestResponse() {
            return message instanceof HttpRequestResponse;
        }

        public boolean isInterceptedResponse() {
            return message instanceof InterceptedResponse;
        }


        public Boolean getCache() {
            return cache;
        }
    }

    public MyTableModel(MontoyaApi api) {
        this.api = api;
        this.log = new ArrayList<>(); // Initialize the list
    }

    @Override
    public synchronized int getRowCount() {
        return log.size(); // Return the size of the list
    }

    @Override
    public int getColumnCount() {
        return 4;
    }

    @Override
    public String getColumnName(int column) {
        return switch (column) {
            case 0 -> "#";
            case 1 -> "Host";
            case 2 -> "Cache";
            case 3 -> "Path";
            default -> "";
        };
    }

    @Override
    public synchronized Object getValueAt(int rowIndex, int columnIndex) {
        LogEntry entry = log.get(rowIndex);

        switch (columnIndex) {
            case 0:
                return rowIndex + 1;
            case 1:
                if (entry.isHttpRequestResponse()) {
                    HttpRequestResponse httpRequestResponse = (HttpRequestResponse) (entry.getMessage());
                    return httpRequestResponse.request().headerValue("Host");
                } else if (entry.isInterceptedResponse()) {
                    InterceptedResponse interceptedResponse = (InterceptedResponse) (entry.getMessage());
                    return interceptedResponse.request().headerValue("Host");
                } else {
                    return "";
                }

            case 2:
                if (entry.getCache()) {
                    return "Yes";
                } else {
                    return "No";
                }
            case 3:
                if (entry.isHttpRequestResponse()) {
                    HttpRequestResponse httpRequestResponse = (HttpRequestResponse) (entry.getMessage());
                    return httpRequestResponse.request().pathWithoutQuery();
                } else if (entry.isInterceptedResponse()) {
                    InterceptedResponse interceptedResponse = (InterceptedResponse) (entry.getMessage());
                    return interceptedResponse.request().pathWithoutQuery();
                } else {
                    return "";
                }
            default:
                return "";
        }
    }

    public synchronized void add(Object message,Boolean cache) {
        LogEntry entry = new LogEntry(message,cache);
        log.add(entry);
        int index = log.size() - 1;
        fireTableRowsInserted(index, index);
    }

    public synchronized Object get(int rowIndex) {
        LogEntry entry = log.get(rowIndex);
        return entry.getMessage();
    }
}