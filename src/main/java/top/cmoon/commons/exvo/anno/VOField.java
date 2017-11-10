package top.cmoon.commons.exvo.anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标识Vo 字段， 参考 {@link cn.net.comsys.xuegong.wechat.util.VOUtil#modelToView}
 * <p/>
 * Created by Administrator on 2017/8/23.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface VOField {


    /**
     * view 名称 ，默认是字段名
     *
     * @return
     */
    String value() default "";


    /**
     * 是否将空字符串转换为 null
     *
     * @return
     */
    boolean emptyToNull() default false;


    /**
     * 如果属性是字符串类型，是否将空转换为空白字符串
     *
     * @return
     */
    boolean nullToEmpty() default false;


    /**
     * 如果用户是自定义类型是否递归，查找 VOField,
     * ，如果配置为 false, 将会直接使用对象值
     *
     * @return
     */
    boolean recursion() default false;


    /**
     * groovy script
     * <p/>
     * 名称为{@code _original} 的变量是字段的最初的值
     *
     * @return
     */
    String script() default "";

}
