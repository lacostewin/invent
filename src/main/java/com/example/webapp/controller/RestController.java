package com.example.webapp.controller;

import com.example.webapp.domain.Journal;
import com.example.webapp.domain.Message;
import com.example.webapp.repos.JournalFind;
import com.example.webapp.repos.MessageRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.configurationprocessor.json.JSONArray;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.data.util.Streamable;
import org.springframework.web.bind.annotation.*;


@org.springframework.web.bind.annotation.RestController
@RequestMapping("api")
public class RestController {
    @Autowired
    private MessageRepo messageRepo;
    @Autowired
    private JournalFind journalFind;

    @GetMapping(value = "{searchsn}", produces = "application/json;charset=UTF-8")
    public String list(@PathVariable String searchsn) {
        Streamable<Message> messages;
            messages = messageRepo.findBySnContainingIgnoreCase(searchsn)
                    .and(messageRepo.findByTextContainingIgnoreCase(searchsn))
                    .and(messageRepo.findByOwnerContainingIgnoreCase(searchsn))
                    .and(messageRepo.findByInvidContainingIgnoreCase(searchsn));

        JSONObject jObject = new JSONObject();
        try
        {
            JSONArray jArray = new JSONArray();
            for (Message mess : messages)
            {
                JSONObject result = new JSONObject();
                result.put("id", mess.getId());
                result.put("name", mess.getText());
                result.put("invid", mess.getInvid());
                result.put("sn", mess.getSn());
                result.put("owner", mess.getOwner());
                jArray.put(result);
            }
            jObject.put("result", jArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jObject.toString();
    }


    @GetMapping(value = "/hist/{id}", produces = "application/json;charset=UTF-8")
    public String hist(@PathVariable Long id) {
        Iterable<Journal> journal = journalFind.findByMessageid(id);
        JSONObject jObject = new JSONObject();
        try
        {
            JSONArray jArray = new JSONArray();
            for (Journal jour : journal)
            {
                JSONObject result = new JSONObject();
                result.put("author", jour.getAuthor());
                result.put("date", jour.getDate());
                result.put("owner", jour.getNewowner());
                jArray.put(result);
            }
            jObject.put("result", jArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jObject.toString();
    }
}