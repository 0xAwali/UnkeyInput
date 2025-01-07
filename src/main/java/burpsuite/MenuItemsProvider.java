package burpsuite;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.http.message.HttpRequestResponse;
import burp.api.montoya.http.message.StatusCodeClass;
import burp.api.montoya.http.message.params.HttpParameterType;
import burp.api.montoya.http.message.requests.HttpRequest;
import burp.api.montoya.persistence.PersistedObject;
import burp.api.montoya.ui.contextmenu.ContextMenuEvent;
import burp.api.montoya.ui.contextmenu.ContextMenuItemsProvider;
import burp.api.montoya.ui.contextmenu.InvocationType;
import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;

import static burp.api.montoya.http.message.params.HttpParameter.*;
import static burpsuite.UnkeyInput.*;

public class MenuItemsProvider implements ContextMenuItemsProvider {

    private final MontoyaApi api;
    private final MyTableModel table;

    private static final String[] HEADERS_TO_REMOVE = {
            "Akamai-Client-Ip", "Cf-Connecting-Ip", "Client-Ip", "Fastly-Client-Ip", "Forwarded", "Referer", "True-Client-Ip",
            "X-Client-Ip", "X-Cluster-Client-Ip", "X-Forwarded-By", "X-Forwarded-Client-Ip", "X-Forwarded-For", "X-Forwarded-For-Original",
            "X-Forwarded-Host", "X-Forwarded-Path", "X-Forwarded-Server", "X-Forwarded-Uri", "X-Host", "X-Http-Forwarded-For",
            "X-Original-Cookie", "X-Original-Forwarded-For", "X-Original-Host", "X-Original-Referer", "X-Original-Url", "X-Original-User-Agent",
            "X-Originating-Host", "X-Originating-Ip", "X-Proxyuser-Host", "X-Proxyuser-Ip", "X-Proxyuser-Uri", "X-Real-Host", "X-Real-Ip",
            "X-Remote-Addr", "X-Remote-Ip", "X-Rewrite-Url", "X-True-Ip", "X-Wap-Client-Ip", "X-Wap-Network-Client-Ip", "X-Wap-Profile"
    };

    private static final String[] CACHE_HEADERS = {
            "Age", "CDN-Cache", "CF-Cache-Status", "Server-Timing", "X-Cache", "X-Cache-Info", "X-Cache-Remote", "X-Check-Cacheable",
            "X-Drupal-Cache", "X-Drupal-Dynamic-Cache", "X-Proxy-Cache", "X-Rack-Cache", "Akamai-Cache-Status"
    };

    PersistedObject persistence;
    private List<String> additionalHeaders;
    private List<String> parameters;

    public MenuItemsProvider(MontoyaApi api, MyTableModel table, PersistedObject persist) {
        this.api = api;
        this.table = table;
        this.additionalHeaders = loadAdditionalHeaders();
        this.parameters = loadParameters();
        this.persistence = persist;


    }

