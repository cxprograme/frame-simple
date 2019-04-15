package com.cx.core;

import java.lang.annotation.Annotation;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Bean 容器
 */
public class BeanContainer {

    /**
     * 是否加载bean
     */
    private boolean isLoadBean=false;

    /**
     * 加载bean的注解列表
     */
    private static final List<Class<? extends Annotation>> BEAN_ANNOTATION
            = Arrays.asList(Component.class, Controller.class, Service.class, Repository.class);
    /**
     * 存放Bean 的Map
     */
    private final Map<Class<?>,Object> beanMap = new ConcurrentHashMap<>();

    /**
     * 获取bean 实例
     *
     * @param clz
     * @return
     */
    public Object getBean(Class<?> clz) {
        if (null == clz){
            return null;
        }
        return beanMap.get(clz);
    }

    /**
     * 获取所有的bean
     * @return
     */
    public Set<Object> getBeans(){
        return new HashSet<>(beanMap.values());
    }

    /**
     * 添加bean
     * @param clz
     * @param bean
     * @return
     */
    public Object addBean(Class<?> clz,Object bean){
        return beanMap.put(clz,bean);
    }

    /**
     * 移除bean
     * @param clz
     * @return
     */
    public Object removeBean(Class<?> clz){
        return beanMap.remove(clz);
    }

    /**
     * bean 数量
     * @return
     */
    public int size(){
        return beanMap.size();
    }

    /**
     * 所有Bean的Class集合
     * @return
     */
    public Set<Class<?>> getClasses(){
        return beanMap.keySet();
    }

    /**
     * 通过注解获取bean的class 集合
     * @param annotation
     * @return
     */
    public Set<Class<?>> getClassesByAnnotation(Class<? extends Annotation> annotation){
        return beanMap.keySet()
                .stream()
                .filter(clz -> clz.isAnnotationPresent(annotation))
                .collect(Collectors.toSet());
    }

    /**
     * 通过实现类或者父类获取Bean的Class集合
     */
    public Set<Class<?>> getClassesBySuper(Class<?> superClass) {
        return beanMap.keySet()
                .stream()
                .filter(superClass::isAssignableFrom)
                .filter(clz -> !clz.equals(superClass))
                .collect(Collectors.toSet());
    }
}
