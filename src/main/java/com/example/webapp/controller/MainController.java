package com.example.webapp.controller;

import com.example.webapp.config.GenerateQRCode;
import com.example.webapp.config.LdapSearch;
import com.example.webapp.domain.*;
import com.example.webapp.repos.*;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.pdf.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.util.Streamable;
import org.springframework.http.ContentDisposition;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.ldap.userdetails.LdapUserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.awt.Rectangle;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.stream.Stream;

@Controller
public class MainController extends GenerateQRCode {
    @Autowired
    private MessageModify messageModify;
    @Autowired
    private MessageRepo messageRepo;
    private LdapSearch ldapSearch;
    @Autowired
    private JournalAdd journalAdd;
    @Autowired
    private JournalFind journalFind;
    @Autowired
    private QRModify qrModify;
    @Autowired
    private JavaMailSender javaMailSender;

    public MainController() {
    }

    String CA = "regions-NSK-DC1-CA";

    // ???????? 403
    @GetMapping ("/403")
    public String error403() {
        return "403";
    }

    public void defaultMethodStream(Map<String, Object> model, Streamable<Message> messages, String ownerth) {
        LdapSearch app = new LdapSearch();
        List<String> list = app.getAllPersonNames();
        model.put("list", list);
        model.put("messages", messages);
        model.put("greet", ownerth);
    }

