package burpsuite;

import burp.api.montoya.http.message.StatusCodeClass;
import burp.api.montoya.persistence.PersistedObject;
import burp.api.montoya.proxy.http.*;
import burp.api.montoya.http.message.requests.HttpRequest;
import burp.api.montoya.http.message.responses.HttpResponse;
import java.util.concurrent.ThreadLocalRandom;
import java.util.HashMap;
import java.util.Map;
import static burp.api.montoya.http.message.params.HttpParameter.cookieParameter;
import static burp.api.montoya.http.message.params.HttpParameter.urlParameter;
import static burpsuite.UnkeyInput.YOUR_MATCH;
import static burpsuite.UnkeyInput.YOUR_PAYLOAD;

public class proxyHandler implements ProxyRequestHandler, ProxyResponseHandler {

    private final MyTableModel table;
    PersistedObject persistence;
    public proxyHandler(MyTableModel table, PersistedObject persist) {
        this.table = table;
        this.persistence = persist;

    }

    private HttpRequest addCustomHeaders(HttpRequest request) {
        Map<String, String> customHeaders = new HashMap<>();
        customHeaders.put("Referer", "Referer-" + persistence.getString(YOUR_PAYLOAD));
        customHeaders.put("Akamai-Client-Ip", "Akamai-Client-Ip-" + persistence.getString(YOUR_PAYLOAD));
        customHeaders.put("Cf-Connecting-Ip", "Cf-Connecting-Ip-" + persistence.getString(YOUR_PAYLOAD));
        customHeaders.put("Client-Ip", "Client-Ip-" + persistence.getString(YOUR_PAYLOAD));
        customHeaders.put("Fastly-Client-Ip", "Fastly-Client-Ip-" + persistence.getString(YOUR_PAYLOAD));
        customHeaders.put("Forwarded", "Forwarded-" + persistence.getString(YOUR_PAYLOAD));
        customHeaders.put("True-Client-Ip", "True-Client-Ip-" + persistence.getString(YOUR_PAYLOAD));
        customHeaders.put("X-Client-Ip", "X-Client-Ip-" + persistence.getString(YOUR_PAYLOAD));
        customHeaders.put("X-Cluster-Client-Ip", "X-Cluster-Client-Ip-" + persistence.getString(YOUR_PAYLOAD));
        customHeaders.put("X-Forwarded-By", "X-Forwarded-By-" + persistence.getString(YOUR_PAYLOAD));
        customHeaders.put("X-Forwarded-Client-Ip", "X-Forwarded-Client-Ip-" + persistence.getString(YOUR_PAYLOAD));
        customHeaders.put("X-Forwarded-For", "X-Forwarded-For-" + persistence.getString(YOUR_PAYLOAD));
        customHeaders.put("X-Forwarded-For-Original", "X-Forwarded-For-Original-" + persistence.getString(YOUR_PAYLOAD));
        customHeaders.put("X-Forwarded-Host", "X-Forwarded-Host-" + persistence.getString(YOUR_PAYLOAD));
        customHeaders.put("X-Forwarded-Path", "X-Forwarded-Path-" + persistence.getString(YOUR_PAYLOAD));
        customHeaders.put("X-Forwarded-Server", "X-Forwarded-Server-" + persistence.getString(YOUR_PAYLOAD));
        customHeaders.put("X-Forwarded-Uri", "X-Forwarded-Uri-" + persistence.getString(YOUR_PAYLOAD));
        customHeaders.put("X-Host", "X-Host-" + persistence.getString(YOUR_PAYLOAD));
        customHeaders.put("X-Http-Forwarded-For", "X-Http-Forwarded-For-" + persistence.getString(YOUR_PAYLOAD));
        customHeaders.put("X-Original-Cookie", "X-Original-Cookie-" + persistence.getString(YOUR_PAYLOAD));
        customHeaders.put("X-Original-Forwarded-For", "X-Original-Forwarded-For-" + persistence.getString(YOUR_PAYLOAD));
        customHeaders.put("X-Original-Host", "X-Original-Host-" + persistence.getString(YOUR_PAYLOAD));
        customHeaders.put("X-Original-Referer", "X-Original-Referer-" + persistence.getString(YOUR_PAYLOAD));
        customHeaders.put("X-Original-Url", "X-Original-Url-" + persistence.getString(YOUR_PAYLOAD));
        customHeaders.put("X-Original-User-Agent", "X-Original-User-Agent-" + persistence.getString(YOUR_PAYLOAD));
        customHeaders.put("X-Originating-Host", "X-Originating-Host-" + persistence.getString(YOUR_PAYLOAD));
        customHeaders.put("X-Originating-Ip", "X-Originating-Ip-" + persistence.getString(YOUR_PAYLOAD));
        customHeaders.put("X-Proxyuser-Host", "X-Proxyuser-Host-" + persistence.getString(YOUR_PAYLOAD));
        customHeaders.put("X-Proxyuser-Ip", "X-Proxyuser-Ip-" + persistence.getString(YOUR_PAYLOAD));
        customHeaders.put("X-Proxyuser-Uri", "X-Proxyuser-Uri-" + persistence.getString(YOUR_PAYLOAD));
        customHeaders.put("X-Real-Host", "X-Real-Host-" + persistence.getString(YOUR_PAYLOAD));
        customHeaders.put("X-Real-Ip", "X-Real-Ip-" + persistence.getString(YOUR_PAYLOAD));
        customHeaders.put("X-Remote-Addr", "X-Remote-Addr-" + persistence.getString(YOUR_PAYLOAD));
        customHeaders.put("X-Remote-Ip", "X-Remote-Ip-" + persistence.getString(YOUR_PAYLOAD));
        customHeaders.put("X-Rewrite-Url", "X-Rewrite-Url-" + persistence.getString(YOUR_PAYLOAD));
        customHeaders.put("X-True-Ip", "X-True-Ip-" + persistence.getString(YOUR_PAYLOAD));
        customHeaders.put("X-Wap-Client-Ip", "X-Wap-Client-Ip-" + persistence.getString(YOUR_PAYLOAD));
        customHeaders.put("X-Wap-Network-Client-Ip", "X-Wap-Network-Client-Ip-" + persistence.getString(YOUR_PAYLOAD));
        customHeaders.put("X-Wap-Profile", "X-Wap-Profile-" + persistence.getString(YOUR_PAYLOAD));

        for (Map.Entry<String, String> entry : customHeaders.entrySet()) {
            request = request.withHeader(entry.getKey(), entry.getValue());
        }

        return request;
    }


