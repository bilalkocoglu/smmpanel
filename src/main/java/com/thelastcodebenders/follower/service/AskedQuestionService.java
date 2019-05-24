package com.thelastcodebenders.follower.service;

import com.thelastcodebenders.follower.configuration.cache.CacheService;
import com.thelastcodebenders.follower.model.AskedQuestion;
import com.thelastcodebenders.follower.repository.AskedQuestionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class AskedQuestionService {
    private static final Logger log = LoggerFactory.getLogger(AskedQuestionService.class);

    private AskedQuestionRepository askedQuestionRepository;
    private CacheService cacheService;

    public AskedQuestionService(AskedQuestionRepository askedQuestionRepository,
                                CacheService cacheService){
        this.askedQuestionRepository = askedQuestionRepository;
        this.cacheService = cacheService;
    }

    public List<String> tableColumns(){
        return Stream.of("Soru", "Action").collect(Collectors.toList());
    }

    @Cacheable("asked-questions")
    public List<AskedQuestion> allAskedQuestions(){
        return askedQuestionRepository.findAll();
    }

    public boolean save(AskedQuestion askedQuestion){
        try {
            askedQuestion = askedQuestionRepository.save(askedQuestion);
            if(askedQuestion == null){
                log.error("Asked Question Service Save Error !");
                return false;
            }else{
                cacheService.askedQuestionsClear();
                allAskedQuestions();
                return true;
            }
        }catch (Exception e){
            log.error("Asked Question Service Save Error - " + e.getMessage());
            return false;
        }
    }

    public boolean delete(long id){
        try {
            askedQuestionRepository.deleteById(id);
            cacheService.askedQuestionsClear();
            allAskedQuestions();
            return true;
        }catch (Exception e){
            log.error("Asked Question Delete Error - " + e.getMessage());
            return false;
        }
    }

}
