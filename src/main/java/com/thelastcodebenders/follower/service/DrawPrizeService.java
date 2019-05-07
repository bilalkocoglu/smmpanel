package com.thelastcodebenders.follower.service;


import com.thelastcodebenders.follower.enums.ServiceState;
import com.thelastcodebenders.follower.enums.UserAction;
import com.thelastcodebenders.follower.model.DrawPrize;
import com.thelastcodebenders.follower.model.Service;
import com.thelastcodebenders.follower.repository.DrawPrizeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@org.springframework.stereotype.Service
public class DrawPrizeService {
    private static final Logger log = LoggerFactory.getLogger(DrawPrizeService.class);

    private DrawPrizeRepository drawPrizeRepository;

    public DrawPrizeService(DrawPrizeRepository drawPrizeRepository){
        this.drawPrizeRepository = drawPrizeRepository;
    }



    public List<DrawPrize> findAll(){
        return drawPrizeRepository.findAll();
    }

    public DrawPrize findById(long id){
        return drawPrizeRepository.getOne(id);
    }

    public List<DrawPrize> findPrizeByService(Service service){
        return drawPrizeRepository.findByService(service);
    }

    public void servicePassivateHandler(Service service){
        List<DrawPrize> drawPrizes = findPrizeByService(service);

        for (DrawPrize drawprize: drawPrizes) {
            drawprize.setState(false);
        }

        drawPrizeRepository.saveAll(drawPrizes);
    }


    public DrawPrize save(DrawPrize drawPrize){
        try {
            drawPrizeValidation(drawPrize);

            double apiPrice = drawPrize.getQuantity() * (drawPrize.getService().getApiPrice()/1000);
            apiPrice = Double.parseDouble(String.format("%.2f", apiPrice));

            drawPrize.setApiPrice(apiPrice);
            drawPrize.setState(false);

            drawPrize = drawPrizeRepository.save(drawPrize);
            return drawPrize;
        }catch (Exception e){
            if (e instanceof RuntimeException)
                throw e;
            else {
                log.error("DrawPrizeService Save Error -> " + e.getMessage());
                throw new RuntimeException("İşlem gerçekleştirilemedi !");
            }
        }
    }

    private boolean drawPrizeValidation(DrawPrize drawPrize){
        if (isNullOrEmpty(drawPrize.getName()) || drawPrize.getQuantity()==0 || drawPrize.getService()==null){
            throw new RuntimeException("Tüm alanları eksiksiz doldurmalısınız !");
        }
        return true;
    }

    private static boolean isNullOrEmpty(String str) {
        if(str != null && !str.isEmpty())
            return false;
        return true;
    }



    public void changeState(long id, UserAction action){
        try {
            DrawPrize drawPrize = findById(id);

            if (drawPrize == null){
                log.error("DrawPrizeService Change State Error -> DrawPrize Not Found !");
                throw new RuntimeException("Böyle bir ödül bulunamadı !");
            }

            if (action == UserAction.ACTIVATE){
                if (drawPrize.getService().getState() != ServiceState.ACTIVE)
                    throw new RuntimeException("Ödülün bağlı olduğu servis pasif durumda !");
                drawPrize.setState(true);
            }
            if (action == UserAction.PASSIVATE){
                drawPrize.setState(false);
            }

            drawPrizeRepository.save(drawPrize);
        }catch (Exception e){
            if (e instanceof RuntimeException)
                throw e;
            else {
                log.error("DrawPrizeService Change State Error -> " + e.getMessage());
                throw new RuntimeException("İşleminiz şu anda gerçekleştirilemedi. Lütfen daha sonra tekrar deneyin.");
            }
        }
    }
}
