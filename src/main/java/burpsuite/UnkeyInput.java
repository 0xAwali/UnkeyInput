package burpsuite;

import burp.api.montoya.BurpExtension;
import burp.api.montoya.MontoyaApi;
import burp.api.montoya.http.message.HttpRequestResponse;
import burp.api.montoya.persistence.PersistedObject;
import burp.api.montoya.proxy.http.InterceptedResponse;
import burp.api.montoya.ui.UserInterface;
import burp.api.montoya.ui.editor.HttpRequestEditor;
import burp.api.montoya.ui.editor.HttpResponseEditor;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.Locale;
import static burp.api.montoya.ui.editor.EditorOptions.READ_ONLY;

public class UnkeyInput implements BurpExtension {

    private MontoyaApi api;
    DateTimeFormatter dateTimeFormatter;

    static final String YOUR_PAYLOAD = "YOUR_PAYLOAD";
    static final String YOUR_MATCH = "YOUR_MATCH";
    static final String YOUR_THREAD = "YOUR_THREAD";
    static final String YOUR_HEADERS_PER_REQUEST = "YOUR_HEADERS_PER_REQUEST";
    static final String YOUR_PARAMETERS_QUERY_PER_REQUEST = "YOUR_PARAMETERS_QUERY_PER_REQUEST";
    static final String YOUR_PARAMETERS_BODY_PER_REQUEST = "YOUR_PARAMETERS_BODY_PER_REQUEST";
    static final String YOUR_PARAMETERS_COOKIE_PER_REQUEST = "YOUR_PARAMETERS_COOKIE_PER_REQUEST";



    @Override
    public void initialize(MontoyaApi montoyaApi) {
        this.api = montoyaApi;
        this.dateTimeFormatter = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH);

        PersistedObject persist =  api.persistence().extensionData();

        String PAYLOAD = persist.getString(YOUR_PAYLOAD);
        if (PAYLOAD == null) {
            PAYLOAD = "<xss>%3Cxss%3E%253Cxss%253E\\u003Cxss\\u003E";
        }
        persist.setString(YOUR_PAYLOAD, PAYLOAD);

        String MATCH = persist.getString(YOUR_MATCH);
        if (MATCH == null) {
            MATCH = "<xss>";
        }
        persist.setString(YOUR_MATCH, MATCH);

        Integer THREAD = persist.getInteger(YOUR_THREAD);
        if (THREAD == null) {
            THREAD = 50;
        }
        persist.setInteger(YOUR_THREAD, THREAD);

        Integer HEADERS = persist.getInteger(YOUR_HEADERS_PER_REQUEST);
        if (HEADERS == null) {
            HEADERS = 100;
        }
        persist.setInteger(YOUR_HEADERS_PER_REQUEST, HEADERS);

        Integer QUERY = persist.getInteger(YOUR_PARAMETERS_QUERY_PER_REQUEST);
        if (QUERY == null) {
            QUERY = 30;
        }
        persist.setInteger(YOUR_PARAMETERS_QUERY_PER_REQUEST, QUERY);

        Integer BODY = persist.getInteger(YOUR_PARAMETERS_BODY_PER_REQUEST);
        if (BODY == null) {
            BODY = 60;
        }
        persist.setInteger(YOUR_PARAMETERS_BODY_PER_REQUEST, BODY);


        Integer COOKIE = persist.getInteger(YOUR_PARAMETERS_COOKIE_PER_REQUEST);
        if (COOKIE == null) {
            COOKIE = 45;
        }
        persist.setInteger(YOUR_PARAMETERS_COOKIE_PER_REQUEST, COOKIE);


