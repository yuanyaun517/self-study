package cn.only.hw.secondmarketserver.util;

/**
 * 描述          ：基于ThreadLocal封装的工具类 用于保存和获取当前登录用户的id
 * 类名          ：BaseContext
 */
public class BaseContext {
    private static final ThreadLocal<Integer> threadLocal = new ThreadLocal<>();

    public static void setCurrentId(int id){
        threadLocal.set(id);
    }

    public static int getCurrentId(){
        return threadLocal.get();
    }
    
}
