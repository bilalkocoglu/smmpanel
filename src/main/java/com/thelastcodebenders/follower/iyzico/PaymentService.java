package com.thelastcodebenders.follower.iyzico;

import com.iyzipay.Options;
import com.iyzipay.model.*;
import com.iyzipay.request.CreateCheckoutFormInitializeRequest;
import com.thelastcodebenders.follower.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class PaymentService {
    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    Options options = new Options();

    public PaymentService(){
        options.setApiKey("sandbox-VAErQ0WL9YQw1iZoIcosEd83huLyHsSx");
        options.setSecretKey("sandbox-tHYHe4jbjH4pWrJ1wVleaF8dqv07a5wk");
        options.setBaseUrl("https://sandbox-api.iyzipay.com");
    }

    public CheckoutFormInitialize createPayment(User user, int price, String ip){
        CreateCheckoutFormInitializeRequest createCheckoutFormInitializeRequest = new CreateCheckoutFormInitializeRequest();
        createCheckoutFormInitializeRequest.setLocale(Locale.TR.getValue());
        createCheckoutFormInitializeRequest.setConversationId("deneme123");
        createCheckoutFormInitializeRequest.setPrice(BigDecimal.valueOf(price));
        createCheckoutFormInitializeRequest.setBasketId("deneme123");
        createCheckoutFormInitializeRequest.setPaymentGroup("OTHER");
        createCheckoutFormInitializeRequest.setCallbackUrl("https://iyzico-test.herokuapp.com/iyzico/callback");
        createCheckoutFormInitializeRequest.setCurrency(Currency.TRY.name());
        createCheckoutFormInitializeRequest.setPaidPrice(BigDecimal.valueOf(price));

        List<Integer> enabledInstallments = new ArrayList<Integer>();
        enabledInstallments.add(2);
        enabledInstallments.add(3);
        enabledInstallments.add(6);
        enabledInstallments.add(9);

        createCheckoutFormInitializeRequest.setEnabledInstallments(enabledInstallments);

        Buyer buyer = new Buyer();
        buyer.setId(user.getId().toString());
        buyer.setName(user.getName());
        buyer.setSurname(user.getSurname());
        buyer.setIdentityNumber("1111111111");
        buyer.setEmail(user.getMail());
        buyer.setCity("Istanbul");
        buyer.setGsmNumber("05347756260");
        buyer.setRegistrationAddress("Istanbul");
        buyer.setCountry("Turkey");
        buyer.setIp(ip);

        createCheckoutFormInitializeRequest.setBuyer(buyer);


        Address shippingAddress = new Address();
        shippingAddress.setContactName(user.getName());
        shippingAddress.setCity("Istanbul");
        shippingAddress.setCountry("Turkey");
        shippingAddress.setAddress("Nidakule Göztepe, Merdivenköy Mah. Bora Sok. No:1");

        createCheckoutFormInitializeRequest.setShippingAddress(shippingAddress);

        Address billingAddress = new Address();
        billingAddress.setContactName(user.getName());
        billingAddress.setCity("Istanbul");
        billingAddress.setCountry("Turkey");
        billingAddress.setAddress("Nidakule Göztepe, Merdivenköy Mah. Bora Sok. No:1");

        createCheckoutFormInitializeRequest.setBillingAddress(billingAddress);

        List<BasketItem> basketItems = new ArrayList<BasketItem>();
        BasketItem firstBasketItem = new BasketItem();
        firstBasketItem.setId("BI101");
        firstBasketItem.setName("Binocular");
        firstBasketItem.setCategory1("Collectibles");
        firstBasketItem.setCategory2("Accessories");
        firstBasketItem.setItemType(BasketItemType.PHYSICAL.name());
        firstBasketItem.setPrice(new BigDecimal(price));
        basketItems.add(firstBasketItem);

        createCheckoutFormInitializeRequest.setBasketItems(basketItems);

        CheckoutFormInitialize checkoutFormInitialize = CheckoutFormInitialize.create(createCheckoutFormInitializeRequest, options);
        return checkoutFormInitialize;
    }



}
