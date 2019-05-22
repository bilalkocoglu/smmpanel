package com.thelastcodebenders.follower.payment.paytr;

import com.google.gson.Gson;
import com.thelastcodebenders.follower.model.User;
import com.thelastcodebenders.follower.payment.paytr.dto.TokenRequest;
import com.thelastcodebenders.follower.payment.paytr.dto.TokenResponse;
import com.thelastcodebenders.follower.service.AccountActivationService;
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
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;

@Service
public class PaytrService {
    private static final Logger log = LoggerFactory.getLogger(PaytrService.class);

    private static final String TOKEN_URL = "https://www.paytr.com/odeme/api/get-token";
    private static final String MERCHANT_ID = "135435";
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
            request.setMerchant_salt(MERCHANT_SALT);

            request.setEmail(user.getMail());
            request.setUser_name(user.getName() + " " + user.getSurname());
            request.setUser_address("İstanbul");
            request.setUser_phone("05347756260");

            request.setPayment_amount(balance*100);
            String oid = accountActivationService.generateRandomPassword(40);
            request.setMerchant_oid(oid);

            request.setUser_ip(ip);

            Object[][] objects = {
                    new Object[]{"Bakiye", String.valueOf(balance), 1}
            };

            String basketJson = new Gson().toJson(objects);
            String basket = Base64.encodeBase64String(basketJson.getBytes(StandardCharsets.UTF_8));
            request.setUser_basket(basket);

            request.setMerchant_ok_url(USER_CALLBACK);
            request.setMerchant_fail_url(USER_FAIL_CALLBACK);

            request.setTimeout_limit("30");
            request.setDebug_on("1");
            request.setTest_mode("1");
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


            return tokenResponse;
        }catch (Exception e){
            log.error("PayTR Service CreateUserToken Error => " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}
