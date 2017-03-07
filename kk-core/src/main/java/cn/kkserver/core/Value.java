package cn.kkserver.core;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Created by zhanghailong on 2016/11/8.
 */

@SuppressWarnings("unchecked")
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

                Class<?> clazz = object.getClass();

                while(clazz != Object.class) {

                    try {
                        java.lang.reflect.Field fd = object.getClass().getField(name);
                        v = fd.get(object);
                        break;
                    } catch (Throwable e) {
                        try {
                            java.lang.reflect.Method md = object.getClass().getMethod(name);
                            v = md.invoke(object);
                            break;
                        }
                        catch (Throwable ee) {}
                    }

                    clazz = clazz.getSuperclass();
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

                Class<?> clazz = object.getClass();

                while(clazz != Object.class) {

                    try {
                        java.lang.reflect.Field fd = object.getClass().getField(name);
                        set(object,fd,value);
                        break;
                    } catch (Throwable e) {
                    }

                    clazz = clazz.getSuperclass();
                }

            }

        }

    }

    public static <T extends Object> T convert(Object value, Class<T> type)  {

        if(value == null) {
            return null;
        }

        if(type.isAssignableFrom(value.getClass())) {
            return (T) value;
        }

        if(type == int.class || type == Integer.class || type == short.class || type == Short.class) {
            return (T) (Integer) intValue(value,0);
        }

        if(type == long.class || type == Long.class) {
            return (T) (Long) longValue(value,0);
        }

        if(type == float.class || type == Float.class) {
            return (T) (Float) floatValue(value,0f);
        }

        if(type == double.class || type == Double.class) {
            return (T) (Double) doubleValue(value,0d);
        }

        if(type == boolean.class || type == Boolean.class) {
            return (T) (Boolean) booleanValue(value,false);
        }

        if(type == String.class) {
            return (T) stringValue(value,null);
        }

        try {

            T v = type.newInstance();

            Class<?> clazz = type;
            Set<String> keys = new TreeSet<String>();
            while(clazz != Object.class) {

                Field[] fds = clazz.getFields();

                for(Field fd : fds) {

                    if(((Modifier.PUBLIC | Modifier.STATIC) & fd.getModifiers()) == Modifier.PUBLIC
                            && ! keys.contains(fd.getName())) {
                        set(v,fd,get(value,fd.getName()));
                        keys.add(fd.getName());
                    }

                }
                clazz = clazz.getSuperclass();
            }

            return v;

        } catch (InstantiationException e) {
        } catch (IllegalAccessException e) {
        }

        return null;
    }

    public static void set(Object object,Field fd, Object value) {

        if(fd.getType().isArray()) {

            final Class<?> type = fd.getType().getComponentType();

            final List<Object> vs = new ArrayList<Object>();

            each(value, new IEacher() {
                @Override
                public boolean each(Object key, Object value) {
                    Object v = convert(value,type);
                    vs.add(v);
                    return true;
                }
            });
            try {
                fd.set(object,vs.toArray((Object[])Array.newInstance(fd.getType().getComponentType(),vs.size())));
            } catch (IllegalAccessException e) {
            }
        } else {
            Object v = convert(value,fd.getType());
            try {
                fd.set(object,v);
            } catch (IllegalAccessException e) {
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

            String key = keys[i];

            if(i + 1 == keys.length){
                set(v,key,value);
                break;
            }

            Object vv = get(v,key);

            if(vv == null) {
                set(v,key,new TreeMap<String,Object>());
                v = get(v,key);
            } else {
                v = vv;
            }

            i ++;
        }

    }

    public static void each(Object object,IEacher eacher) {

        if(object != null) {

            if(object instanceof Map) {
                for(Object key : ((Map) object).keySet()) {
                    if(eacher.each(key,((Map) object).get(key)) == false) {
                        break;
                    }
                }
            }
            else if(object instanceof List) {
                int size = ((List) object).size();
                for(int i=0;i<size;i++) {
                    if(eacher.each(i,((List) object).get(i)) == false) {
                        break;
                    }
                }
            }
            else if(object.getClass().isArray()) {
                int size = Array.getLength(object);
                for(int i=0;i<size;i++) {
                    if(eacher.each(i,Array.get(object,i)) == false) {
                        break;
                    }
                }
            }
            else if(object instanceof IKeys) {
                for(String key : ((IKeys) object).keys()) {
                    if(eacher.each(key,Value.get(object,key)) == false) {
                        break;
                    }
                }
            }
            else {
                Class<?> clazz = object.getClass();
                Set<String> keys = new TreeSet<String>();
                while(clazz != Object.class) {

                    Field[] fds = clazz.getFields();

                    for(Field fd : fds) {

                        if(! keys.contains(fd.getName())) {

                            try {
                                if(eacher.each(fd.getName(),fd.get(object)) == false) {
                                    break;
                                }
                            } catch (IllegalAccessException e) {
                            }

                            keys.add(fd.getName());
                        }

                    }
                    clazz = clazz.getSuperclass();
                }
            }
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

        if(v.getClass() == int.class) {
            return (int) v;
        }

        if(v.getClass() == long.class) {
            return (int)(long) v;
        }

        if(v.getClass() == short.class) {
            return (int)(short) v;
        }

        if(v.getClass() == float.class) {
            return (int) (float) v ;
        }

        if(v.getClass() == double.class) {
            return (int) (double) v ;
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

        if(v.getClass() == int.class) {
            return (long)(int) v;
        }

        if(v.getClass() == long.class) {
            return (long) v;
        }

        if(v.getClass() == short.class) {
            return (long)(short) v;
        }

        if(v.getClass() == float.class) {
            return (long) (float) v ;
        }

        if(v.getClass() == double.class) {
            return (long) (double) v ;
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

        if(v.getClass() == boolean.class) {
            return (boolean) v;
        }

        if(v.getClass() == int.class) {
            return (int) v != 0;
        }

        if(v.getClass() == long.class) {
            return (long) v != 0;
        }

        if(v.getClass() == short.class) {
            return (short) v != 0;
        }

        if(v.getClass() == float.class) {
            return (float) v != 0;
        }

        if(v.getClass() == double.class) {
            return (double) v != 0;
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

        if(v.getClass() == int.class) {
            return (float) (int) v;
        }

        if(v.getClass() == long.class) {
            return (float) (long) v;
        }

        if(v.getClass() == short.class) {
            return (float) (short) v;
        }

        if(v.getClass() == float.class) {
            return (float) v;
        }

        if(v.getClass() == double.class) {
            return (float) (double) v;
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

        if(v.getClass() == int.class) {
            return (double) (int) v;
        }

        if(v.getClass() == long.class) {
            return (double) (long) v;
        }

        if(v.getClass() == short.class) {
            return (double) (short) v;
        }

        if(v.getClass() == float.class) {
            return (double) (float) v;
        }

        if(v.getClass() == double.class) {
            return (double) v;
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