    public void defaultMethodIter(Map<String, Object> model, Iterable<Message> messages, String ownerth) {
        LdapSearch app = new LdapSearch();
        List<String> list = app.getAllPersonNames();
        model.put("list", list);
        model.put("messages", messages);
        model.put("greet", ownerth);
    }

//  ?????????????????? ???? ???????????????????????????? ?? ???????????? ?? ???????????????? ???? page ?????? ?????? ?????? ???????????????????????? ???? ?????????????????????????????????????? ??????????????????????
    @GetMapping("/")
    public String searchown(Map<String, Object> model) {
        Streamable<Message> messages;
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_INVENTUSER"))) {
//            Object sAMAccountName = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
//            String sAMAccName = ((UserDetails) sAMAccountName).getUsername();
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            String username = ((LdapUserDetails) principal).getDn();
            String ownerth = username.split("\\,")[0].split("=")[1];
            model.put("greet", ownerth);
            messages = messageRepo.findByOwner(ownerth);
            model.put("messages", messages);
            return "ownthing.html";
        } else {
            if (auth != null && auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_INVENTADMIN"))) {
//                Object sAMAccountName = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
//                String sAMAccName = ((UserDetails) sAMAccountName).getUsername();
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

//  ?????????????????????? ???????????????????????????? ?????????? ??????
    @PostMapping("/")
    public String confirms (@RequestParam(name = "checkboxName", required = false)
                            String[] checkboxValue,
                            String invid,
                            String subject,
                            String issuer,
                            String from,
                            String till,
                            String certsn,
                            Map<String, Object> model,
                            HttpServletResponse remform) throws DocumentException, IOException, ParseException {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = ((LdapUserDetails) principal).getDn();
        String ownerth = username.split("\\,")[0].split("=")[1];
        Streamable<Message> messages;
        messages = messageRepo.findByOwner(ownerth);

        if (checkboxValue != null && checkboxValue.length == messages.stream().count()) {
            if (issuer != null && !issuer.equals("0")) {
              if (issuer.split("=")[1].equals(CA)) {
                Date certdate = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").parse(till);
                if (System.currentTimeMillis() < certdate.getTime()) {
                    if (subject.split("=")[1].equals(ownerth)) {
                                SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");
                                Date date = new Date(System.currentTimeMillis());
                                Streamable<Message> messag;
                                messag = messageRepo.findByInvid(invid);

                                final String FONT = "fonts/segoeuisl.ttf";
                                BaseFont bf = BaseFont.createFont(FONT, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
                                Font font = new Font(bf, 12, Font.NORMAL);
                                Font fontsignb = new Font(bf, 11, Font.BOLD);
                                Font fontsignbb = new Font(bf, 11, Font.BOLD, Color.blue);
                                Font fontsignn = new Font(bf, 10, Font.NORMAL);

                                String headerKey = "Content-Disposition";

                                    String filenames = "???????????????????????????? " + ownerth + " " + formatter.format(date) + ".pdf";
                                    ContentDisposition contentDisposition = ContentDisposition.builder("attachment")
                                            .filename(filenames, StandardCharsets.UTF_8)
                                            .build();
                                    String headerValue = "attachment; filename=" + contentDisposition;
                                    remform.setContentType("application/pdf");
                                    remform.setHeader(headerKey, headerValue);

                                Document document = new Document(PageSize.A4.rotate());
                                PdfWriter writer = PdfWriter.getInstance(document, remform.getOutputStream());

                                HeaderFooter header = new HeaderFooter(new Phrase("???????????????????????????????????? ?????????????????? ???                              ???? " + formatter.format(date) + " ??.", font), false);
                                document.setHeader(header);
                                HeaderFooter footer = new HeaderFooter(new Phrase("???????????????? "), new Phrase("."));

                                footer.setAlignment(HeaderFooter.ALIGN_RIGHT);
                                footer.setBorder(com.lowagie.text.Rectangle.NO_BORDER);
                                document.setFooter(footer);

                                document.left(100f);
                                document.top(150f);
                                document.open();

                                PdfPTable table = new PdfPTable(4);
                                table.setWidthPercentage(100);
                                table.setSpacingBefore(10);
                                table.setHeaderRows(1);


                                Stream.of("????????????????????????", "??????. ???", "??????. ???", "????????????????")
                                        .forEach(columnTitle -> {
                                            PdfPCell head = new PdfPCell();
                                            head.setBackgroundColor(Color.ORANGE);
                                            head.setBorderWidth(1);
                                            head.setPadding(5);
                                            head.setHorizontalAlignment(1);
                                            head.setPhrase(new Phrase(columnTitle, font));
                                            table.addCell(head);
                                        });
                                for (Message mess : messages) {
                                    table.addCell(mess.getText());
                                    table.addCell(mess.getInvid());
                                    table.addCell(mess.getSn());
                                    table.addCell(ownerth);
                                }
                                document.add(table);

                                PdfContentByte cb = writer.getDirectContent();

                                Rectangle rect = new Rectangle();

                                cb.roundRectangle(
                                        rect.x + 200f,
                                        rect.y + 60f,
                                        rect.width + 480,
                                        rect.height - 55, -10
                                );

                                final String IMG = "src/main/resources/static/pmhm.png";
                                Image image = Image.getInstance(IMG);
                                image.scaleAbsolute(63, 33);

                                ColumnText ct1 = new ColumnText(cb);
                                ct1.setSimpleColumn(280, -10, 410, 51);
                                ct1.addElement(new Paragraph("???????????????? ????????????????\n?????????????????????? ????????????????", fontsignb));
                                ColumnText ct2 = new ColumnText(cb);
                                ct2.setSimpleColumn(410, -10, 800, 60);
                                ct2.addElement(new Paragraph("????????????????: " + subject.split("=")[1], fontsignbb));
                                ColumnText ct3 = new ColumnText(cb);
                                ct3.setSimpleColumn(410, -25, 800, 42);
                                ct3.addElement(new Paragraph("????????????????????: " + certsn, fontsignn));
                                ColumnText ct4 = new ColumnText(cb);
                                ct4.setSimpleColumn(410, -30, 800, 28);
                                ct4.addElement(new Paragraph("???????????????????????? ?? : " + from + " ???? " + till, fontsignn));
                                ColumnText ct5 = new ColumnText(cb);
                                ct5.setSimpleColumn(209, -10, 410, 48);
                                ct5.addElement(image);

                                ct1.go();
                                ct2.go();
                                ct3.go();
                                ct4.go();
                                ct5.go();

                                cb.stroke();

                                document.close();

                                model.put("greet", ownerth);
                                model.put("messages", messages);
                                return "ownthing.html";
                            } else {
                                defaultMethodStream(model, messages, ownerth);
                                model.put("error", "???????? ???????????????????? ?????????? ???? ??????!");
                                return "ownthing.html";
                            }
                        } else {
                            defaultMethodStream(model, messages, ownerth);
                            model.put("error", "???????????????????? ??????????????????!");
                            return "ownthing.html";
                        }
                    } else {
                        defaultMethodStream(model, messages, ownerth);
                        model.put("error", "???????????????????? ?????????? ???????????? ???????????????????????? ?????? ???? ????????????!");
                        return "ownthing.html";
                    }
            } else {
                defaultMethodStream(model, messages, ownerth);
                model.put("error", "???????????????????? ???? ????????????!");
                return "ownthing.html";
            }
        } else {
             messages = messageRepo.findByOwner(ownerth);
             model.put("messages", messages);
             model.put("greet", ownerth);
             model.put("error", "?????????? ?????????????? ?????? ??????. ???????? ?? ?????? ?????? ??????????-???????? ?????? ?????????????????? ?? ??????????????????????.");
             return "ownthing.html";
         }
    }

// ?????????????? ?????? ?????? ???? ????????????????
    @GetMapping("/main")
    public String main (Map <String, Object> model) {
//        LdapSearch app = new LdapSearch();
//        List<String> list = app.getAllPersonNames();
//        model.put("list", list);
        Pageable limit = PageRequest.of(0,10);
        Iterable<Message> messages = messageRepo.findAll(limit);
//        model.put("messages", messages);
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = ((LdapUserDetails) principal).getDn();
        String ownerth = username.split("\\,")[0].split("=")[1];
//        model.put("greet", ownerth);
        defaultMethodIter(model, messages, ownerth);
        return "main";
    }

// ?????????????? ?????? ?????? ???? ???????????????? ????????????
    @GetMapping("/search")
    public String search2 (String searchsn, Map<String, Object> model) {
//        LdapSearch app = new LdapSearch();
//        List<String> list = app.getAllPersonNames();
//        model.put("list", list);
        Pageable limit = PageRequest.of(0,10);
        Iterable<Message> messages = messageRepo.findAll(limit);
//        model.put("messages", messages);
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = ((LdapUserDetails) principal).getDn();
        String ownerth = username.split("\\,")[0].split("=")[1];
//        model.put("greet", ownerth);
        defaultMethodIter(model, messages, ownerth);
        model.put("ssn", searchsn);
        return "search";
    }


// ?????????? ???? ?????????????????? ????????????, ???????????????? ?????? ?? ??????????????????.
    @GetMapping("/find")
    public String search (String searchsn, Map<String, Object> model) {
        Streamable<Message> messages;
        if (searchsn != null && !searchsn.isEmpty()) {
            messages = messageRepo.findBySnContainingIgnoreCase(searchsn)
                    .and(messageRepo.findByTextContainingIgnoreCase(searchsn))
                    .and(messageRepo.findByOwnerContainingIgnoreCase(searchsn))
                    .and(messageRepo.findByInvidContainingIgnoreCase(searchsn));
//            LdapSearch app = new LdapSearch();
//            List<String> list = app.getAllPersonNames();
//            model.put("list", list);
//            model.put("messages", messages);
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            String username = ((LdapUserDetails) principal).getDn();
            String ownerth = username.split("\\,")[0].split("=")[1];
//            model.put("greet", ownerth);
            defaultMethodStream(model, messages, ownerth);
            model.put("ssn", searchsn);
            return "search";
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
            model.put("ssn", searchsn);
            return "search";
        }
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

    // ???????????????????????? QR ???????? ???? ???????????? ?? ???? ?? ?????????? ???? ????????????????
    @PostMapping("/qrcode")
    public String qrcode(@RequestParam Long id,
                         @RequestParam String sn,
                         @RequestParam String text,
                         @RequestParam String invid,
                         Map<String, Object> model) throws IOException, WriterException {
        if (messageRepo.selectQrcodeById(id) != null & ParamsQRCode(text, invid, sn).toString("X", " ").equals(messageRepo.selectQrcodeById(id))) {
            BitMatrix qrm = BitMatrix.parse(messageRepo.selectQrcodeById(id), "X", " ");
            Message messages = new Message (id, sn, text, invid);
            model.put("messages", messages);
            String imageString = createQRImage(text, invid, qrm);
            model.put("qr64", imageString);
            } else {
                qrModify.setQRCodeFor(ParamsQRCode(text, invid, sn).toString("X", " "), id);
                BitMatrix qrm = BitMatrix.parse(messageRepo.selectQrcodeById(id), "X", " ");
                Message messages = new Message (id, sn, text, invid);
                model.put("messages", messages);
                String imageString = createQRImage(text, invid, qrm);
                model.put("qr64", imageString);
        }
        return "qrcode";
    }

// ???????????????????????????? ?????????????????? ?????? ?????????? ???????????? list ???? ???????????? ???????????????? ??????????
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
        Message messagess = new Message(owner, id, sn, text, author, invid);
//        LdapSearch app = new LdapSearch();
//        List<String> list = app.getAllPersonNames();
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = ((LdapUserDetails) principal).getDn();
        String ownerth = username.split("\\,")[0].split("=")[1];
        Pageable limit = PageRequest.of(0,10);
        Iterable<Message> messages = messageRepo.findAll(limit);
        Optional<Message> ch = messageRepo.findById(id);
        String ch2 = ch.get().getOwner();
        if (!owner.equals(ch2)) {
            journal.setMessageid(id);
            journal.setNewowner(owner);
            journal.setAuthor(author);
            journal.setDate(Calendar.getInstance().getTime());
            journalAdd.save(journal);
            messageModify.save(messagess);
//            model.put("messages", messages);
//            model.put("greet", ownerth);
//            model.put("list", list);
            defaultMethodIter(model, messages, ownerth);

            SimpleMailMessage msg = new SimpleMailMessage();
            try {
                File devFile = new File("src/main/resources/static/config.ini");
                BufferedReader fin = new BufferedReader(new FileReader(devFile));
                String line;
                while ((line = fin.readLine()) != null) {
                    if (line.startsWith("setTo"))
                        msg.setTo(line.split("=")[1].split(","));
                    if (line.startsWith("setFrom")) {
                        msg.setFrom(line.split("=")[1]);
                    }
                }
                msg.setSubject("?????????????????????? ??????");
                msg.setText("?????????????????? ??????????????????????:\n\n" + "??????:\t\t" + text + "\n??????. ???:\t" + sn + "\n??????. ???:\t" + invid + "\n????????????????:\t" + owner);
                javaMailSender.send(msg);
            } catch (IOException e) {
                System.out.println("???????? ???? ????????????!");
            } catch (MailSendException e) {
                System.out.println("???????????????? 'setTo' ???? ????????????!");
            }
            return "main";
        } else {
            model.put("error", "?????????????????? ??????????????????!");
//            model.put("list", list);
//            model.put("messages", messagess);
//            model.put("greet", ownerth);
            defaultMethodIter(model, messages, ownerth);
            return "main.html";
        }
    }


// ???????????????????????????? ?????????????????? ?????? ?????????? ???????????? list ???? ???????????? ?????????? ????????????
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
        Message messagess = new Message (owner, id, sn, text, author, invid);

//        LdapSearch app = new LdapSearch();
//        List<String> list = app.getAllPersonNames();
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = ((LdapUserDetails) principal).getDn();
        String ownerth = username.split("\\,")[0].split("=")[1];


        Pageable limit = PageRequest.of(0,10);
        Iterable<Message> messages = messageRepo.findAll(limit);



        Optional<Message> ch = messageRepo.findById(id);
        String ch2 = ch.get().getOwner();
        if (!owner.equals(ch2)) {
            journal.setMessageid(id);
            journal.setNewowner(owner);
            journal.setAuthor(author);
            journal.setDate(Calendar.getInstance().getTime());
            journalAdd.save(journal);
            messageModify.save(messagess);
//            model.put("messages", messages);
//            model.put("list", list);
//            model.put("greet", ownerth);
            defaultMethodIter(model, messages, ownerth);

            SimpleMailMessage msg = new SimpleMailMessage();
            try {
                File devFile = new File("src/main/resources/static/config.ini");
                BufferedReader fin = new BufferedReader(new FileReader(devFile));
                String line;
                while ((line = fin.readLine()) != null) {
                    if (line.startsWith("setTo"))
                        msg.setTo(line.split("=")[1].split(","));
                    if (line.startsWith("setFrom")) {
                        msg.setFrom(line.split("=")[1]);
                    }
                }
                msg.setSubject("?????????????????????? ??????");
                msg.setText("?????????????????? ??????????????????????:\n\n" + "??????:\t\t" + text + "\n??????. ???:\t" + sn + "\n??????. ???:\t" + invid + "\n????????????????:\t" + owner);
                javaMailSender.send(msg);
            } catch (IOException e) {
                System.out.println("???????? ???? ????????????!");
            } catch (MailSendException e) {
                System.out.println("???????????????? 'setTo' ???? ????????????!");
            }
            return "redirect:/search";
        } else {
            model.put("error", "?????????????????? ??????????????????!");
//            model.put("list", list);
//            model.put("messages", messagess);
//            model.put("greet", ownerth);
            defaultMethodIter(model, messages, ownerth);
            return "search";
        }
    }

// ???????????????? ???? ???????????????????????? ?????????????????????? ?????????? ???????????????? ?????? ?? ???????????????????? ??????.
    @PostMapping("/main")
    public String add (
            @RequestParam String owner,
            @RequestParam String text,
            @RequestParam String sn,
            @RequestParam String invid,
            @RequestParam String author, Map<String, Object> model) throws IOException, WriterException {
        Message message = new Message (text, sn, owner, author, invid, ParamsQRCode(text, invid, sn).toString("X", " "));
        if (text != null && !text.isEmpty() & owner != null && !owner.isEmpty() & invid != null && !invid.isEmpty()) {
            if (!text.matches("^[0-9].*$")) {
                if (!messageRepo.existsMessageByInvidIgnoreCase(invid)) {
                    messageRepo.save(message);
                    LdapSearch app = new LdapSearch();
                    List<String> list = app.getAllPersonNames();
                    model.put("list", list);
                    Pageable limit = PageRequest.of(0,10);
                    Iterable<Message> messages = messageRepo.findAll(limit);
                    model.put("messages", messages);
                    Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
                    String username = ((LdapUserDetails) principal).getDn();
                    String ownerth = username.split("\\,")[0].split("=")[1];
                    model.put("greet", ownerth);
                } else
                model.put("error", "?????????? ?????????? ?????? ????????????????????!");
                LdapSearch app = new LdapSearch();
                List<String> list = app.getAllPersonNames();
                model.put("list", list);
                Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
                String username = ((LdapUserDetails) principal).getDn();
                String ownerth = username.split("\\,")[0].split("=")[1];
                model.put("greet", ownerth);
            } else
            model.put("error", "?????? ???? ???????????? ???????????????????? ?? ??????????");
            Pageable limit = PageRequest.of(0,10);
            Iterable<Message> messages = messageRepo.findAll(limit);
            model.put("messages", messages);
            LdapSearch app = new LdapSearch();
            List<String> list = app.getAllPersonNames();
            model.put("list", list);
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            String username = ((LdapUserDetails) principal).getDn();
            String ownerth = username.split("\\,")[0].split("=")[1];
            model.put("greet", ownerth);
        } else {
            model.put("error", "?????????????????? ?????? ????????!");
            Pageable limit = PageRequest.of(0,10);
            Iterable<Message> messages = messageRepo.findAll(limit);
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


// ???????????????? ?????? ???? ???? ???????????????? ??????????.
@PostMapping("/remove")
public String remove (@RequestParam Long id,
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
                        SimpleDateFormat formatter= new SimpleDateFormat("dd.MM.yyyy");
                        Date date = new Date(System.currentTimeMillis());
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
                        for (Message messs : messag) {
                            String filenames = "C????????????e ?????? ??????. ??? " + messs.getInvid() + " ???? " + formatter.format(date) + ".pdf";
                            ContentDisposition contentDisposition = ContentDisposition.builder("attachment")
                                    .filename(filenames, StandardCharsets.UTF_8)
                                    .build();
                            String headerValue = "attachment; filename=" + contentDisposition;
                            remform.setContentType("application/pdf");
                            remform.setHeader(headerKey, headerValue);
                        }

                        Document document = new Document(PageSize.A4.rotate());
                        PdfWriter writer = PdfWriter.getInstance(document, remform.getOutputStream());

                        HeaderFooter header = new HeaderFooter(new Phrase("?????? ???? ???????????????? ???????????????????? ???                             ???? " + formatter.format(date) + " ??.", font), false);
                        document.setHeader(header);
                        HeaderFooter footer = new HeaderFooter(new Phrase("???????????????? "), new Phrase("."));

                        footer.setAlignment(HeaderFooter.ALIGN_RIGHT);
                        footer.setBorder(com.lowagie.text.Rectangle.NO_BORDER);
                        document.setFooter(footer);

                        document.left(100f);
                        document.top(150f);
                        document.open();

                        PdfPTable table = new PdfPTable(3);
                        table.setWidthPercentage(100);
                        table.setSpacingBefore(10);
                        table.setHeaderRows(1);

                        Stream.of("????????????????????????", "??????. ???", "??????????????????")
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
                                rect.x + 200f,
                                rect.y + 60f,
                                rect.width + 465,
                                rect.height - 55, -10
                        );

                        final String IMG = "src/main/resources/static/pmhm.png";
                        Image image = Image.getInstance(IMG);
                        image.scaleAbsolute(63, 33);

                        ColumnText ct1 = new ColumnText(cb);
                        ct1.setSimpleColumn(280, -10, 410, 51);
                        ct1.addElement(new Paragraph("???????????????? ????????????????\n?????????????????????? ????????????????", fontsignb));
                        ColumnText ct2 = new ColumnText(cb);
                        ct2.setSimpleColumn(410, -10, 800, 60);
                        ct2.addElement(new Paragraph("????????????????: " + cert.getSubjectDN().toString().split("\\,")[1].split("=")[1], fontsignbb));
                        ColumnText ct3 = new ColumnText(cb);
                        ct3.setSimpleColumn(410, -25, 800, 42);
                        ct3.addElement(new Paragraph("????????????????????: " + cert.getSerialNumber().toString(16), fontsignn));
                        ColumnText ct4 = new ColumnText(cb);
                        ct4.setSimpleColumn(410, -30, 800, 28);
                        Date notBefore = cert.getNotBefore();
                        Date notAfter = cert.getNotAfter();
                        ct4.addElement(new Paragraph("???????????????????????? ?? : " + formatter.format(notBefore) + " ???? " + formatter.format(notAfter), fontsignn));
                        ColumnText ct5 = new ColumnText(cb);
                        ct5.setSimpleColumn(209, -10, 410, 48);
                        ct5.addElement(image);

                        ct1.go();
                        ct2.go();
                        ct3.go();
                        ct4.go();
                        ct5.go();
                        cb.stroke();
                        document.close();

                        model.put("greet", ownerth);
                        messageRepo.deleteById(id);
                        journalFind.deleteByMessageid(id);
                        model.put("messages", messagedel);
                        return "main.html";
                    } else {
//                        LdapSearch app = new LdapSearch();
//                        List<String> list = app.getAllPersonNames();
//                        model.put("list", list);
                        Pageable limit = PageRequest.of(0,10);
                        Iterable<Message> messages = messageRepo.findAll(limit);
//                        model.put("messages", messages);
//                        model.put("greet", ownerth);
                        defaultMethodIter(model, messages, ownerth);
                        model.put("error", "???????? ???????????????????? ?????????? ???? ??????!");
                        return "main.html";
                    }
                } else {
//                    LdapSearch app = new LdapSearch();
//                    List<String> list = app.getAllPersonNames();
//                    model.put("list", list);
                    Pageable limit = PageRequest.of(0,10);
                    Iterable<Message> messages = messageRepo.findAll(limit);
//                    model.put("messages", messages);
//                    model.put("greet", ownerth);
                    defaultMethodIter(model, messages, ownerth);
                    model.put("error", "???????????????????? ??????????????????!");
                    return "main.html";
                }
            } else {
//                LdapSearch app = new LdapSearch();
//                List<String> list = app.getAllPersonNames();
//                model.put("list", list);
                Pageable limit = PageRequest.of(0,10);
                Iterable<Message> messages = messageRepo.findAll(limit);
//                model.put("messages", messages);
//                model.put("greet", ownerth);
                defaultMethodIter(model, messages, ownerth);
                model.put("error", "???????????????????? ?????????? ???????????? ????????????????????????!");
                return "main.html";
            }
        } else {
//            LdapSearch app = new LdapSearch();
//            List<String> list = app.getAllPersonNames();
//            model.put("list", list);
            Pageable limit = PageRequest.of(0,10);
            Iterable<Message> messages = messageRepo.findAll(limit);
//            model.put("messages", messages);
//            model.put("greet", ownerth);
            defaultMethodIter(model, messages, ownerth);
            model.put("error", "???????????????????? ???? ????????????.");
            return "main.html";
        }
    } catch (CertificateException e) {
//        LdapSearch app = new LdapSearch();
//        List<String> list = app.getAllPersonNames();
//        model.put("list", list);
        Pageable limit = PageRequest.of(0,10);
        Iterable<Message> messages = messageRepo.findAll(limit);
//        model.put("messages", messages);
//        model.put("greet", ownerth);
        defaultMethodIter(model, messages, ownerth);
        model.put("error", "???????????? ???? ????????????????????.");
        return "main.html";
    }
}


