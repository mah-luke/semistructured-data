package ssd;

import javax.xml.xpath.*;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;

/**
 *
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

    private Batch curBatch; //TODO (optional): make to Arraylist

    private int level = 0;

    private int curPid = 0;

    private int curBatchId = 0;

    public VPHandler(Document doc) {
        vpDoc = doc;
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
    }

    @Override
    public void endDocument() throws SAXException {
        System.out.println("--- EOF ---");
    }

    @Override
    public void startElement(String namespaceURI, String localName, String qualifiedName, Attributes atts) throws SAXException {
        System.out.println("\t".repeat(level++) + qualifiedName);

        switch (qualifiedName) {
            case ("batch"):
                curBatch = new Batch(vpDoc);
                curBatch.setDescription(atts.getValue("description"));
                break;

            case ("vaccine"):
                curBatch.setVaccineName(atts.getValue("name"));
                curBatch.setVaccineType(atts.getValue("type"));
                break;

            case ("patient"):
                curBatch.addPatient(new Patient(
                        atts.getValue("name"),
                        atts.getValue("birth_year"),
                        atts.getValue("residence"),
                        atts.getValue("risk-group")
                ));
                break;

            case ("batch-id"):
                break;
        }
//		System.out.println("atts: ----");
//		for (int i = 0; i<atts.getLength(); i++) {
//			System.out.println(atts.getValue(i));
//		}
//		System.out.println("LocalName: " + localName);
//		System.out.println("qualifiedName: " + qualifiedName);
//		System.out.println("namespaceUri: " + namespaceURI);
//		System.out.println("eletext: " + eleText);
//		System.out.println("end atts ---");
//        if (qualifiedName.equals("vaccine")) {
//            handleVaccineTypes(atts);
//            handleVaccines(atts);
//
//
//        } else if (qualifiedName.equals("batch")) {
//            batchDescr = atts.getValue("description");
//        }
    }

    @Override
    public void endElement(String namespaceURI, String localName, String qualifiedName) throws SAXException {
        System.out.println("\t".repeat(--level) + "/" + qualifiedName);

        switch (qualifiedName) {
            case ("batch"):
                curBatch.apply();
                break;

            case ("batch-id"):
                removeBatch(eleText);
                break;

            case ("date"):
                curBatch.setDate(eleText);
                break;

            case ("size"):
                curBatch.setSize(eleText);
                break;

            case ("order-date"):
                curBatch.setOrderDate(eleText);
                break;

            case ("vaccination-date"):
                curBatch.lastPatient().addVacDate(eleText);
                break;
        }
    }

    private Element createEl(String name, String text) {
        Element newEl = vpDoc.createElement(name);
        newEl.setTextContent(text);

        return newEl;
    }

    private Element createEl(String name, String text, String[][] atts, Element[] childs) {
        Element newEl = createEl(name, text);

        for (String[] att : atts) newEl.setAttribute(att[0], att[1]);
        for (Element child : childs) newEl.appendChild(child);

        return newEl;
    }

    private void removeBatch(String batchId) {
        // remove batch from vaccines
        Node vacBatch = getNodes(String.format("//vaccines/vaccine/batch[@id='%s']", batchId)).item(0);
        Node siblingBatch = vacBatch.getNextSibling().getNextSibling();
        if (siblingBatch.getNodeName().equals("info"))
            vacBatch.getParentNode().removeChild(siblingBatch);
        vacBatch.getParentNode().removeChild(vacBatch);

        // remove batch entries from patients
        NodeList vaccinesNL = getNodes(String.format("//patients/patient/vaccine[@ref_batch='%s']", batchId));
        for (int i = 0; i < vaccinesNL.getLength(); i++) {
            Node curVac = vaccinesNL.item(i);
            Node siblingVac = curVac.getNextSibling().getNextSibling();

            curVac.getParentNode().removeChild(siblingVac);
            curVac.getParentNode().removeChild(curVac);
        }
    }

    private NodeList getNodes(String path) {
        try {
            return (NodeList) xPath.compile(path).evaluate(vpDoc, XPathConstants.NODESET);
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }
        return null;
    }

    private class Batch {
        private String description;
        private String vaccineName;
        private String vaccineType;
        private String date;
        private String size;
        private String orderDate;
        private String batchId = getNewBatchId();
        private ArrayList<Patient> patients = new ArrayList<>();
        private Document doc;

        public Batch(Document doc) {
            this.doc = doc;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public void setVaccineName(String vaccineName) {
            this.vaccineName = vaccineName;
        }

        public void setVaccineType(String vaccineType) {
            this.vaccineType = vaccineType;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public void setSize(String size) {
            this.size = size;
        }

        public void setOrderDate(String orderDate) {
            this.orderDate = orderDate;
        }

        public void addPatient(Patient patient) {
            patients.add(patient);
        }

        public Patient lastPatient() {
            return patients.get(patients.size() - 1);
        }

        public void apply() {
            handleVaccineTypes();
            handleVaccines();
            handlePatients();
        }

        private String getNewBatchId() {
            NodeList curIds = getNodes("//batch/@id");
            String newId;
            do {
                newId = String.format("AbcD-%04d-1A2", curBatchId++);
            } while (!isIdValid(curIds, newId));

            return newId;
        }

        private String getNewPid() {
            NodeList curIds = getNodes("//patients/patient/@pid");
            String newId;
            do {
                newId = String.format("P%03d", curPid++);
            } while (!isIdValid(curIds, newId));
            return newId;
        }

        private boolean isIdValid(NodeList oldIds, String newId) {
            for (int i = 0; i < oldIds.getLength(); i++) {
                if (newId.equals(oldIds.item(i).getNodeValue()))
                    return false;
            }
            return true;
        }

        private void handleVaccineTypes() {
            NodeList vaccineNamesN = getNodes("//vaccine-types/vaccine/name");
            NodeList vaccines = getNodes("//vaccine-types/vaccine");

            for (int i = 0; i < vaccines.getLength(); i++) {
                if (vaccineName.equals(vaccineNamesN.item(i).getTextContent())) {
                    return; // vaccine found, no need for new vaccine in vaccine-types
                }
            }

            // create new node in vaccine-types since it isn't contained already
            System.out.println("--- create new vaccine in vaccine-types ---");
            NodeList vaccineTypes = getNodes("//vaccine-types");

            Element newType = createEl("type", vaccineType);
            Element newName = createEl("name", vaccineName);
            Element newAuth = createEl("authorized", "true");

            Element newVac = createEl("vaccine", null,
                    new String[][]{},
                    new Element[]{newName, newType, newAuth});
            vaccineTypes.item(0).appendChild(newVac);
        }

        private void handleVaccines() {
            final String query = String.format("//vaccines/vaccine[@type_ref='%s']", vaccineName);
            NodeList vacNL = getNodes(query);

            // check if vaccine in vaccines already exists
            Node vacN = vacNL.item(0);

            // create new vaccine in vaccines if none with the correct type_ref exists
            if (vacN == null) {
                Node vaccines = getNodes("//vaccines").item(0);

                Element newVac = createEl("vaccine", null,
                        new String[][]{{"type_ref", vaccineName}},
                        new Element[]{}
                );

                vaccines.appendChild(newVac);
                vacN = newVac;
            }

            // create and add batch element
            Element newBatch = createEl("batch", description,
                    new String[][]{{"id", batchId}},
                    new Element[]{}
            );

            vacN.appendChild(newBatch);

            // create and add info element
            Element newSize = createEl("size", size);
            Element newOrderDate = createEl("order-date", orderDate);

            Element newInfo = createEl("info", null,
                    new String[][]{{"date", date}},
                    new Element[]{newSize, newOrderDate}
            );

            vacN.appendChild(newInfo);
        }

        private void handlePatients() {
            for (Patient patient : patients) {
                final String query = String.format("//patients/patient[@name='%s' and @birth_year='%s']",
                        patient.getName(),
                        patient.getBirthYear()
                );

                NodeList mainsNL = getNodes(query + "/residences/main");
                Node patientN = null;

                for (int i = 0; i < mainsNL.getLength(); i++) {
                    if (mainsNL.item(i).getTextContent().equals(patient.getResidence()))
                        patientN = mainsNL.item(i).getParentNode().getParentNode();
                }

                if (patientN == null) {
                    Node patientsN = getNodes("//patients").item(0);

                    Element newRiskGroup = createEl("risk-group", patient.getRiskGroup());
                    Element newMain = createEl("main", patient.residence);
                    Element newResidences = createEl("residences", null);
                    newResidences.appendChild(newMain);

                    Element newPatient = createEl("patient", null,
                            new String[][]{
                                    {"birth_year", patient.getBirthYear()},
                                    {"name", patient.getName()},
                                    {"pid", getNewPid()}
                            },
                            new Element[]{newRiskGroup, newResidences}
                    );

                    patientsN.appendChild(newPatient);
                    patientN = newPatient;
                }

                for (String vacDate : patient.getVacDates()) {
                    System.out.println("patient: " + patient.name);

                    Element newVaccine = createEl("vaccine", null,
                            new String[][]{{"ref_batch", batchId}},
                            new Element[]{}
                    );

                    Element newVacDate = createEl("vaccination-date", null,
                            new String[][] {{"date", vacDate}},
                            new Element[] {}
                    );

                    Node residences = getNodes(query + "/residences").item(0);
                    // PatientN.lastChild geht nicht???
                    patientN.insertBefore(newVaccine, residences);
                    patientN.insertBefore(newVacDate, residences);
                }
            }
        }

        @Override
        public String toString() {
            StringBuilder str = new StringBuilder();
            str.append("Batch:");
            str.append("\t\nDescription: ").append(description);
            str.append("\t\nVaccineName: ").append(vaccineName);
            str.append("\t\nVaccineType: ").append(vaccineType);
            str.append("\t\nDate: ").append(date);
            str.append("\t\nSize: ").append(size);
            str.append("\t\nOrderDate: ").append(orderDate);
            str.append("\t\nPatients: ").append(patients);
            return str.toString();
        }
    }

    private class Patient {
        private String name;
        private String birth_year;
        private String residence;
        private String riskGroup;
        private ArrayList<String> vacDates = new ArrayList<>();

        public Patient(String name, String birth_year, String residence, String riskGroup) {
            this.name = name;
            this.birth_year = birth_year;
            this.residence = residence;
            this.riskGroup = riskGroup;
        }

        public void addVacDate(String vacDate) {
            vacDates.add(vacDate);
        }

        public String getName() {
            return name;
        }

        public String getBirthYear() {
            return birth_year;
        }

        public String getResidence() {
            return residence;
        }

        public String getRiskGroup() {
            return riskGroup;
        }

        public ArrayList<String> getVacDates() {
            return vacDates;
        }
    }
}
