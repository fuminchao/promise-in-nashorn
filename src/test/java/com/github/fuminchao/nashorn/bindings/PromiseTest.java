package com.github.fuminchao.nashorn.bindings;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.junit.Assert;
import org.junit.Test;

import jdk.nashorn.api.scripting.ScriptUtils;
import sun.misc.IOUtils;

public class PromiseTest {

    private ScriptEngine engine;

    public PromiseTest() {

        final ScriptEngineManager factory = new ScriptEngineManager();

        engine = factory.getEngineByName("nashorn");

        Bindings b = engine.createBindings();

        b.put("Promise", EngineUtils.importJavaType(Promise.class));
        b.put("assertTrue", new BiConsumer<String, Boolean>() {

            @Override
            public void accept(String s, Boolean aBoolean) {

                assertTrue(s, aBoolean);
            }
        });

        engine.setBindings(b, ScriptContext.ENGINE_SCOPE);
    }

    private Object runScript(String filename) throws IOException, ScriptException {

        try (InputStream input = this.getClass().getClassLoader().getResourceAsStream(filename)) {

            String fn = new String(IOUtils.readFully(input, -1, true));

//            System.out.println(ScriptUtils.parse(fn, "xxxxx", false));
            return ((Promise) engine.eval(fn)).await();
        }
    }

    @Test
    public void test() throws IOException, ScriptException {
        assertEquals(99, runScript("test.js"));
    }
}
