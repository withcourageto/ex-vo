package top.cmoon.commons.exvo;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.beanutils.PropertyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.cmoon.commons.exvo.anno.*;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * Created by Administrator on 2017/8/23.
 */
public class VOUtil {


    public static final Logger logger = LoggerFactory.getLogger(VOUtil.class);

    /**
     * 获取Vo字段，
     * <p/>
     * 如果是Java官方类库中的类，直接返回空List（没有元素）
     * <p/>
     * 如果类使用{@link  top.cmoon.commons.exvo.anno.VOType VOType} 标识，
     * 并且值为 {@link  top.cmoon.commons.exvo.anno.ModelToViewPolicy#ALL_FIELD ALL_FIELD},
     * 那么所有的非静态字段都是 VoField， 否则只有使用
     * {@link cn.net.comsys.xuegong.wechat.base.anno.VOField},
     * {@link cn.net.comsys.xuegong.wechat.base.anno.VOFields},
     * {@link cn.net.comsys.xuegong.wechat.base.anno.VOFieldMapping}
     * 标识的字段才能是VO字段
     * <p/>
     * 注意，此中方式不支持获取父类的 {@code Field}
     *
     * @param modelClass
     * @return
     */
    public static List<Field> getVOFields(Class<?> modelClass) {

        if (ClassUtil.isNotUserClass(modelClass)) {
            return Collections.emptyList();
        }

        List<Field> viewFields = null;
        if (modelClass.isAnnotationPresent(VOType.class)) {
            VOType voTypeAnno = modelClass.getAnnotation(VOType.class);
            if (voTypeAnno.value() == ModelToViewPolicy.ALL_FIELD) {
                viewFields = ClassUtil.getUserClassInstanceFields(modelClass);
            }
        }

        if (viewFields == null) {
            viewFields = ClassUtil.getFieldsWithinAnno(modelClass, VOField.class, VOFields.class, VOFieldMapping.class);
        }

        return viewFields;

    }

    public static Collection<Object> modelToView(Collection<?> modelList) {

        Collection<Object> result = newInstanceFrom(modelList);

        for (Object model : modelList) {
            result.add(modelToView(model));
        }

        return result;
    }

