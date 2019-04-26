package com.thelastcodebenders.follower.service;

import com.thelastcodebenders.follower.enums.MailType;
import com.thelastcodebenders.follower.model.Message;
import com.thelastcodebenders.follower.model.User;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import javax.mail.internet.MimeMessage;
import java.util.HashMap;
import java.util.Map;

@Service
public class MailService {
    private static final Logger log = LoggerFactory.getLogger(MailService.class);

    private static final String HOST = "localhost:8090";

    private JavaMailSender javaMailSender;
    private Configuration freeMarkerConfig;

    public MailService(JavaMailSender javaMailSender,
                       Configuration freeMarkerConfig){
        this.javaMailSender = javaMailSender;
        this.freeMarkerConfig = freeMarkerConfig;
    }

    private boolean sendMail(String subject, String destination, String body){
        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage);

            mimeMessageHelper.setTo(destination);
            mimeMessageHelper.setText(body, true);
            mimeMessageHelper.setSubject(subject);

            javaMailSender.send(mimeMessage);

            log.info("Mail Başarıyla Gönderildi");

            return true;
        }catch (Exception e){
            log.error(e.toString());
            return false;
        }
    }

    public void sendUpdateServiceStateMail(String adminMail, String body){
        String subject = "Update Services !";

        sendMail(subject, adminMail, body);
    }

    public void sendBalanceWarningMail(String adminMail, String body){
        String subject = "Api Balance Update !";

        sendMail(subject, adminMail, body);
    }

    @Async
    public void asynsSendMail(MailType mailType, User fromUser, User destUser, String message){
        try {
            Map model = new HashMap();
            Template template = null;
            String subject = "";
            String body = "";

            if (mailType == MailType.CREATETICKET){
                subject = "Yeni Destek Talebi !";
                model.put("button_message", fromUser.getName() + " " + fromUser.getSurname() +" tarafından cevaplamanız gereken yeni bir destek talebi oluşturuldu! Aşağıdaki butona tıklayarak cevaplayabilirsiniz.");
                model.put("button_text", "CEVAPLA");
                model.put("button_href", HOST + "/login");
            }else if (mailType == MailType.RESPONSETICKET){
                subject = "Destek Talebiniz Yanıtlandı !";
                model.put("button_message", "Destek talebiniz yanıtlandı! Cevaplamak için aşağıdaki butona basabilirsiniz.");
                model.put("button_text", "CEVAPLA");
                model.put("button_href", HOST + "/login");
            }else if (mailType == MailType.PAYMENTNTFREQ){
                subject = "Yeni Ödeme Bildirimi !";
                model.put("button_message", fromUser.getName() + ' ' + fromUser.getSurname() + " Kullanıcısı tarafından yeni bir ödeme bildirimi oluşturuldu !");
                model.put("button_text", "CEVAPLA");
                model.put("button_href", HOST + "/login");
            }else if (mailType == MailType.PAYMENTNTFRES){
                subject = "Ödeme Bildiriminiz Onaylandı !";
                model.put("button_message","Ödeme bildiriminiz onaylandı ve bakiyeniz güncellendi. Şimdi özgürce alışveriş yapabilirsiniz !");
                model.put("button_text", "CEVAPLA");
                model.put("button_href", HOST + "/login");
            }else if(mailType == MailType.ACCOUNTACTIVATE) {
                subject = "SosyalTrend'e Hoşgeldiniz !";
                model.put("button_message","Aramıza hoşgeldin ! Hesabını aktifleştirmek ve sosyal medyada yeni bir yüz olmak için butona tıkla !");
                model.put("button_text", "AKTİFLEŞTİR");
                model.put("button_href", HOST + "/account-activate/" + message);
            }else {
                return;
            }


            model.put("title", "Social XXX");

            template = freeMarkerConfig.getTemplate("mail-create-ticket.ftl");
            body = FreeMarkerTemplateUtils.processTemplateIntoString(template, model);

            sendMail(subject, destUser.getMail(), body);
        }catch (Exception e){
            log.error("Mail Service Send Mail Error -> " + e.getMessage());
        }
    }

}
