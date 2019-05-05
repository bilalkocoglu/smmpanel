package com.thelastcodebenders.follower.service;

import com.thelastcodebenders.follower.assembler.UserAssembler;
import com.thelastcodebenders.follower.dto.ChangePasswordDTO;
import com.thelastcodebenders.follower.dto.RegisterFormDTO;
import com.thelastcodebenders.follower.enums.RoleType;
import com.thelastcodebenders.follower.enums.SyncMailType;
import com.thelastcodebenders.follower.enums.UserAction;
import com.thelastcodebenders.follower.model.Role;
import com.thelastcodebenders.follower.model.User;
import com.thelastcodebenders.follower.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import javax.security.auth.login.LoginException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@org.springframework.stereotype.Service
public class UserService {
    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    private static final int PASSLENGTH = 5;

    private UserRepository userRepository;
    private UserAssembler userAssembler;
    private RoleService roleService;
    private AccountActivationService accountActivationService;
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    private MailService mailService;

    public UserService(UserRepository userRepository,
                       UserAssembler userAssembler,
                       RoleService roleService,
                       AccountActivationService accountActivationService,
                       BCryptPasswordEncoder bCryptPasswordEncoder,
                       MailService mailService){
        this.userRepository = userRepository;
        this.userAssembler = userAssembler;
        this.roleService = roleService;
        this.accountActivationService = accountActivationService;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.mailService = mailService;
    }

    public int userCount(){
        //1 her zaman admin, 2 her zaman user
        Role role = roleService.findByRole(RoleType.USER);
        return (int)userRepository.countByRole(role);
    }


    public List<String> tableColumns(){
        return Stream.of("Id", "Ad-Soyad", "Mail", "Durum", "Telefon", "Bakiye", "İşlemler").collect(Collectors.toList());
    }



    public List<User> allUser(){
        Role role = roleService.findByRole(RoleType.USER);
        return userRepository.findByRole(role);
    }

    public User findUserById(long id){
        Optional<User> opt = userRepository.findById(id);
        if (!opt.isPresent()){
            log.error("User Service Find User Error - " + id + " User Not Found !");
            return null;
        }
        return opt.get();
    }

    public User findUserByMail(String mail){
        List<User> users = userRepository.findByMail(mail);
        if (users.size()>0){
            return users.get(0);
        }else {
            log.error("User Service Find By Mail Error ! ");
            return null;
        }
    }



    @Transactional
    public boolean updateUserBalance(User user, double amount){
        try {
            user.setBalance(user.getBalance() + amount);
            userRepository.save(user);
            return true;
        }catch (Exception e){
            log.error("User Service Update Balance Error - " + e.getMessage() );
            return false;
        }
    }

    public boolean adminBasedUpdate(long id, User user, String newBalanceParam){
        try {
            double newbalance = Double.valueOf(newBalanceParam);
            User usr = findUserById(id);
            usr.setName(user.getName());
            usr.setSurname(user.getSurname());
            usr.setMail(user.getMail());
            usr.setNumber(user.getNumber());
            usr.setBalance(newbalance);
            userRepository.save(usr);
            return true;
        }catch (Exception e){
            log.error("User Service Admin Based Update Error - " + e.getMessage());
            return false;
        }
    }



    public boolean changeState(long id, UserAction action){
        try {
            Optional<User> opt = userRepository.findById(id);

            if (!opt.isPresent()){
                log.error("User Service Change State Error - Boyle bir kullanici bulunamadi !");
                return false;
            }

            User user = opt.get();
            if (action == UserAction.ACTIVATE)
                user.setState(true);
            if (action == UserAction.PASSIVATE)
                user.setState(false);

            userRepository.save(user);
            return true;
        }catch (Exception e){
            log.error("User Service Change State Error - " + e.getMessage());
            return false;
        }
    }



    public double getAuthUserBalance() throws LoginException {
        User user = getAuthUser();

        if (user == null){
            log.error("User Service Auth User Balance Error -> user = null !");
            return -1;
        }
        else
            return Double.parseDouble(String.format("%.2f", user.getBalance()));
    }

    public String getAuthUserName() throws LoginException {
        User user = getAuthUser();
        return user.getName() + ' ' + user.getSurname();
    }

    public User getAuthUser() throws LoginException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = findUserByMail(auth.getName());

