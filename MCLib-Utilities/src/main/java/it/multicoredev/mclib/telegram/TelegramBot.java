package it.multicoredev.mclib.telegram;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Copyright © 2019-2020 by Lorenzo Magni
 * This file is part of MCLib.
 * MCLib is under "The 3-Clause BSD License", you can find a copy <a href="https://opensource.org/licenses/BSD-3-Clause">here</a>.
 * <p>
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 * <p>
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING,
 * BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY,
 * OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */
public class TelegramBot {
    private final String QUERY;
    private String token;
    private List<String> targetChatIds = new LinkedList<>();
    private String defaultParseMode = "markdown";
    private boolean defaultWebPagePreview = true;
    private boolean defaultDisableNotification = false;

    public TelegramBot(@NotNull String token, Collection<String> targetChatIds) {
        this.token = token;
        QUERY = "https://api.telegram.org/bot" + token + "/";
        if (targetChatIds != null) this.targetChatIds.addAll(targetChatIds);
    }

    public TelegramBot(@NotNull String token) {
        this(token, null);
    }

    public String getToken() {
        return token;
    }

    public List<String> getTargetChatIds() {
        return targetChatIds;
    }

    public void addTargetChatIds(String... chatIds) {
        targetChatIds.addAll(Arrays.asList(chatIds));
    }

    public void removeTargetChatIds(String... chatIds) {
        for (String chatId : chatIds) {
            targetChatIds.remove(chatId);
        }
    }

    public void setDefaultParseMode(String defaultParseMode) {
        this.defaultParseMode = defaultParseMode;
    }

    public void setDefaultWebPagePreview(boolean defaultWebPagePreview) {
        this.defaultWebPagePreview = defaultWebPagePreview;
    }

    public void setDefaultDisableNotification(boolean defaultDisableNotification) {
        this.defaultDisableNotification = defaultDisableNotification;
    }

    public boolean sendMessage(@NotNull String text, @NotNull String chatId, String parseMode, boolean webPagePreview, boolean disableNotification, @Nullable Integer replyToMessageId) {
        String query;

        if (parseMode == null) parseMode = defaultParseMode;
        parseMode = parseMode.toLowerCase();

        if (!parseMode.equalsIgnoreCase("html") && !parseMode.equalsIgnoreCase("markdown"))
            parseMode = defaultParseMode;

        try {
            query = QUERY +
                    "sendMessage" +
                    "?chat_id=" + chatId +
                    "&text=" + URLEncoder.encode(text, "UTF-8") +
                    "&parse_mode=" + parseMode +
                    "&disable_web_page_preview=" + !webPagePreview +
                    "&disable_notification=" + disableNotification;
            if (replyToMessageId != null) query = query + "&reply_to_message_id=" + replyToMessageId;
        } catch (UnsupportedEncodingException ignored) {
            return false;
        }

        try {
            URL url = new URL(query);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestMethod("GET");
            connection.getInputStream().close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public int sendMessage(@NotNull String text, String parseMode, boolean webPagePreview, boolean disableNotification, @Nullable Integer replyToMessageId) {
        if (targetChatIds.isEmpty())
            throw new IllegalStateException("There is no target chat id for this bot. To use this method add target chat ids to this class");

        int sent = 0;
        for (String chatId : targetChatIds) {
            if (sendMessage(text, chatId, parseMode, webPagePreview, disableNotification, replyToMessageId)) sent++;
        }

        return sent;
    }

    public boolean sendMessage(@NotNull String text, @NotNull String chatId, String parseMode, boolean webPagePreview, boolean disableNotification) {
        return sendMessage(text, chatId, parseMode, webPagePreview, disableNotification, null);
    }

    public int sendMessage(@NotNull String text, String parseMode, boolean webPagePreview, boolean disableNotification) {
        return sendMessage(text, parseMode, webPagePreview, disableNotification, null);
    }

    public boolean sendMessage(@NotNull String text, @NotNull String chatId, String parseMode, @Nullable Integer replyToMessage) {
        return sendMessage(text, chatId, parseMode, defaultWebPagePreview, defaultDisableNotification, replyToMessage);
    }

    public int sendMessage(@NotNull String text, String parseMode, @Nullable Integer replyToMessage) {
        return sendMessage(text, parseMode, defaultWebPagePreview, defaultDisableNotification, replyToMessage);
    }

    public boolean sendMessage(@NotNull String text, @NotNull String chatId) {
        return sendMessage(text, chatId, defaultParseMode, defaultWebPagePreview, defaultDisableNotification, null);
    }

    public int sendMessage(@NotNull String text) {
        return sendMessage(text, defaultParseMode, defaultWebPagePreview, defaultDisableNotification, null);
    }
}
