package pers.translate.gtrans.api;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import pers.translate.gtrans.http.HttpClient4;
import pers.translate.gtrans.http.ParamPairList;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.*;

import static java.lang.Character.getNumericValue;

/**
 * Google Translate API
 */
public class GoogleTranslate {

    private final HttpClient4 httpClient4;        // HttpClient
    private final ArrayList<BigInteger> tkk;      // tkk for calculate tk
    private final HashMap<String, String> header; // Default Request Header
    private static final String baseURL = "https://translate.googleapis.com/translate_a/t?client=webapp&dt=bd" +
            "&dt=ex&dt=ld&dt=md&dt=qca&dt=rw&dt=rm&dt=ss&dt=t&dt=at&ie=UTF-8&oe=UTF-8&otf=2&ssel=0&tsel=0&kc=1" +
            "&sl=%1$s&tl=%2$s&hl=%2$s&tk=%3$s";

    /**
     * Default constructor
     */
    public GoogleTranslate(){
        this(new HttpClient4());
    }

    /**
     * constructor
     * @param httpClient4 a HttpClient4 object
     */
    public GoogleTranslate(HttpClient4 httpClient4){
        this.httpClient4 = httpClient4;

        this.tkk = new ArrayList<BigInteger>();
        tkk.add(new BigInteger("406398"));
        tkk.add(new BigInteger("2087938574"));

        this.header = new HashMap<String, String>();
        this.header.put("Accept", "*/*");
        this.header.put("Connection", "keep-alive");
        this.header.put("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_6_8) " +
                    "AppleWebKit/535.19 (KHTML, like Gecko) Chrome/18.0.1025.168 Safari/535.19");
    }

    /**
     * Destructor
     */
    @Override
    protected void finalize(){
        this.httpClient4.shutdown();
    }

    /**
     * translate api
     * @param singleSource single source to translate
     * @param fromLang origin language
     * @param toLang target language
     * @return List result
     * @throws IOException source can't be encode to utf-8 / http request error
     */
    public List<String> translate(final String singleSource, String fromLang, String toLang) throws IOException {
        return translate(new LinkedList<String>(){{add(singleSource);}}, fromLang, toLang);
    }

    /**
     * translate api
     * @param source two or more source to translate
     * @param fromLang origin language
     * @param toLang target language
     * @return result
     * @throws IOException source can't be encode to utf-8 / http request error
     */
    public List<String> translate(List<String> source, String fromLang, String toLang)
            throws IOException {
        // build url with params
        String strSource = sourceToString(source);
        String apiUrl = addUrlParams(fromLang, toLang, this.calculateTk(strSource));

        // build data of post request
        ParamPairList postData = getPostData(source);

        // json serialization
        String jsonResult = this.httpClient4.doPost(apiUrl, this.header, postData);
        return jsonSerialization(jsonResult, source.size() == 1);
    }

    /**
     * build api url
     * @param fromLang origin language
     * @param toLang target language
     * @param tk tk of source
     * @return api url
     */
    private String addUrlParams(String fromLang, String toLang, String tk){
        return String.format(baseURL, fromLang, toLang, tk);
    }

    /**
     * combine all source to a String
     * @param source List translate source
     * @return String combined source
     */
    private String sourceToString(List<String> source){
        StringBuilder combineStr = new StringBuilder();
        for(String item: source)
            combineStr.append(item);
        return combineStr.toString();
    }

    /**
     * build data of post request
     * @param source List translate source
     * @return ParamPairList
     */
    private ParamPairList getPostData(List<String> source) {
        ParamPairList paramPairList = new ParamPairList();
        for(String item: source)
            paramPairList.add("q", item);
        return paramPairList;
    }

    /**
     * serialize json result
     * @param jsonResult result
     * @param isSingle single or multiple result
     * @return List<String>
     */
    private List<String> jsonSerialization(String jsonResult, boolean isSingle){
        List<String> resultList = new LinkedList<String>();
        if (isSingle){
            resultList.add(JsonParser.parseString(jsonResult).getAsJsonArray().get(0).getAsString());
        } else {
            // jsonResult like "[[[[["hello, world"]],null,"ko"],[[["hello my friends"]],null,"zh-cn"]]]"
            JsonArray resultArray = JsonParser.parseString(jsonResult).getAsJsonArray().get(0).getAsJsonArray();
            for (JsonElement item : resultArray)
                resultList.add(item.getAsJsonArray().get(0).getAsJsonArray().get(0).getAsJsonArray().get(0).getAsString());
        }
        return resultList;
    }

    /**
     * calculate tk
     * @param source str to translate
     * @return translate result
     * @throws UnsupportedEncodingException source can't be encode to utf-8
     */
    private String calculateTk(String source) throws UnsupportedEncodingException {
        byte[] sourceBytes = source.getBytes("UTF-8"); // encode utf-8

        BigInteger a = tkk.get(0);
        for (byte k : sourceBytes)
            // java byte range from -127~128, use '& 0xff' turn to range 0~256
            a = rl(a.add(new BigInteger(String.valueOf(k & 0xff))), "+-a^+6");
        a = rl(a, "+-3^+b+-f");

        a = a.xor(tkk.get(1));
        if (a.compareTo(new BigInteger("0")) < 0)
            a = (a.and(new BigInteger("2147483647"))).add(new BigInteger("2147483648"));
        a = a.remainder(new BigInteger("1000000")); // get mod

        return String.format("%s.%s", a, a.xor(tkk.get(0)));
    }

    /**
     * util function for calculate tk
     * @param a BigInteger
     * @param b String
     * @return BigInteger
     */
    private static BigInteger rl(BigInteger a, String b){
        for (int c = 0; c < b.length() - 2; c += 3){
            char d = b.charAt(c + 2);
            int e;
            if (d >= 'a')
                e = d - 87;
            else
                e = getNumericValue((int)d);

            BigInteger f;
            if (b.charAt(c + 1) == '+')
                f = a.shiftRight(e);
            else
                f = a.shiftLeft(e);

            if (b.charAt(c) == '+')
                a = a.add(f).and(new BigInteger("4294967295"));
            else
                a = a.xor(f);
        }
        return a;
    }
}
