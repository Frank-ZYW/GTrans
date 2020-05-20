# GTrans

Free Google Translation API library build by Java 8.

GTrans uses the [Google Translate Ajax API](https://translate.google.com/) to make calls to such methods as detect and translate.



## Features

- Fast, reliable and simple
- Auto language detection & Support Hundreds of languages 
- Single / Bulk translation
- Connection pooling (Apache HttpClient)



## How to use

### Basic Usage (Single translation)

Provide source & target language to enable translate, you can also use `auto`  if you don't know type.

```java
// get client api
GoogleTranslate googleTranslate = new GoogleTranslate();

// single words translation
String singleWords = "안녕하세요 세계";
List<String> result = googleTranslate.translate(singleWords, "auto", "en");
System.out.println(result.toString());
```

```java
>>>[Hello world]
```

### Advanced Usage (Bulk translation)

Use String List can translate a batch of strings with one HTTP Request.

```java
// get client api
GoogleTranslate googleTranslate = new GoogleTranslate();

// bulk words translation
List<String> bulkWords = new LinkedList<String>();
bulkWords.add("使用しています");
bulkWords.add("Google翻译接口");

List<String> result = googleTranslate.translate(bulkWords, "auto", "en");
System.out.println(result.toString());
```

```java
>>>[I'm using, Google translation interface]
```

### Language Detect

Identify the language used in a given sentence or sentences.

```java
// get client api
GoogleTranslate googleTranslate = new GoogleTranslate();
List<String> result;

String singleWords = "안녕하세요 세계";

List<String> bulkWords = new LinkedList<String>();
bulkWords.add("使用しています");
bulkWords.add("Google翻译接口");

// single detect
result = googleTranslate.detect(singleWords);
System.out.println(result.toString());

// bulk detect
result = googleTranslate.detect(bulkWords);
System.out.println(result.toString());
```

```
>>>[ko]
>>>[ja, zh-CN]
```



## How it works

We found a way to generate such token with a special algorithm. 

```java
/**
 * calculate tk
 * @param source str to translate
 * @return token
 * @throws UnsupportedEncodingException source can't be encode to utf-8
 */
public String calculateTk(String source) throws UnsupportedEncodingException {
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
```

The algorithm has multi-language version, like [Python](https://github.com/sloria/TextBlob/blob/dev/textblob/translate.py).



## Main API Document





## Note

DISCLAIMER: this is an unofficial library using the web API of translate.googleapis.com and also is not associated with Google.

- The API has limitations. Read [Google Service Items](https://policies.google.com/terms?) get more information.
- If you want to use a stable API,  use [Google's official translate API](https://cloud.google.com/translate/docs) instead.
- If you visit too frequently, you may get HTTP 429/430 error or errors. It means your IP probably has been banned. The restriction will be lifted after 24 hours.

## License

GTrans has already licensed under the MIT License.