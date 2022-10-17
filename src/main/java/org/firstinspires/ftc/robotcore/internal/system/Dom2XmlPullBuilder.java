package org.firstinspires.ftc.robotcore.internal.system;

import java.io.IOException;
import java.io.Reader;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

public class Dom2XmlPullBuilder {
    protected static final boolean NAMESPACES_SUPPORTED = false;

    /* access modifiers changed from: protected */
    public Document newDoc() throws XmlPullParserException {
        try {
            DocumentBuilder newDocumentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            newDocumentBuilder.getDOMImplementation();
            return newDocumentBuilder.newDocument();
        } catch (FactoryConfigurationError e) {
            throw new XmlPullParserException("could not configure factory JAXP DocumentBuilderFactory: " + e, (XmlPullParser) null, e);
        } catch (ParserConfigurationException e2) {
            throw new XmlPullParserException("could not configure parser JAXP DocumentBuilderFactory: " + e2, (XmlPullParser) null, e2);
        }
    }

    /* access modifiers changed from: protected */
    public XmlPullParser newParser() throws XmlPullParserException {
        return XmlPullParserFactory.newInstance().newPullParser();
    }

    public Element parse(Reader reader) throws XmlPullParserException, IOException {
        return parse(reader, newDoc());
    }

    public Element parse(Reader reader, Document document) throws XmlPullParserException, IOException {
        XmlPullParser newParser = newParser();
        newParser.setFeature("http://xmlpull.org/v1/doc/features.html#process-namespaces", true);
        newParser.setInput(reader);
        newParser.next();
        return parse(newParser, document);
    }

    public Element parse(XmlPullParser xmlPullParser, Document document) throws XmlPullParserException, IOException {
        return parseSubTree(xmlPullParser, document);
    }

    public Element parseSubTree(XmlPullParser xmlPullParser) throws XmlPullParserException, IOException {
        return parseSubTree(xmlPullParser, newDoc());
    }

    public Element parseSubTree(XmlPullParser xmlPullParser, Document document) throws XmlPullParserException, IOException {
        return new BuildProcess().parseSubTree(xmlPullParser, document);
    }

    static class BuildProcess {
        private Document docFactory;

        /* renamed from: pp */
        private XmlPullParser f278pp;
        private boolean scanNamespaces;

        private BuildProcess() {
            this.scanNamespaces = true;
        }

        public Element parseSubTree(XmlPullParser xmlPullParser, Document document) throws XmlPullParserException, IOException {
            this.f278pp = xmlPullParser;
            this.docFactory = document;
            return parseSubTree();
        }

        private Element parseSubTree() throws XmlPullParserException, IOException {
            if (this.f278pp.getEventType() == 0) {
                while (this.f278pp.getEventType() != 2) {
                    this.f278pp.next();
                }
            }
            this.f278pp.require(2, (String) null, (String) null);
            String name = this.f278pp.getName();
            String namespace = this.f278pp.getNamespace();
            Element createElementNS = this.docFactory.createElementNS(namespace, name);
            for (int i = 0; i < this.f278pp.getAttributeCount(); i++) {
                String attributeNamespace = this.f278pp.getAttributeNamespace(i);
                String attributeName = this.f278pp.getAttributeName(i);
                String attributeValue = this.f278pp.getAttributeValue(i);
                if (attributeNamespace == null || attributeNamespace.length() == 0) {
                    createElementNS.setAttribute(attributeName, attributeValue);
                } else {
                    String attributePrefix = this.f278pp.getAttributePrefix(i);
                    if (attributePrefix != null) {
                        attributeName = attributePrefix + ":" + attributeName;
                    }
                    createElementNS.setAttributeNS(attributeNamespace, attributeName, attributeValue);
                }
            }
            while (this.f278pp.next() != 3) {
                if (this.f278pp.getEventType() == 2) {
                    createElementNS.appendChild(parseSubTree(this.f278pp, this.docFactory));
                } else if (this.f278pp.getEventType() == 4) {
                    createElementNS.appendChild(this.docFactory.createTextNode(this.f278pp.getText()));
                } else {
                    throw new XmlPullParserException("unexpected event " + XmlPullParser.TYPES[this.f278pp.getEventType()], this.f278pp, (Throwable) null);
                }
            }
            this.f278pp.require(3, namespace, name);
            return createElementNS;
        }

        private void declareNamespaces(XmlPullParser xmlPullParser, Element element) throws DOMException, XmlPullParserException {
            if (this.scanNamespaces) {
                this.scanNamespaces = false;
                int namespaceCount = xmlPullParser.getNamespaceCount(xmlPullParser.getDepth()) - 1;
                for (int i = namespaceCount; i >= xmlPullParser.getNamespaceCount(0); i--) {
                    String namespacePrefix = xmlPullParser.getNamespacePrefix(i);
                    int i2 = namespaceCount;
                    while (true) {
                        if (i2 <= i) {
                            declareOneNamespace(xmlPullParser, i, element);
                            break;
                        }
                        String namespacePrefix2 = xmlPullParser.getNamespacePrefix(i2);
                        if ((namespacePrefix != null && namespacePrefix.equals(namespacePrefix2)) || (namespacePrefix != null && namespacePrefix == namespacePrefix2)) {
                            break;
                        }
                        i2--;
                    }
                }
                return;
            }
            for (int namespaceCount2 = xmlPullParser.getNamespaceCount(xmlPullParser.getDepth() - 1); namespaceCount2 < xmlPullParser.getNamespaceCount(xmlPullParser.getDepth()); namespaceCount2++) {
                declareOneNamespace(xmlPullParser, namespaceCount2, element);
            }
        }

        private void declareOneNamespace(XmlPullParser xmlPullParser, int i, Element element) throws DOMException, XmlPullParserException {
            String str;
            String namespacePrefix = xmlPullParser.getNamespacePrefix(i);
            String namespaceUri = xmlPullParser.getNamespaceUri(i);
            if (namespacePrefix != null) {
                str = "xmlns:" + namespacePrefix;
            } else {
                str = "xmlns";
            }
            element.setAttributeNS("http://www.w3.org/2000/xmlns/", str, namespaceUri);
        }
    }
}
