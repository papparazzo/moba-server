/*
 *  Project:    moba-server
 *
 *  Copyright (C) 2025 Stefan Paproth <pappi-@gmx.de>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program. If not, see <https://www.gnu.org/licenses/agpl.txt>.
 *
 */
package moba.server.apiconnector;

import moba.server.json.JsonDecoder;
import moba.server.json.JsonException;
import moba.server.json.streamreader.JsonStreamReaderBytes;
import moba.server.json.stringreader.JsonStringReader;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public final class OAuth2HttpClient {

    private String accessToken;

    private final String clientId;
    private final String clientSecret;
    private final String endPoint;

    public OAuth2HttpClient(String endPoint, String clientId, String clientSecret)
    throws JsonException, URISyntaxException, IOException, InterruptedException {
        this.endPoint = endPoint;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.accessToken = getAccessToken(clientId, clientSecret);
    }

    public Object get(String uri)
    throws ApiConnectorException {
        HttpRequest.Builder builder = HttpRequest.newBuilder().GET();
        return this.sendRequest(builder, uri);
    }

    private Object sendRequest(HttpRequest.Builder builder, String uri)
    throws ApiConnectorException {
        try {
           return sendRequest(builder, new URI(uri), false);
        } catch (URISyntaxException | IOException | JsonException e) {
            throw new ApiConnectorException("could not send request <" + uri + ">", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiConnectorException("interrupted while loading <" + uri + ">", e);
        }
    }

    private Object sendRequest(HttpRequest.Builder builder, URI uri, boolean retry)
    throws IOException, InterruptedException, JsonException, URISyntaxException, ApiConnectorException {
        HttpRequest request =
            builder.
            setHeader("Authorization", "Bearer " + accessToken).
            version(HttpClient.Version.HTTP_2).
            uri(uri).
            build();

        HttpResponse<String> response =
            HttpClient.newBuilder().
            followRedirects(HttpClient.Redirect.NORMAL).
            build().send(request, HttpResponse.BodyHandlers.ofString());

        var status = response.statusCode();

        if(status == 401 && retry) {
            throw new ApiConnectorException("Unauthorized after refreshing access token");
        }

        if(status == 401) {
            accessToken = getAccessToken(clientId, clientSecret);
            return sendRequest(builder, uri, true);
        }

        if(status >= 300) {
            throw new ApiConnectorException("Unexpected response code: " + response.statusCode());
        }

        var body = response.body();

        if (body == null || body.isEmpty()) {
            return null;
        }

        byte[] b = response.body().getBytes();

        JsonDecoder decoder = new JsonDecoder(new JsonStringReader(new JsonStreamReaderBytes(b)));

        return decoder.decode();
    }

    @SuppressWarnings("unchecked")
    private String getAccessToken(String clientId, String clientSecret)
    throws URISyntaxException, IOException, InterruptedException, JsonException {
        Map<String, String> formData = new HashMap<>();
        formData.put("grant_type", "client_credentials");
        formData.put("client_id", clientId);
        formData.put("client_secret", clientSecret);

        HttpRequest request =
            HttpRequest.newBuilder().
            uri(new URI( endPoint + "access_token")).
            header("Accept", "application/json").
            header("Content-Type", "application/x-www-form-urlencoded").
            version(HttpClient.Version.HTTP_2).
            POST(HttpRequest.BodyPublishers.ofString(getFormDataAsString(formData))).
            build();

        HttpResponse<String> response =
            HttpClient.newBuilder().build().send(request, HttpResponse.BodyHandlers.ofString());

        byte[] b = response.body().getBytes();

        JsonDecoder decoder = new JsonDecoder(new JsonStringReader(new JsonStreamReaderBytes(b)));

        Map<String, Object> o = (Map<String, Object>)decoder.decode();

        return (String)o.get("access_token");
    }

    private static String getFormDataAsString(Map<String, String> formData) {
        StringBuilder formBodyBuilder = new StringBuilder();
        for (Map.Entry<String, String> singleEntry : formData.entrySet()) {
            if (!formBodyBuilder.isEmpty()) {
                formBodyBuilder.append("&");
            }
            formBodyBuilder.append(URLEncoder.encode(singleEntry.getKey(), StandardCharsets.UTF_8));
            formBodyBuilder.append("=");
            formBodyBuilder.append(URLEncoder.encode(singleEntry.getValue(), StandardCharsets.UTF_8));
        }
        return formBodyBuilder.toString();
    }
}