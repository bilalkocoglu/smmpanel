package com.thelastcodebenders.follower.service;


import com.thelastcodebenders.follower.assembler.DrawPrizeAssembler;
import com.thelastcodebenders.follower.dto.DrawPrizeSpinnerItem;
import com.thelastcodebenders.follower.enums.ServiceState;
import com.thelastcodebenders.follower.enums.UserAction;
import com.thelastcodebenders.follower.exception.DetectedException;
import com.thelastcodebenders.follower.model.DrawPrize;
import com.thelastcodebenders.follower.model.DrawVisit;
import com.thelastcodebenders.follower.model.Service;
import com.thelastcodebenders.follower.repository.DrawPrizeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@org.springframework.stereotype.Service
public class DrawPrizeService {
    private static final Logger log = LoggerFactory.getLogger(DrawPrizeService.class);

    private DrawPrizeRepository drawPrizeRepository;
    private DrawPrizeAssembler drawPrizeAssembler;

    public DrawPrizeService(DrawPrizeRepository drawPrizeRepository,
                            DrawPrizeAssembler drawPrizeAssembler){
        this.drawPrizeRepository = drawPrizeRepository;
        this.drawPrizeAssembler = drawPrizeAssembler;
    }



    public List<DrawPrize> findAll(){
        return drawPrizeRepository.findAll();
    }

    public DrawPrize findById(long id){
        Optional<DrawPrize> opt = drawPrizeRepository.findById(id);
        if (opt.isPresent())
            return opt.get();
        else {
            log.error("Draw Prize Service findById Error -> Not Found !");
            return null;
        }
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
            if (e instanceof DetectedException)
                throw e;
            else {
                log.error("DrawPrizeService Save Error -> " + e.getMessage());
                throw new DetectedException("İşlem gerçekleştirilemedi !");
            }
        }
    }

    private boolean drawPrizeValidation(DrawPrize drawPrize){
        if (isNullOrEmpty(drawPrize.getName()) || drawPrize.getQuantity()==0 || drawPrize.getService()==null){
            throw new DetectedException("Tüm alanları eksiksiz doldurmalısınız !");
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
                throw new DetectedException("Böyle bir ödül bulunamadı !");
            }

            if (action == UserAction.ACTIVATE){
                if (drawPrize.getService().getState() != ServiceState.ACTIVE)
                    throw new DetectedException("Ödülün bağlı olduğu servis pasif durumda !");
                drawPrize.setState(true);
            }
            if (action == UserAction.PASSIVATE){
                drawPrize.setState(false);
            }

            drawPrizeRepository.save(drawPrize);
        }catch (Exception e){
            if (e instanceof DetectedException)
                throw e;
            else {
                log.error("DrawPrizeService Change State Error -> " + e.getMessage());
                throw new DetectedException("İşleminiz şu anda gerçekleştirilemedi. Lütfen daha sonra tekrar deneyin.");
            }
        }
    }

    public List<DrawPrizeSpinnerItem> getSpinnerItems(){
        List<DrawPrize> drawPrizes = drawPrizeRepository.findTop3ByState(true,
                new Sort(Sort.Direction.ASC, "apiPrice"));

        List<DrawPrizeSpinnerItem> drawPrizeSpinnerItems = new ArrayList<>();

        for (DrawPrize drawPrize: drawPrizes) {
            drawPrizeSpinnerItems.add(drawPrizeAssembler.convertDrawPrizeToServiceMode(drawPrize));
        }

        return drawPrizeSpinnerItems;
    }
}
