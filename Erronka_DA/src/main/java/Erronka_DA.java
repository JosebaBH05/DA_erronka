
import org.w3c.dom.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Properties;

//Klase hau kanpineko bezero baten datuak gordetzeko erabiltzen da.
class Kanpina {
    String id, izena, abizena, emaila;
    String telefonoa, ostatuMota, sortzeEguna;
}

//Programa nagusia da. XML eta JSON fitxategiekin lan egiten da.
public class Erronka_DA {

    // 1. XML-a irakurri eta bezeroak kargatu
    public static List<Kanpina> kargatuBezeroak(String xmlBidea) {

        List<Kanpina> kanpinakLista = new ArrayList<>();

        try {
            File xmlFile = new File(xmlBidea);

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();

            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

            Document doc = dBuilder.parse(xmlFile);
            doc.getDocumentElement().normalize();

            NodeList nList = doc.getElementsByTagName("row");

            for (int i = 0; i < nList.getLength(); i++) {

                Node nNode = nList.item(i);

                if (nNode.getNodeType() == Node.ELEMENT_NODE) {

                    Element eElement = (Element) nNode;

                    Kanpina k = new Kanpina();

                    k.id = eElement.getElementsByTagName("id").item(0).getTextContent();
                    k.izena = eElement.getElementsByTagName("izena").item(0).getTextContent();
                    k.abizena = eElement.getElementsByTagName("abizena").item(0).getTextContent();
                    k.emaila = eElement.getElementsByTagName("emaila").item(0).getTextContent();
                    k.telefonoa = eElement.getElementsByTagName("telefonoa").item(0).getTextContent();
                    k.ostatuMota = eElement.getElementsByTagName("ostatu_mota").item(0).getTextContent();
                    k.sortzeEguna = eElement.getElementsByTagName("sortzeEguna").item(0).getTextContent();

                    kanpinakLista.add(k);
                }
            }

        } catch (Exception e) {
            System.err.println("Errorea XML-a irakurtzean: " + e.getMessage());
        }

        return kanpinakLista;
    }


    // 2. C#-tik JSON jaso eta Kanpina zerrenda bihurtu
    public static List<Kanpina> jasoBezeroak() throws Exception {

        BufferedReader br = new BufferedReader(
            new InputStreamReader(System.in, StandardCharsets.UTF_8)
        );

        StringBuilder json = new StringBuilder();
        String lerroa;

        while ((lerroa = br.readLine()) != null) {
            json.append(lerroa);
        }

        Object emaitza = new JsonParser(json.toString()).parse();

        if (!(emaitza instanceof List<?>)) {
            throw new Exception("JSON-ak array bat izan behar du.");
        }

        List<Kanpina> kanpinak = new ArrayList<>();

        for (Object elementua : (List<?>) emaitza) {

            if (!(elementua instanceof Map<?, ?>)) {
                throw new Exception("Bezero bakoitza JSON objektu bat izan behar da.");
            }

            Map<?, ?> datuak = (Map<?, ?>) elementua;

            Kanpina k = new Kanpina();

            k.id = balioa(datuak, "id");
            k.izena = balioa(datuak, "izena");
            k.abizena = balioa(datuak, "abizena");
            k.emaila = balioa(datuak, "emaila");
            k.telefonoa = balioa(datuak, "telefonoa");
            k.ostatuMota = balioa(datuak, "ostatuMota");
            k.sortzeEguna = balioa(datuak, "sortzeEguna");

            kanpinak.add(k);
        }

        return kanpinak;
    }


    // JSON balioak lortu
    private static String balioa(Map<?, ?> datuak, String gakoa) {

        Object balioa = datuak.get(gakoa);

        return balioa == null ? "" : balioa.toString();
    }


