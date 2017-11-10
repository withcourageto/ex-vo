package top.cmoon.commons.exvo.configuration;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Component;
import top.cmoon.commons.exvo.ClassUtil;
import top.cmoon.commons.exvo.VOUtil;
import top.cmoon.commons.exvo.anno.OpType;
import top.cmoon.commons.exvo.anno.VOMethodPoint;
import top.cmoon.commons.exvo.bean.ReturnInfo;
import top.cmoon.commons.exvo.constant.ReturnCode;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2017/8/23.
 */
@Aspect
public class VOAspect {

    public static final Logger logger = LoggerFactory.getLogger(VOAspect.class);

    @Pointcut(value = "execution(@top.cmoon.commons.exvo.anno.VOMethodPoint java.lang.Object *(..)) ")
    public void modelToView() {
    }


    @Around("modelToView()")
    public Object encode(ProceedingJoinPoint point) throws Throwable {

        Object result = point.proceed(point.getArgs());
        OpType opType = getOpType(point);

        switch (opType) {
            case MANIPULATION:

                if (result == null) {
                    return ReturnInfo.SUCCESS;
                } else {
                    return new ReturnInfo(ReturnCode.OP_FAILURE, result.toString());
                }
            case QUERY:
                if (result == null) {
                    throw new RuntimeException("返回结果不能为空");
                }

                Class<?> resultClass = result.getClass();
                if (List.class.isAssignableFrom(resultClass)) {
                    return new ReturnInfo(VOUtil.modelToView((List) result));
                }

                if (Collection.class.isAssignableFrom(resultClass)) {
                    return new ReturnInfo(VOUtil.modelToView((Collection) result));
                }

                if (Map.class.isAssignableFrom(resultClass)) {
                    return new ReturnInfo(VOUtil.modelToView((Map) result));
                }

                if (ClassUtil.isUserClass(resultClass)) {
                    return new ReturnInfo(VOUtil.modelToView(result));
                } else {
                    throw new RuntimeException("返回结果不是用户自定义class，并且不是 List,或者Collection 的子类，不能使用VOPoint进行切面");
                }
            default:
                return result;

        }

    }


    private OpType getOpType(ProceedingJoinPoint point) {
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();

        VOMethodPoint voPointAnno = method.getAnnotation(VOMethodPoint.class);
        return voPointAnno.type();
    }


}
