package paddedsocks.config;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class JSRunner {
    private Invocable jsRun;

    public JSRunner(String[] jsFiles) {
        ScriptEngineManager engineManager = new ScriptEngineManager();
        ScriptEngine scriptEngine = engineManager.getEngineByName("JavaScript");

        // read script file
        try {
            for (String currFile : jsFiles)
                scriptEngine.eval(Files.newBufferedReader(Paths.get(currFile), StandardCharsets.UTF_8));
        } catch (ScriptException | IOException e) {
            e.printStackTrace();
        }

        this.jsRun = (Invocable) scriptEngine;
    }

    public String exec(String funcName, Object... args) {
        try {
            Object result = jsRun.invokeFunction(funcName, args);
            if (result instanceof String)
                return (String) result;
        } catch (ScriptException | NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void main(String[] args) {
        JSRunner js = new JSRunner(new String[]{"wpad.dat", "pac_utils.js"});
        // call function from script file
        System.out.println(js.exec("FindProxyForURL", "https://confluence.oraclecorp.com", "confluence.oraclecorp.com"));
        System.out.println(js.exec("FindProxyForURL", "https://www.oracle.com", "www.oracle.com"));
        System.out.println(js.exec("FindProxyForURL", "https://www.google.com", "www.google.com"));
        System.out.println(js.exec("FindProxyForURL", "https://www.airtel.in", "www.airtel.in"));
        System.out.println(js.exec("FindProxyForURL", "https://admindocs.netsuite.com", "admindocs.netsuite.com"));

        System.out.println(js.exec("FindProxyForURL", "", "confluence.oraclecorp.com"));
        System.out.println(js.exec("FindProxyForURL", "", "www.oracle.com"));
        System.out.println(js.exec("FindProxyForURL", "", "www.google.com"));
        System.out.println(js.exec("FindProxyForURL", "", "www.airtel.in"));
        System.out.println(js.exec("FindProxyForURL", "", "admindocs.netsuite.com"));
    }
}
