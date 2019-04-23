package com.thelastcodebenders.follower.service;

import com.thelastcodebenders.follower.enums.RoleType;
import com.thelastcodebenders.follower.model.Role;
import com.thelastcodebenders.follower.model.User;
import com.thelastcodebenders.follower.repository.RoleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RoleService {
    private static final Logger log = LoggerFactory.getLogger(RoleService.class);

    private RoleRepository roleRepository;

    public RoleService(RoleRepository roleRepository){
        this.roleRepository = roleRepository;
    }

    public List<Role> allRoles(){
        return roleRepository.findAll();
    }

    public Role findById(long id){
        Optional<Role> opt = roleRepository.findById(id);
        if (!opt.isPresent()){
            log.error("RoleType Service Find By Id Error - " + id + " RoleType Not Found !");
            return null;
        }
        return opt.get();
    }

    public Role findByRole(RoleType roleType){
        if (roleType == RoleType.ADMIN){
            List<Role> roles = roleRepository.findByRole("ADMIN");
            if (roles.isEmpty()){
                log.error("RoleType Service Find By Role Error");
                return null;
            }else {
                return roles.get(0);
            }
        }else if (roleType == RoleType.USER){
            List<Role> roles = roleRepository.findByRole("USER");
            if (roles.isEmpty()){
                log.error("RoleType Service Find By Role Error");
                return null;
            }else {
                return roles.get(0);
            }
        }else {
            log.error("RoleType Service Find By Role Error");
            return null;
        }
    }
}