        api.extension().setName("Unkey Input");
        MyTableModel table = new MyTableModel(api);
        api.userInterface().registerSuiteTab("Unkey Input", LoggerTab(table,persist));
        api.proxy().registerRequestHandler(new proxyHandler(table,persist));
        api.proxy().registerResponseHandler(new proxyHandler(table,persist));
        api.userInterface().registerContextMenuItemsProvider(new MenuItemsProvider(api,table,persist));
    }

    private Component LoggerTab(MyTableModel table, PersistedObject persist) {

        JTabbedPane responseCacheableGUI = new JTabbedPane();

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        UserInterface userInterface = api.userInterface();
        HttpRequestEditor requestViewer = userInterface.createHttpRequestEditor(READ_ONLY);
        HttpResponseEditor responseViewer = userInterface.createHttpResponseEditor(READ_ONLY);

        JTabbedPane requestTab = new JTabbedPane();
        requestTab.addTab("Request", requestViewer.uiComponent());

        JTabbedPane responseTab = new JTabbedPane();
        responseTab.addTab("Response", responseViewer.uiComponent());

        JSplitPane splitTabs = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);

        splitTabs.setLeftComponent(requestTab);
        splitTabs.setRightComponent(responseTab);

        splitPane.setRightComponent(splitTabs);

        JTable jTable = new JTable(table) {
            @Override
            public void changeSelection(int rowIndex, int columnIndex, boolean toggle, boolean extend) {
                super.changeSelection(rowIndex, columnIndex, toggle, extend);
                Object message = table.get(convertRowIndexToModel(rowIndex));
                if (message instanceof InterceptedResponse) {
                    InterceptedResponse interceptedResponseMessage = (InterceptedResponse) message;
                    requestViewer.setRequest(interceptedResponseMessage.request());
                    responseViewer.setResponse(interceptedResponseMessage);

                } else if (message instanceof HttpRequestResponse) {
                    HttpRequestResponse httpRequestResponseMessage = (HttpRequestResponse) message;
                    requestViewer.setRequest(httpRequestResponseMessage.request());
                    responseViewer.setResponse(httpRequestResponseMessage.response());
                }
            }
        };

        jTable.setRowHeight(30);
        jTable.setAutoCreateRowSorter(true);
        TableRowSorter<TableModel> sorter = new TableRowSorter<>(table);
        jTable.setRowSorter(sorter);
        sorter.setComparator(0, new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return o1.compareTo(o2); // Compare as integers
            }
        });

        jTable.getRowSorter().toggleSortOrder(0);
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        jTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        jTable.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);


        TableColumn ID = jTable.getColumnModel().getColumn(0);
        ID.setMinWidth(50);
        ID.setMaxWidth(100);

        TableColumn Host = jTable.getColumnModel().getColumn(1);
        Host.setMinWidth(350);
        Host.setMaxWidth(700);


        TableColumn CACHE = jTable.getColumnModel().getColumn(2);
        CACHE.setMinWidth(100);
        CACHE.setMaxWidth(150);

        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem sendToRepeaterMenuItem = new JMenuItem("Send to Repeater");
        popupMenu.add(sendToRepeaterMenuItem);

        Action sendToRepeaterAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int[] selectedRows = jTable.getSelectedRows();
                for (int selectedRow : selectedRows) {
                    Object message= table.get(jTable.convertRowIndexToModel(selectedRow));
                    if (message instanceof InterceptedResponse) {
                        InterceptedResponse interceptedResponseMessage = (InterceptedResponse) message;
                        api.repeater().sendToRepeater(interceptedResponseMessage.request(),dateTimeFormatter.format(LocalDateTime.now()));

                    } else if (message instanceof HttpRequestResponse) {
                        HttpRequestResponse httpRequestResponseMessage = (HttpRequestResponse) message;
                        api.repeater().sendToRepeater(httpRequestResponseMessage.request(),dateTimeFormatter.format(LocalDateTime.now()));

                    }
                }
            }
        };

        sendToRepeaterMenuItem.addActionListener(sendToRepeaterAction);
        jTable.setComponentPopupMenu(popupMenu);

        KeyStroke keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_DOWN_MASK);
        jTable.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(keyStroke, "sendToRepeater");
        jTable.getActionMap().put("sendToRepeater", sendToRepeaterAction);

        JScrollPane scrollPane = new JScrollPane(jTable);
        splitPane.setLeftComponent(scrollPane);

        responseCacheableGUI.addTab("Logger",splitPane);
        responseCacheableGUI.addTab("Settings",new Settings(persist));

        return responseCacheableGUI;
    }

}
