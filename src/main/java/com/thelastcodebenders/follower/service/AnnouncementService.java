package com.thelastcodebenders.follower.service;

import com.thelastcodebenders.follower.configuration.cache.CacheService;
import com.thelastcodebenders.follower.model.Announcement;
import com.thelastcodebenders.follower.repository.AnnouncementRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class AnnouncementService {
    private static final Logger log = LoggerFactory.getLogger(AnnouncementService.class);

    private AnnouncementRepository announcementRepository;
    private CacheService cacheService;

    public AnnouncementService(AnnouncementRepository announcementRepository,
                               CacheService cacheService){
        this.announcementRepository = announcementRepository;
        this.cacheService = cacheService;
    }

    public List<String> tableColumns(){
        return Stream.of("Başlık", "Action").collect(Collectors.toList());
    }

    @Cacheable("announcements")
    public List<Announcement> findAll(){
        return announcementRepository.findAll();
    }

    public boolean save(Announcement announcement){
        try {
            announcement.setDate(LocalDate.now());
            announcement = announcementRepository.save(announcement);
            if(announcement == null){
                log.error("Announcement Service Save Error !");
                return false;
            }else{
                cacheService.announcementsClear();
                findAll();
                return true;
            }
        }catch (Exception e){
            log.error("Announcement Service Save Error - " + e.getMessage());
            return false;
        }
    }

    public boolean delete(long id){
        try {
            announcementRepository.deleteById(id);
            cacheService.announcementsClear();
            findAll();
            return true;
        }catch (Exception e){
            log.error("Announcement Service Delete Error - " + e.getMessage());
            return false;
        }
    }
}
