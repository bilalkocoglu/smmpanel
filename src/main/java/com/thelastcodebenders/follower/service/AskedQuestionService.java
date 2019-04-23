package com.thelastcodebenders.follower.service;

import com.thelastcodebenders.follower.model.AskedQuestion;
import com.thelastcodebenders.follower.repository.AskedQuestionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class AskedQuestionService {
    private static final Logger log = LoggerFactory.getLogger(AskedQuestionService.class);

    private AskedQuestionRepository askedQuestionRepository;

    public AskedQuestionService(AskedQuestionRepository askedQuestionRepository){
        this.askedQuestionRepository = askedQuestionRepository;
    }

    public List<String> tableColumns(){
        return Stream.of("Soru", "Action").collect(Collectors.toList());
    }

    public List<AskedQuestion> allAskedQuestions(){
        return askedQuestionRepository.findAll();
    }

    public boolean save(AskedQuestion askedQuestion){
        try {
            askedQuestion = askedQuestionRepository.save(askedQuestion);
            if(askedQuestion == null){
                log.error("Asked Question Service Save Error !");
                return false;
            }else
                return true;
        }catch (Exception e){
            log.error("Asked Question Service Save Error - " + e.getMessage());
            return false;
        }
    }

    public boolean delete(long id){
        try {
            askedQuestionRepository.deleteById(id);
            return true;
        }catch (Exception e){
            log.error("Asked Question Delete Error - " + e.getMessage());
            return false;
        }
    }

}