//  ???????????????? ?????? ???? ???? ?????????? ????????????.
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
                            SimpleDateFormat formatter= new SimpleDateFormat("dd.MM.yyyy");
                            Date date = new Date(System.currentTimeMillis());
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
                            for (Message messs : messag) {
                                String filenames = "C????????????e ?????? ??????. ???" + messs.getInvid() + " ???? " + formatter.format(date) + ".pdf";
                                ContentDisposition contentDisposition = ContentDisposition.builder("attachment")
                                        .filename(filenames, StandardCharsets.UTF_8)
                                        .build();
                                String headerValue = "attachment; filename=" + contentDisposition;
                                remform.setContentType("application/pdf");
                                remform.setHeader(headerKey, headerValue);
                            }

                            Document document = new Document(PageSize.A4.rotate());
                            PdfWriter writer = PdfWriter.getInstance(document, remform.getOutputStream());

                            HeaderFooter header = new HeaderFooter(new Phrase("?????? ???? ???????????????? ???????????????????? ???                             ???? " + formatter.format(date) + " ??.", font), false);
                            document.setHeader(header);
                            HeaderFooter footer = new HeaderFooter(new Phrase("???????????????? "), new Phrase("."));

                            footer.setAlignment(HeaderFooter.ALIGN_RIGHT);
                            footer.setBorder(com.lowagie.text.Rectangle.NO_BORDER);
                            document.setFooter(footer);

                            document.left(100f);
                            document.top(150f);
                            document.open();


                            PdfPTable table = new PdfPTable(3);
                            table.setWidthPercentage(100);
                            table.setSpacingBefore(10);
                            table.setHeaderRows(1);

                            Stream.of("????????????????????????", "??????. ???", "??????????????????")
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
                                    rect.x + 200f,
                                    rect.y + 60f,
                                    rect.width + 465,
                                    rect.height - 55, -10
                            );

                            final String IMG = "src/main/resources/static/pmhm.png";
                            Image image = Image.getInstance(IMG);
                            image.scaleAbsolute(63, 33);

                            ColumnText ct1 = new ColumnText(cb);
                            ct1.setSimpleColumn(280, -10, 410, 51);
                            ct1.addElement(new Paragraph("???????????????? ????????????????\n?????????????????????? ????????????????", fontsignb));
                            ColumnText ct2 = new ColumnText(cb);
                            ct2.setSimpleColumn(410, -10, 800, 60);
                            ct2.addElement(new Paragraph("????????????????: " + cert.getSubjectDN().toString().split("\\,")[1].split("=")[1], fontsignbb));
                            ColumnText ct3 = new ColumnText(cb);
                            ct3.setSimpleColumn(410, -25, 800, 42);
                            ct3.addElement(new Paragraph("????????????????????: " + cert.getSerialNumber().toString(16), fontsignn));
                            ColumnText ct4 = new ColumnText(cb);
                            ct4.setSimpleColumn(410, -30, 800, 28);
                            Date notBefore = cert.getNotBefore();
                            Date notAfter = cert.getNotAfter();
                            ct4.addElement(new Paragraph("???????????????????????? ?? : " + formatter.format(notBefore) + " ???? " + formatter.format(notAfter), fontsignn));
                            ColumnText ct5 = new ColumnText(cb);
                            ct5.setSimpleColumn(209, -10, 410, 48);
                            ct5.addElement(image);

                            ct1.go();
                            ct2.go();
                            ct3.go();
                            ct4.go();
                            ct5.go();
                        cb.stroke();
                        document.close();

                        model.put("greet", ownerth);
                        messageRepo.deleteById(id);
                        journalFind.deleteByMessageid(id);
                        model.put("messages", messagedel);
                        return "search.html";

                        } else {
//                            LdapSearch app = new LdapSearch();
//                            List<String> list = app.getAllPersonNames();
//                            model.put("list", list);
                            Pageable limit = PageRequest.of(0,10);
                            Iterable<Message> messages = messageRepo.findAll(limit);
//                            model.put("messages", messages);
//                            model.put("greet", ownerth);
                            defaultMethodIter(model, messages, ownerth);
                            model.put("error", "???????? ???????????????????? ?????????? ???? ??????!");
                            return "search.html";
                        }

                    } else {
//                        LdapSearch app = new LdapSearch();
//                        List<String> list = app.getAllPersonNames();
//                        model.put("list", list);
                        Pageable limit = PageRequest.of(0,10);
                        Iterable<Message> messages = messageRepo.findAll(limit);
//                        model.put("messages", messages);
//                        model.put("greet", ownerth);
                        defaultMethodIter(model, messages, ownerth);
                        model.put("error", "???????????????????? ??????????????????!");
                        return "search.html";
                    }
                } else {
//                    LdapSearch app = new LdapSearch();
//                    List<String> list = app.getAllPersonNames();
//                    model.put("list", list);
                    Pageable limit = PageRequest.of(0,10);
                    Iterable<Message> messages = messageRepo.findAll(limit);
//                    model.put("messages", messages);
//                    model.put("greet", ownerth);
                    defaultMethodIter(model, messages, ownerth);
                    model.put("error", "???????????????????? ?????????? ???????????? ????????????????????????!");
                    return "search.html";
                }
            } else {
//                LdapSearch app = new LdapSearch();
//                List<String> list = app.getAllPersonNames();
//                model.put("list", list);
                Pageable limit = PageRequest.of(0,10);
                Iterable<Message> messages = messageRepo.findAll(limit);
//                model.put("messages", messages);
//                model.put("greet", ownerth);
                defaultMethodIter(model, messages, ownerth);
                model.put("error", "???????????????????? ???? ????????????.");
                return "search.html";
            }
        } catch (CertificateException e) {
//            LdapSearch app = new LdapSearch();
//            List<String> list = app.getAllPersonNames();
//            model.put("list", list);
            Pageable limit = PageRequest.of(0,10);
            Iterable<Message> messages = messageRepo.findAll(limit);
//            model.put("messages", messages);
//            model.put("greet", ownerth);
            defaultMethodIter(model, messages, ownerth);
            model.put("error", "???????????? ???? ????????????????????.");
            return "search.html";
        }
    }
}