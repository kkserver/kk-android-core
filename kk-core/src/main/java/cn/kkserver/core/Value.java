package cn.kkserver.core;

import java.lang.reflect.Array;
import java.util.List;
import java.util.Map;

/**
 * Created by zhanghailong on 2016/11/8.
 */

public final class Value {

    public Object get(Object object,String name) {

        Object v = null;

        if(object != null) {

            if(object instanceof Map) {
                v = ((Map<String,Object>) object).get(name);
            }
            else if(object instanceof List) {

                int i = Integer.valueOf(name);

                if(i >=0 && i < ((List<?>) object).size()) {
                    v = ((List<?>) object).get(i);
                }
            }
            else if(object.getClass().isArray()) {

                int size = Array.getLength(object);
                int i = Integer.valueOf(name);

                if(i >= 0 && i < size) {
                    v = Array.get(object,i);
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
                    catch (Throwable ee) {
                    }
                }
            }

        }

        return v;
    }

    public void set(Object object,String name,Object value) {

        if(object != null) {

            if(object instanceof Map) {
                if(value == null) {
                    if(((Map<String,Object>) object).containsKey(name)) {
                        ((Map<String, Object>) object).remove(name);
                    }
                }
                else {
                    ((Map<String, Object>) object).put(name, value);
                }
            }
            else if(object instanceof List) {

                int i = Integer.valueOf(name);
                if(i >= 0 && i < ((List<?>) object).size()) {
                    ((List<Object>) object).set(i,value);
                }
                else if(i == ((List<?>) object).size()) {
                    ((List<Object>) object).add(value);
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

    public Object get(Object object, String[] keys) {

        Object v = object;
        int i = 0;

        while(v != null && keys != null && i < keys.length) {
            v = get(v,keys[i++]);
        }

        return v;
    }

    public void set(Object object, String[] keys, Object value) {

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
}
