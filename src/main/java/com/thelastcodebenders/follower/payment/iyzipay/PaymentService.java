package com.thelastcodebenders.follower.payment.iyzipay;

import com.iyzipay.Options;
import com.iyzipay.model.*;
import com.iyzipay.request.CreateCheckoutFormInitializeRequest;
import com.iyzipay.request.RetrieveCheckoutFormRequest;
import com.thelastcodebenders.follower.model.Package;
import com.thelastcodebenders.follower.model.User;
import com.thelastcodebenders.follower.model.VisitorUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class PaymentService {
    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);
    private static final String BALANCE_CALLBACK_URL = "http://localhost:8090/user/iyzico/callback";
    private static final String PACKAGE_CALLBACK_URL = "http://localhost:8090/package/order/iyzico/callback";

    Options options = new Options();

    public PaymentService(){
        options.setApiKey("sandbox-VAErQ0WL9YQw1iZoIcosEd83huLyHsSx");
        options.setSecretKey("sandbox-tHYHe4jbjH4pWrJ1wVleaF8dqv07a5wk");
        options.setBaseUrl("https://sandbox-api.iyzipay.com");
    }

    public CheckoutFormInitialize createBalancePayment(User user, int price, String ip){
        CreateCheckoutFormInitializeRequest createCheckoutFormInitializeRequest = new CreateCheckoutFormInitializeRequest();
        createCheckoutFormInitializeRequest.setLocale(Locale.TR.getValue());
        createCheckoutFormInitializeRequest.setConversationId("deneme123");
        createCheckoutFormInitializeRequest.setPrice(BigDecimal.valueOf(price));
        createCheckoutFormInitializeRequest.setBasketId("deneme123");
        createCheckoutFormInitializeRequest.setPaymentGroup("OTHER");
        createCheckoutFormInitializeRequest.setCallbackUrl(BALANCE_CALLBACK_URL);
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
        buyer.setIdentityNumber("user" + user.getId());
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
        shippingAddress.setAddress("Beşiktaş");

        createCheckoutFormInitializeRequest.setShippingAddress(shippingAddress);

        Address billingAddress = new Address();
        billingAddress.setContactName(user.getName());
        billingAddress.setCity("Istanbul");
        billingAddress.setCountry("Turkey");
        billingAddress.setAddress("Beşiktaş");

        createCheckoutFormInitializeRequest.setBillingAddress(billingAddress);

        List<BasketItem> basketItems = new ArrayList<BasketItem>();
        BasketItem firstBasketItem = new BasketItem();
        firstBasketItem.setId("100001");
        firstBasketItem.setName("Bakiye");
        firstBasketItem.setCategory1("Media");
        firstBasketItem.setItemType(BasketItemType.VIRTUAL.name());
        firstBasketItem.setPrice(new BigDecimal(price));
        basketItems.add(firstBasketItem);

        createCheckoutFormInitializeRequest.setBasketItems(basketItems);

        CheckoutFormInitialize checkoutFormInitialize = CheckoutFormInitialize.create(createCheckoutFormInitializeRequest, options);
        return checkoutFormInitialize;
    }

    public CheckoutFormInitialize createPackagePayment(VisitorUser visitorUser, Package pkg, String ip){
        CreateCheckoutFormInitializeRequest createCheckoutFormInitializeRequest = new CreateCheckoutFormInitializeRequest();
        createCheckoutFormInitializeRequest.setLocale(Locale.TR.getValue());
        createCheckoutFormInitializeRequest.setConversationId("visitoruser-" + visitorUser.getId());
        createCheckoutFormInitializeRequest.setPrice(BigDecimal.valueOf(pkg.getPrice()));
        createCheckoutFormInitializeRequest.setBasketId("visitoruser-" + visitorUser.getId());
        createCheckoutFormInitializeRequest.setPaymentGroup("OTHER");
        createCheckoutFormInitializeRequest.setCallbackUrl(PACKAGE_CALLBACK_URL);
        createCheckoutFormInitializeRequest.setCurrency(Currency.TRY.name());
        createCheckoutFormInitializeRequest.setPaidPrice(BigDecimal.valueOf(pkg.getPrice()));

        List<Integer> enabledInstallments = new ArrayList<Integer>();
        enabledInstallments.add(2);
        enabledInstallments.add(3);
        enabledInstallments.add(6);
        enabledInstallments.add(9);

        createCheckoutFormInitializeRequest.setEnabledInstallments(enabledInstallments);

        Buyer buyer = new Buyer();
        buyer.setId(visitorUser.getId().toString());
        buyer.setName(visitorUser.getName());
        buyer.setSurname(visitorUser.getSurname());
        buyer.setIdentityNumber("user" + visitorUser.getId());
        buyer.setEmail(visitorUser.getEmail());
        buyer.setCity("Istanbul");
        buyer.setGsmNumber("05347756260");
        buyer.setRegistrationAddress("Istanbul");
        buyer.setCountry("Turkey");
        buyer.setIp(ip);

        createCheckoutFormInitializeRequest.setBuyer(buyer);


        Address shippingAddress = new Address();
        shippingAddress.setContactName(visitorUser.getName());
        shippingAddress.setCity("Istanbul");
        shippingAddress.setCountry("Turkey");
        shippingAddress.setAddress("Beşiktaş");

        createCheckoutFormInitializeRequest.setShippingAddress(shippingAddress);

        Address billingAddress = new Address();
        billingAddress.setContactName(visitorUser.getName());
        billingAddress.setCity("Istanbul");
        billingAddress.setCountry("Turkey");
        billingAddress.setAddress("Beşiktaş");

        createCheckoutFormInitializeRequest.setBillingAddress(billingAddress);

        List<BasketItem> basketItems = new ArrayList<BasketItem>();
        BasketItem firstBasketItem = new BasketItem();
        firstBasketItem.setId(pkg.getId().toString());
        firstBasketItem.setName(pkg.getName());
        firstBasketItem.setCategory1("Media");
        firstBasketItem.setItemType(BasketItemType.VIRTUAL.name());
        firstBasketItem.setPrice(new BigDecimal(pkg.getPrice()));
        basketItems.add(firstBasketItem);

        createCheckoutFormInitializeRequest.setBasketItems(basketItems);

        CheckoutFormInitialize checkoutFormInitialize = CheckoutFormInitialize.create(createCheckoutFormInitializeRequest, options);
        return checkoutFormInitialize;
    }



    public CheckoutForm infoPayment(String token, String conversationId){
        RetrieveCheckoutFormRequest request = new RetrieveCheckoutFormRequest();
        request.setLocale(Locale.TR.getValue());
        //request.setConversationId(conversationId);
        request.setToken(token);


        CheckoutForm checkoutForm = CheckoutForm.retrieve(request, options);
        return checkoutForm;
    }

}
