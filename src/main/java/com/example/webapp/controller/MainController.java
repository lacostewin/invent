package com.example.webapp.controller;
import com.example.webapp.config.LdapSearch;
import com.example.webapp.domain.Message;
import com.example.webapp.repos.MessageRepo;
import com.example.webapp.repos.MessageRepo2;
import com.example.webapp.repos.MessageDel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Streamable;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@Controller
public class MainController {
    @Autowired
    private MessageRepo messageRepo;
    @Autowired
    private MessageRepo2 messageRepo2;
    @Autowired
    private MessageDel messageDel;
    private LdapSearch ldapSearch;

// Редирект с /
    @GetMapping("/")
    public String index() {
        return "redirect:/main";
    }

// Выводим все ТМЦ на страницу
    @GetMapping("/main")
    public String main (Map <String, Object> model) {
        Iterable<Message> messages = messageRepo2.findAll();
        model.put("messages", messages);
        LdapSearch app = new LdapSearch();
        List<String> list = app.getAllPersonNames();
        model.put("list", list);
        return "main";
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
                    messageRepo2.save(message);
                    Iterable<Message> messages = messageRepo2.findAll();
                    model.put("messages", messages);
                    LdapSearch app = new LdapSearch();
                    List<String> list = app.getAllPersonNames();
                    model.put("list", list);
                } else
                    model.put("error", "Такой серийный номер уже существует!");
            } else
                model.put("error", "ТМЦ не должно начинаться с цифры");
            Iterable<Message> messages = messageRepo2.findAll();
            model.put("messages", messages);
        } else {
            model.put("error", "Заполните все поля!");
            Iterable<Message> messages = messageRepo2.findAll();
            model.put("messages", messages);
        }
        return "main";
    }
// Выводим все ТМЦ на страницу
    @GetMapping("/search")
    public String search(Map<String, Object> model) {
        Iterable<Message> messages = messageRepo.findAll();
        model.put("messages", messages);
        return "search";
    }

// Поиск по серийному номеру, названию ТМЦ и владельцу.
    @PostMapping("/search")
    public String search (String searchsn, Map<String, Object> model) {
        Streamable<Message> messages;
        if (searchsn != null && !searchsn.isEmpty()) {
            messages = messageRepo.findBySnContainingIgnoreCase(searchsn).and(messageRepo.findByTextContainingIgnoreCase(searchsn)).and(messageRepo.findByOwnerContainingIgnoreCase(searchsn));
        } else {
            Iterable<Message> messages2 = messageRepo2.findAll();
            model.put("message", messages2);
            return "redirect:/search";
        }
        model.put("messages", messages);
        return "search";
    }

// Удаление ТМЦ из БД.
    @PostMapping("/remove")
    public String remove (@RequestParam String sn, Map<String, Object> model) {
        Message messagedel = new Message (sn);
        messageDel.deleteBySn(sn);
        model.put("messages", messagedel);
        return "redirect:/main";
    }
}