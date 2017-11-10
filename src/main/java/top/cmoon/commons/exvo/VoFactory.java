package top.cmoon.commons.exvo;

import javassist.*;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ClassFile;
import javassist.bytecode.ConstPool;
import javassist.bytecode.FieldInfo;
import javassist.bytecode.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.cmoon.commons.exvo.anno.VOField;
import top.cmoon.commons.exvo.anno.VOFieldMapping;
import top.cmoon.commons.exvo.anno.VOFields;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Administrator on 2017/8/31.
 */
public class VoFactory {

    private static Logger logger = LoggerFactory.getLogger(VoFactory.class);

    private static final String VO_CLASS_SUFFIX = "$VO";

    /**
     * 根据 model class 创建VO Object
     *
     * @param modelClass
     * @return
     */
    public static Object createVoObject(Class<?> modelClass) {

        if (modelClass == null) {
            throw new NullPointerException();
        }

        try {
            return getVoClass(modelClass).newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static Map<String, Class<?>> cached = new ConcurrentHashMap<>();

    public static Class<?> getVoClass(Class<?> modelClass) {

        if (modelClass == null) {
            throw new NullPointerException();
        }

        Class<?> cachedClass = cached.get(modelClass.getName());
        if (cachedClass != null) {
            logger.info("get vo class from cache:{}" + modelClass.getName());
            return cachedClass;
        }

        ClassPool pool = new ClassPool(true);
        pool.appendClassPath(new LoaderClassPath(Thread.currentThread().getContextClassLoader()));

        String entityClassName = modelClass.getCanonicalName();
        String voClassName = entityClassName + VO_CLASS_SUFFIX;
        try {
            CtClass ctClass = pool.makeClass(voClassName);
            ClassFile ccFile = ctClass.getClassFile();
            ConstPool constpool = ccFile.getConstPool();


            List<Field> voFields = VOUtil.getVOFields(modelClass);
            for (Field voField : voFields) {
                if (VOUtil.withoutVoAnno(voField)) {
                    CtField ctField = new CtField(pool.get(voField.getType().getCanonicalName()), voField.getName(), ctClass);
                    ctField.setModifiers(Modifier.PRIVATE);

                    ctClass.addMethod(CtNewMethod.setter("set" + StringUtil.upperFirstChar(voField.getName()), ctField));
                    ctClass.addMethod(CtNewMethod.getter((voField.getType() == boolean.class ? "is" : "get") + StringUtil.upperFirstChar(voField.getName()), ctField));
                    ctClass.addField(ctField);

                    copyAnno(constpool, voField, ctField);
                    continue;
                }


                List<VOField> voFieldAnnos = VOUtil.getVOFieldAnnos(voField);
                for (VOField voFieldAnno : voFieldAnnos) {
                    String viewKeyName = VOUtil.getViewKeyName(voField, voFieldAnno);
                    createVoFieldAndMethod(pool, ctClass, constpool, voField, viewKeyName);
                }

                VOFieldMapping voFieldMappingAnno = voField.getAnnotation(VOFieldMapping.class);
                if (voFieldMappingAnno != null) {
                    String viewKeyName = VOUtil.getViewKeyName(voField, voFieldMappingAnno);
                    createVoFieldAndMethod(pool, ctClass, constpool, voField, viewKeyName);
                }

            }


            Class<?> resultClass = ctClass.toClass();
            cached.put(modelClass.getName(), resultClass);
            return resultClass;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    private static void createVoFieldAndMethod(ClassPool pool, CtClass ctClass, ConstPool constpool, Field voField, String viewKeyName) throws CannotCompileException, NotFoundException, IllegalAccessException, InvocationTargetException {
        CtField ctField = new CtField(pool.get("java.lang.Object"), viewKeyName, ctClass);
        ctField.setModifiers(Modifier.PRIVATE);

        ctClass.addMethod(CtNewMethod.setter("set" + StringUtil.upperFirstChar(viewKeyName), ctField));
        ctClass.addMethod(CtNewMethod.getter("get" + StringUtil.upperFirstChar(viewKeyName), ctField));
        ctClass.addField(ctField, CtField.Initializer.constant("default"));

        copyAnno(constpool, voField, ctField);
    }

    private static void copyAnno(ConstPool constpool, Field voField, CtField ctField) throws IllegalAccessException, InvocationTargetException {

        FieldInfo fieldInfo = ctField.getFieldInfo();

        // 属性附上注解
        java.lang.annotation.Annotation[] fieldAnnos = voField.getAnnotations();
        for (java.lang.annotation.Annotation anno : fieldAnnos) {
            AnnotationsAttribute fieldAttr = new AnnotationsAttribute(constpool, AnnotationsAttribute.visibleTag);
            String annoName = anno.annotationType().getName();

            if (annoName.equals(VOField.class.getName()) || annoName.equals(VOFieldMapping.class.getName()) || annoName.equals(VOFields.class.getName())) {
                continue;
            }
            if (anno.annotationType().getPackage().getName().equals("javax.persistence")) {
                continue;
            }

            if (anno.annotationType().getPackage().getName().startsWith("org.hibernate")) {
                continue;
            }


            Annotation autowired = new Annotation(annoName, constpool);

            Method[] methods = anno.annotationType().getDeclaredMethods();
            for (Method method : methods) {
                Class<?> returnType = method.getReturnType();
                // FIXME
                if (returnType == String.class) {
                    autowired.addMemberValue(method.getName(), new StringMemberValue((String) method.invoke(anno), constpool));
                } else if (returnType == int.class) {
                    IntegerMemberValue integerMemberValue = new IntegerMemberValue(constpool);
                    integerMemberValue.setValue((Integer) method.invoke(anno));
                    autowired.addMemberValue(method.getName(), integerMemberValue);
                } else if (returnType == int[].class) {

                } else if (returnType == boolean.class) {
                    autowired.addMemberValue(method.getName(), new BooleanMemberValue((Boolean) method.invoke(anno), constpool));
                } else if (returnType.isAnnotation()) {
                    // FIXME ： 暂时不处理注解
                    //Annotation annotation = new Annotation(returnType.getName(), constpool);
                    //Object obj = method.invoke(anno);
                    //autowired.addMemberValue(method.getName(), new AnnotationMemberValue(annotation, constpool));
                } else if (returnType.isEnum()) {
                    EnumMemberValue val = new EnumMemberValue(constpool);

                    Enum original = (Enum) method.invoke(anno);

                    val.setType(original.getDeclaringClass().getName());
                    val.setValue(original.name());
                    autowired.addMemberValue(method.getName(), val);
                }
            }

            fieldAttr.addAnnotation(autowired);
            fieldInfo.addAttribute(fieldAttr);
        }
    }


}
