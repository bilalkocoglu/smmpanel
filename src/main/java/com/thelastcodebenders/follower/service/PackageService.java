package com.thelastcodebenders.follower.service;


import com.thelastcodebenders.follower.assembler.PackageAssembler;
import com.thelastcodebenders.follower.dto.PackageFormDTO;
import com.thelastcodebenders.follower.enums.ServiceState;
import com.thelastcodebenders.follower.enums.UserAction;
import com.thelastcodebenders.follower.model.Package;
import com.thelastcodebenders.follower.model.Service;
import com.thelastcodebenders.follower.model.User;
import com.thelastcodebenders.follower.repository.PackageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@org.springframework.stereotype.Service
public class PackageService {
    private static final Logger log = LoggerFactory.getLogger(PackageService.class);

    private PackageRepository packageRepository;
    private PackageAssembler packageAssembler;
    private ServiceService serviceService;

    public PackageService(PackageRepository packageRepository,
                          PackageAssembler packageAssembler,
                          ServiceService serviceService){
        this.packageRepository = packageRepository;
        this.packageAssembler = packageAssembler;
        this.serviceService = serviceService;
    }

    public List<String> packageColumns(){
        return Stream.of("Paket Adı", "Kategori", "Servis", "Miktar", "Fiyat", "Durum", "Action").collect(Collectors.toList());
    }

    public boolean save(PackageFormDTO packageFormDTO){
        try {
            Service service = serviceService.findServiceById(Long.valueOf(packageFormDTO.getServiceId()));
            Package pkg = packageAssembler.convertFormDtoToPackage(packageFormDTO, service);
            packageRepository.save(pkg);
            return true;
        }catch (Exception e){
            log.error("Package Service Save Error -> " + e.getMessage());
            return false;
        }
    }

    public List<Package> findPackageByService(Service service){
        return packageRepository.findByService(service);
    }

    public void saveAll(List<Package> packages){
        packageRepository.saveAll(packages);
    }

    public List<Package> allPackages(){
        return packageRepository.findAll();
    }

    public boolean changeState(long id, UserAction action){
        try {
            Optional<Package> opt = packageRepository.findById(id);

            if (!opt.isPresent()){
                log.error("Package Service Change State Error - Boyle bir paket bulunamadi !");
                return false;
            }

            Package pkg = opt.get();
            if (action == UserAction.ACTIVATE){
                if (pkg.getService().getState()== ServiceState.PASSIVE || pkg.getService().getState()== ServiceState.DELETED){
                    throw new RuntimeException("Aktifleştirmek istediğiniz paketin bağlı olduğu servis pasif !");
                }
                pkg.setState(true);
            }
            if (action == UserAction.PASSIVATE)
                pkg.setState(false);

            pkg = packageRepository.save(pkg);
            return true;
        }catch (Exception e){
            if (e instanceof RuntimeException)
                throw e;
            log.error("Package Service Change State Error - " + e.getMessage());
            return false;
        }
    }

}
