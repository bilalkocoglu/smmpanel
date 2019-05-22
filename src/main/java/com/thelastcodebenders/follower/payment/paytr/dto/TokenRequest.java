package com.thelastcodebenders.follower.payment.paytr.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TokenRequest {
    private String merchant_id;        //mağaza no
    private String merchant_key;
    private String merchant_salt;

    private String merchant_oid;    //her işlem için benzersiz olmalıdır

    private int payment_amount;     //alınacak tutar(*100)
    private String currency;        //default TL

    private Object[][] user_basket;

    private int no_installment;     //taksit seçeneği(1-0) 1 ise taksitsiz 0 taksitli
    //0,2,3,4,5,6,7,8,9,10,11,12 max_installment Sıfır (0) gönderilmesi durumunda yürürlükteki en fazla izin verilen taksit geçerli olur
    private int max_installment;

    private String paytr_token;     //örnek kodlardan bakılacak

    private String user_name;
    private String user_address;
    private String user_phone;
    private String email;           //musteri mail
    private String user_ip;         //kullanıcı ipsi

    private String merchant_ok_url;
    private String merchant_fail_url;

    private int test_mode;          //1-test 0-canlı
    private int debug_on;           //1-test 0-canlı
    private int timeout_limit;      //30
    private String lang;            //tr - en
}