    // 3. CSV-ra esportatu
    public static void esportatuCSVra(
        List<Kanpina> kanpinak,
        String fitxategiBidea
    ) throws IOException {

        File fitxategia = new File(fitxategiBidea);

        if (fitxategia.getParentFile() != null) {
            fitxategia.getParentFile().mkdirs();
        }

        try (PrintWriter pw = new PrintWriter(
            Files.newBufferedWriter(
                fitxategia.toPath(),
                StandardCharsets.UTF_8
            )
        )) {

            pw.println("id,izena,abizena,emaila,telefonoa,ostatuMota,sortzeEguna");

            for (Kanpina k : kanpinak) {

                pw.println(
                    csvBalioa(k.id) + "," +
                    csvBalioa(k.izena) + "," +
                    csvBalioa(k.abizena) + "," +
                    csvBalioa(k.emaila) + "," +
                    csvBalioa(k.telefonoa) + "," +
                    csvBalioa(k.ostatuMota) + "," +
                    csvBalioa(k.sortzeEguna)
                );
            }
        }

        System.err.println("CSV esportazioa ondo gauzatu da: " + fitxategiBidea);
    }


    // CSV balioak babestu
    private static String csvBalioa(String balioa) {

        if (balioa == null) {
            balioa = "";
        }

        return "\"" + balioa.replace("\"", "\"\"") + "\"";
    }


    // 4. XML-ra esportatu
    public static void esportatuXMLra(
        List<Kanpina> kanpinak,
        String fitxategiBidea
    ) throws Exception {

        File fitxategia = new File(fitxategiBidea);

        if (fitxategia.getParentFile() != null) {
            fitxategia.getParentFile().mkdirs();
        }

        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();

        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

        Document doc = docBuilder.newDocument();

        Element rootElement = doc.createElement("bezeroak");
        doc.appendChild(rootElement);

        for (Kanpina k : kanpinak) {

            Element row = doc.createElement("row");

            gehituElementua(doc, row, "id", k.id);
            gehituElementua(doc, row, "izena", k.izena);
            gehituElementua(doc, row, "abizena", k.abizena);
            gehituElementua(doc, row, "emaila", k.emaila);
            gehituElementua(doc, row, "telefonoa", k.telefonoa);
            gehituElementua(doc, row, "ostatu_mota", k.ostatuMota);
            gehituElementua(doc, row, "sortzeEguna", k.sortzeEguna);

            rootElement.appendChild(row);
        }

        TransformerFactory transformerFactory =
            TransformerFactory.newInstance();

        Transformer transformer =
            transformerFactory.newTransformer();

        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

        DOMSource source = new DOMSource(doc);

        StreamResult result = new StreamResult(fitxategia);

        transformer.transform(source, result);

        System.err.println("XML esportazioa ondo gauzatu da: " + fitxategiBidea);
    }


    // XML elementuak sortu
    private static void gehituElementua(
        Document doc,
        Element parent,
        String izena,
        String balioa
    ) {

        Element elementua = doc.createElement(izena);

        elementua.appendChild(
            doc.createTextNode(balioa == null ? "" : balioa)
        );

        parent.appendChild(elementua);
    }


    // 5. JSON string seguru bihurtu
    private static String jsonSegurua(String testua) {

        if (testua == null) {
            return "";
        }

        return testua
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t");
    }


    // 6. .jar dagoen karpeta lortu
    private static File lortuJarKarpeta() {

        try {

            File jarFitxategia = new File(
                Erronka_DA.class.getProtectionDomain()
                    .getCodeSource().getLocation().toURI()
            );

            return jarFitxategia.isFile()
                ? jarFitxategia.getParentFile()
                : jarFitxategia;

        } catch (URISyntaxException e) {
            return new File(".");
        }
    }


