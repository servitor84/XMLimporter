package de.di.xml;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author A. Sopicki
 */
public class IndexFileReader {

    private static final int PILCROW_SIGN = 0xb6;
    private File indexFile = null;
    private Logger logger = null;
    private Map<String, Integer> map;
    private String currentMaskName;
    private int currentMaxNumberOfIndexField;
    private Map<String, IndexValue> indexData = new HashMap<String, IndexValue>();
    /*build 00956*/
    ParserException hasError = null;

    public IndexFileReader(File indexFile) {
        this(indexFile, null, null);
    }

    public IndexFileReader(File indexFile, Logger logger, Map<String, Integer> map) {
        this.indexFile = indexFile;
        this.map = map;
        if (logger == null) {
            this.logger = Logger.getLogger(getClass().getName());
        } else {
            this.logger = logger;
        }
    }

    public void parse() throws ParserException, Exception {
        Document doc = null;
        Node node = null;
        Node objNode = null;
        InputStream is = null;
        try {
            DocumentBuilderFactory fac = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = fac.newDocumentBuilder();

//            BufferedReader bf = new BufferedReader(new FileReader(indexFile));
//            String line;
//            StringBuilder result = new StringBuilder();
//            while ((line = bf.readLine()) != null) {
//                result.append(line);
//                result.append("\r\n");
//            }
//            bf.close();
//            BufferedWriter bw = new BufferedWriter(new FileWriter(indexFile));
//            bw.write(result.toString());
//            bw.flush();
//            bw.close();
            is = new FileInputStream(indexFile);
            doc = builder.parse(is);
       } catch (Exception ex) {
//            logger.log(Level.FATAL, null, ex);           
            throw ex;
            //return;
       }
        finally
        {
            if(is != null) { is.close(); }
        }
        
        assert doc != null;

        node = doc.getFirstChild();        

        if (node == null) {
            logger.warn("\t\t\t Missing eloobjlist. Illegal index file!");
        }

        if (node.getAttributes() == null || node.getAttributes().getNamedItem("ver") == null) {
            logger.warn("\t\t\t version attribute missing. Illegal index file!");
            return;
        }

        if (!node.getAttributes().getNamedItem("ver").getNodeValue().equals("1.0")) {
            logger.warn("\t\t\t Unsupported version for eloobjlist. Illegal index file!");
            return;
        }

        objNode = doc.getElementsByTagName("obj").item(0);       
        

        if (objNode == null) {
            logger.warn("\t\t\t No obj node found. Illegal index file!");
            return;
        }

        assert objNode != null;

        node = objNode.getFirstChild();

        if (node == null) {
            logger.warn("\t\t\t Missing data for obj node. Illegal index file!");
            return;
        }

        try {
            while (node != null) {
                String name = node.getNodeName();
                if (name.equals("#text") || name.equals("dtype") || name.equals("marker") || name.equals("structurelist")) {
                    node = node.getNextSibling();
                    continue;
                } else if (name.equals("docfile")) {
                    handleNameNode(node);
                } else if (name.equals("desc")) {
                    handleValueNode(node);
                } else if (name.equals("idate")) {
                    handleValueNode(node);
                } else if (name.equals("xdate")) {
                    handleValueNode(node);
                } else if (name.equals("type")) {
                    String maskName = node.getAttributes().getNamedItem("value").getNodeValue();
                    checkMaskName(maskName);
                    handleValueNode(node);
                } else if (name.equals("memo")) {
                    handleMemoNode(node);
                } else if (name.equals("indexlist")) {
                    handleIndexListNode(node);
                } else if (name.equals("destlist")) {
                    handleDestListNode(node);
                } else if (name.equals("workflowlist")) {
                    handleWorkflowListNode(node);
                } else if (name.equals("versioning")) {
                    handleVersioningNode(node);
                }

                node = node.getNextSibling();
            }
        } catch (ParserException pex) {
            if (indexData != null) {
                indexData.clear();
            }            
            throw new ParserException("Error while parsing obj node. Illegal index file! " + pex.getMessage());

        } catch (Exception ex) {
            if (indexData != null) {
                indexData.clear();
            }           
            throw new Exception("Error while parsing index file! " + ex.getMessage());
        }
    }

