package cn.kkserver.core;

import java.lang.reflect.Array;
import java.util.List;
import java.util.Map;

/**
 * Created by zhanghailong on 2016/11/8.
 */

public final class Value {

    public static Object get(Object object,String name) {

        Object v = null;

        if(object != null) {

            if(object instanceof Map) {
                v = ((Map) object).get(name);
            }
            else if(object instanceof List) {

                if("@length".equals(name)) {
                    v =  ((List) object).size();
                }
                else if("@first".equals(name)) {
                    if(((List) object).size()>0) {
                        v = ((List) object).get(0);
                    }
                    else {
                        v = null;
                    }
                }
                else if("@last".equals(name)) {
                    if(((List) object).size()>0) {
                        v = ((List) object).get(((List) object).size() - 1);
                    }
                    else {
                        v = null;
                    }
                }
                else {
                    int i = Integer.valueOf(name);

                    if (i >= 0 && i < ((List) object).size()) {
                        v = ((List) object).get(i);
                    }
                }
            }
            else if(object.getClass().isArray()) {

                int size = Array.getLength(object);

                if("@length".equals(name)) {
                    v =  size;
                }
                else if("@first".equals(name)) {
                    if(size>0) {
                        v = Array.get(object, 0);
                    }
                    else {
                        v = null;
                    }
                }
                else if("@last".equals(name)) {
                    if(size>0) {
                        v = Array.get(object, size - 1);
                    }
                    else {
                        v = null;
                    }
                }
                else {
                    int i = Integer.valueOf(name);
                    if (i >= 0 && i < size) {
                        v = Array.get(object, i);
                    }
                }
            }
            else if(object instanceof IGetter){
                v = ((IGetter) object).get(name);
            }
            else {
                try {
                    java.lang.reflect.Field fd = object.getClass().getField(name);
                    v = fd.get(object);
                } catch (Throwable e) {
                    try {
                        java.lang.reflect.Method md = object.getClass().getMethod(name);
                        v = md.invoke(object);
                    }
                    catch (Throwable ee) {}
                }
            }

        }

        return v;
    }

    public static void set(Object object,String name,Object value) {

        if(object != null) {

            if(object instanceof Map) {
                if(value == null) {
                    if(((Map) object).containsKey(name)) {
                        ((Map) object).remove(name);
                    }
                }
                else {
                    ((Map) object).put(name, value);
                }
            }
            else if(object instanceof List) {

                int i = Integer.valueOf(name);
                if(i >= 0 && i < ((List) object).size()) {
                    ((List) object).set(i,value);
                }
                else if(i == ((List) object).size()) {
                    ((List) object).add(value);
                }
            }
            else if(object.getClass().isArray()) {

                int size = Array.getLength(object);
                int i = Integer.valueOf(name);

                if(i >= 0 && i < size) {
                    Array.set(object,i, value);
                }
            }
            else if(object instanceof ISetter){
                ((ISetter) object).set(name,value);
            }
            else {
                try {
                    java.lang.reflect.Field fd = object.getClass().getField(name);
                    fd.set(object,value);
                } catch (Throwable e) {
                }
            }

        }

    }

    public static Object get(Object object, String[] keys) {

        Object v = object;
        int i = 0;

        while(v != null && keys != null && i < keys.length) {
            v = get(v,keys[i++]);
        }

        return v;
    }

    public static void set(Object object, String[] keys, Object value) {

        Object v = object;
        int i = 0;

        while(v != null && keys != null && i < keys.length) {

            if(i + 1 == keys.length){
                set(v,keys[i],value);
                break;
            }

            v = get(v,keys[i++]);
        }

    }

    public static <T> T[] slice(T[] array,int start,int end) {
        int n = end - start;
        T[] vs = (T[]) Array.newInstance(array.getClass().getComponentType(),n);
        for(int i=0;i<n;i++) {
            vs[i] = array[start + i];
        }
        return vs;
    }

    public static <T> T[] slice(T[] array,int start) {
        return slice(array,start,array.length);
    }

    public static <T> T[] join(T[] array, T ... objects){
        int n = array.length + (objects == null ? 0: objects.length);
        T[] vs = (T[]) Array.newInstance(array.getClass().getComponentType(),n);
        int i=0;
        for(T v : array) {
            vs[i ++] = v;
        }
        if(objects != null) {
            for(T v: objects) {
                vs[i ++] = v;
            }
        }
        return vs;
    }

    public static <T> String joinString(T[] array,String v) {
        StringBuilder sb = new StringBuilder();
        for(int i=0;i<array.length;i++) {
            if( i != 0) {
                sb.append(v);
            }
            T vv = array[i];
            if(vv instanceof String) {
                sb.append((String) vv);
            }
            else {
                sb.append(vv.toString());
            }
        }
        return sb.toString();
    }

    public static <T> String joinString(T[] array) {
        return joinString(array,",");
    }

    public static <T> boolean equals(T[] v1,T[] v2) {
        if(v1 == v2) {
            return true;
        }
        if(v1 == null || v2 == null) {
            return false;
        }
        if(v1.length != v2.length) {
            return false;
        }
        for(int i=0;i<v1.length;i++) {
            if(!v1[i].equals(v2[i])){
                return false;
            }
        }
        return true;
    }

    public static String stringValue(Object v, String defaultValue) {

        if(v == null) {
            return defaultValue;
        }

        if(v instanceof String) {
            return (String) v;
        }

        return v.toString();
    }

    public static int intValue(Object v, int defaultValue) {

        if(v == null) {
            return defaultValue;
        }

        if(v instanceof Integer) {
            return (Integer) v;
        }

        if(v instanceof  Number) {
            return ((Number) v).intValue();
        }

        if(v instanceof  String) {
            try {
                return Integer.valueOf((String) v);
            }
            catch(Throwable e) { }
        }

        return defaultValue;
    }

    public static long longValue(Object v, long defaultValue) {

        if(v == null) {
            return defaultValue;
        }

        if(v instanceof Long) {
            return (Long) v;
        }

        if(v instanceof  Number) {
            return ((Number) v).longValue();
        }

        if(v instanceof  String) {
            try {
                return Long.valueOf((String) v);
            }
            catch(Throwable e) { }
        }

        return defaultValue;
    }

    public static boolean booleanValue(Object v, boolean defaultValue) {

        if(v == null) {
            return defaultValue;
        }

        if(v instanceof Boolean) {
            return (Boolean) v;
        }

        if(v instanceof  String) {
            return "true".equals(v) || "yes".equals(v);
        }

        return defaultValue;
    }

    public static float floatValue(Object v, float defaultValue) {

        if(v == null) {
            return defaultValue;
        }

        if(v instanceof Float) {
            return (Float) v;
        }

        if(v instanceof  Number) {
            return ((Number) v).floatValue();
        }

        if(v instanceof  String) {
            try {
                return Float.valueOf((String) v);
            }
            catch(Throwable e) { }
        }

        return defaultValue;
    }

    public static double doubleValue(Object v, double defaultValue) {

        if(v == null) {
            return defaultValue;
        }

        if(v instanceof Double) {
            return (Double) v;
        }

        if(v instanceof  Number) {
            return ((Number) v).doubleValue();
        }

        if(v instanceof  String) {
            try {
                return Double.valueOf((String) v);
            }
            catch(Throwable e) { }
        }

        return defaultValue;
    }

}
