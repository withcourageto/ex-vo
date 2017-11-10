package top.cmoon.commons.exvo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.*;

/**
 * Created by Administrator on 2017/8/25.
 */
public class GroovyScriptUtil {


    private static final Logger logger = LoggerFactory.getLogger(GroovyScriptUtil.class);

    private static ScriptEngineManager manager = new ScriptEngineManager();
    private static ScriptEngine engine = manager.getEngineByName("groovy");


    public static Object eval(Object original, String script) {

        ScriptContext newContext = new SimpleScriptContext();
        newContext.setBindings(engine.createBindings(), ScriptContext.ENGINE_SCOPE);
        Bindings engineScope = newContext.getBindings(ScriptContext.ENGINE_SCOPE);

        engineScope.put("_original", original);

        // evaluate JavaScript code
        try {
            Object result = engine.eval(script, engineScope);
            return result;
        } catch (ScriptException e) {

            String errMsg = "Script evaluation exception, script:" + script + ", original:" + original;
            logger.error(errMsg, e);
            throw new RuntimeException(errMsg, e);
        }

    }


}
