package org.sharedhealth.datasense.aqs;


import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import org.sharedhealth.datasense.config.DatasenseProperties;
import org.sharedhealth.datasense.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Component
public class AqsFTLProcessor implements AqsTemplateProcessor {

    private AqsExecutor executor;
    private DatasenseProperties datasenseProperties;
    private Configuration cfg;

    @Autowired
    public AqsFTLProcessor(AqsExecutor executor, DatasenseProperties datasenseProperties) {
        this.executor = executor;
        this.datasenseProperties = datasenseProperties;
    }

    private void initializeConfiguration() throws IOException {
        if(cfg == null){
            synchronized (Configuration.class) {
                if(cfg == null){
                    Configuration cfg = new Configuration(Configuration.VERSION_2_3_21);
                    cfg.setDirectoryForTemplateLoading(new File(StringUtil.removeSuffix(datasenseProperties.getAqsTemplateLocationPath(), "/")));
                    cfg.setDefaultEncoding("UTF-8");
                    cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
                    this.cfg = cfg;
                }
            }
        }
    }

    public String process(String aqsConfigFile, Map<String, Object> params) {
        try {
            initializeConfiguration();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try (StringWriter writer = new StringWriter()) {
            AqsConfig aqsConfig = executor.loadAqsConfig(aqsConfigFile);
            HashMap<String, Object> results = executor.fetchResults(aqsConfig, params);
            results.putAll(params);
            String templateFile = aqsConfig.getTemplateName() + ".ftl";
            Template tmpl = cfg.getTemplate(templateFile);
            tmpl.process(results, writer);
            return writer.toString();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (TemplateException e) {
            e.printStackTrace();
        }
        return null;
    }

}
