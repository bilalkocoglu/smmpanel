package com.thelastcodebenders.follower.payment.paytr;

import com.google.gson.Gson;
import com.thelastcodebenders.follower.client.telegram.TelegramService;
import com.thelastcodebenders.follower.enums.AsyncMailType;
import com.thelastcodebenders.follower.model.CardPayment;
import com.thelastcodebenders.follower.model.Package;
import com.thelastcodebenders.follower.model.User;
import com.thelastcodebenders.follower.model.VisitorUser;
import com.thelastcodebenders.follower.payment.paytr.dto.CallbackRequest;
import com.thelastcodebenders.follower.payment.paytr.dto.TokenRequest;
import com.thelastcodebenders.follower.payment.paytr.dto.TokenResponse;
import com.thelastcodebenders.follower.service.*;
import org.apache.tomcat.util.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Service
public class PaytrService {
    private static final Logger log = LoggerFactory.getLogger(PaytrService.class);

    private static final String TOKEN_URL = "https://www.paytr.com/odeme/api/get-token";
    private static final String MERCHANT_ID = "135435";
    private static final String MERCHANT_KEY = "hZGwHWfRgzyN6YM4";
    private static final String MERCHANT_SALT = "A5MuRFAut6Q4u479";

    private static final String USER_CALLBACK = "https://sosyaltrend.net/user/load-balance";
    private static final String USER_FAIL_CALLBACK = "https://sosyaltrend.net/user/load-balance";
    private static final String VISITOR_CALLBACK = "https://sosyaltrend.net/all-packages";
    private static final String VISITOR_FAIL_CALLBACK = "https://sosyaltrend.net/all-packages";

    private RestTemplate restTemplate;
    private AccountActivationService accountActivationService;

    private CardPaymentService cardPaymentService;
    private VisitorUserService visitorUserService;
    private UserService userService;
    private DrawService drawService;
    private TelegramService telegramService;
    private OrderService orderService;
    private MailService mailService;

    public PaytrService(RestTemplate restTemplate,
                        AccountActivationService accountActivationService,
                        CardPaymentService cardPaymentService,
                        VisitorUserService visitorUserService,
                        UserService userService,
                        DrawService drawService,
                        TelegramService telegramService,
                        OrderService orderService,
                        MailService mailService){
        this.restTemplate = restTemplate;
        this.accountActivationService = accountActivationService;

        this.cardPaymentService = cardPaymentService;
        this.visitorUserService = visitorUserService;
        this.userService = userService;
        this.drawService = drawService;
        this.telegramService = telegramService;
        this.orderService = orderService;
        this.mailService = mailService;
    }

