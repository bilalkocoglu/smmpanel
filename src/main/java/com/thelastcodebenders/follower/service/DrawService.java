package com.thelastcodebenders.follower.service;

import com.thelastcodebenders.follower.client.ClientService;
import com.thelastcodebenders.follower.client.dto.OrderStatusResponse;
import com.thelastcodebenders.follower.dto.CountDownDTO;
import com.thelastcodebenders.follower.enums.OrderStatusType;
import com.thelastcodebenders.follower.model.*;
import com.thelastcodebenders.follower.repository.DrawCountRepository;
import com.thelastcodebenders.follower.repository.DrawOrderRepository;
import com.thelastcodebenders.follower.repository.DrawVisitRepository;
import org.apache.catalina.Lifecycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class DrawService {
    private static final Logger log = LoggerFactory.getLogger(DrawService.class);
    private static final int DRAW_COUNT = 5;

    private DrawCountRepository drawCountRepository;
    private DrawOrderRepository drawOrderRepository;
    private DrawVisitRepository drawVisitRepository;
    private DrawPrizeService drawPrizeService;
    private ClientService clientService;

    public DrawService(DrawCountRepository drawCountRepository,
                       DrawOrderRepository drawOrderRepository,
                       DrawVisitRepository drawVisitRepository,
                       DrawPrizeService drawPrizeService,
                       ClientService clientService){
        this.drawCountRepository = drawCountRepository;
        this.drawOrderRepository = drawOrderRepository;
        this.drawVisitRepository = drawVisitRepository;
        this.drawPrizeService = drawPrizeService;
        this.clientService = clientService;
    }

    public DrawCount findDrawCountByUser(User user){
        List<DrawCount> drawCounts = drawCountRepository.findByUser(user);

        if (drawCounts.isEmpty())
            return null;
        else
            return drawCounts.get(0);
    }

    public DrawVisit findDrawVisitById(long id){
        Optional<DrawVisit> opt = drawVisitRepository.findById(id);
        if (opt.isPresent())
            return opt.get();
        else {
            log.error("Draw Service findDrawVisitById Error -> Not Found !");
            return null;
        }
    }

    public List<DrawVisit> findDrawVisitByUser(User user){
        List<DrawVisit> drawVisits = drawVisitRepository.findByUser(user, new Sort(Sort.Direction.DESC, "id"));
        return drawVisits;
    }

    public List<DrawOrder> findDrawOrderByUser(User user){
        List<DrawOrder> drawOrders = new ArrayList<>();

        List<DrawVisit> drawVisits = findDrawVisitByUser(user);

        for (DrawVisit drawVisit: drawVisits) {
            if (drawVisit.getDrawOrder() != null)
                drawOrders.add(drawVisit.getDrawOrder());
        }

        return drawOrders;
    }

    public void addDrawCount(User user){
        DrawCount drawCount = findDrawCountByUser(user);

        if (drawCount == null){
            drawCount = newDrawCount(user);
        }

        drawCount.setCount(drawCount.getCount()+DRAW_COUNT);

        drawCountRepository.save(drawCount);

    }

    public DrawCount newDrawCount(User user){
        DrawCount drawCount = DrawCount.builder().user(user).count(0).build();
        drawCount = drawCountRepository.save(drawCount);
        return drawCount;
    }

    public CountDownDTO drawPermission(User user){
        DrawCount drawCount = findDrawCountByUser(user);

        if (drawCount.getCount() == 0){
            throw new RuntimeException("Çekiliş hakkınız bulunmamaktadır. Çekilişlere katılmak için lütfen bakiye yükleyiniz !");
        }

        List<DrawVisit> drawVisits = drawVisitRepository.findByUser(user, new Sort(Sort.Direction.DESC, "date"));

/*
        System.out.println("-------------------Draw Visits");
        for (DrawVisit d: drawVisits) {
            System.out.println(d.toString());
        }
*/
        if (drawVisits.isEmpty()){
            return null; //Çekiliş yapabilir !
        }else {

            DrawVisit endVisit = drawVisits.get(0);
            Duration duration = Duration.between(endVisit.getDate(), LocalDateTime.now());
            long durationMinutes = (24*60) - duration.toMinutes();
            if ( durationMinutes < 0)
                return null;
            else {
                return CountDownDTO.builder()
                        .endtimeSeconds((int) (durationMinutes * 60))
                        .build();
            }
        }
    }

    public boolean drawActionPermission(User user){
        try {
            CountDownDTO countDownDTO = drawPermission(user);

            if (countDownDTO == null)
                return true;
            else
                throw new RuntimeException("Yeni bir çekilişe katılmak için biraz daha zamana ihtiyacınız var !");
        }catch (Exception e){
            if ( e instanceof RuntimeException)
                throw e;
            log.error("DrawService DrawActionPermission Error -> " + e.getMessage());
            return false;
        }
    }

    @Transactional
    public long createDrawVisit(User user){
        DrawVisit drawVisit = DrawVisit.builder()
                .date(LocalDateTime.now())
                .user(user)
                .build();
        drawVisit = drawVisitRepository.save(drawVisit);

        DrawCount drawCount = findDrawCountByUser(user);

        drawCount.setCount(drawCount.getCount()-1);
        drawCountRepository.save(drawCount);
        return drawVisit.getId();
    }

    public DrawPrize newPrize(String url, long prizeId, long drawVisitId){
        try {
            DrawVisit drawVisit = findDrawVisitById(drawVisitId);

            if (drawVisit == null){
                throw new RuntimeException("Geçersiz Çekiliş !");
            }else if (drawVisit.getDrawOrder() != null){
                throw new RuntimeException("Geçersiz Çekiliş !");
            }

            DrawPrize drawPrize = drawPrizeService.findById(prizeId);

            if (drawPrize == null){
                throw new RuntimeException("Geçersiz Çekiliş !");
            }

            String apiOrderId = clientService.createDrawPrizeOrderReturnOrderId(drawPrize.getService(), url, String.valueOf(drawPrize.getQuantity()));

            if (apiOrderId == null || apiOrderId.equals("0")){
                throw new RuntimeException("Siparişiniz verilemedi. Bunu bir destek talebi açıp bildirirseniz arkadaşlarımız en kısa sürede gerekli yardımı sağlayacaktır.");
            }

            DrawOrder drawOrder = DrawOrder.builder()
                    .apiOrderId(apiOrderId)
                    .drawPrize(drawPrize)
                    .status(OrderStatusType.PENDING)
                    .closed(false)
                    .build();

            drawOrder = drawOrderRepository.save(drawOrder);

            drawVisit.setDrawOrder(drawOrder);
            drawVisitRepository.save(drawVisit);
            return drawPrize;
        }catch (Exception e){
            if (e instanceof RuntimeException)
                throw e;
            else
                throw new RuntimeException("Siparişiniz verilemedi. Bunu bir destek talebi açıp bildirirseniz arkadaşlarımız en kısa sürede gerekli yardımı sağlayacaktır.");
        }
    }

    //shedule task method !!
    public void updateActiveOrderStatus(){
        try {
            List<DrawOrder> activeOrders = drawOrderRepository.findByClosed(false);


            for (DrawOrder order: activeOrders) {
                OrderStatusResponse orderStatusResponse = clientService.orderStatus(order.getApiOrderId(), order.getDrawPrize().getService().getApi());

                if (orderStatusResponse.getStatus().equals("Pending")){
                    //sipariş alındı
                    log.info(order.getId() + " -> Pending !");

                    order.setStatus(OrderStatusType.PENDING);
                }
                else if (orderStatusResponse.getStatus().equals("In progress")){
                    //yükleniyor
                    log.info(order.getId() + " -> Inprogress !");

                    order.setStatus(OrderStatusType.INPROGRESS);
                }
                else if (orderStatusResponse.getStatus().equals("Completed")){
                    //tamamlandı
                    log.info(order.getId() + " -> Complated !");


                    order.setClosed(true);
                    order.setStatus(OrderStatusType.COMPLETED);
                }
                else if (orderStatusResponse.getStatus().equals("Partial")){
                    //bir kısmı tamamlandı kalanı iade edildi
                    log.info(order.getId() + " -> Partial !");

                    order.setClosed(true);
                    order.setStatus(OrderStatusType.PARTIAL);
                }
                else if (orderStatusResponse.getStatus().equals("Processing")){
                    //gönderim sırasında
                    log.info(order.getId() + " -> Processing !");

                    order.setStatus(OrderStatusType.PROCESSING);
                }
                else if (orderStatusResponse.getStatus().equals("Canceled")){
                    //iptal edildi
                    //para iade edilecek, api bakiyesi güncellenecek
                    log.info(order.getId() + " -> Canceled !");


                    order.setClosed(true);
                    order.setStatus(OrderStatusType.CANCELED);
                }

                order = drawOrderRepository.save(order);
            }
        }catch (Exception e){
            log.error("Order Service Order Status Update Error -> " + e.getMessage());
        }
    }

}
