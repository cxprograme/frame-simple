package com.cx.utils;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.JarURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.stream.Collectors;


/**
 * 类操作工具类
 */
@Slf4j
public class ClassUtils {
    private final static Logger log = LoggerFactory.getLogger(ClassUtils.class);
    /**
     * file形式url协议
     */
    public static final String FILE_PROTOCOL = "file";

    /**
     * jar形式url协议
     */
    public static final String JAR_PROTOCOL = "jar";

    /**
     * 获取classloder
     * @return
     */
    public static  ClassLoader getClassLoader(){
        return Thread.currentThread().getContextClassLoader();
    }

    /**
     * 实例化 class
     * 通过Class类中的静态方法forName，直接获取到一个类的字节码文件对象，此时该类还是源文件阶段，并没有变为字节码文件
     * @param className
     * @return
     */
    public static Class<?> loadClass(String className){
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            log.debug("newInstance error", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 实例化 class
     * @param className
     * @param <T>
     * @return
     */
    public static <T> T newInstance(String className){
        Class<?> claszz = loadClass(className);
        try {
            return (T)claszz.newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
            log.error("newInstance error", e);
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            log.error("newInstance error", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 设置类的属性值
     * @param field
     * @param target
     * @param value
     * @param accessible
     */
    public static void setField(Field field,Object target,Object value,boolean accessible){
        field.setAccessible(accessible);
        try {
            field.set(target,value);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    /**
     * 从Path 获取class
     * @param classPath
     * @param basePath
     * @param basePackage
     * @return
     */
    public static Class<?> getClassByPath(Path classPath, Path basePath, String basePackage){
        String packageName = classPath.toString().replace(classPath.toString(),"");
        String className = (basePackage+packageName)
                .replace("/","")
                .replace("\\","")
                .replace(".class","");
        return loadClass(className);
    }

    /**
     * 从Jar 获取class
     * @param jarEntry
     * @return
     */
    public static Class<?> getClassByJar(JarEntry jarEntry){
        String jarEntryName = jarEntry.getName();
        String className = jarEntryName.substring(0, jarEntryName.lastIndexOf(".")).replaceAll("/", ".");
        return loadClass(className);
    }

    public static Set<Class<?>> getPackageClass(String basePackage) {
        URL url =getClassLoader()
                .getResource(basePackage.replace(".", "/"));
        if (null == url){
            throw new RuntimeException("无法获取项目路径文件");
        }
        try {
            if (url.getProtocol().equalsIgnoreCase(FILE_PROTOCOL)){
                //普通文件夹
                File file = new File(url.getFile());
                Path basePath = file.toPath();
                return Files.walk(basePath)
                            .filter(path -> path.toFile().getName().endsWith(".class"))
                            .map(path -> getClassByPath(path, basePath, basePackage))
                            .collect(Collectors.toSet());

            }
            if (url.getProtocol().equalsIgnoreCase(JAR_PROTOCOL)){
                //jar包
                JarURLConnection jarURLConnection = (JarURLConnection) url.openConnection();
                return jarURLConnection.getJarFile()
                        .stream()
                        .filter(jarEntry -> jarEntry.getName().endsWith(".class"))
                        .map(ClassUtils::getClassByJar)
                        .collect(Collectors.toSet());
            }
            return Collections.emptySet();
        } catch (IOException e) {
            log.error("load package error", e);
            throw new RuntimeException(e);

        }
    }

}
