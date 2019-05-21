package com.thelastcodebenders.follower.client.telegram;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class TelegramService {
    private static final Logger log = LoggerFactory.getLogger(TelegramService.class);
    private static final String CLİENT_URL
            ="https://api.telegram.org/bot889175437:AAGJPCIMiRPDenOYMo2KkEVYb-ueBWhU65E/sendMessage";

    private RestTemplate restTemplate;

    public TelegramService (RestTemplate restTemplate){
        this.restTemplate = restTemplate;
    }

    public void sendAdminMessage(String message){
        String channel = Channels.ADMIN.getValue();
        String url = CLİENT_URL + "?chat_id=" + channel + "&text="+ message;

        ResponseEntity<String> res = restTemplate.getForEntity(url, String.class);
    }

    public void sendCommunityMessage(String message){
        String channel = Channels.COMMUNITY.getValue();
        String url = CLİENT_URL + "?chat_id=" + channel + "&text="+ message;

        ResponseEntity res = restTemplate.getForEntity(url, ResponseEntity.class);
    }

    @Async
    public void asyncSendAdminMessage(String message){
        sendAdminMessage(message);
    }

    @Async
    public void asyncSendCommunityMessage(String message){
        sendCommunityMessage(message);
    }
}
