package com.thelastcodebenders.follower.service;


import com.thelastcodebenders.follower.model.DrawPrize;
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
}
