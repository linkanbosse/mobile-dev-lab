package se.mobiledev.lab;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import javax.xml.parsers.DocumentBuilderFactory;
import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class StringResourcesTest {
    private static final String[] REQUIRED = {"app_name", "back_exit_hint"};

    @Test
    public void requiredStringsExistAndAreNotEmpty() throws Exception {
        File file = new File("src/main/res/values/strings.xml");
        assertTrue("strings.xml saknas", file.isFile());

        NodeList nodes = DocumentBuilderFactory.newInstance()
            .newDocumentBuilder()
            .parse(file)
            .getElementsByTagName("string");

        Map<String, String> strings = new HashMap<>();
        for (int i = 0; i < nodes.getLength(); i++) {
            Element element = (Element) nodes.item(i);
            strings.put(element.getAttribute("name"), element.getTextContent().trim());
        }

        for (String name : REQUIRED) {
            assertTrue("Saknar sträng: " + name, strings.containsKey(name));
            assertFalse("Tom sträng: " + name, strings.get(name).isEmpty());
        }
    }
}
