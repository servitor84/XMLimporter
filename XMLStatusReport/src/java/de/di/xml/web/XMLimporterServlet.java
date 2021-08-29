package de.di.xml.web;

import java.io.*;
import javax.servlet.*;
import java.util.Locale;
import de.di.xml.Info;
import java.util.Properties;
import javax.servlet.http.*;
import de.di.xml.Importer;

public class XMLimporterServlet extends HttpServlet {

    public static final String importerAttribute = "de.di.xml.xmlimporter";
    public static java.util.List<String> errorStatus = null;

    @Override
    public void init() throws ServletException {
        // The absolute path of conf folder will be investigated and committed 
        ServletContext servletContext = getServletContext();
        String contextPathConf = servletContext.getRealPath("/conf");
        //String contextPathProfiles = servletContext.getRealPath("/profiles");
        Info.setConfigPath(contextPathConf + "\\config.properties");
        //Info.setProfilePath(contextPathProfiles + "\\activeProfile.properties");
        Info.setProperties(new File(servletContext.getRealPath("/conf") + "\\license.txt"), servletContext.getServletContextName());
        //Check if the XMLimporter is currently running
        Importer importer = (Importer) getServletContext().getAttribute(XMLimporterServlet.importerAttribute);
        //start the XMLimporter
        if (importer == null) {
            try {
                //get the config file as an InputStream
                InputStream confIS = getServletContext().getResourceAsStream("/conf/config.properties");
                InputStream license = getServletContext().getResourceAsStream("/conf/license.txt");
                Properties confPr = new Properties();
                if (confIS == null) {
                    throw new Exception("Config file not found");
                } else {
                    confPr.load(confIS);
                    confIS.close();
                }
                if (license == null) {
                    throw new Exception("License file not found");
                }
                //start the application
                importer = new Importer(confPr, license);
                getServletContext().setAttribute(XMLimporterServlet.importerAttribute, importer);
                importer.start();
                errorStatus = importer.getErrorStatus();
            } catch (Exception ex) {
                getServletContext().log("Startup failed due to Exception. ",
                        ex);
                if (errorStatus == null) {
                    errorStatus = new java.util.ArrayList<String>();
                    errorStatus.add(ex.getMessage());
                }
            }
        }
    }

    @Override
    public void destroy() {
        //check if the XmlImporter is active
        Importer importer = (Importer) getServletContext().getAttribute(XMLimporterServlet.importerAttribute);
        //shutdown the XmlImporter
        if (importer != null) {
            importer.shutdown();
            try {
                importer.join(5000);
            } catch (InterruptedException iex) {
            }
            //remove it from the servlet context
            getServletContext().removeAttribute(XMLimporterServlet.importerAttribute);
        }
    }

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     */
    protected void processRequest(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {
        if (request.getLocale().equals(Locale.GERMAN)) {
            //forward get and post requests to the index.html file
            request.getRequestDispatcher("/index.html").forward(request, response);
        } else {
            //forward get and post requests to the index_en.html file
            request.getRequestDispatcher("/index_en.html").forward(request, response);
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     */
    @Override
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     */
    @Override
    protected void doPost(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }
    // </editor-fold>
}
