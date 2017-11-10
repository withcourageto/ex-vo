package top.cmoon.commons.exvo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.FatalBeanException;
import org.springframework.util.Assert;

import java.beans.PropertyDescriptor;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;

/**
 * Created by ye on 2017/8/29.
 */
public class BeanUtils extends org.springframework.beans.BeanUtils {


    /**
     * 跳过{@link BeanUtils#deepCopyProperties}深度复制的注解
     *
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface SkipDeepCopy {}

    private static final Logger logger = LoggerFactory.getLogger(BeanUtils.class);

    /**
     *
     * @param source 资源（修改成的对象）
     * @param target 目标（原对象）
     * @param nullCopy 资源为null是否拷贝，true拷贝，false不拷贝
     * @param excluedFieldName 不拷贝的属性名，格式[类名.属性名,...]，如果为自定义类可以在属性上添加注解@SkipDeepCopy
     */
    public static void deepCopyProperties(Object source, Object target, boolean nullCopy, List<String> excluedFieldName) {

        try {
            Class<?> tc = target.getClass();
            Class<?> sc = source.getClass();

            for (Field tf : tc.getDeclaredFields()) {
                String tfName = tf.getName();
                String tcName = tc.getName();
                SkipDeepCopy skipAnno = tf.getAnnotation(SkipDeepCopy.class);
                if (skipAnno != null) continue;
                if (excluedFieldName != null && excluedFieldName.contains(tcName + "." + tfName)) continue;
                Field sf = null;
                try {
                    sf = sc.getDeclaredField(tf.getName());
                } catch (NoSuchFieldException e) {
                    logger.debug("没有属性" + tf.getName(), e);
                }
                if (!nullCopy && sf == null) continue;

                tf.setAccessible(true);
                sf.setAccessible(true);
                Object tfValue = tf.get(target);
                Object sfValue = sf.get(source);

                if (tf.getType().getClassLoader() != null) { // 自定义类

                    if (sfValue != null) {
                        if (tfValue == null) tf.set(target, sfValue);
                        else {
                            deepCopyProperties(sfValue, tfValue, nullCopy, excluedFieldName);
                        }
                    }
                } else {

                    tf.set(target, sfValue);
                }
            }
        } catch (Exception e) {
            logger.error("复制对象报错", e);
            throw new RuntimeException(e);
        }

    }



    public static void copyProperties(Object source, Object target) throws BeansException {
//        Assert.notNull(source, "Source must not be null");
//        Assert.notNull(target, "Target must not be null");
        Class<?> actualEditable = target.getClass();
        PropertyDescriptor[] targetPds = getPropertyDescriptors(actualEditable);
        for (PropertyDescriptor targetPd : targetPds) {
            if (targetPd.getWriteMethod() != null) {
                PropertyDescriptor sourcePd = getPropertyDescriptor(source.getClass(), targetPd.getName());
                if (sourcePd != null && sourcePd.getReadMethod() != null) {
                    try {
                        Method readMethod = sourcePd.getReadMethod();
                        if (!Modifier.isPublic(readMethod.getDeclaringClass().getModifiers())) {
                            readMethod.setAccessible(true);
                        }
                        Object value = readMethod.invoke(source);
                        // 这里判断以下value是否为空 当然这里也能进行一些特殊要求的处理 例如绑定时格式转换等等
                        if (value != null) {
                            Method writeMethod = targetPd.getWriteMethod();
                            if (!Modifier.isPublic(writeMethod.getDeclaringClass().getModifiers())) {
                                writeMethod.setAccessible(true);
                            }
                            writeMethod.invoke(target, value);
                        }
                    } catch (Throwable ex) {
                        throw new FatalBeanException("Could not copy properties from source to target", ex);
                    }
                }
            }
        }
    }


    public static void copyProperty(Object source, Object target, String propertyName) throws BeansException {
        Assert.notNull(source, "Source must not be null");
        Assert.notNull(target, "Target must not be null");

        PropertyDescriptor targetPd = getPropertyDescriptor(target.getClass(), propertyName);
        PropertyDescriptor sourcePd = getPropertyDescriptor(source.getClass(), propertyName);
        if (targetPd != null && targetPd.getWriteMethod() != null) {
            if (sourcePd != null && sourcePd.getReadMethod() != null) {

                try {
                    Method readMethod = sourcePd.getReadMethod();
                    if (!Modifier.isPublic(readMethod.getDeclaringClass().getModifiers())) {
                        readMethod.setAccessible(true);
                    }

                    Object value = readMethod.invoke(source);

                    Method writeMethod = targetPd.getWriteMethod();
                    if (!Modifier.isPublic(writeMethod.getDeclaringClass().getModifiers())) {
                        writeMethod.setAccessible(true);
                    }
                    writeMethod.invoke(target, value);

                } catch (Throwable ex) {
                    throw new FatalBeanException("Could not copy properties from source to target", ex);
                }
            } else {
                throw new RuntimeException("没有获取属性的方法：" + source.getClass().getName() + propertyName);
            }
        } else {
            throw new RuntimeException("没有写入的方法：" + target.getClass().getName() + "" + propertyName);
        }


    }


}
