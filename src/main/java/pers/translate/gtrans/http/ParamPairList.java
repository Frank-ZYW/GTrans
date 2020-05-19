package pers.translate.gtrans.http;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Param pair list for Http Request
 */
public class ParamPairList {

    // pair list
    private final List<NameValuePair> pairList;

    /**
     * Default constructor
     */
    public ParamPairList(){
        pairList = new LinkedList<NameValuePair>();
    }

    /**
     * Constructor with Map
     */
    public ParamPairList(Map<String, Object> params){
        pairList = new LinkedList<NameValuePair>();
        for (Map.Entry<String, Object> entry : params.entrySet())
            pairList.add(new BasicNameValuePair(entry.getKey(), entry.getValue().toString()));
    }

    /**
     * add param pair
     * @param key key
     * @param value value
     */
    public void add(String key, Object value){
        pairList.add(new BasicNameValuePair(key, value.toString()));
    }

    /**
     * Find param pair by key
     * @param key key
     * @return NameValuePair
     */
    public NameValuePair findParamPairByKey(String key){
        for(NameValuePair item: pairList)
            if (item.getName().equals(key))
                return item;
        return null;
    }

    /**
     * Find all param pair by key
     * @param key key
     * @return NameValuePair
     */
    public List<NameValuePair> findAllParamPairByKey(String key){
        List<NameValuePair> results = new LinkedList<NameValuePair>();
        for(NameValuePair item: pairList)
            if (item.getName().equals(key))
                results.add(item);
        if (results.isEmpty())
            return null;
        else
            return results;
    }

    /**
     * Is param pair exist
     * @param key key
     * @param value value
     * @return bool
     */
    public boolean isParamPairExist(String key, String value){
        for(NameValuePair item: pairList)
            if (item.getName().equals(key) && item.getValue().equals(value))
                return true;
        return false;
    }

    /**
     * Is list empty
     * @return bool
     */
    public boolean isEmpty(){
        return pairList.isEmpty();
    }

    /**
     * get pair
     * @param i pair index
     * @return NameValuePair
     */
    public NameValuePair get(int i){
        return pairList.get(i);
    }

    /**
     * get param list
     * @return list
     */
    public List<NameValuePair> getPairList(){
        return pairList;
    }

    /**
     * get list size
     * @return size
     */
    public int size(){
        return pairList.size();
    }
}