        if (user == null){
            log.error("User Service get Auth User Error -> user = null");
            throw new LoginException();
        }
        else
            return user;
    }

    public User getAdmin(){
        Role role = roleService.findByRole(RoleType.ADMIN);
        List<User> users = userRepository.findByRole(role);
        if (users.isEmpty()){
            log.error("User Service Get Admin Error -> Not Found");
            return null;
        }
        return users.get(0);
    }


    public boolean saveUser(RegisterFormDTO registerForm){
        try {
            if (!registerFormValidation(registerForm)){
                throw new RuntimeException("Tüm bilgileri doğru bir şekilde girmelisiniz !");
            }

            List<User> users = userRepository.findByMail(registerForm.getMail());
            if (users.size()>0){
                throw new RuntimeException("Kayıt için girdiğiniz mail adresi kullanımda !");
            }


            Role role = roleService.findByRole(RoleType.USER);
            User user = userAssembler.convertFormDtoToUser(registerForm, role);
            user = userRepository.save(user);
            //aktivasyon kodu oluşturup asenkron mail gönder.

            accountActivationService.generateSecretKey(user);

            return true;
        }catch (Exception e){
            if (e instanceof RuntimeException)
                throw e;
            log.error("User Service Save User Error -> " + e.getMessage());
            return false;
        }
    }

    private boolean registerFormValidation(RegisterFormDTO registerForm){
        if ( isNullOrEmpty(registerForm.getMail()) || isNullOrEmpty(registerForm.getName())
                || isNullOrEmpty(registerForm.getPassword())
            || isNullOrEmpty(registerForm.getRepairPass()) || isNullOrEmpty(registerForm.getSurname())){
            return false;
        }

        if ( !registerForm.getPassword().equals(registerForm.getRepairPass()) ){
            return false;
        }

        if ( registerForm.getPassword().length()<PASSLENGTH){
            throw new RuntimeException("Şifreniz 5 karakter veya daha fazla olmalıdır !");
        }

        return true;
    }

    private static boolean isNullOrEmpty(String str) {
        if(str != null && !str.isEmpty())
            return false;
        return true;
    }


    public boolean accountActivateWithMail(String secketKey){
        try {
            User user = accountActivationService.getUserByKey(secketKey);

            if (user == null){
                log.error("User Null !");
                return false;
            }

            if (user.isState()){
                log.error("Kullanıcı zaten aktif !");
                throw new RuntimeException("Mail adresi zaten onaylanmış !");
            }

            changeState(user.getId(), UserAction.ACTIVATE);

            return true;        //success
        }catch (Exception e){
            if (e instanceof RuntimeException)
                throw e;
            log.error("User Service Account Activate With Secret Error ! -> " + e.getMessage());
            return false;
        }
    }

    public boolean resetPassword(String email){
        try {
            User user = findUserByMail(email);

            if (user == null)
                throw new RuntimeException("Böyle bir kullanıcı bulunamadı !");
            if (user.getRole().getRole().equals("ADMIN"))
                throw new RuntimeException("Böyle bir kullanıcı bulunamadı !");
            if (!user.isState())
                throw new RuntimeException("Önce hesabınızı aktifleştirmelisiniz !");

            String rndPass = accountActivationService.generateRandomPassword(PASSLENGTH);
            user.setPassword(bCryptPasswordEncoder.encode(rndPass));
            userRepository.save(user);

            boolean res = mailService.syncSendMail(SyncMailType.RESETPASS, user, user, rndPass);

            if ( res )
                return true;
            else
                throw new RuntimeException("İşleminiz şu anda gerçekleştirilemedi. Lürfen daha sonra tekrar deneyin.");

        }catch (Exception e){
            if ( e instanceof RuntimeException)
                throw e;
            log.error("User Service Reset Password Error -> " + e.getMessage());
            throw new RuntimeException("İşleminiz şu anda gerçekleştirilemedi. Lürfen daha sonra tekrar deneyin.");
        }
    }

    public void changePassword(ChangePasswordDTO changePasswordDTO) throws LoginException{
        try {
            changePasswordDtoValidation(changePasswordDTO);

            User user = getAuthUser();


            if (!bCryptPasswordEncoder.matches(changePasswordDTO.getCurrentPass(), user.getPassword())){
                throw new RuntimeException("Eski şifrenizi hatalı girdiniz !");
            }

            user.setPassword(bCryptPasswordEncoder.encode(changePasswordDTO.getNewPass()));
            userRepository.save(user);
        }catch (Exception e){
            if ( e instanceof RuntimeException)
                throw e;
            else {
                log.error("User Service Reset Password Error -> " + e.getMessage());
                throw new RuntimeException("İşleminiz şu anda gerçekleştirilemedi. Lütfen daha sonra tekrar deneyin.");
            }
        }
    }

    private void changePasswordDtoValidation(ChangePasswordDTO changePasswordDTO){
        if (isNullOrEmpty(changePasswordDTO.getCurrentPass()) || isNullOrEmpty(changePasswordDTO.getNewPass())
                || isNullOrEmpty(changePasswordDTO.getNewPassAgain())){
            throw new RuntimeException("Tüm alanları doldurmalısınız !");
        }else if(!changePasswordDTO.getNewPass().equals(changePasswordDTO.getNewPassAgain())){
            throw new RuntimeException("Şifreleriniz uyuşmuyor !");
        }else if(changePasswordDTO.getNewPass().length()<PASSLENGTH){
            throw new RuntimeException("Yeni şifreniz 5 karakter veya daha uzun olmalıdır !");
        }
    }
}