    public Map<String, IndexValue> getIndexData() {
        return indexData;
    }

    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    private void handleIndexListNode(Node listNode) throws ParserException {
        Node node = listNode.getFirstChild();        
        IndexValue first = null;
        IndexValue sibling = null;
        IndexValue current = null;
                
        int i = 0;
        while (node != null) {
            if (node.getNodeType() != Node.ELEMENT_NODE) {
                node = node.getNextSibling();
                continue;
            }

            if (node.getAttributes() == null || node.getAttributes().getNamedItem("name") == null) {
                throw new ParserException("Illegal index tag in index file!");
            }

            Node attribute = node.getAttributes().getNamedItem("name");

            if (attribute == null) {
                throw new ParserException("Missing name for index tag in index file!");
            }

            String value = attribute.getNodeValue();

//            try {
//                i = Integer.parseInt(value);                
//                {
//                    if (i < 0 || i > currentMaxNumberOfIndexField) {                                        
//                        //logger.warn("\t\t\t\tIndex value out of range! Should be 1 - " + currentMaxNumberOfIndexField + "value in file: " + i);
////                        logger.warn("\t\t\t\tNo Indexfield more - " + currentMaxNumberOfIndexField + " value(s) in Mask: '" + currentMaskName + "'" +
////                                    ", ");                        
//                        /*Removed bug for illegal number of lines. /instead through using 
//                        @see# handleNameNode va arunca in loc exceptia dupa ce initializeaza fileName.
//                    
//                        */
//                        /*build 00956*/
////                      hasError = new ParserException("Index value out of range! Should be 1 - " + currentMaxNumberOfIndexField + ".");
////                      throw new ParserException("Index value out of range! Should be 1 - " + currentMaxNumberOfIndexField + ".");
//                    }
//                }    
//            } catch (NumberFormatException nfe) {
//                throw new ParserException("Only numeric index names allowed! " + nfe.getMessage());
//            }

            if (first == null) {
                first = new IndexValue("name", value);
                sibling = first;
                current = first;
            } else {
                sibling.setSibling(new IndexValue("name", value));
                sibling = sibling.getSibling();
                current = sibling;
            }

            attribute = node.getAttributes().getNamedItem("value");

            if (attribute == null) {
                throw new ParserException("Missing value for index tag in index file!");
            }

            value = StringEscapeUtils.unescapeHtml4(attribute.getNodeValue());
            /* 05.03.20
            Eintrag im Indexfeld zu groß
            */
            if(value.length() > 255) {
                value = value.substring(0, 254);
            }
            // ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++SL

            current.setNext(new IndexValue("value", value));
            current = current.getNext();

            attribute = node.getAttributes().getNamedItem("mode");

            if (attribute != null) {
                value = attribute.getNodeValue();

                current.setNext(new IndexValue("mode", value));
                current = current.getNext();
            }

            node = node.getNextSibling();            
        }
        // +++        
        if (i < 0 || i > currentMaxNumberOfIndexField) 
        {
            logger.warn("\t\t\t\tMask: '" + currentMaskName + "' has " + currentMaxNumberOfIndexField + " index fields; XML file has " + i + " fields with index data");
        }
        // +++
        if (first != null) {
            indexData.put("indexlist", first);
        }
    }

    private void handleDestListNode(Node listNode) throws ParserException {
        Node node = listNode.getFirstChild();

        IndexValue first = null;
        IndexValue sibling = null;
        IndexValue current = null;

        while (node != null) {
            if (node.getNodeType() != Node.ELEMENT_NODE) {
                node = node.getNextSibling();
                continue;
            }

            if (node.getAttributes() == null || node.getAttributes().getNamedItem("value") == null) {
                throw new ParserException("Illegal destination tag in index file!");
            }

            Node attribute = node.getAttributes().getNamedItem("value");

            if (attribute == null) {
                throw new ParserException("Missing value for destination tag in index file!");
            }

            String value = attribute.getNodeValue();

            if (first == null) {
                first = new IndexValue("value", value);
                sibling = first;
                current = first;
            } else {
                sibling.setSibling(new IndexValue("value", value));
                sibling = sibling.getSibling();
                current = sibling;
            }

            attribute = node.getAttributes().getNamedItem("type");

            if (attribute != null) {
                value = attribute.getNodeValue();

                current.setNext(new IndexValue("type", value));
                current = current.getNext();
            }

            node = node.getNextSibling();
        }

        if (first != null) {
            indexData.put("destlist", first);
        }
    }

    private void handleValueNode(Node valueNode) throws ParserException {
        Node value = valueNode.getAttributes().getNamedItem("value");
        Archiving.description = value.getNodeValue();
        if (value == null || value.getNodeValue() == null) {
            throw new ParserException("Error while parsing node. Illegal index file!");
        }

        if (logger.isDebugEnabled()) {
            logger.debug("\t\t\t" + valueNode.getNodeName() + ": " + value.getNodeValue());
        }

        indexData.put((valueNode.getNodeName()), new IndexValue("value", StringEscapeUtils.unescapeHtml4(value.getNodeValue())));
    }