    // 7. MAIN
    public static void main(String[] args) {

        try {

            if (args.length == 0) {
                throw new Exception("Eragiketa mota zehaztu behar da.");
            }

            String eragiketa = args[0].toLowerCase();

            // IMPORTAR: XML-tik irakurri eta C#-ri JSON bidali
            if (eragiketa.equals("importar")) {

                Properties propietateak = new Properties();

                File jarKarpeta = lortuJarKarpeta();

                File konfigurazioa = new File(
                    jarKarpeta, "konfigurazio.txt"
                );

                try (FileInputStream fis =
                    new FileInputStream(konfigurazioa)) {

                    propietateak.load(fis);
                }

                String xmlBidea =
                    propietateak.getProperty("xml.ruta");

                if (xmlBidea == null) {
                    throw new Exception(
                        "'xml.ruta' ez da konfigurazioan aurkitu."
                    );
                }

                File xmlFile = new File(xmlBidea);

                if (!xmlFile.isAbsolute()) {
                    xmlFile = new File(jarKarpeta, xmlBidea);
                }

                List<Kanpina> kanpinak =
                    kargatuBezeroak(xmlFile.getPath());

                // JSON-a C#-ri bidali
                System.out.println("[");

                for (int i = 0; i < kanpinak.size(); i++) {

                    Kanpina k = kanpinak.get(i);

                    System.out.println("  {");
                    System.out.println("    \"id\": \"" + jsonSegurua(k.id) + "\",");
                    System.out.println("    \"izena\": \"" + jsonSegurua(k.izena) + "\",");
                    System.out.println("    \"abizena\": \"" + jsonSegurua(k.abizena) + "\",");
                    System.out.println("    \"emaila\": \"" + jsonSegurua(k.emaila) + "\",");
                    System.out.println("    \"telefonoa\": \"" + jsonSegurua(k.telefonoa) + "\",");
                    System.out.println("    \"ostatuMota\": \"" + jsonSegurua(k.ostatuMota) + "\",");
                    System.out.println("    \"sortzeEguna\": \"" + jsonSegurua(k.sortzeEguna) + "\"");

                    System.out.print(
                        "  }" +
                        (i < kanpinak.size() - 1 ? "," : "") +
                        "\n"
                    );
                }

                System.out.println("]");
            }

            // EXPORTAR: C#-tik JSON jaso eta CSV edo XML sortu
            else if (eragiketa.equals("csv") ||
                     eragiketa.equals("xml")) {

                List<Kanpina> kanpinak = jasoBezeroak();

                File jarKarpeta = lortuJarKarpeta();

                Properties propietateak = new Properties();

                File konfigurazioa = new File(
                    jarKarpeta, "konfigurazio.txt"
                );

                try (FileInputStream fis =
                    new FileInputStream(konfigurazioa)) {

                    propietateak.load(fis);
                }

                String csvIrteera = propietateak.getProperty(
                    "export.csv",
                    new File(
                        jarKarpeta,
                        "3PAG2_E1_export.csv"
                    ).getPath()
                );

                String xmlIrteera = propietateak.getProperty(
                    "export.xml",
                    new File(
                        jarKarpeta,
                        "3PAG2_E1_export.xml"
                    ).getPath()
                );

                if (eragiketa.equals("csv")) {

                    esportatuCSVra(kanpinak, csvIrteera);

                } else {

                    esportatuXMLra(kanpinak, xmlIrteera);
                }

                System.out.println("OK");
            }

            else {

                throw new Exception(
                    "Erabili: importar, csv edo xml."
                );
            }

        } catch (Exception e) {

            System.err.println("Errorea: " + e.getMessage());
            System.exit(1);
        }
    }


    // 8. JSON irakurgailua (kanpoko libreriarik gabe)
    static class JsonParser {

        private final String json;
        private int pos = 0;

        JsonParser(String json) {
            this.json = json;
        }

        public Object parse() throws Exception {

            Object emaitza = balioaIrakurri();

            espazioakSaltatu();

            if (pos != json.length()) {
                throw new Exception("JSON-aren amaieran datu soberakinak daude.");
            }

            return emaitza;
        }

        private Object balioaIrakurri() throws Exception {

            espazioakSaltatu();

            if (pos >= json.length()) {
                throw new Exception("JSON hutsa edo osatugabea.");
            }

            char c = json.charAt(pos);

            if (c == '{') return objektuaIrakurri();
            if (c == '[') return arrayaIrakurri();
            if (c == '"') return stringaIrakurri();

            if (c == 't') {
                hitzaIrakurri("true");
                return Boolean.TRUE;
            }

            if (c == 'f') {
                hitzaIrakurri("false");
                return Boolean.FALSE;
            }

            if (c == 'n') {
                hitzaIrakurri("null");
                return null;
            }

            if (c == '-' || Character.isDigit(c)) {
                return zenbakiaIrakurri();
            }

            throw new Exception("JSON balio baliogabea: " + c);
        }

        private Map<String, Object> objektuaIrakurri() throws Exception {

            Map<String, Object> objektua = new LinkedHashMap<>();

            pos++;
            espazioakSaltatu();

            if (karaktereaDago('}')) {
                pos++;
                return objektua;
            }

            while (true) {

                espazioakSaltatu();

                if (pos >= json.length() || json.charAt(pos) != '"') {
                    throw new Exception("JSON objektuko gakoa baliogabea.");
                }

                String gakoa = stringaIrakurri();

                espazioakSaltatu();
                esperoKarakterea(':');

                Object balioa = balioaIrakurri();

                objektua.put(gakoa, balioa);

                espazioakSaltatu();

                if (karaktereaDago('}')) {
                    pos++;
                    break;
                }

                esperoKarakterea(',');
            }

            return objektua;
        }

