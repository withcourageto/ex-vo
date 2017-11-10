package top.cmoon.commons.exvo.anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标识类 为VoType, 当调用{@link cn.net.comsys.xuegong.wechat.util.VOUtil#modelToView}的使用，
 * 使用 VoType 会产生相应的作用，
 * <p/>
 * 如果指定 value 为 {@link ModelToViewPolicy#ALL_FIELD}, 所有的字段将会成为Vo字段，
 * 如果指定 value 为 {@link ModelToViewPolicy#WITHIN_VO_FIELD}，只有被
 * {@link cn.net.comsys.xuegong.wechat.base.anno.VOField},
 * {@link cn.net.comsys.xuegong.wechat.base.anno.VOFieldMapping},
 * {@link cn.net.comsys.xuegong.wechat.base.anno.VOFields}
 * 标识的字段才是 VoField
 * <p/>
 * Created by Administrator on 2017/8/24.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface VOType {


    ModelToViewPolicy value() default ModelToViewPolicy.ALL_FIELD;

}
