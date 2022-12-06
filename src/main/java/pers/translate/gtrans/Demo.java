package pers.translate.gtrans;

import pers.translate.gtrans.api.GoogleTranslate;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class Demo {

    public static void main(String[] args) throws IOException {

        // get client api
        GoogleTranslate googleTranslate = new GoogleTranslate();
        List<String> result;

        // single translation
        String singleWords = "안녕하세요 세계";
        result = googleTranslate.translate(singleWords, "auto", "en");
        System.out.println(result.toString());

        // bulk translate
        List<String> bulkWords = new LinkedList<>();
        bulkWords.add("使用しています");
        bulkWords.add("Google翻译接口");
        result = googleTranslate.translate(bulkWords, "auto", "en");
        System.out.println(result.toString());

        // single detect
        result = googleTranslate.detect(singleWords);
        System.out.println(result.toString());

        // bulk detect
        result = googleTranslate.detect(bulkWords);
        System.out.println(result.toString());

    }
}
