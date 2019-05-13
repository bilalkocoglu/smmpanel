package com.thelastcodebenders.follower.service;

import com.thelastcodebenders.follower.dto.VisitorMessageDTO;
import com.thelastcodebenders.follower.exception.DetectedException;
import com.thelastcodebenders.follower.model.User;
import com.thelastcodebenders.follower.model.VisitorMessage;
import com.thelastcodebenders.follower.repository.VisitorMessageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class VisitorMessageService {
    private static final Logger log = LoggerFactory.getLogger(VisitorMessageService.class);
    private final int TIME_OUT = 30;        //minute

    private MailService mailService;
    private UserService userService;
    private VisitorMessageRepository visitorMessageRepository;

    public VisitorMessageService(MailService mailService,
                                 UserService userService,
                                 VisitorMessageRepository visitorMessageRepository){
        this.mailService = mailService;
        this.userService = userService;
        this.visitorMessageRepository = visitorMessageRepository;
    }

    private boolean mesageDTOvalidate(VisitorMessageDTO messageDTO){
        if (isNullOrEmpty(messageDTO.getEmail()) || isNullOrEmpty(messageDTO.getMessage()) || isNullOrEmpty(messageDTO.getName())){
            return false;
        }
        if ( messageDTO.getEmail().length() > 80 || messageDTO.getMessage().length() > 1000 || messageDTO.getName().length() > 100){
            throw new DetectedException("Çok uzun değer girdiniz !");
        }
        return true;
    }

    private static boolean isNullOrEmpty(String str) {
        if(str != null && !str.isEmpty())
            return false;
        return true;
    }


    public boolean sendVisitorMessage(VisitorMessageDTO visitorMessageDTO, String userip){
        try {

            if (!mesageDTOvalidate(visitorMessageDTO) || userip == null){
                log.error("Visitor Message Service sendVisitorMessage Error => Request Not Valid !");
                throw new DetectedException("Tüm alanları doldurmalısınız !");
            }

            List<VisitorMessage> visitorMessages = visitorMessageRepository.findByIpAddr(userip);
            VisitorMessage visitorMessage = null;
            if (visitorMessages.isEmpty()){
                visitorMessage = VisitorMessage.builder().ipAddr(userip).build();
            }else{
                visitorMessage = visitorMessages.get(0);
                //süre kontrolü
                LocalDateTime timeOutDate = LocalDateTime.now().minus(TIME_OUT, ChronoUnit.MINUTES);
                if (visitorMessage.getLocalDateTime().compareTo(timeOutDate)>0){
                    log.error("Visitor Message Service Send Visitor Message Error -> Not Time Out !");
                    throw new DetectedException(TIME_OUT + " Dakikada bir yalnızca bir mesaj gönderebilirsiniz !");
                }
            }

            String subject = visitorMessageDTO.getName() + " Ziyaretçisinden Mesaj !";
            String body = visitorMessageDTO.getMessage() + "<br><br><br>" +
                    "Gönderen Adı : " + visitorMessageDTO.getName() + "<br>"+
                    "Gönderen Mail : " + visitorMessageDTO.getEmail() + "<br>"+
                    "Gönderen IP : " + userip;
            User admin = userService.getAdmin();

            visitorMessage.setLocalDateTime(LocalDateTime.now());
            mailService.asyncSendMail(subject, admin.getMail(), body);


            visitorMessageRepository.save(visitorMessage);
            return true;
        }catch (Exception e){
            if (e instanceof DetectedException)
                throw e;
            else{
                log.error("Visitor Message Service sendVisitorMessage Error => " + e.getMessage());
                throw new DetectedException("Mesajınız şu anda gönderilemedi. Lütfen daha sonra tekrar deneyin.");
            }
        }
    }
}