        private List<Object> arrayaIrakurri() throws Exception {

            List<Object> arraya = new ArrayList<>();

            pos++;
            espazioakSaltatu();

            if (karaktereaDago(']')) {
                pos++;
                return arraya;
            }

            while (true) {

                arraya.add(balioaIrakurri());

                espazioakSaltatu();

                if (karaktereaDago(']')) {
                    pos++;
                    break;
                }

                esperoKarakterea(',');
            }

            return arraya;
        }

        private String stringaIrakurri() throws Exception {

            esperoKarakterea('"');

            StringBuilder sb = new StringBuilder();

            while (pos < json.length()) {

                char c = json.charAt(pos++);

                if (c == '"') {
                    return sb.toString();
                }

                if (c == '\\') {

                    if (pos >= json.length()) {
                        throw new Exception("JSON escape baliogabea.");
                    }

                    char e = json.charAt(pos++);

                    switch (e) {

                        case '"': sb.append('"'); break;
                        case '\\': sb.append('\\'); break;
                        case '/': sb.append('/'); break;
                        case 'b': sb.append('\b'); break;
                        case 'f': sb.append('\f'); break;
                        case 'n': sb.append('\n'); break;
                        case 'r': sb.append('\r'); break;
                        case 't': sb.append('\t'); break;

                        case 'u':

                            if (pos + 4 > json.length()) {
                                throw new Exception("Unicode escape osatugabea.");
                            }

                            String hex = json.substring(pos, pos + 4);

                            try {
                                sb.append((char) Integer.parseInt(hex, 16));
                            } catch (NumberFormatException ex) {
                                throw new Exception("Unicode escape baliogabea.");
                            }

                            pos += 4;
                            break;

                        default:
                            throw new Exception("JSON escape baliogabea: \\" + e);
                    }

                } else {

                    if (c < 0x20) {
                        throw new Exception("Kontrol karaktere baliogabea JSON stringean.");
                    }

                    sb.append(c);
                }
            }

            throw new Exception("JSON string osatugabea.");
        }

        private String zenbakiaIrakurri() throws Exception {

            int hasiera = pos;

            if (karaktereaDago('-')) pos++;

            if (pos >= json.length()) {
                throw new Exception("Zenbaki baliogabea.");
            }

            if (karaktereaDago('0')) {
                pos++;
            } else {
                digituakIrakurri();
            }

            if (karaktereaDago('.')) {
                pos++;
                digituakIrakurri();
            }

            if (karaktereaDago('e') || karaktereaDago('E')) {

                pos++;

                if (karaktereaDago('+') || karaktereaDago('-')) {
                    pos++;
                }

                digituakIrakurri();
            }

            String zenbakia = json.substring(hasiera, pos);

            try {
                return new java.math.BigDecimal(zenbakia).toPlainString();
            } catch (NumberFormatException e) {
                throw new Exception("Zenbaki baliogabea: " + zenbakia);
            }
        }

        private void digituakIrakurri() throws Exception {

            int hasiera = pos;

            while (pos < json.length()
                && Character.isDigit(json.charAt(pos))) {
                pos++;
            }

            if (pos == hasiera) {
                throw new Exception("Zenbaki baliogabea.");
            }
        }

        private void hitzaIrakurri(String hitza) throws Exception {

            if (!json.startsWith(hitza, pos)) {
                throw new Exception("JSON balio baliogabea.");
            }

            pos += hitza.length();
        }

        private void esperoKarakterea(char c) throws Exception {

            espazioakSaltatu();

            if (!karaktereaDago(c)) {
                throw new Exception("Espero zen karakterea: " + c);
            }

            pos++;
        }

        private boolean karaktereaDago(char c) {
            return pos < json.length() && json.charAt(pos) == c;
        }

        private void espazioakSaltatu() {

            while (pos < json.length()
                && Character.isWhitespace(json.charAt(pos))) {
                pos++;
            }
        }
    }
}