    private boolean hasCacheHeaders(HttpResponse response) {
        String[] cacheHeaders = {"Age", "CDN-Cache", "CF-Cache-Status", "Server-Timing", "X-Cache", "X-Cache-Info", "X-Cache-Remote", "X-Check-Cacheable", "X-Drupal-Cache", "X-Drupal-Dynamic-Cache", "X-Proxy-Cache", "X-Rack-Cache", "Akamai-Cache-Status"};
        for (String header : cacheHeaders) {
            if (response.hasHeader(header)) {
                return true;
            }
        }
        return false;
    }


    @Override
    public ProxyRequestReceivedAction handleRequestReceived(InterceptedRequest interceptedRequest) {
        return ProxyRequestReceivedAction.continueWith(interceptedRequest);
    }

    @Override
    public ProxyRequestToBeSentAction handleRequestToBeSent(InterceptedRequest interceptedRequest) {
        try {
            long randomNumber = ThreadLocalRandom.current().nextLong(100_000_000_000L, 1_000_000_000_000L);
            HttpRequest modifiedRequest = interceptedRequest
                    .withAddedParameters(urlParameter("_parameter", String.valueOf(randomNumber)))
                    .withAddedParameters(cookieParameter("_Cookie",String.valueOf(randomNumber)))
                    .withRemovedHeader("If-Modified-Since")
                    .withRemovedHeader("If-None-Match")
                    .withHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/" + randomNumber + " Safari/" + randomNumber)
                    .withHeader("Pragma", "akamai-x-check-cacheable,akamai-x-cache-on")
                    .withHeader("Fastly-Debug", "1");

            // Add custom headers
            modifiedRequest = addCustomHeaders(modifiedRequest);
            return ProxyRequestToBeSentAction.continueWith(modifiedRequest);
        } catch (Exception e) {
            return ProxyRequestToBeSentAction.continueWith(interceptedRequest);
        }
    }
    @Override
    public ProxyResponseReceivedAction handleResponseReceived(InterceptedResponse interceptedResponse) {
        try {
            if (interceptedResponse.body().toString().contains(persistence.getString(YOUR_MATCH)) || interceptedResponse.headers().toString().contains(persistence.getString(YOUR_MATCH))){
                if (hasCacheHeaders(interceptedResponse)) {
                    table.add(interceptedResponse,true);
                } else if (!interceptedResponse.isStatusCodeClass(StatusCodeClass.CLASS_3XX_REDIRECTION)){
                    table.add(interceptedResponse,false);
                }
            }
            return ProxyResponseReceivedAction.continueWith(interceptedResponse);
        } catch (Exception e) {
            return ProxyResponseReceivedAction.continueWith(interceptedResponse);
        }
    }

    @Override
    public ProxyResponseToBeSentAction handleResponseToBeSent(InterceptedResponse interceptedResponse) {
        return ProxyResponseToBeSentAction.continueWith(interceptedResponse);
    }
}