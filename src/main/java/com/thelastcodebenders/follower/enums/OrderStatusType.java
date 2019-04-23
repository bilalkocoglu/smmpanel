package com.thelastcodebenders.follower.enums;

public enum OrderStatusType {
    PENDING, INPROGRESS, COMPLETED, PARTIAL, PROCESSING, CANCELED
    //pending => sipariş alındı
    //inprogress => yükleniyor
    //completed => tamamlandı
    //partial => bir kısmı tamamlandı kalanı iade edildi
    //processing => gönderim sırasında
    //canceled => iptal edildi
}
