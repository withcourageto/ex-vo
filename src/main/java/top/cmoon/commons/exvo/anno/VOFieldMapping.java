package top.cmoon.commons.exvo.anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by Administrator on 2017/8/30.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface VOFieldMapping {

    String value() default "";


    /**
     * 请参考 groovy 字面量的Map
     * [0:'未提交', 1:'审批中', 2: '审批中', 3: '审批中', 4:'审批通过', 5:'拒绝', 6 :'驳回', 7:'审批中']
     *
     * @return
     */
    String rule();
}
