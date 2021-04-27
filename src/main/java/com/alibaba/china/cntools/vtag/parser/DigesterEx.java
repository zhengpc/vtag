package com.alibaba.china.cntools.vtag.parser;

import org.apache.commons.digester3.Digester;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class DigesterEx extends Digester {

    public DigesterEx(final String dtd) {
        this.setEntityResolver((String publicId, String systemId) -> {
            if (systemId.endsWith(dtd)) {
                return new InputSource(DigesterEx.class.getResourceAsStream(dtd));
            }
            return null;
        });
        this.setValidating(true);
    }

    @Override
    public void push(Object object) {
        super.push(object);

        if (object != null && object instanceof Locatable) {
            ((Locatable)object).setLine(getDocumentLocator().getLineNumber());
        }
    }

    @Override
    public void error(SAXParseException exception) throws SAXException {
        Configurable root = getRoot();
        if (root == null) {
            super.error(exception);
        } else {
            root.warn(exception.getLineNumber(), exception.getMessage());
        }
    }

    @Override
    public void fatalError(SAXParseException exception) throws SAXException {
        Configurable root = getRoot();
        if (root == null) {
            super.fatalError(exception);
        } else {
            root.warn(exception.getLineNumber(), exception.getMessage());
        }
    }

    @Override
    public void warning(SAXParseException exception) throws SAXException {
        Configurable root = getRoot();
        if (root == null) {
            super.warning(exception);
        } else {
            root.warn(exception.getLineNumber(), exception.getMessage());
        }
    }

}
