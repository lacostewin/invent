package com.example.webapp.controller;
import com.example.webapp.config.LdapSearch;
import com.example.webapp.domain.*;
import com.example.webapp.repos.*;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.pdf.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Streamable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.ldap.userdetails.LdapUserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.awt.Rectangle;
import java.io.*;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.stream.Stream;

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

    String CA = "regions-NSK-DC1-CA";

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
            String ownerth = username.split("\\,")[0].split("=")[1];
            model.put("greet", ownerth);
            messages = messageRepo.findByOwner(ownerth);
            model.put("messages", messages);
            return "ownthing.html";
        } else {
            if (auth != null && auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_INVENTADMIN"))) {
                Object sAMAccountName = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
                String sAMAccName = ((UserDetails) sAMAccountName).getUsername();
                Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
                String username = ((LdapUserDetails) principal).getDn();
                String ownerth = username.split("\\,")[0].split("=")[1];
                model.put("greet", ownerth);
                messages = messageRepo.findByOwner(ownerth);
                model.put("messages", messages);
                return "ownthing.html";
            }
        }
        return "ownthing.html";
    }

    //  Прохождение инвентаризации своих ТМЦ
    @PostMapping("/")
    public String confirms (
            @RequestParam(name = "checkboxName", required = false)String[] checkboxValue, Map<String, Object> model) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = ((LdapUserDetails) principal).getDn();
        String ownerth = username.split("\\,")[0].split("=")[1];
        model.put("greet", ownerth);
        Streamable<Message> messages;
        messages = messageRepo.findByOwner(ownerth);
        model.put("messages", messages);
        if (checkboxValue != null) {
            System.out.println("checkbox is checked");
            return "redirect:/";
        }
        else {
            messages = messageRepo.findByOwner(ownerth);
            model.put("messages", messages);
            model.put("greet", ownerth);
            model.put("error", "Нужно выбрать все ТМЦ. Если у Вас нет каких-либо ТМЦ свяжитесь с назначившим.");
            return "ownthing.html";
        }
    }

    // Выводим все ТМЦ на страницу
    @GetMapping("/main")
    public String main (Map <String, Object> model) {
        LdapSearch app = new LdapSearch();
        List<String> list = app.getAllPersonNames();
        model.put("list", list);
        Iterable<Message> messages = messageRepo.findAll();
        model.put("messages", messages);
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = ((LdapUserDetails) principal).getDn();
        String ownerth = username.split("\\,")[0].split("=")[1];
        model.put("greet", ownerth);
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
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = ((LdapUserDetails) principal).getDn();
        String ownerth = username.split("\\,")[0].split("=")[1];
        model.put("greet", ownerth);
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
        journal.setAuthor(author);
        journal.setDate(Calendar.getInstance().getTime());
        journalAdd.save(journal);
        Message messages = new Message (owner, id, sn, text, author, invid);
        messageModify.save(messages);
        model.put("messages", messages);
        LdapSearch app = new LdapSearch();
        List<String> list = app.getAllPersonNames();
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = ((LdapUserDetails) principal).getDn();
        String ownerth = username.split("\\,")[0].split("=")[1];
        model.put("greet", ownerth);
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
        journal.setAuthor(author);
        journal.setDate(Calendar.getInstance().getTime());
        journalAdd.save(journal);
        Message messages = new Message (owner, id, sn, text, author, invid);
        messageModify.save(messages);
        model.put("messages", messages);
        LdapSearch app = new LdapSearch();
        List<String> list = app.getAllPersonNames();
        model.put("list", list);
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = ((LdapUserDetails) principal).getDn();
        String ownerth = username.split("\\,")[0].split("=")[1];
        model.put("greet", ownerth);
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
        if (text != null && !text.isEmpty() & owner != null && !owner.isEmpty() & invid != null && !invid.isEmpty()) {
            if (!text.matches("^[0-9].*$")) {
                if (!messageRepo.existsMessageByInvidIgnoreCase(invid)) {
                    messageRepo.save(message);
                    LdapSearch app = new LdapSearch();
                    List<String> list = app.getAllPersonNames();
                    model.put("list", list);
                    Iterable<Message> messages = messageRepo.findAll();
                    model.put("messages", messages);
                    Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
                    String username = ((LdapUserDetails) principal).getDn();
                    String ownerth = username.split("\\,")[0].split("=")[1];
                    model.put("greet", ownerth);
                } else
                model.put("error", "Такой номер уже существует!");
                LdapSearch app = new LdapSearch();
                List<String> list = app.getAllPersonNames();
                model.put("list", list);
                Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
                String username = ((LdapUserDetails) principal).getDn();
                String ownerth = username.split("\\,")[0].split("=")[1];
                model.put("greet", ownerth);
            } else
            model.put("error", "ТМЦ не должно начинаться с цифры");
            Iterable<Message> messages = messageRepo.findAll();
            model.put("messages", messages);
            LdapSearch app = new LdapSearch();
            List<String> list = app.getAllPersonNames();
            model.put("list", list);
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            String username = ((LdapUserDetails) principal).getDn();
            String ownerth = username.split("\\,")[0].split("=")[1];
            model.put("greet", ownerth);
        } else {
            model.put("error", "Заполните все поля!");
            Iterable<Message> messages = messageRepo.findAll();
            model.put("messages", messages);
            LdapSearch app = new LdapSearch();
            List<String> list = app.getAllPersonNames();
            model.put("list", list);
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            String username = ((LdapUserDetails) principal).getDn();
            String ownerth = username.split("\\,")[0].split("=")[1];
            model.put("greet", ownerth);
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
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            String username = ((LdapUserDetails) principal).getDn();
            String ownerth = username.split("\\,")[0].split("=")[1];
            model.put("greet", ownerth);
        } else {
            LdapSearch app = new LdapSearch();
            List<String> list = app.getAllPersonNames();
            model.put("list", list);
            Iterable<Message> messages2 = messageRepo.findAll();
            model.put("message", messages2);
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            String username = ((LdapUserDetails) principal).getDn();
            String ownerth = username.split("\\,")[0].split("=")[1];
            model.put("greet", ownerth);
            return "redirect:/search";
        }
        LdapSearch app = new LdapSearch();
        List<String> list = app.getAllPersonNames();
        model.put("list", list);
        model.put("messages", messages);
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = ((LdapUserDetails) principal).getDn();
        String ownerth = username.split("\\,")[0].split("=")[1];
        model.put("greet", ownerth);
        return "/search";
    }


