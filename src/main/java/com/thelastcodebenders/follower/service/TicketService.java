package com.thelastcodebenders.follower.service;

import com.thelastcodebenders.follower.assembler.MessageAssembler;
import com.thelastcodebenders.follower.assembler.TicketAssembler;
import com.thelastcodebenders.follower.dto.CreateTicketFormDTO;
import com.thelastcodebenders.follower.dto.tickets.UserTicket;
import com.thelastcodebenders.follower.enums.MailType;
import com.thelastcodebenders.follower.enums.RoleType;
import com.thelastcodebenders.follower.model.Message;
import com.thelastcodebenders.follower.model.Ticket;
import com.thelastcodebenders.follower.model.User;
import com.thelastcodebenders.follower.repository.MessageRepository;
import com.thelastcodebenders.follower.repository.TicketRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.security.auth.login.LoginException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class TicketService {
    private static final Logger log = LoggerFactory.getLogger(TicketService.class);

    private TicketRepository ticketRepository;
    private UserService userService;
    private TicketAssembler ticketAssembler;
    private MessageAssembler messageAssembler;
    private MessageRepository messageRepository;
    private MailService mailService;

    public TicketService(TicketRepository ticketRepository,
                         UserService userService,
                         TicketAssembler ticketAssembler,
                         MessageRepository messageRepository,
                         MessageAssembler messageAssembler,
                         MailService mailService){
        this.ticketRepository = ticketRepository;
        this.userService = userService;
        this.ticketAssembler = ticketAssembler;
        this.messageAssembler = messageAssembler;
        this.messageRepository = messageRepository;
        this.mailService = mailService;
    }

    public int unreadTickets(RoleType roleType) throws LoginException {
        if (roleType == RoleType.ADMIN){
            List<Ticket> activeTickets = ticketRepository.findByClosed(false);

            int unreadcount = 0;

            for (Ticket ticket: activeTickets) {
                if (ticket.isClosed() == false){
                    List<Message> messages = messageRepository.findByTicket(ticket);
                    if (!messages.isEmpty()){
                        Message message = messages.get(messages.size()-1);
                        if (message.isUser())
                            unreadcount++;
                    }
                }
            }

            return unreadcount;
        }else if (roleType == RoleType.USER){
            List<Ticket> activeTickets = ticketRepository.findByFromUserAndClosed(userService.getAuthUser(), false);
            int unreadcount = 0;

            for (Ticket ticket: activeTickets) {
                if (ticket.isClosed() == false){
                    List<Message> messages = messageRepository.findByTicket(ticket);
                    if (!messages.isEmpty()){
                        Message message = messages.get(messages.size()-1);
                        if (!message.isUser())
                            unreadcount++;
                    }
                }
            }

            return unreadcount;
        }else {
            return 00;
        }
    }

    public Ticket findTicketById(long ticketId){
        try{
            Optional<Ticket> opt = ticketRepository.findById(ticketId);
            if (!opt.isPresent()){
                log.error("Ticket Service Find Ticket By Id Error !");
                return null;
            }else
                return opt.get();
        }catch (Exception e){
            log.error("Ticket Service Find Ticket By Id Error -> " + e.getMessage());
            return null;
        }
    }

    public List<UserTicket> allAdminTicket(){
        List<Ticket> tickets = ticketRepository.findAll();
        List<UserTicket> userTickets = new ArrayList<>();

        for (Ticket ticket: tickets) {
            UserTicket userTicket = new UserTicket();
            userTicket.setTicket(ticket);

            List<Message> messages = messageRepository.findByTicket(ticket);

            Message lastMessage = messages.get(messages.size()-1);

            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm");
            userTicket.setLastChange(lastMessage.getDate().format(dateTimeFormatter));

            if (!lastMessage.isUser())
                userTicket.setAnswer(false);
            else
                userTicket.setAnswer(true);

            userTickets.add(userTicket);
        }
        return userTickets;
    }

    public UserTicket oneTicket(long ticketId){
        UserTicket userTicket = new UserTicket();
        Ticket ticket = findTicketById(ticketId);
        userTicket.setTicket(ticket);

        if (ticket == null)
            return null;

        List<Message> messages = messageRepository.findByTicket(ticket);
        userTicket.setMessages(messages);
        return userTicket;
    }

    public List<UserTicket> allUserTicket() throws LoginException {
        List<Ticket> tickets = ticketRepository.findByFromUser(userService.getAuthUser());

        List<UserTicket> userTickets = new ArrayList<>();

        for (Ticket ticket: tickets) {
            UserTicket userTicket = new UserTicket();
            userTicket.setTicket(ticket);

            List<Message> messages = messageRepository.findByTicket(ticket);

            Message lastMessage = messages.get(messages.size()-1);

            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm");
            userTicket.setLastChange(lastMessage.getDate().format(dateTimeFormatter));

            if (lastMessage.isUser())
                userTicket.setAnswer(false);
            else
                userTicket.setAnswer(true);

            userTickets.add(userTicket);
        }
        return userTickets;
    }

    public boolean responseTicket(RoleType roleType, String messageBody, long ticketId){
        Ticket ticket = findTicketById(ticketId);
        //isClosed kontrolü yap
        if (ticket == null)
            return false;

        if (ticket.isClosed()){
            log.error("Ticket Service Response Ticket Error ! -> Ticket is closed.");
            return false;
        }

        if (roleType == RoleType.ADMIN){
            Message message = messageAssembler.convertStringToMessage(messageBody, false, ticket);
            try {
                message = messageRepository.save(message);

                mailService.asynsSendMail(MailType.RESPONSETICKET, ticket.getFromUser(), ticket.getFromUser());
                //burada ortadaki user kullanılmadığı için sallama verilmiştir normalde işlemi yapan admindir.

                return true;
            }catch (Exception e){
                log.error("Ticket Service Response Ticket Error -> " + e.getMessage());
                return false;
            }
        }else if (roleType == RoleType.USER){
            Message message = messageAssembler.convertStringToMessage(messageBody, true, ticket);
            try {
                message = messageRepository.save(message);
                mailService.asynsSendMail(MailType.RESPONSETICKET, ticket.getFromUser(), userService.getAdmin());
                return true;
            }catch (Exception e){
                log.error("Ticket Service Response Ticket Error -> " + e.getMessage());
                return false;
            }
        }else{
            log.error("Ticket Service Response Ticket Error -> Invalid RoleType");
            return false;
        }
    }

    public boolean createTicket(CreateTicketFormDTO createTicketForm) throws LoginException {
        User user = userService.getAuthUser();

        Ticket ticket = ticketAssembler.convertFormDtoToTicket(createTicketForm, user);

        ticket = saveTicket(ticket);

        if (ticket == null)
            return false;

        Message message = messageAssembler.convertStringToMessage(createTicketForm.getMessage(), true, ticket);

        message = saveMessage(message);

        if (message == null){
            ticketRepository.deleteById(ticket.getId());
            return false;
        }

        User admin = userService.getAdmin();

        if (admin == null){
            log.error("Cant Send Mail Because Admin Not Found !");
        }else {
            mailService.asynsSendMail(MailType.CREATETICKET, user, admin);
        }

        return true;
    }

    public boolean closeTicket(long ticketId){
        Ticket ticket = findTicketById(ticketId);

        if (ticket == null)
            return false;
        ticket.setClosed(true);
        try {
            ticket = ticketRepository.save(ticket);
            if (ticket == null){
                log.error("Ticket Service Close Ticket Error !");
                return false;
            }else
                return true;
        }catch (Exception e){
            log.error("Ticket Service Close Ticket Error -> " + e.getMessage());
            return false;
        }
    }

    public Ticket saveTicket(Ticket ticket){
        try {
            ticket = ticketRepository.save(ticket);
            if (ticket != null)
                return ticket;
            else {
                log.error("Ticket Service Ticket Save Error ! ");
                return null;
            }
        } catch (Exception e){
            log.error("Ticket Service Ticket Save Error ! -> " + e.getMessage());
            return null;
        }
    }

    public Message saveMessage(Message message){
        try {
            message = messageRepository.save(message);
            if (message != null)
                return message;
            else {
                log.error("Ticket Service Message Save Error ! ");
                return null;
            }
        } catch (Exception e){
            log.error("Ticket Service Message Save Error ! -> " + e.getMessage());
            return null;
        }
    }
}
