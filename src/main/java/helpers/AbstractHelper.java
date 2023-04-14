package helpers;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.w3c.dom.*;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public abstract class AbstractHelper {

    protected Logger logger = LogManager.getLogger(WebDriverContainer.class);
    protected ConfigContainer config = ConfigContainer.getInstance();




    public String getCharacterDataFromElement(Element e) {
        Node child = e.getFirstChild();
        if (child instanceof CharacterData) {
            CharacterData cd = (CharacterData) child;
            return cd.getData();
        }
        return "?";
    }

    public Document getXMLFromString(String xml) throws Exception {
        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document document = builder.parse(new InputSource(new StringReader(xml)));
        return document;
    }

    public String getValueFromXML(String name, String response) throws Exception {
        Document doc = getXMLFromString(response);
        NodeList nodes = doc.getElementsByTagName(name);
        Element line = (Element) nodes.item(0);
        return getCharacterDataFromElement(line);
    }

    public String readFileToString(String path) throws IOException {
        //todo заменить на helpers.Util.readFileToString
        return new String(
                Files.readAllBytes(Paths.get(path)),
                StandardCharsets.UTF_8
        );
    }
}
