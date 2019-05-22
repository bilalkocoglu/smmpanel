package com.thelastcodebenders.follower.payment.paytr;

import com.thelastcodebenders.follower.model.User;
import com.thelastcodebenders.follower.payment.paytr.dto.TokenRequest;
import com.thelastcodebenders.follower.payment.paytr.dto.TokenResponse;
import com.thelastcodebenders.follower.service.AccountActivationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Formatter;

@Service
public class PaytrService {
    private static final Logger log = LoggerFactory.getLogger(PaytrService.class);

    private static final String TOKEN_URL = "https://www.paytr.com/odeme/api/get-token";
    private static final int MERCHANT_ID = 135435;
    private static final String MERCHANT_KEY = "hZGwHWfRgzyN6YM4";
    private static final String MERCHANT_SALT = "A5MuRFAut6Q4u479";

    private static final String USER_CALLBACK = "https://sosyaltrend.net/user/load-balance";
    private static final String USER_FAIL_CALLBACK = "https://sosyaltrend.net/user/load-balance";
    private static final String VISITOR_CALLBACK = "visitor deneme";
    private static final String VISITOR_FAIL_CALLBACK = "visitor fail deneme";

    private RestTemplate restTemplate;
    private AccountActivationService accountActivationService;

    public PaytrService(RestTemplate restTemplate,
                        AccountActivationService accountActivationService){
        this.restTemplate = restTemplate;
        this.accountActivationService = accountActivationService;
    }

    public TokenResponse userCreateToken(User user, String ip, int balance) throws NoSuchAlgorithmException, InvalidKeyException {
        try {
            TokenRequest request = new TokenRequest();
            request.setMerchant_id(MERCHANT_ID);
            request.setMerchant_key(MERCHANT_KEY);
            request.setMerchant_salt(MERCHANT_SALT);

            request.setEmail(user.getMail());
            request.setUser_name(user.getName() + " " + user.getSurname());
            request.setUser_address("Istanbul");
            request.setUser_phone("05347756260");

            request.setPayment_amount(balance*100);
            String oid = accountActivationService.generateRandomPassword(40);
            request.setMerchant_oid(oid);

            request.setUser_ip(ip);

            Object[][] objects = {
                    new Object[]{"Bakiye", String.valueOf(balance), 1}
            };
            request.setUser_basket(objects);

            request.setMerchant_ok_url(USER_CALLBACK);
            request.setMerchant_fail_url(USER_FAIL_CALLBACK);

            request.setTimeout_limit(30);
            request.setDebug_on(1);
            request.setTest_mode(1);
            request.setNo_installment(0);
            request.setMax_installment(0);
            request.setCurrency("TL");
            request.setLang("tr");

            String[] infs = new String[]{String.valueOf(request.getMerchant_id()), request.getUser_ip(), request.getMerchant_oid(),
                    request.getEmail(), String.valueOf(request.getPayment_amount()), request.getUser_basket().toString(),
                    String.valueOf(request.getNo_installment()), String.valueOf(request.getMax_installment()),
                    request.getCurrency(), String.valueOf(request.getTest_mode()), request.getMerchant_salt()};
            String concat = "";

            for (String info : infs ) {
                concat += info;
            }

            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            sha256_HMAC.init(new SecretKeySpec(request.getMerchant_key().getBytes(StandardCharsets.UTF_8), "HmacSHA256"));

            byte[] bytes = sha256_HMAC.doFinal(concat.getBytes(StandardCharsets.UTF_8));



            request.setPaytr_token(new String(bytes));

            System.out.println("send Req");
            ResponseEntity<TokenResponse> res = restTemplate.postForEntity(TOKEN_URL, request, TokenResponse.class);

            System.out.println(res);
            System.out.println(res.getBody());

            return res.getBody();
        }catch (Exception e){
            log.error("PayTR Service CreateUserToken Error => " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}
