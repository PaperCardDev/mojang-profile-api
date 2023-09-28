package cn.paper_card;


import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.UUID;


public class MojangProfileApi {
    public record Profile(
            String name,
            UUID uuid
    ) {
    }


    private final @NotNull Gson gson = new Gson();


    private static @Nullable String handleUuid(@NotNull String uuid) {
        // 2055446784cb4773a084e3cfa867d480 ->
        // 20554467-84cb-4773-a084-e3cfa867d480
        // 8 4 4 4 12

        if (uuid.length() < 32) return null;

        final String part1 = uuid.substring(0, 8);
        final String part2 = uuid.substring(8, 12);
        final String part3 = uuid.substring(12, 16);
        final String part4 = uuid.substring(16, 20);
        final String part5 = uuid.substring(20, 32);

        return part1 + '-' + part2 + '-' + part3 + '-' + part4 + '-' + part5;
    }

    private @NotNull Profile parseJsonObject(@NotNull JsonObject jsonObject) throws Exception {
        // 2055446784cb4773a084e3cfa867d480 ->
        // 20554467-84cb-4773-a084-e3cfa867d480
        // 8 4 4 4 12

        final JsonElement idElement = jsonObject.get("id");
        if (idElement == null) throw new Exception("返回的Json对象中不包含id属性！");

        final String idStr = idElement.getAsString();
        if (idStr == null) throw new Exception("返回的Json对象中不包含id属性！");

        final String idStr2 = handleUuid(idStr);

        if (idStr2 == null) throw new Exception("不正确的UUID：%s".formatted(idStr));

        final UUID uuid;
        try {
            uuid = UUID.fromString(idStr2);
        } catch (IllegalArgumentException e) {
            throw new Exception(e);
        }

        // name
        final JsonElement nameElement = jsonObject.get("name");
        if (nameElement == null) throw new Exception("返回的Json对象中不包含name属性！");

        final String nameStr = nameElement.getAsString();
        if (nameStr == null) throw new Exception("返回的Json对象中不包含name属性！");


        return new Profile(nameStr, uuid);
    }

    public @NotNull Profile requestByName(@NotNull String playerName) throws Exception {
        // https://api.mojang.com/users/profiles/minecraft/Paper99

        final URL url;

        try {
            url = new URL("https://api.mojang.com/users/profiles/minecraft/" + playerName);
        } catch (MalformedURLException e) {
            throw new Exception(e);
        }

        final HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();

        final String resp = readResp(connection);

        /*
        {
            "id": "2055446784cb4773a084e3cfa867d480",
            "name": "Paper99"
        }
        */

        final JsonObject jsonObject;
        try {
            jsonObject = this.gson.fromJson(resp, JsonObject.class);
        } catch (JsonSyntaxException e) {
            throw new Exception("Json解析失败！");
        }

        if (jsonObject == null) throw new Exception("Json解析失败！");

        return this.parseJsonObject(jsonObject);
    }


    public @NotNull Profile requestByUuid(@NotNull UUID uuid) throws Exception {
        // https://api.mojang.com/user/profile/2055446784cb4773a084e3cfa867d480

        final URL url;

        try {
            url = new URL("https://api.mojang.com/user/profile/" + uuid.toString().replace("-", ""));
        } catch (MalformedURLException e) {
            throw new Exception(e);
        }

        final HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();

        final String resp = readResp(connection);

        /*
        {
            "id": "2055446784cb4773a084e3cfa867d480",
            "name": "Paper99"
        }
        */

        final JsonObject jsonObject;
        try {
            jsonObject = this.gson.fromJson(resp, JsonObject.class);
        } catch (JsonSyntaxException e) {
            throw new Exception("Json解析失败！");
        }

        return this.parseJsonObject(jsonObject);
    }

    private static void closeInput(@NotNull BufferedReader bufferedReader, @NotNull InputStreamReader inputStreamReader, @NotNull InputStream inputStream) throws IOException {

        IOException exception = null;
        try {
            bufferedReader.close();
        } catch (IOException e) {
            exception = e;
        }

        try {
            inputStreamReader.close();
        } catch (IOException e) {
            exception = e;
        }

        try {
            inputStream.close();
        } catch (IOException e) {
            exception = e;
        }

        if (exception != null) throw exception;
    }

    private static @NotNull String readResp(@NotNull HttpsURLConnection connection) throws IOException {
        final InputStream inputStream = connection.getInputStream();
        final InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
        final BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

        final StringBuilder builder = new StringBuilder();
        String line;
        try {
            while ((line = bufferedReader.readLine()) != null) {
                builder.append(line);
                builder.append('\n');
            }
        } catch (IOException e) {
            try {
                closeInput(bufferedReader, inputStreamReader, inputStream);
            } catch (IOException ignored) {
            }
            throw e;
        }
        closeInput(bufferedReader, inputStreamReader, inputStream);
        return builder.toString();

    }
}
