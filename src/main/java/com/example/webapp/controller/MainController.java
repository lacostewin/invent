package com.example.webapp.controller;
import com.example.webapp.config.LdapSearch;
import com.example.webapp.domain.*;
import com.example.webapp.repos.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Streamable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.ldap.userdetails.LdapUserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
public class MainController {
    @Autowired
    private MessageModify messageModify;
    @Autowired
    private MessageRepo messageRepo;
    private LdapSearch ldapSearch;
    @Autowired
    private JournalAdd journalAdd;
    @Autowired
    private JournalFind journalFind;

    public MainController() {
    }

    // Своя 403
    @GetMapping ("/403")
    public String error403() {
        return "403";
    }

//  Проверяем на принадлежность к группе и деректим на page где все ТМЦ закреплённые за аутентифицированным сотрудником
    @GetMapping("/")
    public String searchown(Map<String, Object> model) {
        Streamable<Message> messages;
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_INVENTUSER"))) {
            Object sAMAccountName = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            String sAMAccName = ((UserDetails) sAMAccountName).getUsername();
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            String username = ((LdapUserDetails) principal).getDn();
            String owner = username.split("\\,")[0].split("=")[1];
            model.put("greet", owner);
            messages = messageRepo.findByOwner(owner);
            model.put("messages", messages);
            return "ownthing.html";
        }
        return "ownthing.html";
    }

    // Выводим все ТМЦ на страницу
    @GetMapping("/main")
    public String main (Map <String, Object> model) {
        LdapSearch app = new LdapSearch();
        List<String> list = app.getAllPersonNames();
        model.put("list", list);
        Iterable<Message> messages = messageRepo.findAll();
        model.put("messages", messages);
        model.put("greet", GetDn.displayName);
        return "main";
    }

    // Выводим все ТМЦ на страницу поиска
    @GetMapping("/search")
    public String search(Map<String, Object> model) {
        LdapSearch app = new LdapSearch();
        List<String> list = app.getAllPersonNames();
        model.put("list", list);
        Iterable<Message> messages = messageRepo.findAll();
        model.put("messages", messages);
        model.put("greet", GetDn.displayName);

        return "/search";
    }

    @PostMapping("/hist")
    public String hist(@RequestParam Long id,
                       @RequestParam String sn,
                       @RequestParam String text,
                       @RequestParam String invid,
                       Map<String, Object> model) {
        Message messages = new Message (id, sn, text, invid);
        model.put("messages", messages);
        Iterable<Journal> journal = journalFind.findByMessageid(id);
        model.put("journal", journal);
        return "hist";
    }

// Редактирование владельца ТМЦ путём выбора list из списка основная форма
    @PostMapping("/update")
    public String modifyowner (
            @RequestParam String owner,
            @RequestParam String sn,
            @RequestParam String text,
            @RequestParam String invid,
            @RequestParam Long id,
            @RequestParam String author,
            Map<String, Object> model) {

        Journal journal = new Journal();
        journal.setMessageid(id);
        journal.setNewowner(owner);
        journal.setDate(Calendar.getInstance().getTime());
        journalAdd.save(journal);

        Message messages = new Message (owner, id, sn, text, author, invid);
        messageModify.save(messages);
        model.put("messages", messages);
        LdapSearch app = new LdapSearch();
        List<String> list = app.getAllPersonNames();
        model.put("greet", GetDn.displayName);
        model.put("list", list);
        return "redirect:/main";
    }


// Редактирование владельца ТМЦ путём выбора list из списка форма поиска
    @PostMapping("/updates")
    public String modifyowners (
            @RequestParam String owner,
            @RequestParam String sn,
            @RequestParam String text,
            @RequestParam Long id,
            @RequestParam String invid,
            @RequestParam String author,
            Map<String, Object> model) {

        Journal journal = new Journal();
        journal.setMessageid(id);
        journal.setNewowner(owner);
        journal.setDate(Calendar.getInstance().getTime());
        journalAdd.save(journal);

        Message messages = new Message (owner, id, sn, text, author, invid);
        messageModify.save(messages);
        model.put("messages", messages);
        LdapSearch app = new LdapSearch();
        List<String> list = app.getAllPersonNames();
        model.put("list", list);
        model.put("greet", GetDn.displayName);
        return "redirect:/search";
    }

