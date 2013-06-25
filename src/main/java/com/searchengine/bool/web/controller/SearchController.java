package com.searchengine.bool.web.controller;

import com.searchengine.bool.config.SearchConfig;
import com.searchengine.bool.service.SearchService;
import com.searchengine.bool.web.form.SearchResult;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.*;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.io.File;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: oracle
 * Date: 02.03.13
 * Time: 10:02
 * To change this template use File | Settings | File Templates.
 */

@Controller
public class SearchController {

    private static Logger logger = LogManager.getLogger(SearchController.class);

    public SearchController() {
        File f = new File(SearchConfig.getInstance().getProperty("search.index"));

        try {
            if (!f.exists()) {
                SearchService.createIndex();
            }
            SearchService.initialize();
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e);
        }
    }


    @RequestMapping("/searchQuery")
    public ModelAndView queryHandler(@RequestParam("query") String query) throws Exception{
        query = StringEscapeUtils.unescapeHtml(query);
        System.out.println("query: " + query);

        ModelAndView mav = new ModelAndView("searchResult");
        SearchResult result = new SearchResult();
        CachedResult.setQuery(
                query.trim().toLowerCase()
               .replaceAll("\\ ", "\\ AND\\ ")
               .replaceAll("\\-", "\\ NOT\\ "));
        CachedResult.setDocuments(
                SearchService.findDocuments(query.toLowerCase()));
        List<String> stringList =
                CachedResult.getContentForPageNumber(1);

        result.setId(0L);
        // set result object for viewing
        result.setPageNumber(1);
        result.setQuery(StringEscapeUtils.escapeHtml(CachedResult.getQuery()));
//        stringList.add("first string");
//        stringList.add("second string");
//        stringList.add("third string");
        result.setContent(stringList);
        if (CachedResult.getDocuments().length > CachedResult.DOCUMENTS_ON_PAGE) {
            result.setHasNext(true);
        }
        mav.addObject("searchResult", result);
        mav.addObject("header", "searchResult");
        return mav;
    }

    @RequestMapping("/searchResult/{pageNumber}")
    public String resultHandler(@PathVariable int pageNumber, Model model) throws Exception{
        System.out.println("pageNumber: " + pageNumber);

        SearchResult result = new SearchResult();
        List<String> stringList =
                CachedResult.getContentForPageNumber(pageNumber);

        result.setId(0L);
        // set result object for viewing
        result.setPageNumber(pageNumber);
        result.setContent(stringList);
        result.setQuery(StringEscapeUtils.escapeHtml(CachedResult.getQuery()));
        if (CachedResult.getDocuments().length > CachedResult.DOCUMENTS_ON_PAGE) {
            result.setHasNext(true);
        }
        model.addAttribute("searchResult", result);

        return "searchResult";
    }

//    private List<IDocument> getQueryResult(String query) {
//        Pattern.compile(".*searchResult.*").matcher(query).find();
//        String[] tokens = query.split("\\ +");
//        List<IToken> tokenList = new ArrayList<IToken>(tokens.length);
//        List<Boolean> signList = new ArrayList<Boolean>(tokens.length);
//        for (String token : tokens) {
//            if (token.charAt(0) == '-') {
//                signList.add(false);
//                tokenList.add(new Token<String>(token.substring(1)));
//            }
//            else {
//                signList.add(true);
//                tokenList.add(new Token<String>(token));
//            }
//
//        }
//
//        return SearchService.findDocuments(tokenList, signList);
//    }


}
