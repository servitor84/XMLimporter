package de.di.xml;


/**
 *
 * @author A. Sopicki
 */
public class ProductInfo {

    public static void readProductInfo(java.io.InputStream in, java.util.Map<String, String> status) {
        java.util.Properties p = new java.util.Properties();
        try {
            p.load(in);
            String version = p.getProperty("app.version");
            String productName = p.getProperty("app.product_name");
            // "<b>DOK</b>inform<sup>&#174;</sup> <b>XML</b><i>importer</i> for <b>ELO</b><sup>&#174;</sup>";
            String product = p.getProperty("app.product");
            String build = p.getProperty("app.build");
            in.close();

            if (version != null) {
             /*   if (build.length() == 3){
                    build = "00" + build;
                } else if (build.length() == 4){
                    build = "0" + build;
                }
                     */
                status.put("version", version);
            } else {
                status.put("version", "");
            }
            
            status.put("product_name", productName);            

            if (product != null) {
                status.put("product", product);
            } else {
                status.put("product", "Illegal product value");
            }
        } catch (Exception e) {            
            status.put("product_name", "");
            status.put("version", "");
            status.put("product", "Illegal product value" + e.getMessage());
        }
    }

}
