package com.thelastcodebenders.follower.assembler;

import com.thelastcodebenders.follower.dto.DrawPrizeSpinnerItem;
import com.thelastcodebenders.follower.model.DrawPrize;
import org.springframework.stereotype.Component;

@Component
public class DrawPrizeAssembler {

    public DrawPrizeSpinnerItem convertDrawPrizeToServiceMode(DrawPrize drawPrize){
        return DrawPrizeSpinnerItem.builder()
                .id(drawPrize.getId())
                .name(drawPrize.getName())
                .build();
    }
}
