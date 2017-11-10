package top.cmoon.commons.exvo.anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 *
 *
 * 对标识方法进行Vo切面：
 * <p/>
 * 被标识的方法返回值将会 使用{@code VOUtil.modelToView}方法 进行Vo字段封装， 再使用 ReturnInfo 类包装 VoObject,最后返回到前端格式如下：
 * <pre>
 *  {
 *     code : 0,
 *     msg : null,
 *     data : {
 *          voFiledName1 : val,
 *          voFiledName2 : val
 *          ...
 *     }
 *  }
 *
 * </pre>
 *
 *
 * 注意：方法的返回值必须是{@code java.lang.Object},否则不能被切面
 *
 * Created by Administrator on 2017/8/23.
 *
 * @see cn.net.comsys.xuegong.wechat.base.anno.VOField
 * @see cn.net.comsys.xuegong.wechat.base.aspect.VOAspect
 * @see cn.net.comsys.xuegong.wechat.util.VOUtil
 */

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface VOMethodPoint {

    OpType type() default OpType.QUERY;

}
