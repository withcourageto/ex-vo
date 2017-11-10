package top.cmoon.commons.exvo;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ClassUtil {

    /**
     * 判断传入Class是用户自定义类还是Java library的类
     *
     * @param clazz
     * @return
     */
    public static boolean isUserClass(Class<?> clazz) {
        return clazz.getClassLoader() != null;
    }

    /**
     * 判断传入Class是用户自定义类还是Java library的类
     *
     * @param clazz
     * @return
     */
    public static boolean isNotUserClass(Class<?> clazz) {
        return clazz.getClassLoader() == null;
    }


    /**
     * 获取自定义用户类的字段列表
     *
     * @param clazz
     * @return
     */
    public static List<Field> getUserClassField(Class<?> clazz) {
        return getUserClassField(clazz, false);
    }

    public static List<Field> getUserClassInstanceFields(Class<?> clazz) {

        List<Field> temp = getUserClassField(clazz, false);


        List<Field> result = new ArrayList<>();
        for (Field field : temp) {
            if (Modifier.isStatic(field.getModifiers()))
                continue;
            result.add(field);
        }

        return result;
    }


    /**
     * @param clazz
     * @param getSupperClassField
     * @return
     */
    public static List<Field> getUserClassField(Class<?> clazz, boolean getSupperClassField) {

        if (clazz == null) {
            throw new NullPointerException();
        }


        List<Field> result = new ArrayList<>();

        if (isUserClass(clazz)) {
            result.addAll(Arrays.asList(clazz.getDeclaredFields()));
        }

        if (getSupperClassField) {
            Class<?> supperClass = clazz.getSuperclass();
            if (supperClass != null && isUserClass(supperClass)) {
                result.addAll(getUserClassField(supperClass, getSupperClassField));
            }
        }


        return result;
    }


    /**
     * 从 Class中获取使用注解标识的字段， 只能获取类自己的字段，不能获取超类的字段
     * <p/>
     * 如果传入多个注解，只要字段被其中一个注解标注，将会被获取到
     *
     * @param clazz
     * @param annoClasses
     * @return
     */
    public static List<Field> getFieldsWithinAnno(Class<?> clazz, Class<? extends Annotation>... annoClasses) {
        return getFieldsWithinAnnoAndType(clazz, null, annoClasses);
    }


    /**
     * 从 Class中获取使用指定注解标识，并且字段类型是指定类型的字段， 自能获取类自己的字段，不能获取超类的字段
     *
     * @param clazz
     * @param annoClasses
     * @param fieldType
     * @return
     */
    public static List<Field> getFieldsWithinAnnoAndType(Class<?> clazz, Class<?> fieldType, Class<? extends Annotation>... annoClasses) {
        List<Field> result = new ArrayList<>();
        Field[] declaredFields = clazz.getDeclaredFields();
        fieldFor:
        for (Field filed : declaredFields) {
            if ((fieldType == null ? true : filed.getType() == fieldType)) {
                for (Class<? extends Annotation> annoClass : annoClasses) {
                    if (filed.isAnnotationPresent(annoClass)) {
                        result.add(filed);
                        continue fieldFor;
                    }
                }

            }
        }
        return result;
    }


    /**
     * 获取没有使用指定注解标识的字段
     *
     * @param clazz
     * @param annoClasses
     * @return
     */
    public static List<Field> getFieldsWithoutAnno(Class<?> clazz, Class<? extends Annotation>... annoClasses) {

        List<Field> result = new ArrayList<Field>();
        Field[] declaredFields = clazz.getDeclaredFields();
        f:
        for (Field filed : declaredFields) {
            for (Class<? extends Annotation> annoClass : annoClasses) {
                if (filed.isAnnotationPresent(annoClass)) {
                    continue f;
                }
            }
            result.add(filed);
        }
        return result;
    }


    /**
     * 获取clazz的超类的泛型参数的类型
     * <p/>
     * 注意： 由于Java泛型的实现机制， 只能获取超类的泛型参数，所以，此方法的参数必须是实现了带泛型的超类的class
     * <pre>
     *  class AbstractDao<M>{
     *  }
     *
     *  class DemoDao extends AbstractDao<Demo>{
     *  }
     *
     *  class DemoDao1 {
     *  }
     *
     *  ClassUtil.getSupperClassGenericType(DemoDao.class); // 正确
     *  ClassUtil.getSupperClassGenericType(DemoDao1.class); // 错误
     * </pre>
     *
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> Class<T> getSupperClassGenericType(Class<?> clazz) {
        return (Class<T>) ((ParameterizedType) clazz.getGenericSuperclass()).getActualTypeArguments()[0];
    }

}