    private void handleNameNode(Node valueNode) throws ParserException {
        Node value = valueNode.getAttributes().getNamedItem("name");
        Archiving.docFilePath = value.getNodeValue();
        if (value == null || value.getNodeValue() == null) {
            throw new ParserException("Error while parsing node. Illegal index file!");
        }
        /*build 00956*/
        if (hasError != null) {
            logger.error("Throwing exception from above.");
            throw hasError;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("\t\t\t" + valueNode.getNodeName() + ": " + value.getNodeValue());
        }

        indexData.put((valueNode.getNodeName()), new IndexValue("name", value.getNodeValue()));
    }

    private void handleMemoNode(Node memoNode) throws ParserException {
        Node textNode = memoNode.getFirstChild();
        StringBuilder memoText = new StringBuilder();
        while (textNode != null) {
            if (textNode.getNodeType() == Node.TEXT_NODE) {
                memoText.append(textNode.getNodeValue());
            }

            textNode = textNode.getNextSibling();
        }

        if (logger.isDebugEnabled()) {
            logger.debug("\t\t\t" + memoNode.getNodeName() + ": " + memoText.toString());
        }

        indexData.put(memoNode.getNodeName(), new IndexValue(null, memoText.toString()));
    }

    private void handleVersioningNode(Node versioningNode) throws ParserException {
        Node node = versioningNode.getFirstChild();

        IndexValue first = null;
        IndexValue sibling = null;
        IndexValue current = null;

        while (node != null) {
            if (node.getNodeType() != Node.ELEMENT_NODE) {
                node = node.getNextSibling();
                continue;
            }

            if (node.getAttributes() == null || node.getAttributes().getNamedItem("name") == null) {
                throw new ParserException("Illegal versioning tag in index file!");
            }

            Node attribute = node.getAttributes().getNamedItem("name");

            if (attribute == null) {
                throw new ParserException("Missing name for versioning tag in index file!");
            }

            String value = attribute.getNodeValue();

            if (first == null) {
                first = new IndexValue("name", value);
                sibling = first;
                current = first;
            } else {
                sibling.setSibling(new IndexValue("name", value));
                sibling = sibling.getSibling();
                current = sibling;
            }

            node = node.getNextSibling();
        }

        if (first != null) {
            indexData.put("versioning", first);
        }
    }

    private void handleWorkflowListNode(Node listNode) throws ParserException {
        Node node = listNode.getFirstChild();

        IndexValue first = null;
        IndexValue sibling = null;
        IndexValue current = null;

        while (node != null) {
            if (node.getNodeType() != Node.ELEMENT_NODE) {
                node = node.getNextSibling();
                continue;
            }

            if (node.getAttributes() == null || node.getAttributes().getNamedItem("name") == null) {
                throw new ParserException("Illegal workflow tag in index file!");
            }

            Node attribute = node.getAttributes().getNamedItem("name");

            if (attribute == null) {
                throw new ParserException("Missing name for workflow tag in index file!");
            }

            String value = attribute.getNodeValue();

            if (first == null) {
                first = new IndexValue("name", value);
                sibling = first;
                current = first;
            } else {
                sibling.setSibling(new IndexValue("name", value));
                sibling = sibling.getSibling();
                current = sibling;
            }

            attribute = node.getAttributes().getNamedItem("template");

            if (attribute == null) {
                throw new ParserException("Missing template attribute for workflow tag in index file!");
            }

            value = attribute.getNodeValue();

            current.setNext(new IndexValue("template", value));
            current = current.getNext();

            node = node.getNextSibling();
        }

        if (first != null) {
            indexData.put("workflowlist", first);
        }
    }

    private void checkMaskName(String maskName) throws ParserException {
        if (map.containsKey(maskName)) {
            currentMaskName = maskName;
            currentMaxNumberOfIndexField = map.get(maskName);
        } else {
            java.util.Set<String> set = map.keySet();
            java.util.Iterator iter = set.iterator();
            String maskList = "";
            int i = 1;
            while (iter.hasNext()) {
                maskList = maskList + i + ". " + iter.next().toString() + "\n ";
                i++;
            }
            throw new ParserException("ELO archiv contains any mask with name " + maskName + "\n "
                    + "Currently XMLimporter use following list of mask :" + "\n " + maskList
                    + "If the mask " + maskName + " is created recently in ELO, then restart XMLimporter");
        }
    }
}
