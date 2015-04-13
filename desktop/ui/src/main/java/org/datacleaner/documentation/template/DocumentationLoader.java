package org.datacleaner.documentation.template;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.datacleaner.descriptors.ComponentDescriptor;
import org.datacleaner.descriptors.ConfiguredPropertyDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

public class DocumentationLoader {

    private static final String FILENAME_TEMPLATE = "/src/main/resources/documentation_template.html";
    static final String OUTPUT_FILENAME = "documentLoaderOutput.txt";

    private Template _template;
    private Map<String, Object> _data;
    private static Logger logger = LoggerFactory.getLogger(DocumentationLoader.class);

    DocumentationLoader() {

        Configuration cfg = new Configuration();
        try {
            // Load the template
            _template = cfg.getTemplate(FILENAME_TEMPLATE);
            _data = new HashMap<String, Object>();
        }

        catch (Exception exception) {
            logger.debug("Exception while trying to initialize the template:", exception);
        }
    }

    public void createDocumentation(ComponentDescriptor<?> componentdescriptor) {

        try {
            _data.put("componentname", componentdescriptor.getDisplayName());
            _data.put("description", componentdescriptor.getDescription());
            _data.put("icon", "loading icon"); // TODO - add an icon
            _data.put("isDistributable", "" + componentdescriptor.isDistributable());
            final Set<ConfiguredPropertyDescriptor> configuredProperties = componentdescriptor
                    .getConfiguredProperties();
            List<ConfiguredPropertyDescriptor> propertiesList = new ArrayList<ConfiguredPropertyDescriptor>(
                    configuredProperties);

            logger.info("properties are: {}", propertiesList.toString());
            _data.put("properties", propertiesList);

            Writer out = new OutputStreamWriter(new FileOutputStream(OUTPUT_FILENAME));
            _template.process(_data, out);
            out.flush();

        } catch (IOException exception) {
            logger.debug("Exception while writing to the file:", exception);

        } catch (TemplateException exception) {
            logger.debug("Exception while loading the template:", exception);
        }
    }
}
