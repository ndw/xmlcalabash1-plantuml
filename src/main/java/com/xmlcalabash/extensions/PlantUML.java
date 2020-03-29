package com.xmlcalabash.extensions;

import com.xmlcalabash.core.XMLCalabash;
import com.xmlcalabash.core.XProcConstants;
import com.xmlcalabash.core.XProcException;
import com.xmlcalabash.core.XProcRuntime;
import com.xmlcalabash.io.ReadablePipe;
import com.xmlcalabash.io.WritablePipe;
import com.xmlcalabash.library.DefaultStep;
import com.xmlcalabash.runtime.XAtomicStep;
import com.xmlcalabash.util.Base64;
import com.xmlcalabash.util.TreeWriter;
import com.xmlcalabash.util.XProcURIResolver;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmNode;
import net.sourceforge.plantuml.FileFormat;
import net.sourceforge.plantuml.FileFormatOption;
import net.sourceforge.plantuml.SourceStringReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.sax.SAXSource;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * Created by IntelliJ IDEA.
 * User: ndw
 * Date: Oct 8, 2008
 * Time: 7:44:07 AM
 * To change this template use File | Settings | File Templates.
 */

@XMLCalabash(
        name = "cx:plantuml",
        type = "{http://xmlcalabash.com/ns/extensions}plantuml")

public class PlantUML extends DefaultStep {
    private static final QName _format = new QName("", "format");
    private static final QName _html = new QName("", "html");
    private static final QName h_img = new QName("", "http://www.w3.org/1999/xhtml", "img");
    private static final QName _src = new QName("", "src");

    private static final String library_xpl = "http://xmlcalabash.com/extension/steps/plantuml.xpl";
    private static final String library_url = "/com/xmlcalabash/extensions/plantuml/library.xpl";

    private ReadablePipe source = null;
    private WritablePipe result = null;

    public PlantUML(XProcRuntime runtime, XAtomicStep step) {
        super(runtime, step);
    }

    public void setInput(String port, ReadablePipe pipe) {
        source = pipe;
    }

    public void setOutput(String port, WritablePipe pipe) {
        result = pipe;
    }

    public void reset() {
        source.resetReader();
        result.resetWriter();
    }

    public void run() throws SaxonApiException {
        super.run();

        boolean html = getOption(_html, false);
        String format = getOption(_format, "png");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            XdmNode doc = source.read();
            String text = doc.getStringValue();
            SourceStringReader reader = new SourceStringReader(text);

            FileFormat fmt = "svg".equals(format) ? FileFormat.SVG : FileFormat.PNG;

            String desc = reader.generateImage(baos, new FileFormatOption(fmt));
            if (desc == null) {
                throw new XProcException("PlantUML diagram returned null");
            }
        } catch (XProcException e) {
            throw e;
        } catch (Exception e) {
            throw new XProcException(e);
        }

        TreeWriter tree = new TreeWriter(runtime);
        tree.startDocument(step.getNode().getBaseURI());

        if ("svg".equals(format)) {
            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray(), 0, baos.size());
            XdmNode svg = runtime.parse(new InputSource(bais));
            if (html) {
                // Perhaps I should remove the SVG namespace and do other stuff here?
                tree.addSubtree(svg);
            } else {
                tree.addSubtree(svg);
            }
        } else {
            String base64 = Base64.encodeBytes(baos.toByteArray(), 0, baos.size());

            if (html) {
                tree.addStartElement(h_img);
                tree.addAttribute(_src, "data:image/png;base64," + base64);
                tree.startContent();
                tree.addEndElement();
            } else {
                tree.addStartElement(XProcConstants.c_data);
                tree.startContent();
                tree.addText("data:image/png;base64," + base64);
                tree.addEndElement();
            }
        }

        tree.endDocument();
        result.write(tree.getResult());
    }

    public static void configureStep(XProcRuntime runtime) {
        XProcURIResolver resolver = runtime.getResolver();
        URIResolver uriResolver = resolver.getUnderlyingURIResolver();
        URIResolver myResolver = new StepResolver(uriResolver);
        resolver.setUnderlyingURIResolver(myResolver);
    }

    private static class StepResolver implements URIResolver {
        Logger logger = LoggerFactory.getLogger(PlantUML.class);
        URIResolver nextResolver = null;

        public StepResolver(URIResolver next) {
            nextResolver = next;
        }

        @Override
        public Source resolve(String href, String base) throws TransformerException {
            try {
                URI baseURI = new URI(base);
                URI xpl = baseURI.resolve(href);
                if (library_xpl.equals(xpl.toASCIIString())) {
                    URL url = PlantUML.class.getResource(library_url);
                    logger.debug("Reading library.xpl for cx:plantuml from " + url);
                    InputStream s = PlantUML.class.getResourceAsStream(library_url);
                    if (s != null) {
                        SAXSource source = new SAXSource(new InputSource(s));
                        return source;
                    } else {
                        logger.info("Failed to read " + library_url + " for cx:plantuml");
                    }
                }
            } catch (URISyntaxException e) {
                // nevermind
            }

            if (nextResolver != null) {
                return nextResolver.resolve(href, base);
            } else {
                return null;
            }
        }
    }
}