// Проверка на корректность заполненния полей описания ТМЦ и добавление ТМЦ.
    @PostMapping("/main")
    public String add (
            @RequestParam String owner,
            @RequestParam String text,
            @RequestParam String sn,
            @RequestParam String invid,
            @RequestParam String author, Map<String, Object> model) {
        Message message = new Message (text, sn, owner, author, invid);
        if (text != null && !text.isEmpty() & sn != null && !sn.isEmpty() & owner != null && !owner.isEmpty() & invid != null && !invid.isEmpty()) {
            if (!text.matches("^[0-9].*$")) {
                if (!messageRepo.existsMessageBySnIgnoreCase(sn) & !messageRepo.existsMessageByInvidIgnoreCase(invid)) {
                    messageRepo.save(message);
                    LdapSearch app = new LdapSearch();
                    List<String> list = app.getAllPersonNames();
                    model.put("list", list);
                    Iterable<Message> messages = messageRepo.findAll();
                    model.put("messages", messages);
                    model.put("greet", GetDn.displayName);
                } else
                    model.put("error", "Такой номер уже существует!");
                LdapSearch app = new LdapSearch();
                List<String> list = app.getAllPersonNames();
                model.put("list", list);
                model.put("greet", GetDn.displayName);
            } else
            model.put("error", "ТМЦ не должно начинаться с цифры");
            Iterable<Message> messages = messageRepo.findAll();
            model.put("messages", messages);
            LdapSearch app = new LdapSearch();
            List<String> list = app.getAllPersonNames();
            model.put("list", list);
            model.put("greet", GetDn.displayName);
        } else {
            model.put("error", "Заполните все поля!");
            Iterable<Message> messages = messageRepo.findAll();
            model.put("messages", messages);
            LdapSearch app = new LdapSearch();
            List<String> list = app.getAllPersonNames();
            model.put("list", list);
            model.put("greet", GetDn.displayName);
        }
        return "main";
    }

// Поиск по серийному номеру, названию ТМЦ и владельцу.
    @PostMapping("/search")
    public String search (String searchsn, Map<String, Object> model) {
        Streamable<Message> messages;
        if (searchsn != null && !searchsn.isEmpty()) {
            messages = messageRepo.findBySnContainingIgnoreCase(searchsn)
                    .and(messageRepo.findByTextContainingIgnoreCase(searchsn))
                    .and(messageRepo.findByOwnerContainingIgnoreCase(searchsn))
                    .and(messageRepo.findByInvidContainingIgnoreCase(searchsn));
            LdapSearch app = new LdapSearch();
            List<String> list = app.getAllPersonNames();
            model.put("list", list);
            model.put("greet", GetDn.displayName);
        } else {
            LdapSearch app = new LdapSearch();
            List<String> list = app.getAllPersonNames();
            model.put("list", list);
            Iterable<Message> messages2 = messageRepo.findAll();
            model.put("message", messages2);
            model.put("greet", GetDn.displayName);
            return "redirect:/search";
        }
        LdapSearch app = new LdapSearch();
        List<String> list = app.getAllPersonNames();
        model.put("list", list);
        model.put("messages", messages);
        model.put("greet", GetDn.displayName);
        return "/search";
    }

// Удаление ТМЦ из БД основная форма.
    @PostMapping("/remove")
    public String remove (@RequestParam String sn, @RequestParam Long id, Map<String, Object> model) {
        Message messagedel = new Message (sn);
        messageRepo.deleteBySn(sn);
        journalFind.deleteByMessageid(id);
        model.put("messages", messagedel);
        model.put("greet", GetDn.displayName);
        return "redirect:/main";
    }

//  Удаление ТМЦ из БД форма поиска.
    @PostMapping("/removes")
    public String removes (@RequestParam String sn, @RequestParam Long id, Map<String, Object> model) {
        Message messagedel = new Message (sn);
        messageRepo.deleteBySn(sn);
        journalFind.deleteByMessageid(id);
        model.put("messages", messagedel);
        model.put("greet", GetDn.displayName);
        return "redirect:/search";
    }



}