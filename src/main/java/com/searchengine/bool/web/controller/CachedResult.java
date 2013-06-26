package com.searchengine.bool.web.controller;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.highlight.TokenSources;
import org.apache.lucene.util.Version;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 * User: entrix
 * Date: 16.03.2012
 * Time: 23:13
 * To change this template use File | Settings | File Templates.
 */
public class CachedResult {

    public static final int DOCUMENTS_ON_PAGE = 10;

    private static ScoreDoc[] documents;

    private static String query = "";

    private static IndexSearcher searcher;

    public  static Query queryL;


    public static ScoreDoc[] getDocuments() {
        return CachedResult.documents;
    }

    public static void setDocuments(ScoreDoc[] documents) {
        CachedResult.documents = documents;
    }

    public static void setIndexSearcher(IndexSearcher indexSearcher){
        CachedResult.searcher = indexSearcher;
    }

    public static String getQuery() {
        return query;
    }

    public static void setQuery(String query) {
        CachedResult.query = query;
    }

    public static List<String> getContentForPageNumber(int pageNumber) throws Exception{
        List<String> content = new ArrayList<String>();

//
//        for (int i = start; i < end; i++) {
//            if (raw) {                              // output raw format
//                System.out.println("doc="+documents[i].doc+" score="+documents[i].score);
//                continue;
//            }
//
//            Document doc = searcher.doc(documents[i].doc);
//            String path = doc.get("path");
//            if (path != null) {
//                System.out.println((i+1) + ". " + path);
//                String title = doc.get("title");
//                if (title != null) {
//                    System.out.println("   Title: " + doc.get("title"));
//                }
//            } else {
//                System.out.println((i+1) + ". " + "No path for this document");
//            }
//
//        }





        if ((pageNumber - 1) <= documents.length / DOCUMENTS_ON_PAGE) {
            int i = (pageNumber - 1) * DOCUMENTS_ON_PAGE;
            for (int j = i; j < i + 10 && j < documents.length; ++j) {
                Document doc = searcher.doc(documents[j].doc);
                String path = doc.get("path");
                String lineInRes = "";
//                String docContent =doc.get("contents"); This will not go cause we dont store contents, load file manually!
                String docContent = readFile(doc.get("path"));

                Highlighter highlighter = new Highlighter(new QueryScorer(queryL));

                Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_43);
                TokenStream tokenStream = TokenSources.getAnyTokenStream(searcher.getIndexReader(), documents[j].doc, "contents", analyzer);

                String fragment = highlighter.getBestFragment(tokenStream, docContent);

//                lineInRes += doc.get("path");
                lineInRes += fragment;
                content.add(doc.get("name") + ":" + "<br/>" +
                        StringEscapeUtils.escapeHtml(
                                lineInRes  ));
            }
        }
        return content;
    }

    private static String readFile( String file ) throws Exception {
        BufferedReader reader = new BufferedReader( new InputStreamReader(new FileInputStream(file), "UTF-8"));
        String         line = null;
        StringBuilder  stringBuilder = new StringBuilder();
        String         ls = System.getProperty("line.separator");

        while( ( line = reader.readLine() ) != null ) {
            stringBuilder.append( line );
            stringBuilder.append( ls );
        }

        return stringBuilder.toString();
    }
}
