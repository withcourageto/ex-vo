package top.cmoon.commons.exvo.anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 此注解用于标识多个 VOField，由于Java 7 不支持重复注解，所以使用此方式
 * <p/>
 * 注意： 如果value ={} ，将不会有VoField注解效果，将会使用字段的原始名称
 * <p/>
 * Created by Administrator on 2017/8/25.
 *
 * @see cn.net.comsys.xuegong.wechat.base.anno.VOField
 * @see cn.net.comsys.xuegong.wechat.base.anno.VOType
 * @see cn.net.comsys.xuegong.wechat.util.VOUtil
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface VOFields {


    VOField[] value() default {@VOField};

}
