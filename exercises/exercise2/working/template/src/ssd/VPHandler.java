package ssd;

import javax.xml.xpath.*;

import org.apache.xerces.impl.xs.opti.NodeImpl;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * TODO: Implement this content handler.
 */
public class VPHandler extends DefaultHandler {
	/**
	 * Use this xPath variable to create xPath expression that can be
	 * evaluated over the XML document.
	 */
	private static XPath xPath = XPathFactory.newInstance().newXPath();
	
	/**
	 * Store and manipulate the  vaccination-plan XML document here.
	 */
	private Document vpDoc;
	
	/**
	 * This variable stores the text content of XML Elements.
	 */
	private String eleText;

	/**
	 * Insert local variables here
	 */

	private int level = 0;

	final private NodeList vaccines;

	public VPHandler(Document doc) {
		vpDoc = doc;
		vaccines = getNodes("//vaccine-types/vaccine");
	}

    @Override
    /**
     * SAX calls this method to pass in character data
     */
  	public void characters(char[] text, int start, int length)
  			    throws SAXException {
  		eleText = new String(text, start, length);
  	}

    /**
     * 
     * Return the current stored vaccination-plan document
     * 
     * @return XML Document
     */
	public Document getDocument() {
		return vpDoc;
	}

	//Specify additional methods to parse the exhibition document and modify the vpDoc
	@Override
	public void startDocument() throws SAXException {
		System.out.println("--- SOF ---");
		NodeList batchList = getNodes("//batch");
	}

	@Override
	public void endDocument() throws SAXException {
		System.out.println("--- EOF ---");
	}

	@Override
	public void startElement(String namespaceURI, String localName, String qualifiedName, Attributes atts) throws SAXException{
		System.out.println("\t".repeat(level++) + qualifiedName);

		NodeList vaccineNames = getNodes("//vaccine-types/vaccine/name");
//		System.out.println("atts: ----");
//		for (int i = 0; i<atts.getLength(); i++) {
//			System.out.println(atts.getValue(i));
//		}
//		System.out.println("LocalName: " + localName);
//		System.out.println("qualifiedName: " + qualifiedName);
//		System.out.println("namespaceUri: " + namespaceURI);
//		System.out.println("eletext: " + eleText);
//		System.out.println("end atts ---");
		if(qualifiedName.equals("vaccine")) {
			for(int i = 0; i < vaccines.getLength(); i++) {
				if (atts.getValue("name").equals(vaccineNames.item(i).getTextContent())) {
					return;
				}
			}
			System.out.println("--- create new node ---");
			NodeList vaccineTypes = getNodes("//vaccine-types");

			Element newType = createEl("type", atts.getValue("type"));
			Element newName = createEl("name", atts.getValue("name"));
			Element newAuth = createEl("authorized", "true");

			Element newVac = createEl("vaccine", null,
					new String[][] {},
					new Element[] {newName, newType, newAuth});
			vaccineTypes.item(0).appendChild(newVac);
//			Element test = vpDoc.createElement("test");
//			test.setAttribute("name", "testname");
//			test.setTextContent("textContent");
//			vaccineTypes.item(0).appendChild(test);
		}
	}

	@Override
	public void endElement(String namespaceURI, String localName, String qualifiedName) throws SAXException{
		System.out.println("\t".repeat(--level) + "/" +  qualifiedName);
	}

	public NodeList getNodes(String path) {
		try {
			return (NodeList) xPath.compile(path).evaluate(vpDoc, XPathConstants.NODESET);
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}
		return null;
	}

	public Element createEl(String name, String text) {
		Element newEl = vpDoc.createElement(name);
		newEl.setTextContent(text);

		return newEl;
	}

	public Element createEl(String name, String text, String[][] atts, Element[] childs) {
		Element newEl = createEl(name, text);

		for (String[] att : atts) newEl.setAttribute(att[0], att[1]);
		for (Element child : childs) newEl.appendChild(child);

		return newEl;
	}
}