    private static Collection<Object> newInstanceFrom(Collection<?> modelList) {
        try {
            return modelList.getClass().newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public static List<Object> modelToView(List<?> modelList) {

        List<Object> result = new ArrayList<>(modelList.size());
        for (Object model : modelList) {
            result.add(modelToView(model));
        }

        return result;
    }


    public static Map<String, Object> modelToView(JSONObject jsonObject) {
        Map<String, Object> result = new HashMap<>();

        for (Map.Entry<String, Object> entry : (jsonObject).entrySet()) {
            result.put(entry.getKey().toString(), modelToView(entry.getValue()));
        }

        return result;
    }


    public static Map<String, Object> modelToView(Map<?, ?> model) {
        Map<String, Object> result = new HashMap<>();

        for (Map.Entry<?, ?> entry : ((Map<?, ?>) model).entrySet()) {
            result.put(entry.getKey().toString(), modelToView(entry.getValue()));
        }

        return result;
    }

    public static Set<Object> modelToView(Set<?> modelSet) {

        if (modelSet == null) {
            return Collections.emptySet();
        }
        Set<Object> result = new HashSet<>(modelSet.size());
        for (Object model : modelSet) {
            result.add(modelToView(model));
        }

        return result;
    }


    /**
     * 此方法将会根据 model 生成相应的Vo类，并根据VO 规则，把相应的值写入Vo对象， 由于Vo值的的不可确定，所以采用Object表示，
     *
     * @param model
     * @return
     * @throws java.lang.NullPointerException if parameter model is null
     */
    static Object modelToView0(Object model) {

        if (model == null) {
            throw new NullPointerException();
        }

        Class<?> modelClass = model.getClass();
        if (ClassUtil.isNotUserClass(modelClass)) {
            throw new IllegalArgumentException("type of parameter model must be user class(not jdk library class)");
        }

        Object instance = VoFactory.createVoObject(modelClass);

        List<Field> viewFields = getVOFields(modelClass);

        if (viewFields.size() == 0) {
            return instance;
        }
        try {
            for (Field field : viewFields) {

                if (Modifier.isStatic(field.getModifiers())) {
                    logger.info("static field , not view object field, type:{}, field:{}", modelClass.getName(), field.getName());
                    continue;
                }

                field.setAccessible(true);
                Object originalFieldVal = field.get(model);

                List<VOField> voFieldAnnos = getVOFieldAnnos(field);
                VOFieldMapping voFieldMappingAnno = field.getAnnotation(VOFieldMapping.class);

                if (withoutVoAnno(voFieldAnnos, voFieldMappingAnno)) {
                    BeanUtils.copyProperty(model, instance, field.getName());
                } else {

                    // VOField, VOFields parse
                    for (VOField voFieldAnno : voFieldAnnos) {
                        String viewName = getViewKeyName(field, voFieldAnno);
                        Object viewVal = getViewVal(field, originalFieldVal, voFieldAnno);
                        PropertyUtils.setProperty(instance, viewName, viewVal);
                    }

                    // VOFieldMapping parse
                    if (voFieldMappingAnno != null) {
                        String viewName = getViewKeyName(field, voFieldMappingAnno);
                        Object viewVal = evalMappingVal(originalFieldVal, voFieldMappingAnno.rule());
                        PropertyUtils.setProperty(instance, viewName, viewVal);
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return instance;
    }


    public static Object[] modelToView(Object[] modelArr) {

        if (modelArr == null) {
            return new Object[0];
        }

        Object[] result = new Object[modelArr.length];
        for (int i = 0; i < modelArr.length; i++) {
            result[i] = modelToView(modelArr[i]);
        }
        return result;
    }


    /**
     * 将普通实体根据 Vo 转换规则转换为 VoObject
     * <p/>
     * 如果参数model是null ，将会被转换为空的 Map
     *
     * @param model 普通实体
     * @return 转换后的Vo对象
     */
    public static Object modelToView(Object model) {


        if (model == null) {
            return Collections.emptyMap();
        }

        if (model instanceof Map) {
            return modelToView(((Map) model));
        }

        if (model instanceof List) {
            return modelToView(((List) model));
        }

        if (model instanceof Set) {
            return modelToView((Set) model);
        }

        if (model instanceof JSONObject) {
            return modelToView(((JSONObject) model));
        }

        if (model.getClass().isArray()) {
            Class<?> elementType = model.getClass().getComponentType();
            if (elementType.isPrimitive()) {
                return model;
            }
            return modelToView((Object[]) model);
        }


        Class<?> modelClass = model.getClass();
        if (ClassUtil.isNotUserClass(modelClass)) {
            return model;
        }


        // 1. 获取Vo 字段
        List<Field> viewFields = getVOFields(modelClass);

        if (viewFields.size() == 0) {
            return Collections.emptyMap();
        }

        if (1 == 1)
            return modelToView0(model);
        return modelToView1(model, modelClass, viewFields);
    }

    @Deprecated
    private static Object modelToView1(Object model, Class<?> modelClass, List<Field> viewFields) {
        Map<String, Object> result = new HashMap<>();
        // 2 转换字段值到View Map
        //Map<String, Object> result = new HashMap<>(viewFields.size());
        try {
            for (Field field : viewFields) {

                if (Modifier.isStatic(field.getModifiers())) {
                    logger.info("static field , not view object field, type:{}, field:{}", modelClass.getName(), field.getName());
                    continue;
                }

                field.setAccessible(true);

                Object originalFieldVal = field.get(model);

                List<VOField> voFieldAnnos = getVOFieldAnnos(field);
                VOFieldMapping voFieldMappingAnno = field.getAnnotation(VOFieldMapping.class);

                if (withoutVoAnno(voFieldAnnos, voFieldMappingAnno)) {
                    putVal(result, field.getName(), originalFieldVal);
                } else {

                    // VOField, VOFields parse
                    for (VOField voFieldAnno : voFieldAnnos) {
                        String viewName = getViewKeyName(field, voFieldAnno);
                        Object viewVal = getViewVal(field, originalFieldVal, voFieldAnno);

                        putVal(result, viewName, viewVal);
                    }

                    // VOFieldMapping parse
                    if (voFieldMappingAnno != null) {
                        String viewKey = getViewKeyName(field, voFieldMappingAnno);
                        Object viewVal = evalMappingVal(originalFieldVal, voFieldMappingAnno.rule());

                        putVal(result, viewKey, viewVal);
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return result;
    }

    private static Object getViewVal(Field field, Object originalFieldVal, VOField voFieldAnno) {
        Class<?> fieldType = field.getType();
        if (ClassUtil.isUserClass(fieldType)) {
            if (voFieldAnno.recursion()) {
                return modelToView(originalFieldVal);
            }
        }

        if (voFieldAnno.nullToEmpty() && originalFieldVal == null && fieldType == String.class) {
            return "";
        }

        if (voFieldAnno.emptyToNull() && fieldType == String.class && "".equals(originalFieldVal)) {
            return null;
        }
        Object scriptEvalVal = evalScriptVal(originalFieldVal, voFieldAnno);

        return scriptEvalVal;
    }

    private static boolean withoutVoAnno(List<VOField> voAnno, VOFieldMapping field) {
        return voAnno.size() == 0 && field == null;
    }


    public static boolean withoutVoAnno(Field field) {

        List<VOField> voAnnoList = getVOFieldAnnos(field);
        return voAnnoList.size() == 0 && !field.isAnnotationPresent(VOFieldMapping.class);
    }


    private static void putVal(Map<String, Object> map, String key, Object val) {
        if (map.containsKey(key)) {
            logger.warn("model to view method : already exists key:{}, will override it", key);
        }
        map.put(key, val);
    }


    public static String getViewKeyName(Field field, VOField voFieldAnno) {
        return voFieldAnno == null ? field.getName() :
                voFieldAnno.value().equals("") ? field.getName() : voFieldAnno.value();
    }

    public static String getViewKeyName(Field field, VOFieldMapping voFieldMappingAnno) {
        return voFieldMappingAnno == null ? field.getName() :
                voFieldMappingAnno.value().equals("") ? field.getName() : voFieldMappingAnno.value();
    }


    private static Object evalScriptVal(Object original, VOField voFieldAnno) {
        if (voFieldAnno == null) {
            return original;
        }

        if (voFieldAnno.script() == null) {
            return original;
        }

        if ("".equals(voFieldAnno.script().trim())) {
            return original;
        }

        return GroovyScriptUtil.eval(original, voFieldAnno.script());
    }


    private static Object evalMappingVal(Object original,
                                         String rule) {

        Object mapping = GroovyScriptUtil.eval(original, rule);


        if (mapping == null) {
            throw new RuntimeException("rule 表达式不对，期望类型:" + "[1:'sdfds',2:'sdf']" + ", 实际值: " + rule);
        }

        if (!(mapping instanceof Map)) {
            throw new RuntimeException("rule 表达式不对，期望类型:" + "[1:'sdfds',2:'sdf']" + ", 实际值: " + rule);
        }

        if (((Map) mapping).containsKey(original)) {
            return ((Map) mapping).get(original);
        }

        logger.warn("由于您配置的rule找不到匹配的键，返回原始值,rule:{}, original:{}", rule, original);
        return original;
    }


    public static List<VOField> getVOFieldAnnos(Field field) {
        List<VOField> voFieldAnnos = new ArrayList<>(1);

        VOField voFieldAnno = field.getAnnotation(VOField.class);
        if (voFieldAnno != null) {
            voFieldAnnos.add(voFieldAnno);
        }

        VOFields voFieldsAnno = field.getAnnotation(VOFields.class);
        if (voFieldsAnno != null) {
            for (VOField voField : voFieldsAnno.value()) {
                voFieldAnnos.add(voField);
            }
        }
        return voFieldAnnos;
    }


}