    // Load additional headers from the resources folder
    private List<String> loadAdditionalHeaders() {
        List<String> headers = new ArrayList<>();
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("allHeaders.txt");
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                headers.add(line.trim());
            }
        } catch (Exception e) {
            api.logging().logToError("Failed to load additional headers: " + e.getMessage());
        }
        return headers;
    }

    // Load parameters from the resources folder
    private List<String> loadParameters() {
        List<String> parameters = new ArrayList<>();
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("Parameters.txt");
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                parameters.add(line.trim());
            }
        } catch (Exception e) {
            api.logging().logToError("Failed to load parameters: " + e.getMessage());
        }
        return parameters;
    }

    @Override
    public List<Component> provideMenuItems(ContextMenuEvent event) {
        if (event.isFrom(InvocationType.PROXY_HISTORY)) {
            List<Component> menuItemList = new ArrayList<>();

            // Existing menu items
            JMenuItem fuzzHeaders = new JMenuItem("TitleCase Headers");
            fuzzHeaders.addActionListener(l -> titleCaseHeaders(event));
            menuItemList.add(fuzzHeaders);

            JMenuItem lowerCaseHeaders = new JMenuItem("LowerCase Headers");
            lowerCaseHeaders.addActionListener(l -> upperORlower(event, true));
            menuItemList.add(lowerCaseHeaders);

            JMenuItem upperCaseHeaders = new JMenuItem("UpperCase Headers");
            upperCaseHeaders.addActionListener(l -> upperORlower(event, false));
            menuItemList.add(upperCaseHeaders);

            JMenuItem convertHyphenToUnderscore = new JMenuItem("Convert Hyphen to Underscore");
            convertHyphenToUnderscore.addActionListener(l -> convertHyphenToUnderscore(event));
            menuItemList.add(convertHyphenToUnderscore);

            JMenuItem duplicateHeader = new JMenuItem("Duplicate Header");
            duplicateHeader.addActionListener(l -> duplicateHeader(event));
            menuItemList.add(duplicateHeader);

            // Add new menu item for Fuzz Parameters with submenus
            JMenu addHexToHeader = new JMenu("Add Hex to Headers");

            JMenuItem HexHeaderValue = new JMenuItem("Hex Before Headers");
            HexHeaderValue.addActionListener(l -> HexHeaderValue(event));
            addHexToHeader.add(HexHeaderValue);

            JMenuItem HeaderHexValue = new JMenuItem("Hex After Headers");
            HeaderHexValue.addActionListener(l -> HeaderHexValue(event));
            addHexToHeader.add(HeaderHexValue);

            menuItemList.add(addHexToHeader);

            JMenuItem duplicateHeaderWithSpace = new JMenuItem("Duplicate Header With Space");
            duplicateHeaderWithSpace.addActionListener(l -> duplicateHeaderWithSpace(event));
            menuItemList.add(duplicateHeaderWithSpace);

            // Add new menu item for Fuzz Parameters with submenus
            JMenu fuzzParametersMenu = new JMenu("Fuzz Parameters");
            // Submenu: Query
            JMenuItem queryMenuItem = new JMenuItem("Query");
            queryMenuItem.addActionListener(l -> fuzzQuery(event));
            fuzzParametersMenu.add(queryMenuItem);

            // Submenu: Body
            JMenu bodyMenu = new JMenu("Body");

            // Submenu under Body: Url-Encoding
            JMenuItem urlEncodingMenuItem = new JMenuItem("urlencoded");
            urlEncodingMenuItem.addActionListener(l -> fuzzBodyUrlEncoding(event));
            bodyMenu.add(urlEncodingMenuItem);

            // Submenu under Body: json
            JMenuItem jsonMenuItem = new JMenuItem("json");
            jsonMenuItem.addActionListener(l -> fuzzBodyJson(event));
            bodyMenu.add(jsonMenuItem);

            // Submenu under Body: xml
            JMenuItem xmlMenuItem = new JMenuItem("xml");
            xmlMenuItem.addActionListener(l -> fuzzBodyXml(event));
            bodyMenu.add(xmlMenuItem);

            fuzzParametersMenu.add(bodyMenu);
            menuItemList.add(fuzzParametersMenu);

            JMenuItem fuzzCookie = new JMenuItem("Fuzz Cookie");
            fuzzCookie.addActionListener(l -> fuzzCookie(event));
            menuItemList.add(fuzzCookie);



            return menuItemList;
        }
        return Collections.emptyList();
    }

    private List<String> getAllHeaders() {
        List<String> allHeaders = new ArrayList<>();
        Collections.addAll(allHeaders, HEADERS_TO_REMOVE);
        allHeaders.addAll(additionalHeaders); // Use headers loaded from the file
        return allHeaders;
    }

    private boolean isCached(HttpRequestResponse httpRequestResponse) {
        if (httpRequestResponse.response().body().toString().contains(persistence.getString(YOUR_MATCH)) || httpRequestResponse.response().headers().toString().contains(persistence.getString(YOUR_MATCH))) {
            for (String header : CACHE_HEADERS) {
                if (httpRequestResponse.response().hasHeader(header)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isNOTCached(HttpRequestResponse httpRequestResponse) {
        if (httpRequestResponse.response().body().toString().contains(persistence.getString(YOUR_MATCH))) {
            if(httpRequestResponse.hasResponse() && (!httpRequestResponse.response().isStatusCodeClass(StatusCodeClass.CLASS_3XX_REDIRECTION))){
                return true;
            }
        }
        return false;
    }


    private <T> List<List<T>> splitIntoChunks(T[] array, int chunkSize) {
        List<List<T>> chunks = new ArrayList<>();
        for (int i = 0; i < array.length; i += chunkSize) {
            List<T> chunk = new ArrayList<>();
            for (int j = i; j < Math.min(i + chunkSize, array.length); j++) {
                chunk.add(array[j]);
            }
            chunks.add(chunk);
        }
        return chunks;
    }

    private void titleCaseHeaders(ContextMenuEvent event) {
        List<HttpRequestResponse> requestResponses = event.selectedRequestResponses();
        if (requestResponses.isEmpty()) {
            return;
        }

        ExecutorService executorService = Executors.newFixedThreadPool(persistence.getInteger(YOUR_THREAD));

        List<String> allHeaders = getAllHeaders();
        List<List<String>> headerChunks = splitIntoChunks(allHeaders.toArray(new String[0]), persistence.getInteger(YOUR_HEADERS_PER_REQUEST));

        for (HttpRequestResponse requestResponse : requestResponses) {
            executorService.submit(() -> {
                try {
                    HttpRequest baseRequest = requestResponse.request();

                    // Remove headers from HEADERS_TO_REMOVE
                    for (String header : HEADERS_TO_REMOVE) {
                        baseRequest = baseRequest.withRemovedHeader(header);
                    }
                    // Process each chunk
                    for (List<String> chunk : headerChunks) {
                        long randomNumber = ThreadLocalRandom.current().nextLong(100_000_000_000L, 1_000_000_000_000L);
                        HttpRequest chunkRequest = baseRequest.withUpdatedParameters(urlParameter("_parameter", String.valueOf(randomNumber)))
                                .withUpdatedParameters(cookieParameter("_Cookie",String.valueOf(randomNumber)))
                                .withHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/" + randomNumber + " Safari/" + randomNumber);

                        for (String header : chunk) {
                            String headerValue = header + "-" + persistence.getString(YOUR_PAYLOAD);
                            chunkRequest = chunkRequest.withHeader(header, headerValue);
                        }

                        HttpRequestResponse modifiedRequestResponse = api.http().sendRequest(chunkRequest);

                        if (isCached(modifiedRequestResponse)) {
                            table.add(modifiedRequestResponse,true);
                        } else if (isNOTCached(modifiedRequestResponse)) {
                            table.add(modifiedRequestResponse,false);
                        }
                    }
                } catch (Exception e) {
                    api.logging().logToError("Failed to send request: " + e.getMessage());
                }
            });
        }
        executorService.shutdown();
    }

    private void upperORlower(ContextMenuEvent event, boolean toLowerCase) {
        List<HttpRequestResponse> requestResponses = event.selectedRequestResponses();
        if (requestResponses.isEmpty()) {
            return;
        }

        ExecutorService executorService = Executors.newFixedThreadPool(persistence.getInteger(YOUR_THREAD));

        List<String> allHeaders = getAllHeaders(); // Use combined headers
        List<List<String>> headerChunks = splitIntoChunks(allHeaders.toArray(new String[0]), persistence.getInteger(YOUR_HEADERS_PER_REQUEST));

        for (HttpRequestResponse requestResponse : requestResponses) {
            executorService.submit(() -> {
                try {
                    HttpRequest baseRequest = requestResponse.request();

                    // Remove headers from HEADERS_TO_REMOVE
                    for (String header : HEADERS_TO_REMOVE) {
                        baseRequest = baseRequest.withRemovedHeader(header);
                    }
                    // Process each chunk
                    for (List<String> chunk : headerChunks) {
                        long randomNumber = ThreadLocalRandom.current().nextLong(100_000_000_000L, 1_000_000_000_000L);
                        HttpRequest chunkRequest = baseRequest.withUpdatedParameters(urlParameter("_parameter", String.valueOf(randomNumber)))
                                .withUpdatedParameters(cookieParameter("_Cookie",String.valueOf(randomNumber)))
                                .withHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/" + randomNumber + " Safari/" + randomNumber);

                        for (String header : chunk) {
                            String headerValue = header + "-" + persistence.getString(YOUR_PAYLOAD); // Set value to "header-xxxxxxxxxxxxxx"
                            chunkRequest = chunkRequest.withHeader(toLowerCase ? header.toLowerCase() : header.toUpperCase(), headerValue);
                        }

                        HttpRequestResponse modifiedRequestResponse = api.http().sendRequest(chunkRequest);

                        if (isCached(modifiedRequestResponse)) {
                            table.add(modifiedRequestResponse,true);
                        } else if (isNOTCached(modifiedRequestResponse)) {
                            table.add(modifiedRequestResponse,false);
                        }
                    }
                } catch (Exception e) {
                    api.logging().logToError("Failed to send request: " + e.getMessage());
                }
            });
        }
        executorService.shutdown();
    }


    private void convertHyphenToUnderscore(ContextMenuEvent event) {
        List<HttpRequestResponse> requestResponses = event.selectedRequestResponses();
        if (requestResponses.isEmpty()) {
            return;
        }

        ExecutorService executorService = Executors.newFixedThreadPool(persistence.getInteger(YOUR_THREAD));

        List<String> allHeaders = getAllHeaders(); // Use combined headers
        List<List<String>> headerChunks = splitIntoChunks(allHeaders.toArray(new String[0]), persistence.getInteger(YOUR_HEADERS_PER_REQUEST));

        for (HttpRequestResponse requestResponse : requestResponses) {
            executorService.submit(() -> {
                try {
                    HttpRequest baseRequest = requestResponse.request();

                    // Remove headers from HEADERS_TO_REMOVE
                    for (String header : HEADERS_TO_REMOVE) {
                        baseRequest = baseRequest.withRemovedHeader(header);
                    }
                    // Process each chunk
                    for (List<String> chunk : headerChunks) {
                        long randomNumber = ThreadLocalRandom.current().nextLong(100_000_000_000L, 1_000_000_000_000L);
                        HttpRequest chunkRequest = baseRequest.withUpdatedParameters(urlParameter("_parameter", String.valueOf(randomNumber)))
                                .withUpdatedParameters(cookieParameter("_Cookie",String.valueOf(randomNumber)))
                                .withHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/" + randomNumber + " Safari/" + randomNumber);

                        for (String header : chunk) {
                            if (header.contains("-")) { // Only process headers containing hyphens
                                String newHeader = header.replace("-", "_"); // Convert hyphens to underscores
                                String headerValue = header + "-" + persistence.getString(YOUR_PAYLOAD); // Set value to "header-xxxxxxxxxxxxxx"
                                chunkRequest = chunkRequest.withHeader(newHeader, headerValue);
                            }
                        }

                        HttpRequestResponse modifiedRequestResponse = api.http().sendRequest(chunkRequest);

                        if (isCached(modifiedRequestResponse)) {
                            table.add(modifiedRequestResponse,true);
                        } else if (isNOTCached(modifiedRequestResponse)) {
                            table.add(modifiedRequestResponse,false);
                        }
                    }
                } catch (Exception e) {
                    api.logging().logToError("Failed to send request: " + e.getMessage());
                }
            });
        }
        executorService.shutdown();
    }

    private void duplicateHeader(ContextMenuEvent event) {
        List<HttpRequestResponse> requestResponses = event.selectedRequestResponses();
        if (requestResponses.isEmpty()) {
            return;
        }

        ExecutorService executorService = Executors.newFixedThreadPool(persistence.getInteger(YOUR_THREAD));

        List<String> allHeaders = getAllHeaders(); // Use combined headers
        List<List<String>> headerChunks = splitIntoChunks(allHeaders.toArray(new String[0]), persistence.getInteger(YOUR_HEADERS_PER_REQUEST));

        for (HttpRequestResponse requestResponse : requestResponses) {
            executorService.submit(() -> {
                try {
                    HttpRequest baseRequest = requestResponse.request();

                    // Remove headers from HEADERS_TO_REMOVE
                    for (String header : HEADERS_TO_REMOVE) {
                        baseRequest = baseRequest.withRemovedHeader(header);
                    }
                    // Process each chunk
                    for (List<String> chunk : headerChunks) {
                        long randomNumber = ThreadLocalRandom.current().nextLong(100_000_000_000L, 1_000_000_000_000L);
                        HttpRequest chunkRequest = baseRequest.withUpdatedParameters(urlParameter("_parameter", String.valueOf(randomNumber)))
                                .withUpdatedParameters(cookieParameter("_Cookie",String.valueOf(randomNumber)))
                                .withHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/" + randomNumber + " Safari/" + randomNumber);

                        for (String header : chunk) {
                            String headerValue = header + "-" + persistence.getString(YOUR_PAYLOAD); // Set value to "header-xxxxxxxxxxxxxx"
                            chunkRequest = chunkRequest.withAddedHeader(header, headerValue); // Add the original header
                            chunkRequest = chunkRequest.withAddedHeader(header, headerValue); // Add the duplicate header
                        }
                        HttpRequestResponse modifiedRequestResponse = api.http().sendRequest(chunkRequest);
                        if (isCached(modifiedRequestResponse)) {
                            table.add(modifiedRequestResponse,true);
                        } else if (isNOTCached(modifiedRequestResponse)) {
                            table.add(modifiedRequestResponse,false);
                        }
                    }
                } catch (Exception e) {
                    api.logging().logToError("Failed to send request: " + e.getMessage());
                }
            });
        }
        executorService.shutdown();
    }


    private void HexHeaderValue(ContextMenuEvent event) {
        List<HttpRequestResponse> requestResponses = event.selectedRequestResponses();
        if (requestResponses.isEmpty()) {
            return;
        }

        // Define the hex values for non-printable characters
        int[] hexValues = {0x1, 0x4, 0x8, 0x9, 0xA, 0xB, 0xC, 0xD, 0x1F, 0x20, 0x7F, 0xA0, 0xFF};

        ExecutorService executorService = Executors.newFixedThreadPool(persistence.getInteger(YOUR_THREAD));


        List<String> allHeaders = getAllHeaders(); // Use combined headers
        List<List<String>> headerChunks = splitIntoChunks(allHeaders.toArray(new String[0]), persistence.getInteger(YOUR_HEADERS_PER_REQUEST));

        for (HttpRequestResponse requestResponse : requestResponses) {
            executorService.submit(() -> {
                try {

                    HttpRequest baseRequest = requestResponse.request();
                    // Remove headers from HEADERS_TO_REMOVE
                    for (String header : HEADERS_TO_REMOVE) {
                        baseRequest = baseRequest.withRemovedHeader(header);
                    }
                    // Process each chunk
                    for (List<String> chunk : headerChunks) {
                        // Create a base request with random parameters
                        // Iterate through hex values and send a separate request for each
                        for (int hexValue : hexValues) {
                            // Convert hex value to a non-printable character
                            char nonPrintableChar = (char) hexValue;

                            // Create a copy of the base request for this iteration
                            long randomNumber = ThreadLocalRandom.current().nextLong(100_000_000_000L, 1_000_000_000_000L);

                            HttpRequest chunkRequest = baseRequest
                                    .withUpdatedParameters(urlParameter("_parameter", String.valueOf(randomNumber)))
                                    .withUpdatedParameters(cookieParameter("_Cookie", String.valueOf(randomNumber)))
                                    .withHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/" + randomNumber + " Safari/" + randomNumber);
                            // Add non-printable character to each header in the chunk
                            for (String header : chunk) {
                                // Prepend the non-printable character to the header name
                                String newHeaderName = nonPrintableChar + header;
                                String headerValue = header + "-" + persistence.getString(YOUR_PAYLOAD); // Set value to "header-xxxxxxxxxxxxxx"
                                // Add the modified header to the request
                                chunkRequest = chunkRequest.withHeader(newHeaderName, headerValue);
                            }
                            // Send the modified request
                            HttpRequestResponse modifiedRequestResponse = api.http().sendRequest(chunkRequest);

                            // Check if the response body is valid and add to the table
                            if (isCached(modifiedRequestResponse)) {
                                table.add(modifiedRequestResponse,true);
                            } else if (isNOTCached(modifiedRequestResponse)) {
                                table.add(modifiedRequestResponse,false);
                            }
                        }
                    }
                } catch (Exception e) {
                    api.logging().logToError("Failed to send request: " + e.getMessage());
                }
            });
        }
        executorService.shutdown();
    }



    private void HeaderHexValue(ContextMenuEvent event) {
        List<HttpRequestResponse> requestResponses = event.selectedRequestResponses();
        if (requestResponses.isEmpty()) {
            return;
        }

        // Define the hex values for non-printable characters
        int[] hexValues = {0x1, 0x4, 0x8, 0x9, 0xA, 0xB, 0xC, 0xD, 0x1F, 0x20, 0x7F, 0xA0, 0xFF};

        ExecutorService executorService = Executors.newFixedThreadPool(persistence.getInteger(YOUR_THREAD));


        List<String> allHeaders = getAllHeaders(); // Use combined headers
        List<List<String>> headerChunks = splitIntoChunks(allHeaders.toArray(new String[0]), persistence.getInteger(YOUR_HEADERS_PER_REQUEST));

        for (HttpRequestResponse requestResponse : requestResponses) {
            executorService.submit(() -> {
                try {

                    HttpRequest baseRequest = requestResponse.request();
                    // Remove headers from HEADERS_TO_REMOVE
                    for (String header : HEADERS_TO_REMOVE) {
                        baseRequest = baseRequest.withRemovedHeader(header);
                    }

                    // Process each chunk
                    for (List<String> chunk : headerChunks) {

                        // Create a base request with random parameters

                        // Iterate through hex values and send a separate request for each
                        for (int hexValue : hexValues) {
                            // Convert hex value to a non-printable character
                            char nonPrintableChar = (char) hexValue;

                            // Create a copy of the base request for this iteration
                            long randomNumber = ThreadLocalRandom.current().nextLong(100_000_000_000L, 1_000_000_000_000L);

                            HttpRequest chunkRequest = baseRequest
                                    .withUpdatedParameters(urlParameter("_parameter", String.valueOf(randomNumber)))
                                    .withUpdatedParameters(cookieParameter("_Cookie", String.valueOf(randomNumber)))
                                    .withHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/" + randomNumber + " Safari/" + randomNumber);
                            // Add non-printable character to each header in the chunk
                            for (String header : chunk) {
                                // Prepend the non-printable character to the header name
                                String newHeaderName = header + nonPrintableChar;
                                String headerValue = header + "-" + persistence.getString(YOUR_PAYLOAD); // Set value to "header-xxxxxxxxxxxxxx"
                                // Add the modified header to the request
                                chunkRequest = chunkRequest.withHeader(newHeaderName, headerValue);
                            }
                            // Send the modified request
                            HttpRequestResponse modifiedRequestResponse = api.http().sendRequest(chunkRequest);

                            // Check if the response body is valid and add to the table
                            if (isCached(modifiedRequestResponse)) {
                                table.add(modifiedRequestResponse,true);
                            } else if (isNOTCached(modifiedRequestResponse)) {
                                table.add(modifiedRequestResponse,false);
                            }
                        }
                    }
                } catch (Exception e) {
                    api.logging().logToError("Failed to send request: " + e.getMessage());
                }
            });
        }
        executorService.shutdown();
    }


    private void duplicateHeaderWithSpace(ContextMenuEvent event) {
        List<HttpRequestResponse> requestResponses = event.selectedRequestResponses();
        if (requestResponses.isEmpty()) {
            return;
        }

        ExecutorService executorService = Executors.newFixedThreadPool(persistence.getInteger(YOUR_THREAD));


        List<String> allHeaders = getAllHeaders(); // Use combined headers
        List<List<String>> headerChunks = splitIntoChunks(allHeaders.toArray(new String[0]), persistence.getInteger(YOUR_HEADERS_PER_REQUEST));

        for (HttpRequestResponse requestResponse : requestResponses) {
            executorService.submit(() -> {
                try {
                    HttpRequest baseRequest = requestResponse.request();

                    // Remove headers from HEADERS_TO_REMOVE
                    for (String header : HEADERS_TO_REMOVE) {
                        baseRequest = baseRequest.withRemovedHeader(header);
                    }
                    // Process each chunk
                    for (List<String> chunk : headerChunks) {
                        long randomNumber = ThreadLocalRandom.current().nextLong(100_000_000_000L, 1_000_000_000_000L);
                        HttpRequest chunkRequest = baseRequest.withUpdatedParameters(urlParameter("_parameter", String.valueOf(randomNumber)))
                                .withUpdatedParameters(cookieParameter("_Cookie",String.valueOf(randomNumber)))
                                .withHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/" + randomNumber + " Safari/" + randomNumber);

                        for (String header : chunk) {
                            String headerValue = header + "-" + persistence.getString(YOUR_PAYLOAD); // Set value to "header-xxxxxxxxxxxxxx"
                            String newHeader = " " + header; // Add space to header name
                            chunkRequest = chunkRequest.withAddedHeader(newHeader, headerValue); // Add the original header
                            chunkRequest = chunkRequest.withAddedHeader(header, headerValue); // Add the duplicate header
                        }
                        HttpRequestResponse modifiedRequestResponse = api.http().sendRequest(chunkRequest);
                        if (isCached(modifiedRequestResponse)) {
                            table.add(modifiedRequestResponse,true);
                        } else if (isNOTCached(modifiedRequestResponse)) {
                            table.add(modifiedRequestResponse,false);
                        }
                    }
                } catch (Exception e) {
                    api.logging().logToError("Failed to send request: " + e.getMessage());
                }
            });
        }
        executorService.shutdown();
    }







    private void fuzzCookie(ContextMenuEvent event) {
        List<HttpRequestResponse> requestResponses = event.selectedRequestResponses();
        if (requestResponses.isEmpty()) {
            return;
        }

        ExecutorService executorService = Executors.newFixedThreadPool(persistence.getInteger(YOUR_THREAD));


        List<String> parameters = loadParameters(); // Load parameters from the file
        List<List<String>> parameterChunks = splitIntoChunks(parameters.toArray(new String[0]), persistence.getInteger(YOUR_PARAMETERS_COOKIE_PER_REQUEST));

        for (HttpRequestResponse requestResponse : requestResponses) {
            executorService.submit(() -> {
                try {
                    HttpRequest baseRequest = requestResponse.request();

                    // Remove unwanted headers
                    for (String header : HEADERS_TO_REMOVE) {
                        baseRequest = baseRequest.withRemovedHeader(header);
                    }

                    // Process each chunk of 50 parameters
                    for (List<String> chunk : parameterChunks) {
                        // Create a new request with the current chunk of parameters
                        long randomNumber = ThreadLocalRandom.current().nextLong(100_000_000_000L, 1_000_000_000_000L);
                        HttpRequest chunkRequest = baseRequest.withUpdatedParameters(urlParameter("_parameter", String.valueOf(randomNumber)))
                                .withHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/" + randomNumber + " Safari/" + randomNumber);

                        // Add each parameter in the chunk to the request
                        for (String parameter : chunk) {
                            chunkRequest = chunkRequest.withAddedParameters(cookieParameter(parameter, persistence.getString(YOUR_PAYLOAD)));
                        }

                        // Send the modified request
                        HttpRequestResponse modifiedRequestResponse = api.http().sendRequest(chunkRequest);

                        // Check if the response is valid and add it to the table
                        if (isCached(modifiedRequestResponse)) {
                            table.add(modifiedRequestResponse,true);
                        } else if (isNOTCached(modifiedRequestResponse)) {
                            table.add(modifiedRequestResponse,false);
                        }
                    }
                } catch (Exception e) {
                    api.logging().logToError("Failed to send request: " + e.getMessage());
                }
            });
        }
        executorService.shutdown();
    }




    // Example action methods for the new menu items
    private void fuzzQuery(ContextMenuEvent event) {
        List<HttpRequestResponse> requestResponses = event.selectedRequestResponses();
        if (requestResponses.isEmpty()) {
            return;
        }

        ExecutorService executorService = Executors.newFixedThreadPool(persistence.getInteger(YOUR_THREAD));

        List<String> parameters = loadParameters(); // Load parameters from the file
        List<List<String>> parameterChunks = splitIntoChunks(parameters.toArray(new String[0]), persistence.getInteger(YOUR_PARAMETERS_QUERY_PER_REQUEST));

        for (HttpRequestResponse requestResponse : requestResponses) {
            executorService.submit(() -> {
                try {
                    HttpRequest baseRequest = requestResponse.request();

                    // Remove unwanted headers
                    for (String header : HEADERS_TO_REMOVE) {
                        baseRequest = baseRequest.withRemovedHeader(header);
                    }

                    // Process each chunk of 50 parameters
                    for (List<String> chunk : parameterChunks) {
                        // Create a new request with the current chunk of parameters
                        long randomNumber = ThreadLocalRandom.current().nextLong(100_000_000_000L, 1_000_000_000_000L);
                        HttpRequest chunkRequest = baseRequest
                                .withUpdatedParameters(cookieParameter("_Cookie",String.valueOf(randomNumber)))
                                .withHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/" + randomNumber + " Safari/" + randomNumber);

                        // Add each parameter in the chunk to the request
                        for (String parameter : chunk) {
                            chunkRequest = chunkRequest.withAddedParameters(parameter(parameter, persistence.getString(YOUR_PAYLOAD), HttpParameterType.URL));
                        }

                        // Send the modified request
                        HttpRequestResponse modifiedRequestResponse = api.http().sendRequest(chunkRequest);

                        // Check if the response is valid and add it to the table
                        if (isCached(modifiedRequestResponse)) {
                            table.add(modifiedRequestResponse,true);
                        } else if (isNOTCached(modifiedRequestResponse)) {
                            table.add(modifiedRequestResponse,false);
                        }
                    }
                } catch (Exception e) {
                    api.logging().logToError("Failed to send request: " + e.getMessage());
                }
            });
        }
        executorService.shutdown();
    }   // Add your logic here


    private void fuzzBodyUrlEncoding(ContextMenuEvent event) {
        List<HttpRequestResponse> requestResponses = event.selectedRequestResponses();
        if (requestResponses.isEmpty()) {
            return;
        }

        ExecutorService executorService = Executors.newFixedThreadPool(persistence.getInteger(YOUR_THREAD));


        List<String> parameters = loadParameters(); // Load parameters from the file
        List<List<String>> parameterChunks = splitIntoChunks(parameters.toArray(new String[0]), persistence.getInteger(YOUR_PARAMETERS_BODY_PER_REQUEST));

        for (HttpRequestResponse requestResponse : requestResponses) {
            executorService.submit(() -> {
                try {
                    HttpRequest baseRequest = requestResponse.request();

                    // Remove unwanted headers
                    for (String header : HEADERS_TO_REMOVE) {
                        baseRequest = baseRequest.withRemovedHeader(header);
                    }

                    // Process each chunk of 50 parameters
                    for (List<String> chunk : parameterChunks) {
                        // Create a new request with the current chunk of parameters
                        long randomNumber = ThreadLocalRandom.current().nextLong(100_000_000_000L, 1_000_000_000_000L);
                        HttpRequest chunkRequest = baseRequest.withUpdatedParameters(urlParameter("_parameter", String.valueOf(randomNumber)))
                                .withUpdatedParameters(cookieParameter("_Cookie",String.valueOf(randomNumber)))
                                .withHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/" + randomNumber + " Safari/" + randomNumber);
                        // Add each parameter in the chunk to the request
                        for (String parameter : chunk) {
                            chunkRequest = chunkRequest.withAddedParameters(parameter(parameter, persistence.getString(YOUR_PAYLOAD), HttpParameterType.BODY));
                        }

                        // Send the modified request
                        HttpRequestResponse modifiedRequestResponse = api.http().sendRequest(chunkRequest);

                        // Check if the response is valid and add it to the table
                        if (isCached(modifiedRequestResponse)) {
                            table.add(modifiedRequestResponse,true);
                        } else if (isNOTCached(modifiedRequestResponse)) {
                            table.add(modifiedRequestResponse,false);
                        }
                    }
                } catch (Exception e) {
                    api.logging().logToError("Failed to send request: " + e.getMessage());
                }
            });
        }
        executorService.shutdown();
    }

    private void fuzzBodyJson(ContextMenuEvent event) {
        List<HttpRequestResponse> requestResponses = event.selectedRequestResponses();
        if (requestResponses.isEmpty()) {
            return;
        }

        ExecutorService executorService = Executors.newFixedThreadPool(persistence.getInteger(YOUR_THREAD));


        List<String> parameters = loadParameters(); // Load parameters from the file
        List<List<String>> parameterChunks = splitIntoChunks(parameters.toArray(new String[0]), persistence.getInteger(YOUR_PARAMETERS_BODY_PER_REQUEST));

        for (HttpRequestResponse requestResponse : requestResponses) {
            executorService.submit(() -> {
                try {
                    HttpRequest baseRequest = requestResponse.request();

                    // Remove unwanted headers
                    for (String header : HEADERS_TO_REMOVE) {
                        baseRequest = baseRequest.withRemovedHeader(header);
                    }

                    // Process each chunk of 50 parameters
                    for (List<String> chunk : parameterChunks) {
                        // Create a new request with the current chunk of parameters
                        long randomNumber = ThreadLocalRandom.current().nextLong(100_000_000_000L, 1_000_000_000_000L);
                        HttpRequest chunkRequest = baseRequest.withUpdatedParameters(urlParameter("_parameter", String.valueOf(randomNumber)))
                                .withUpdatedParameters(cookieParameter("_Cookie",String.valueOf(randomNumber)))
                                .withHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/" + randomNumber + " Safari/" + randomNumber);

                        // Add each parameter in the chunk to the request
                        for (String parameter : chunk) {
                            chunkRequest = chunkRequest.withAddedParameters(parameter(parameter, persistence.getString(YOUR_PAYLOAD), HttpParameterType.JSON));
                        }

                        // Send the modified request
                        HttpRequestResponse modifiedRequestResponse = api.http().sendRequest(chunkRequest);

                        if (isCached(modifiedRequestResponse)) {
                            table.add(modifiedRequestResponse,true);
                        } else if (isNOTCached(modifiedRequestResponse)) {
                            table.add(modifiedRequestResponse,false);
                        }
                    }
                } catch (Exception e) {
                    api.logging().logToError("Failed to send request: " + e.getMessage());
                }
            });
        }
        executorService.shutdown();
    }

    private void fuzzBodyXml(ContextMenuEvent event) {
        List<HttpRequestResponse> requestResponses = event.selectedRequestResponses();
        if (requestResponses.isEmpty()) {
            return;
        }

        ExecutorService executorService = Executors.newFixedThreadPool(persistence.getInteger(YOUR_THREAD));


        List<String> parameters = loadParameters(); // Load parameters from the file
        List<List<String>> parameterChunks = splitIntoChunks(parameters.toArray(new String[0]), persistence.getInteger(YOUR_PARAMETERS_BODY_PER_REQUEST));

        for (HttpRequestResponse requestResponse : requestResponses) {
            executorService.submit(() -> {
                try {
                    HttpRequest baseRequest = requestResponse.request();

                    // Remove unwanted headers
                    for (String header : HEADERS_TO_REMOVE) {
                        baseRequest = baseRequest.withRemovedHeader(header);
                    }

                    // Process each chunk of 50 parameters
                    for (List<String> chunk : parameterChunks) {
                        // Create a new request with the current chunk of parameters
                        long randomNumber = ThreadLocalRandom.current().nextLong(100_000_000_000L, 1_000_000_000_000L);
                        HttpRequest chunkRequest = baseRequest.withUpdatedParameters(urlParameter("_parameter", String.valueOf(randomNumber)))
                                .withUpdatedParameters(cookieParameter("_Cookie",String.valueOf(randomNumber)))
                                .withHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/" + randomNumber + " Safari/" + randomNumber);

                        // Add each parameter in the chunk to the request
                        for (String parameter : chunk) {
                            chunkRequest = chunkRequest.withAddedParameters(parameter(parameter, persistence.getString(YOUR_PAYLOAD), HttpParameterType.XML));
                        }

                        // Send the modified request
                        HttpRequestResponse modifiedRequestResponse = api.http().sendRequest(chunkRequest);

                        // Check if the response is valid and add it to the table
                        if (isCached(modifiedRequestResponse)) {
                            table.add(modifiedRequestResponse,true);
                        } else if (isNOTCached(modifiedRequestResponse)) {
                            table.add(modifiedRequestResponse,false);
                        }
                    }
                } catch (Exception e) {
                    api.logging().logToError("Failed to send request: " + e.getMessage());
                }
            });
        }
        executorService.shutdown();
    }
}