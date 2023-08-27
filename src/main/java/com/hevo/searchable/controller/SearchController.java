package com.hevo.searchable.controller;

import com.hevo.searchable.FileService;
import com.hevo.searchable.exceptions.SearchException;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Log4j2
public class SearchController {

    @Autowired private FileService fileService;

    //Todo: RateLimit at Api Layer in middlewares
    @GetMapping("/search")
    public List<String> search(@RequestParam("app_id") String client, @RequestParam("q") String query) {
        try{
            return fileService.search(client, query);
        }catch (Exception e){
            log.error("search_exception", e);
            throw new SearchException("Something went wrong while searching!!!");
        }
    }
}
