package com.thelastcodebenders.follower.service;

import com.thelastcodebenders.follower.enums.AsyncMailType;
import com.thelastcodebenders.follower.enums.SyncMailType;
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

    private static final String HOST = "sosyaltrend.net";

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
    public void asyncSendVisitorOrderMail(String mail, String orderId, String name, String surname){
        try {
            Map model = new HashMap();
            Template template = freeMarkerConfig.getTemplate("mail-create-ticket.ftl");
            String subject = "Siparişiniz Alındı !";

            model.put("button_message", "Merhaba " + name + " " + surname + ", aramıza hoşgeldiniz ! Ödemeniz ve siparişiniz alınmıştır. Aşağıdaki sipariş numaranız ile sipariş durumunuzu kontrol edebilirsiniz !");
            model.put("button_text", orderId);
            model.put("button_href", HOST + "/");
            model.put("title", "Sosyal Trend");
            model.put("create_order_link", HOST + "/all-packages");
            model.put("terms_use_link", HOST+"/terms-use");

            String body = FreeMarkerTemplateUtils.processTemplateIntoString(template, model);

            sendMail(subject, mail, body);
        }catch (Exception e){
            log.error("Mail Service asyncSendVisitorOrderMail Error => " + e.getMessage());
        }
    }

    @Async
    public void asyncSendMail(String subject, String destination, String body){
        sendMail(subject,destination,body);
    }

    @Async
    public void asyncSendMail(AsyncMailType asyncMailType, User fromUser, User destUser, String message){
        try {
            Map model = new HashMap();
            Template template = null;
            String subject = "";
            String body = "";

            if (asyncMailType == AsyncMailType.CREATETICKET){
                subject = "Yeni Destek Talebi !";
                model.put("button_message", fromUser.getName() + " " + fromUser.getSurname() +" tarafından cevaplamanız gereken yeni bir destek talebi oluşturuldu! Aşağıdaki butona tıklayarak cevaplayabilirsiniz.");
                model.put("button_text", "CEVAPLA");
                model.put("button_href", HOST + "/");
            }else if (asyncMailType == AsyncMailType.RESPONSETICKET){
                subject = "Destek Talebiniz Yanıtlandı !";
                model.put("button_message", "Destek talebiniz yanıtlandı! Cevaplamak için aşağıdaki butona basabilirsiniz.");
                model.put("button_text", "CEVAPLA");
                model.put("button_href", HOST + "/");
            }else if (asyncMailType == AsyncMailType.PAYMENTNTFREQ){
                subject = "Yeni Ödeme Bildirimi !";
                model.put("button_message", fromUser.getName() + ' ' + fromUser.getSurname() + " Kullanıcısı tarafından yeni bir ödeme bildirimi oluşturuldu !");
                model.put("button_text", "CEVAPLA");
                model.put("button_href", HOST + "/");
            }else if (asyncMailType == AsyncMailType.PAYMENTNTFRES){
                subject = "Ödeme Bildiriminiz Onaylandı !";
                model.put("button_message","Ödeme bildiriminiz onaylandı ve bakiyeniz güncellendi. Şimdi özgürce alışveriş yapabilirsiniz !");
                model.put("button_text", "CEVAPLA");
                model.put("button_href", HOST + "/");
            }else if(asyncMailType == AsyncMailType.ACCOUNTACTIVATE) {
                subject = "SosyalTrend'e Hoşgeldiniz !";
                model.put("button_message","Aramıza hoşgeldin ! Hesabını aktifleştirmek ve sosyal medyada yeni bir yüz olmak için butona tıkla !");
                model.put("button_text", "AKTİFLEŞTİR");
                model.put("button_href", HOST + "/account-activate/" + message);
            }else {
                return;
            }

            model.put("create_order_link", HOST + "/all-packages");
            model.put("terms_use_link", HOST+"/terms-use");
            model.put("title", "Sosyal Trend");

            template = freeMarkerConfig.getTemplate("mail-create-ticket.ftl");
            body = FreeMarkerTemplateUtils.processTemplateIntoString(template, model);

            sendMail(subject, destUser.getMail(), body);
        }catch (Exception e){
            log.error("Mail Service Async Send Mail Error -> " + e.getMessage());
        }
    }

    public boolean syncSendMail(SyncMailType mailType, User fromUser, User destUser, String message){
        try {
            Map model = new HashMap();
            Template template = null;
            String subject = "";
            String body = "";

            if (mailType == SyncMailType.RESETPASS){
                subject = "Şifreniz Sıfırlandı !";
                model.put("button_message","Sizin için şifrenizi sıfırladık. Güvenlik açısından bu şifre ile giriş yaptıktan sonra profilinizden şifrenizi deiştirmenizi tavsiye ederiz.");
                model.put("button_text", message);  //password
                model.put("button_href", HOST + "/");
            }
            else
                return false;

            model.put("create_order_link", HOST + "/all-packages");
            model.put("terms_use_link", HOST+"/terms-use");
            model.put("title", "Sosyal Trend");

            template = freeMarkerConfig.getTemplate("mail-create-ticket.ftl");
            body = FreeMarkerTemplateUtils.processTemplateIntoString(template, model);

            sendMail(subject, destUser.getMail(), body);

            return true;
        }catch (Exception e){
            log.error("Mail Service Sync Send Mail Error -> " + e.getMessage());
            return false;
        }
    }
}
