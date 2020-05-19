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

        // single word translate
        String singleWord = "안녕하세요 세계";
        result = googleTranslate.translate(singleWord, "auto", "en");
        System.out.println(result.toString());

        // multiple words translate
        List<String> multipleWords = new LinkedList<String>();
        multipleWords.add("使用しています");
        multipleWords.add("Google翻译接口");

        result = googleTranslate.translate(multipleWords, "auto", "en");
        System.out.println(result.toString());
    }

}
