package com.example.webapp.controller;
import com.example.webapp.config.LdapSearch;
import com.example.webapp.domain.Message;
import com.example.webapp.repos.MessageRepo;
import com.example.webapp.repos.MessageRepo2;
import com.example.webapp.repos.MessageModify;
import com.example.webapp.repos.MessageDel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Streamable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@Controller
public class MainController {
    @Autowired
    private MessageModify messageModify;
    @Autowired
    private MessageRepo messageRepo;
    @Autowired
    private MessageRepo2 messageRepo2;
    @Autowired
    private MessageDel messageDel;
    private LdapSearch ldapSearch;

    public MainController() {
    }

    // Своя 403
    @GetMapping ("/403")
    public String error403() {
        return "403";
    }

//    // Своя Home
//    @GetMapping ("/home")
//    public String home() {
//        return "home";
//    }



    // Редирект с /
    @GetMapping("/")
    public String index(Map <String, Object> model) {
        LdapSearch app = new LdapSearch();
        List<String> list = app.getAllPersonNames();
        model.put("list", list);
        return "redirect:/main";
    }

// Выводим все ТМЦ на страницу
    @GetMapping("/main")
    public String main (Map <String, Object> model) {
        LdapSearch app = new LdapSearch();
        List<String> list = app.getAllPersonNames();
        model.put("list", list);
        Iterable<Message> messages = messageRepo2.findAll();
        model.put("messages", messages);

        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            Collection<? extends GrantedAuthority> username = ((UserDetails)principal).getAuthorities();
            System.out.println(username);
        } else {
            String username = principal.toString();
            System.out.println(username);
        }

        return "main";
    }

// Редактирование владельца ТМЦ путём выбора list из списка основная форма
    @PostMapping("/update")
    public String modifyowner (
            @RequestParam String owner,
            @RequestParam String sn,
            @RequestParam String text,
            @RequestParam Integer id,
            Map<String, Object> model) {
        Message messages = new Message (owner, id, sn, text);
        messageModify.save(messages);
        model.put("messages", messages);
        LdapSearch app = new LdapSearch();
        List<String> list = app.getAllPersonNames();
        model.put("list", list);
        return "redirect:/main";
    }

// Редактирование владельца ТМЦ путём выбора list из списка форма поиска
    @PostMapping("/updates")
    public String modifyowners (
            @RequestParam String owner,
            @RequestParam String sn,
            @RequestParam String text,
            @RequestParam Integer id,
            Map<String, Object> model) {
        Message messages = new Message (owner, id, sn, text);
        messageModify.save(messages);
        model.put("messages", messages);
        LdapSearch app = new LdapSearch();
        List<String> list = app.getAllPersonNames();
        model.put("list", list);
        return "redirect:/search";
    }

// Проверка на корректность заполненния полей описания ТМЦ и добавление ТМЦ.
    @PostMapping("/main")
    public String add (
            @RequestParam String owner,
            @RequestParam String text,
            @RequestParam String sn, Map<String, Object> model) {
        Message message = new Message (text, sn, owner);
        if (text != null && !text.isEmpty() & sn != null && !sn.isEmpty() & owner != null && !owner.isEmpty()) {
            if (!text.matches("^[0-9].*$")) {
                if (!messageRepo2.existsMessageBySnIgnoreCase(sn)) {
                    LdapSearch app = new LdapSearch();
                    List<String> list = app.getAllPersonNames();
                    model.put("list", list);
                    messageRepo2.save(message);
                    Iterable<Message> messages = messageRepo2.findAll();
                    model.put("messages", messages);
                } else
                    model.put("error", "Такой серийный номер уже существует!");
                LdapSearch app = new LdapSearch();
                List<String> list = app.getAllPersonNames();
                model.put("list", list);
            } else
                model.put("error", "ТМЦ не должно начинаться с цифры");
            Iterable<Message> messages = messageRepo2.findAll();
            model.put("messages", messages);
            LdapSearch app = new LdapSearch();
            List<String> list = app.getAllPersonNames();
            model.put("list", list);
        } else {
            model.put("error", "Заполните все поля!");
            Iterable<Message> messages = messageRepo2.findAll();
            model.put("messages", messages);
            LdapSearch app = new LdapSearch();
            List<String> list = app.getAllPersonNames();
            model.put("list", list);
        }
        return "main";
    }
// Выводим все ТМЦ на страницу
    @GetMapping("/search")
    public String search(Map<String, Object> model) {
        LdapSearch app = new LdapSearch();
        List<String> list = app.getAllPersonNames();
        model.put("list", list);
        Iterable<Message> messages = messageRepo.findAll();
        model.put("messages", messages);
        return "/search";
    }

// Поиск по серийному номеру, названию ТМЦ и владельцу.
    @PostMapping("/search")
    public String search (String searchsn, Map<String, Object> model) {
        Streamable<Message> messages;
        if (searchsn != null && !searchsn.isEmpty()) {
            messages = messageRepo.findBySnContainingIgnoreCase(searchsn).and(messageRepo.findByTextContainingIgnoreCase(searchsn)).and(messageRepo.findByOwnerContainingIgnoreCase(searchsn));
            LdapSearch app = new LdapSearch();
            List<String> list = app.getAllPersonNames();
            model.put("list", list);
        } else {
            LdapSearch app = new LdapSearch();
            List<String> list = app.getAllPersonNames();
            model.put("list", list);
            Iterable<Message> messages2 = messageRepo2.findAll();
            model.put("message", messages2);
            return "redirect:/search";
        }
        LdapSearch app = new LdapSearch();
        List<String> list = app.getAllPersonNames();
        model.put("list", list);
        model.put("messages", messages);
        return "/search";
    }

// Удаление ТМЦ из БД основная форма.
    @PostMapping("/remove")
    public String remove (@RequestParam String sn, Map<String, Object> model) {
        Message messagedel = new Message (sn);
        messageDel.deleteBySn(sn);
        model.put("messages", messagedel);
        return "redirect:/main";
    }

//  Удаление ТМЦ из БД форма поиска.
    @PostMapping("/removes")
    public String removes (@RequestParam String sn, Map<String, Object> model) {
        Message messagedel = new Message (sn);
        messageDel.deleteBySn(sn);
        model.put("messages", messagedel);
        return "redirect:/search";
    }
}