package ssd;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.w3c.dom.Document;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;


public class SSD {
    private static DocumentBuilderFactory documentBuilderFactory;
    private static DocumentBuilder documentBuilder;
    private static SAXParserFactory saxParserFactory;
    private static SAXParser saxParser;
    private static Validator validator;

	public static void main(String[] args) throws Exception {
		if (args.length != 3) {
            System.err.println("Usage: java SSD <input.xml> <batch-delivery.xml> <output.xml>");
            System.exit(1);
        }
	
		String inputPath = args[0];
		String batchdeliveryPath = args[1];
		String outputPath = args[2];
		
    
       initialise();
       transform(inputPath, batchdeliveryPath, outputPath);
    }
	
	private static void initialise() throws Exception {    
       documentBuilderFactory = DocumentBuilderFactory.newInstance();
       documentBuilderFactory.setNamespaceAware(true);
       documentBuilder = documentBuilderFactory.newDocumentBuilder();

       Schema schema = SchemaFactory
           .newDefaultInstance()
           .newSchema(new File("resources/vaccination-plan.xsd"));
       validator = schema.newValidator();

       saxParserFactory = SAXParserFactory.newInstance();
       saxParserFactory.setValidating(true);
       saxParser = saxParserFactory.newSAXParser();
    }
	
	/**
     * Use this method to encapsulate the main logic for this example. 
     * 
     * First read in the vaccination-plan document. 
     * Second create a VPHandler and an XMLReader (SAX parser)
     * Third parse the  batch-delivery  document
     * Last get the Document from the VPHandler and use a
     *    Transformer to print the document to the output path.
     * 
     * @param inputPath Path to the xml file to get read in.
     * @param batchdeliveryPath Path to the batch-delivery xml file
     * @param outputPath Path to the output xml file.
     */
    private static void transform(String inputPath, String batchdeliveryPath, String outputPath) throws Exception {
        // Read in the data from the vaccination-plan xml document, created in Exercise Sheet 1
        Document inputDoc = readToDocument(inputPath);

        // Create an input source for the batch-delivery document
        VPHandler vpHandler = new VPHandler(inputDoc);

        // start the actual parsing
        XMLReader parser = saxParser.getXMLReader();
        parser.setContentHandler(vpHandler);
        parser.setErrorHandler(vpHandler);
        parser.parse(batchdeliveryPath);

        // Validate file before storing ???
        DOMSource input = new DOMSource(inputDoc);
        validator.validate(input);

        // get the document from the VPHandler
        Document parsedDoc = vpHandler.getDocument();
        DOMSource source = new DOMSource(parsedDoc);

        //validate
        validator.validate(source);

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();

        //store the document
        try (FileWriter out = new FileWriter(outputPath)){
            StreamResult result = new StreamResult(out);
            transformer.transform(source, result);
        }
    }

    /**
     * Prints an error message and exits with return code 1
     * 
     * @param message The error message to be printed
     */
    public static void exit(String message) {
    	System.err.println(message);
    	System.exit(1);
    }

    /**
     * Creates and returns a parsed XML Document from given inputPath
     *
     * @param inputPath path of the XML Document
     * @return parsed Document
     */
    private static Document readToDocument(String inputPath) {
        try {
            File inputFile = new File(inputPath);
            Document inputDoc = documentBuilder.parse(inputFile);
            inputDoc.getDocumentElement().normalize();
            return inputDoc;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    

}
