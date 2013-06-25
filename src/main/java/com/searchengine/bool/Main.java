package com.searchengine.bool;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * Created with IntelliJ IDEA.
 * User: avvolkov
 * Date: 19.02.13
 * Time: 20:54
 * To change this template use File | Settings | File Templates.
 */
public class Main {

    private static Logger logger = LogManager.getLogger(Main.class);

    public static void main(String[] args) {

        logger.info("sdsdsd");
        logger.error("2332323123");
//        List<Document> documents = new ArrayList<Document>(4);
//
//        documents.add(new Document(" the next door"));
//        documents.add(new Document("in under the previous floor"));
//        documents.add(new Document("rights for the public poor"));
//        documents.add(new Document("you seen this background cloor"));
//        SearchService.addDocuments(documents);
//
//        List<IToken> tokens =
//                new WordTokenizer().getTokensFromDocument(new Document(
//                        "in the"
//                ));
//        List<Boolean> signs = new ArrayList<Boolean>(4);
//        List<IDocument> answer   = new ArrayList<IDocument>();
//        signs.add(true);
//        signs.add(true);
//
//        answer = SearchService.findDocuments(tokens, signs);
//        for (IDocument ans : answer) {
//            System.out.println("Doucument Id --> " + ans.getContent());
//        }
    }
}