// Удаление ТМЦ из БД основная форма.
@PostMapping("/remove")
public String remove (@RequestParam Long id,
                       @RequestParam String invid,
                       MultipartFile file, HttpServletResponse remform, Map<String, Object> model) throws IOException{
    Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    String username = ((LdapUserDetails) principal).getDn();
    String ownerth = username.split("\\,")[0].split("=")[1];
    try {
        if (!file.isEmpty()) {
            CertificateFactory fac = CertificateFactory.getInstance("X509");
            X509Certificate cert = (X509Certificate) fac.generateCertificate(file.getInputStream());
            if (cert.getIssuerX500Principal().toString().split("\\,")[0].split("=")[1].equals(CA)) {
                if (System.currentTimeMillis() < cert.getNotAfter().getTime()) {
                    Message messagedel = new Message(id);



                    /// Создание формы акта списания (доработать!)
                    Streamable<Message> messag;
                    messag = messageRepo.findByInvid(invid);

                    final String FONT = "fonts/segoeuisl.ttf";
                    BaseFont bf = BaseFont.createFont(FONT, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
                    Font font = new Font(bf, 12, Font.NORMAL);

                    remform.setContentType("application/pdf");
                    String headerKey = "Content-Disposition";
                    String headerValue = "attachment; filename=destroy.pdf";
                    remform.setHeader(headerKey, headerValue);

                    Document document = new Document(PageSize.A4);
                    PdfWriter.getInstance(document, remform.getOutputStream());

                    document.open();

                    document.add(new Paragraph("Акт на списание материалов №  от  г.", font));

                    PdfPTable table = new PdfPTable(3);
                    table.setWidthPercentage(100);
                    table.setSpacingBefore(10);
                    table.setSpacingAfter(10);
                    Stream.of("Наименование", "Инв. №", "Инициатор")
                            .forEach(columnTitle -> {
                                PdfPCell head = new PdfPCell();
                                head.setBackgroundColor(Color.ORANGE);
                                head.setBorderWidth(1);
                                head.setPadding(5);
                                head.setHorizontalAlignment(1);
                                head.setPhrase(new Phrase(columnTitle, font));
                                table.addCell(head);
                            });
                    for (Message mess : messag) {
                        table.addCell(mess.getText());
                        table.addCell(mess.getInvid());
                        table.addCell(ownerth);
                    }
                    document.add(table);
                    document.close();
//////////////


                    model.put("greet", ownerth);
                    messageRepo.deleteById(id);
                    journalFind.deleteByMessageid(id);
                    model.put("messages", messagedel);
//                        System.out.println("Author: " + cert.getSubjectDN().toString().split("\\,")[1].split("=")[1]);
//                        System.out.println("SN: " + cert.getSerialNumber().toString(16));
                    return "redirect:/main";
                } else {
                    LdapSearch app = new LdapSearch();
                    List<String> list = app.getAllPersonNames();
                    model.put("list", list);
                    Iterable<Message> messages = messageRepo.findAll();
                    model.put("messages", messages);
                    model.put("greet", ownerth);
                    model.put("error", "Сертификат просрочен!");
                    return "/search.html";
                }
            } else {
                LdapSearch app = new LdapSearch();
                List<String> list = app.getAllPersonNames();
                model.put("list", list);
                Iterable<Message> messages = messageRepo.findAll();
                model.put("messages", messages);
                model.put("greet", ownerth);
                model.put("error", "Сертификат выдан не НП ИВЦ!");
                return "/search.html";
            }
        } else {
            LdapSearch app = new LdapSearch();
            List<String> list = app.getAllPersonNames();
            model.put("list", list);
            Iterable<Message> messages = messageRepo.findAll();
            model.put("messages", messages);
            model.put("greet", ownerth);
            model.put("error", "Сертификат не выбран.");
            return "/search.html";
        }
    } catch (CertificateException e) {
        LdapSearch app = new LdapSearch();
        List<String> list = app.getAllPersonNames();
        model.put("list", list);
        Iterable<Message> messages = messageRepo.findAll();
        model.put("messages", messages);
        model.put("greet", ownerth);
        model.put("error", "Выбран не сертификат.");
        return "/search.html";
    }
}


//  Удаление ТМЦ из БД форма поиска.
    @PostMapping("/removes")
    public String removes (@RequestParam Long id,
                           @RequestParam String invid,
                           MultipartFile file, HttpServletResponse remform, Map<String, Object> model) throws IOException, DocumentException{
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = ((LdapUserDetails) principal).getDn();
        String ownerth = username.split("\\,")[0].split("=")[1];
        try {
            if (!file.isEmpty()) {
                CertificateFactory fac = CertificateFactory.getInstance("X509");
                X509Certificate cert = (X509Certificate) fac.generateCertificate(file.getInputStream());
                if (cert.getIssuerX500Principal().toString().split("\\,")[0].split("=")[1].equals(CA)) {
                    if (System.currentTimeMillis() < cert.getNotAfter().getTime()) {
                        if (cert.getSubjectDN().toString().split("\\,")[1].split("=")[1].equals(ownerth)) {
                        Message messagedel = new Message(id);


                        Streamable<Message> messag;
                        messag = messageRepo.findByInvid(invid);

                        final String FONT = "fonts/segoeuisl.ttf";
                        BaseFont bf = BaseFont.createFont(FONT, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
                        Font font = new Font(bf, 12, Font.NORMAL);
                        Font fontsignb = new Font(bf, 11, Font.BOLD);
                        Font fontsignbb = new Font(bf, 11, Font.BOLD, Color.blue);
                        Font fontsignn = new Font(bf, 10, Font.NORMAL);

                        String headerKey = "Content-Disposition";
                        String headerValue = "attachment; filename=destroy.pdf";
                        remform.setContentType("application/pdf");
                        remform.setHeader(headerKey, headerValue);

                        Document document = new Document(PageSize.A4.rotate());
                        PdfWriter writer = PdfWriter.getInstance(document, remform.getOutputStream());

                        final String IMG = "src/main/resources/static/pmhm.png";
                        Image image = Image.getInstance(IMG);
                        image.setAbsolutePosition(208, 48);
                        image.scaleAbsolute(63, 33);

                        document.left(100f);
                        document.top(150f);
                        document.open();
                            SimpleDateFormat formatter= new SimpleDateFormat("dd.MM.yyyy");
                            Date date = new Date(System.currentTimeMillis());
                            document.add(new Paragraph("Акт на списание материалов от " + formatter.format(date) + " г.", font));

                        document.add(image);

                        PdfPTable table = new PdfPTable(3);
                        table.setWidthPercentage(100);
                        table.setSpacingBefore(10);
                        table.setSpacingAfter(10);

                        Stream.of("Наименование", "Инв. №", "Инициатор")
                                .forEach(columnTitle -> {
                                    PdfPCell head = new PdfPCell();
                                    head.setBackgroundColor(Color.ORANGE);
                                    head.setBorderWidth(1);
                                    head.setPadding(5);
                                    head.setHorizontalAlignment(1);
                                    head.setPhrase(new Phrase(columnTitle, font));
                                    table.addCell(head);
                                });
                        for (Message mess : messag) {
                            table.addCell(mess.getText());
                            table.addCell(mess.getInvid());
                            table.addCell(ownerth);
                        }
                        document.add(table);

                        PdfContentByte cb = writer.getDirectContent();

                            Rectangle rect = new Rectangle();

                            cb.roundRectangle(
                                rect.x + 195f,
                                rect.y + 85f,
                                rect.width + 270,
                                rect.height - 80, -10
                        );

                        ColumnText ct1 = new ColumnText(cb);
                        ct1.setSimpleColumn(290, -5, 410, 86);
                        ct1.addElement(new Paragraph("Документ подписан\nэлектронной подписью", fontsignb));
                        ColumnText ct2 = new ColumnText(cb);
                        ct2.setSimpleColumn(204, -10, 410, 51);
                        ct2.addElement(new Paragraph("Владелец: " + cert.getSubjectDN().toString().split("\\,")[1].split("=")[1], fontsignbb));
                        ColumnText ct3 = new ColumnText(cb);
                        ct3.setSimpleColumn(204, -25, 490, 36);
                        ct3.addElement(new Paragraph("Сертификат: " + cert.getSerialNumber().toString(16), fontsignn));
                        ColumnText ct4 = new ColumnText(cb);
                        ct4.setSimpleColumn(204, -30, 580, 24);
                        Date notBefore = cert.getNotBefore();
                        Date notAfter = cert.getNotAfter();
                        SimpleDateFormat dateFor = new SimpleDateFormat("dd.MM.yyyy");
                        ct4.addElement(new Paragraph("Действителен с : " + dateFor.format(notBefore) + " по " + dateFor.format(notAfter), fontsignn));
                        ct1.go();
                        ct2.go();
                        ct3.go();
                        ct4.go();
                        cb.stroke();
                        document.close();

                        model.put("greet", ownerth);
                        messageRepo.deleteById(id);
                        journalFind.deleteByMessageid(id);
                        model.put("messages", messagedel);
                        return "redirect:/search";

                        } else {
                            LdapSearch app = new LdapSearch();
                            List<String> list = app.getAllPersonNames();
                            model.put("list", list);
                            Iterable<Message> messages = messageRepo.findAll();
                            model.put("messages", messages);
                            model.put("greet", ownerth);
                            model.put("error", "Этот сертификат выдан не Вам!");
                            return "/search.html";
                        }

                    } else {
                        LdapSearch app = new LdapSearch();
                        List<String> list = app.getAllPersonNames();
                        model.put("list", list);
                        Iterable<Message> messages = messageRepo.findAll();
                        model.put("messages", messages);
                        model.put("greet", ownerth);
                        model.put("error", "Сертификат просрочен!");
                        return "/search.html";
                    }
                } else {
                    LdapSearch app = new LdapSearch();
                    List<String> list = app.getAllPersonNames();
                    model.put("list", list);
                    Iterable<Message> messages = messageRepo.findAll();
                    model.put("messages", messages);
                    model.put("greet", ownerth);
                    model.put("error", "Сертификат выдан не НП ИВЦ!");
                    return "/search.html";
                }
            } else {
                LdapSearch app = new LdapSearch();
                List<String> list = app.getAllPersonNames();
                model.put("list", list);
                Iterable<Message> messages = messageRepo.findAll();
                model.put("messages", messages);
                model.put("greet", ownerth);
                model.put("error", "Сертификат не выбран.");
                return "/search.html";
            }
        } catch (CertificateException e) {
            LdapSearch app = new LdapSearch();
            List<String> list = app.getAllPersonNames();
            model.put("list", list);
            Iterable<Message> messages = messageRepo.findAll();
            model.put("messages", messages);
            model.put("greet", ownerth);
            model.put("error", "Выбран не сертификат.");
            return "/search.html";
        }
    }
}