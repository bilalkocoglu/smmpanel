package com.thelastcodebenders.follower.iyzico;

import com.iyzipay.Options;
import com.iyzipay.model.CheckoutFormInitialize;
import com.iyzipay.model.Locale;
import com.iyzipay.request.CreateCheckoutFormInitializeRequest;
import com.thelastcodebenders.follower.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class PaymentService {
    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    Options options = new Options();

    public PaymentService(){
        options.setApiKey("sandbox-VAErQ0WL9YQw1iZoIcosEd83huLyHsSx");
        options.setSecretKey("sandbox-tHYHe4jbjH4pWrJ1wVleaF8dqv07a5wk");
        options.setBaseUrl("https://sandbox-api.iyzipay.com");
    }
/*
    public CheckoutFormInitialize createPayment(User user, int price){
        CreateCheckoutFormInitializeRequest createCheckoutFormInitializeRequest = new CreateCheckoutFormInitializeRequest();
        createCheckoutFormInitializeRequest.setLocale(Locale.TR.getValue());
        createCheckoutFormInitializeRequest.setConversationId("deneme123");
        createCheckoutFormInitializeRequest.setPrice(BigDecimal.valueOf(price));
        createCheckoutFormInitializeRequest.setBasketId("deneme123");
        createCheckoutFormInitializeRequest.setPaymentGroup("OTHER");
        createCheckoutFormInitializeRequest.setCallbackUrl();
    }

 */
}
