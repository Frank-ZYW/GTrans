# GTrans

Free Google Translation API build by Java 8



## How to use

single words translate

```java
// get client api
GoogleTranslate googleTranslate = new GoogleTranslate();

// single words translate
List<String> result = googleTranslate.translate(singleWord, "auto", "en");
System.out.println(result.toString());
```

result

```java
>>>[Hello world]
```



multiple words translate

```java
// get client api
GoogleTranslate googleTranslate = new GoogleTranslate();

// multiple words translate
List<String> multipleWords = new LinkedList<String>();
multipleWords.add("使用しています");
multipleWords.add("Google翻译接口");

List<String> result = googleTranslate.translate(multipleWords, "auto", "en");
System.out.println(result.toString());
```

result

```java
>>>[I'm using, Google translation interface]
```



## How does it work

