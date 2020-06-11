package com.github.fuminchao.nashorn.bindings;

import javax.script.Invocable;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

public class EngineUtils {

    private static ScriptEngine engine;

    static {

        engine = new ScriptEngineManager().getEngineByName("nashorn");

        try {
            engine.eval("function toJsArray() { return Array.prototype.concat.apply([], arguments) }");
            engine.eval("function importJavaType(name) { return Java.type(name) }");

        } catch (ScriptException e) {
            e.printStackTrace();
        }
    }

    private EngineUtils() {
    }

    private static Object invokeFunction(String fn, Object... args) {

        try {
            return ((Invocable) engine).invokeFunction(fn, args);
        } catch (ScriptException | NoSuchMethodException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Object importJavaType(Class clazz) {
        return invokeFunction("importJavaType", clazz.getCanonicalName());
    }

    public static Object toJsArray(Object[] arr) {
        return invokeFunction("toJsArray", arr);
    }
}
