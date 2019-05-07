package com.thelastcodebenders.follower.service;

import com.thelastcodebenders.follower.model.DrawCount;
import com.thelastcodebenders.follower.model.User;
import com.thelastcodebenders.follower.repository.DrawCountRepository;
import com.thelastcodebenders.follower.repository.DrawOrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DrawService {
    private static final Logger log = LoggerFactory.getLogger(DrawService.class);
    private static final int DRAW_COUNT = 5;

    private DrawCountRepository drawCountRepository;
    private DrawOrderRepository drawOrderRepository;

    public DrawService(DrawCountRepository drawCountRepository,
                       DrawOrderRepository drawOrderRepository){
        this.drawCountRepository = drawCountRepository;
        this.drawOrderRepository = drawOrderRepository;
    }

    public DrawCount findDrawCountByUser(User user){
        List<DrawCount> drawCounts = drawCountRepository.findByUser(user);

        if (drawCounts.isEmpty())
            return null;
        else
            return drawCounts.get(0);
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

    public int getDrawCount(User user){
        DrawCount drawCount = findDrawCountByUser(user);
        return drawCount.getCount();
    }
}