    public TokenResponse userCreateToken(User user, String ip, int balance) throws NoSuchAlgorithmException, InvalidKeyException {
        try {
            //System.out.println(ip);
            TokenRequest request = new TokenRequest();
            request.setMerchant_id(MERCHANT_ID);
            request.setMerchant_salt(MERCHANT_SALT);

            request.setEmail(user.getMail());
            request.setUser_name(user.getName() + " " + user.getSurname());
            request.setUser_address("İstanbul");
            request.setUser_phone("05347756260");

            request.setPayment_amount(balance*100);
            String oid = accountActivationService.generateRandomPassword(40);
            request.setMerchant_oid('1'+oid);       //1 balance 2 visitor user

            request.setUser_ip("188.119.44.74");

            Object[][] objects = {
                    new Object[]{"Bakiye", String.valueOf(balance), 1}
            };

            String basketJson = new Gson().toJson(objects);
            String basket = Base64.encodeBase64String(basketJson.getBytes(StandardCharsets.UTF_8));
            request.setUser_basket(basket);

            request.setMerchant_ok_url(USER_CALLBACK);
            request.setMerchant_fail_url(USER_FAIL_CALLBACK);

            request.setTimeout_limit("30");
            request.setDebug_on("0");
            request.setTest_mode("0");
            request.setNo_installment("0");
            request.setMax_installment("0");
            request.setCurrency("TL");
            request.setLang("tr");




            String[] infs = new String[]{request.getMerchant_id(), request.getUser_ip(), request.getMerchant_oid(),
                    request.getEmail(), String.valueOf(request.getPayment_amount()), basket,
                    String.valueOf(request.getNo_installment()), String.valueOf(request.getMax_installment()),
                    request.getCurrency(), String.valueOf(request.getTest_mode()), request.getMerchant_salt()};
            StringBuilder concat = new StringBuilder();

            for (String info : infs ) {
                concat.append(info);
            }

            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(MERCHANT_KEY.getBytes(), "HmacSHA256");
            sha256_HMAC.init(secret_key);

            String hash = Base64.encodeBase64String(sha256_HMAC.doFinal(concat.toString().getBytes()));
            request.setPaytr_token(hash);


            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            //System.out.println(request.toString());
            // if you need to pass form parameters in request with headers.
            MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
            map.add("merchant_id", request.getMerchant_id());
            map.add("user_ip", request.getUser_ip());
            map.add("merchant_oid", request.getMerchant_oid());
            map.add("email",request.getEmail());
            map.add("payment_amount", String.valueOf(request.getPayment_amount()));
            map.add("user_basket", basket);
            map.add("paytr_token", request.getPaytr_token());
            map.add("debug_on",request.getDebug_on());
            map.add("test_mode", request.getTest_mode());
            map.add("no_installment", request.getNo_installment());
            map.add("max_installment", request.getMax_installment());
            map.add("user_name",request.getUser_name());
            map.add("user_address", request.getUser_address());
            map.add("user_phone", request.getUser_phone());
            map.add("merchant_ok_url",request.getMerchant_ok_url());
            map.add("merchant_fail_url",request.getMerchant_fail_url());
            map.add("timeout_limit",request.getTimeout_limit());
            map.add("currency",request.getCurrency());
            map.add("lang", request.getLang());

            HttpEntity<MultiValueMap<String, String>> req = new HttpEntity<>(map, headers);
            ResponseEntity<String> res = restTemplate.postForEntity(TOKEN_URL, req, String.class);

            TokenResponse tokenResponse = new Gson().fromJson(res.getBody(), TokenResponse.class);

            tokenResponse.setMerchant_oid(request.getMerchant_oid());
            return tokenResponse;
        }catch (Exception e){
            log.error("PayTR Service CreateUserToken Error => " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public TokenResponse visitorCreateToken(VisitorUser visitorUser, String ip, Package pkg) throws NoSuchAlgorithmException, InvalidKeyException{
        try {
            //System.out.println(ip);
            TokenRequest request = new TokenRequest();
            request.setMerchant_id(MERCHANT_ID);
            request.setMerchant_salt(MERCHANT_SALT);

            request.setEmail(visitorUser.getEmail());
            request.setUser_name(visitorUser.getName() + " " + visitorUser.getSurname());
            request.setUser_address("İstanbul");
            request.setUser_phone("05347756260");

            request.setPayment_amount((int) (pkg.getPrice()*100));
            String oid = accountActivationService.generateRandomPassword(40);
            request.setMerchant_oid('2'+oid);       //1 balance 2 visitor user

            request.setUser_ip("188.119.44.74");

            Object[][] objects = {
                    new Object[]{String.valueOf(pkg.getId()), String.valueOf(pkg.getPrice()), 1}
            };

            String basketJson = new Gson().toJson(objects);
            String basket = Base64.encodeBase64String(basketJson.getBytes(StandardCharsets.UTF_8));
            request.setUser_basket(basket);

            request.setMerchant_ok_url(VISITOR_CALLBACK);
            request.setMerchant_fail_url(VISITOR_CALLBACK);

            request.setTimeout_limit("30");
            request.setDebug_on("0");
            request.setTest_mode("0");
            request.setNo_installment("0");
            request.setMax_installment("0");
            request.setCurrency("TL");
            request.setLang("tr");




            String[] infs = new String[]{request.getMerchant_id(), request.getUser_ip(), request.getMerchant_oid(),
                    request.getEmail(), String.valueOf(request.getPayment_amount()), basket,
                    String.valueOf(request.getNo_installment()), String.valueOf(request.getMax_installment()),
                    request.getCurrency(), String.valueOf(request.getTest_mode()), request.getMerchant_salt()};
            StringBuilder concat = new StringBuilder();

            for (String info : infs ) {
                concat.append(info);
            }

            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(MERCHANT_KEY.getBytes(), "HmacSHA256");
            sha256_HMAC.init(secret_key);

            String hash = Base64.encodeBase64String(sha256_HMAC.doFinal(concat.toString().getBytes()));
            request.setPaytr_token(hash);


            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            //System.out.println(request.toString());
            // if you need to pass form parameters in request with headers.
            MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
            map.add("merchant_id", request.getMerchant_id());
            map.add("user_ip", request.getUser_ip());
            map.add("merchant_oid", request.getMerchant_oid());
            map.add("email",request.getEmail());
            map.add("payment_amount", String.valueOf(request.getPayment_amount()));
            map.add("user_basket", basket);
            map.add("paytr_token", request.getPaytr_token());
            map.add("debug_on",request.getDebug_on());
            map.add("test_mode", request.getTest_mode());
            map.add("no_installment", request.getNo_installment());
            map.add("max_installment", request.getMax_installment());
            map.add("user_name",request.getUser_name());
            map.add("user_address", request.getUser_address());
            map.add("user_phone", request.getUser_phone());
            map.add("merchant_ok_url",request.getMerchant_ok_url());
            map.add("merchant_fail_url",request.getMerchant_fail_url());
            map.add("timeout_limit",request.getTimeout_limit());
            map.add("currency",request.getCurrency());
            map.add("lang", request.getLang());

            HttpEntity<MultiValueMap<String, String>> req = new HttpEntity<>(map, headers);
            ResponseEntity<String> res = restTemplate.postForEntity(TOKEN_URL, req, String.class);

            TokenResponse tokenResponse = new Gson().fromJson(res.getBody(), TokenResponse.class);

            tokenResponse.setMerchant_oid(request.getMerchant_oid());
            return tokenResponse;
        }catch (Exception e){
            log.error("PayTR Service CreateUserToken Error => " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public void callbackAction(CallbackRequest callbackRequest){
        CardPayment cardPayment = null;
        VisitorUser visitorUser = null;
        if (callbackRequest.getMerchant_oid().charAt(0) == '1'){
            cardPayment = cardPaymentService.findActiveByToken(callbackRequest.getMerchant_oid());
        }else if (callbackRequest.getMerchant_oid().charAt(0) == '2'){
            visitorUser = visitorUserService.findActiveByToken(callbackRequest.getMerchant_oid());
        }

        if (callbackRequest.getStatus().equals("success")){
            if (cardPayment != null){
                cardPayment.setFinished(true);
                cardPaymentService.update(cardPayment);
                int amount = Integer.parseInt(callbackRequest.getPayment_amount()) / 100;

                //kampanya
                // 50 - 150 = %5
                // 150 - 250 = %10
                // 250 - --  = %15
                double amountdbl;
                if (amount >= 50 && amount < 150)
                    amountdbl = amount + (double) amount * 0.05;
                else if (amount >= 150 && amount< 250 )
                    amountdbl = amount + (double) amount * 0.10;
                else if (amount>=250)
                    amountdbl = amount + (double) amount * 0.15;
                else
                    amountdbl = (double) amount;

                userService.updateUserBalance(cardPayment.getUser(), amountdbl);
                drawService.addDrawCount(cardPayment.getUser());
                telegramService.asyncSendAdminMessage(cardPayment.getUser().getId()+ "-" +cardPayment.getUser().getName() + " " + cardPayment.getUser().getSurname() + " kullanıcısı tarafından "+ amount + " TL bakiye eklendi.");
            }
            if (visitorUser != null){
                visitorUser.setFinished(true);
                visitorUserService.update(visitorUser);

                long orderId = orderService.createVisitorPackageOrderReturnOrderId(visitorUser.getPkg(), visitorUser.getUrl());

                if (orderId == -1){
                    log.error("Ödeme alındı fakat sipariş verilemedi. Konu hakkında sayfa altındaki yer alan iletişim formundan bizimle iletişime geçiniz.");
                    //admin info
                    telegramService.asyncSendAdminMessage(visitorUser.getEmail() + " ziyaretçisinden bir kullanıcıdan ödeme alındı fakat " + visitorUser.getPkg().getService().getApi().getName() + " API'den " + visitorUser.getPkg().getService().getId() + " idli servis ile ilgili cevap gelmediği için sipariş verilemedi.");
                    //user info
                    mailService.asyncSendVisitorOrderMail(visitorUser.getEmail(), "-1" , visitorUser.getName(), visitorUser.getSurname(), false);
                }

                //admin info
                telegramService.asyncSendAdminMessage(visitorUser.getEmail() + " ziyaretçisinden " + visitorUser.getPkg().getPrice() + " tutarında ödeme alındı ve " + visitorUser.getPkg().getId() + "paketinden sipariş verildi.");
                //user info
                mailService.asyncSendVisitorOrderMail(visitorUser.getEmail(), String.valueOf(orderId), visitorUser.getName(), visitorUser.getSurname(), true);
            }
        }else {
            log.error("PayTR Service Callback status not Success ! CallbackRequest => " + callbackRequest.toString());
        }
    }
}
