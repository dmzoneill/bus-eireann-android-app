package ie.buseireann;

import android.util.Log;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerPNames;
import org.apache.http.conn.params.ConnPerRouteBean;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;

public final class WebHelper {
  /** HTTP port. */
  private static final int HTTP_PORT = 80;

  /** HTTPS port. */
  private static final int HTTPS_PORT = 443;

  /** Max pooled connections. */
  private static final int MAX_CONNECTIONS = 30;

  /** Index of the value attribute in a split hidden input tag. */
  private static final int HIDDEN_VALUE_INDEX = 5;

  /** Max later-result pages to follow. */
  private static final int MAX_LATER_PAGES = 7;

  /** Result column: departure. */
  private static final int COL_DEPARTURE = 3;

  /** Result column: arrival. */
  private static final int COL_ARRIVAL = 5;

  /** Result column: duration. */
  private static final int COL_DURATION = 7;

  /** Result column: changes. */
  private static final int COL_CHANGES = 8;

  /** The instance. */
  private static WebHelper instance = null;

  // web pages
  /** The baseUrl. */
  private String baseUrl;

  /** The sequenceUrl. */
  private String sequenceUrl;

  // browser
  /** The userAgent. */
  private String userAgent;

  // http stuff
  /** The cookies. */
  private List<Cookie> cookies;

  /** The httpclient. */
  private DefaultHttpClient httpclient;

  /** The response. */
  private HttpResponse response;

  /** The entity. */
  private HttpEntity entity;

  /** The postFields. */
  private List<NameValuePair> postFields;

  // result holders
  /** The lastError. */
  private String lastError;

  /** The error. */
  private boolean error;

  /** The result. */
  private String result;

  /** The output. */
  private boolean output;

  // data
  /** The ambiguous. */
  private boolean ambiguous;

  /** The connectionsList. */
  private boolean connectionsList;

  /** The toList. */
  private ArrayList<String[]> toList;

  /** The fromList. */
  private ArrayList<String[]> fromList;

  /** The connections. */
  private ArrayList<Connection> connections;

  /** The lastTo. */
  private String lastTo;

  /** The lastFrom. */
  private String lastFrom;

  // this instance

  private WebHelper() {
    this.baseUrl = "http://194.106.151.94/jplan/bin/query.exe/en?OK#focus";
    this.userAgent =
        "Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.9.1.4pre) Gecko/20090829 Ubuntu/9.04 (jaunty)"
            + " Shiretoko/3.5.4pre";
    this.initHTTPClient();
  }

  /** Init h t t p client. */
  public void initHTTPClient() {
    try {
      SchemeRegistry schemeRegistry = new SchemeRegistry();
      schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), HTTP_PORT));
      schemeRegistry.register(new Scheme("https", new EasySSLSocketFactory(), HTTPS_PORT));

      HttpParams params = new BasicHttpParams();
      params.setParameter(ConnManagerPNames.MAX_TOTAL_CONNECTIONS, MAX_CONNECTIONS);
      params.setParameter(
          ConnManagerPNames.MAX_CONNECTIONS_PER_ROUTE, new ConnPerRouteBean(MAX_CONNECTIONS));
      params.setParameter(CoreProtocolPNames.USE_EXPECT_CONTINUE, false);
      HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);

      ClientConnectionManager cm = new SingleClientConnManager(params, schemeRegistry);
      this.httpclient = new DefaultHttpClient(cm, params);

    } catch (Exception e) {
      this.error = true;
      this.lastError = e.getMessage();
    }
  }

  private boolean getRequest(final String url) {
    try {
      HttpGet httpget = new HttpGet(url);
      httpget.setHeader("User-Agent", this.userAgent);
      this.response = this.httpclient.execute(httpget);
      this.readPage();
      return true;
    } catch (Exception e) {
      this.error = true;
      this.lastError = e.getMessage();
      return false;
    }
  }

  private boolean postRequest(final String url, final List<NameValuePair> fields) {
    try {
      HttpPost reqPost = new HttpPost(url);
      reqPost.setHeader("User-Agent", this.userAgent);
      reqPost.setEntity(new UrlEncodedFormEntity(fields, HTTP.UTF_8));
      this.response = this.httpclient.execute(reqPost);
      this.readPage();
      return true;
    } catch (Exception e) {
      this.error = true;
      this.lastError = e.getMessage();
      return false;
    }
  }

  /**
   * Get result.
   *
   * @return the result
   */
  public String getResult() {
    return this.result;
  }

  private String removeHTML(final String html) {
    html = html.trim();
    html = html.replaceAll("\\<.*?>", "");
    html = html.trim();
    html = html.replaceAll("&nbsp;", "");
    html = html.trim();
    html = html.replaceAll("&amp;", "");
    html = html.trim();
    html = html.replaceAll("\\n", "");
    html = html.trim();
    html = html.replaceAll("\\n\\r", "");
    html = html.trim();
    html = html.replaceAll("&euro;", "€");

    return html;
  }

  private void readPage() {
    this.cookies = this.httpclient.getCookieStore().getCookies();

    String html = "";
    try {
      InputStream in = this.response.getEntity().getContent();
      BufferedReader reader = new BufferedReader(new InputStreamReader(in));
      StringBuilder str = new StringBuilder();
      String line = null;
      while ((line = reader.readLine()) != null) {
        if (this.output) {
          Log.e("WebHelper", line);
        }
        str.append(line);
      }
      in.close();
      html = str.toString();
    } catch (Exception e) {
      html = e.getMessage();
      e.printStackTrace();
    }

    this.result = html;
  }

  /**
   * Get last error.
   *
   * @return the result
   */
  public String getLastError() {
    this.error = false;
    return this.lastError;
  }

  /**
   * Get instance.
   *
   * @return the result
   */
  public static WebHelper getInstance() {
    if (WebHelper.instance == null) {
      WebHelper.instance = new WebHelper();
    }

    return WebHelper.instance;
  }

  /** Prepare select boxes. */
  public void prepareSelectBoxes() {
    this.toList = new ArrayList<String[]>();
    this.fromList = new ArrayList<String[]>();

    Pattern selectRegex =
        Pattern.compile(
            "<select[^>]*>(.*?)</select>",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.MULTILINE);
    Matcher selects = selectRegex.matcher(this.result);

    Pattern fromHiddenRegex =
        Pattern.compile(
            "<input type=\"hidden\" name=\"REQ0JourneyStopsS0K\" value=\"(.*?)\">",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.MULTILINE);
    Matcher fromHidden = fromHiddenRegex.matcher(this.result);

    while (fromHidden.find()) {
      String[] hidden = fromHidden.group().split("\"");
      String[] data = {hidden[HIDDEN_VALUE_INDEX], this.lastFrom};
      this.fromList.add(data);
    }

    Pattern toHiddenRegex =
        Pattern.compile(
            "<input type=\"hidden\" name=\"REQ0JourneyStopsZ0K\" value=\"(.*?)\">",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.MULTILINE);
    Matcher toHidden = toHiddenRegex.matcher(this.result);

    while (toHidden.find()) {
      String[] hidden = toHidden.group().split("\"");
      String[] data = {hidden[HIDDEN_VALUE_INDEX], this.lastTo};
      this.toList.add(data);
    }

    while (selects.find()) {
      Pattern optionRegex =
          Pattern.compile(
              "<option[^>]*>(.*?)</option>",
              Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.MULTILINE);
      Matcher options = optionRegex.matcher(selects.group());

      if (selects.group().contains("id=\"from\"")) {
        while (options.find()) {
          String[] option = options.group().split("\"");
          String val = option[1];
          String name = option[2];
          int startHtml = name.indexOf(">");
          int endHtml = name.indexOf("<");
          name = name.substring(startHtml + 1, endHtml).trim();
          String[] data = {val, name};
          this.fromList.add(data);
        }
      } else if (selects.group().contains("id=\"to\"")) {
        while (options.find()) {
          String[] option = options.group().split("\"");
          String val = option[1];
          String name = option[2];
          int startHtml = name.indexOf(">");
          int endHtml = name.indexOf("<");
          name = name.substring(startHtml + 1, endHtml).trim();
          String[] data = {val, name};
          this.toList.add(data);
        }
      }
    }

    Pattern formRegex =
        Pattern.compile(
            "<form[^>]*>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.MULTILINE);
    Matcher form = formRegex.matcher(this.result);

    while (form.find()) {
      String[] parts = form.group().split("\"");
      this.sequenceUrl = parts[1];
    }
  }

  /**
   * Search connection.
   *
   * @param date the date
   * @param from the from
   * @param to the to
   * @param time the time
   * @return the result
   */
  public boolean searchConnection(
      final String date, final String from, final String to, final String time) {
    this.ambiguous = false;
    this.connectionsList = false;

    this.postFields = new ArrayList<NameValuePair>();
    this.postFields.add(new BasicNameValuePair("REQ0HafasSearchForw", "1"));
    this.postFields.add(new BasicNameValuePair("REQ0HafasSkipLongChanges", "1"));
    this.postFields.add(new BasicNameValuePair("REQ0JourneyDate", date));
    this.postFields.add(new BasicNameValuePair("REQ0JourneyStopsS0A", "1"));
    this.postFields.add(new BasicNameValuePair("REQ0JourneyStopsS0G", from));
    this.postFields.add(new BasicNameValuePair("REQ0JourneyStopsS0ID", ""));
    this.postFields.add(new BasicNameValuePair("REQ0JourneyStopsZ0A", "1"));
    this.postFields.add(new BasicNameValuePair("REQ0JourneyStopsZ0G", to));
    this.postFields.add(new BasicNameValuePair("REQ0JourneyStopsZ0ID", ""));
    this.postFields.add(new BasicNameValuePair("REQ0JourneyTime", time));
    this.postFields.add(new BasicNameValuePair("queryPageDisplayed", "yes"));
    this.postFields.add(new BasicNameValuePair("start", "Search connection"));
    this.postFields.add(new BasicNameValuePair("wDayExt0", "Mo|Tu|We|Th|Fr|Sa|Su"));

    this.lastTo = to;
    this.lastFrom = from;

    this.output = false;

    if (this.postRequest(this.baseUrl, this.postFields)) {
      if (this.result.contains("Your input is ambiguous")) {
        this.ambiguous = true;
        this.prepareSelectBoxes();
      } else if (this.result.contains("Connections sorted by")) {
        this.connectionsList = true;
        this.prepareConnectionsList();
      }

      return true;
    } else {
      return false;
    }
  }

  /**
   * Get key.
   *
   * @param value the value
   * @param list the list
   * @return the result
   */
  public String getKey(final String value, final ArrayList<String[]> list) {
    for (int y = 0; y < list.size(); y++) {
      if (list.get(y)[1].compareTo(value) == 0) {
        Log.e("Found", list.get(y)[0]);
        return list.get(y)[0];
      }
    }

    return "";
  }

  private void prepareConnectionsList() {
    int i = 0;
    while (i < MAX_LATER_PAGES) {
      String nextUrl = "";
      Pattern laterRegex =
          Pattern.compile(
              "<a[^>]*>later(.*?)</a>",
              Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.MULTILINE);
      Matcher later = laterRegex.matcher(this.result);

      while (later.find()) {
        String[] parts = later.group().split("\"");
        nextUrl = parts[1];
        break;
      }
      i++;

      // Only follow "later" links that stay on the journey planner host.
      if (!nextUrl.startsWith("http://194.106.151.94/")) {
        break;
      }

      this.getRequest(nextUrl);
    }

    Pattern connectionsRegex =
        Pattern.compile(
            "<table[^>]*>(.*?)</table>",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.MULTILINE);
    Matcher connMatcher = connectionsRegex.matcher(this.result);

    this.connections = new ArrayList<Connection>();

    while (connMatcher.find()) {
      if (connections.group().contains("summary=\"Connections overview\"")) {
        Pattern rowsRegex =
            Pattern.compile(
                "<tr[^>]*>(.*?)</tr>",
                Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.MULTILINE);
        Matcher rows = rowsRegex.matcher(connMatcher.group());

        while (rows.find()) {
          int col = 0;

          Pattern colsRegex =
              Pattern.compile(
                  "<td[^>]*>(.*?)</td>",
                  Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.MULTILINE);
          Matcher cols = colsRegex.matcher(rows.group());

          String sFrom = "";
          String sTo = "";
          String sdate = "";
          String sdeparture = "";
          String sarrival = "";
          String sduration = "";
          String schanges = "";

          while (cols.find()) {
            String td = cols.group();

            if (col == 1) {
              String[] places = td.split("<br />");
              sFrom = this.removeHTML(places[0]);
              sTo = this.removeHTML(places[1]);
            } else if (col == COL_DEPARTURE) {
              sdate = this.removeHTML(td);
            } else if (col == COL_ARRIVAL) {
              String[] times = td.split("<br />");
              sdeparture = this.removeHTML(times[0]);
              sarrival = this.removeHTML(times[1]);
            } else if (col == COL_DURATION) {
              sduration = this.removeHTML(td);
            } else if (col == COL_CHANGES) {
              schanges = this.removeHTML(td);
            }

            col++;
          }

          this.connections.add(
              new Connection(sFrom, sTo, sarrival, sdeparture, sduration, schanges, sdate));
        }
      }
    }

    try {
      this.connections.remove(0);
    } catch (Exception p) {
    }
  }

  /**
   * Search connection.
   *
   * @param date the date
   * @param from the from
   * @param to the to
   * @param time the time
   * @param seq the seq
   * @return the result
   */
  public boolean searchConnection(
      final String date, final String from, final String to, final String time, final int seq) {
    this.ambiguous = false;

    this.postFields = new ArrayList<NameValuePair>();
    this.postFields.add(new BasicNameValuePair("REQ0HafasSearchForw", "1"));
    this.postFields.add(new BasicNameValuePair("REQ0HafasSkipLongChanges", "1"));
    this.postFields.add(new BasicNameValuePair("REQ0JourneyDate", date));
    this.postFields.add(new BasicNameValuePair("REQ0JourneyStopsS0A", "1"));
    this.postFields.add(
        new BasicNameValuePair("REQ0JourneyStopsS0K", this.getKey(from, this.fromList)));
    this.postFields.add(new BasicNameValuePair("REQ0JourneyStopsZ0A", "1"));
    this.postFields.add(
        new BasicNameValuePair("REQ0JourneyStopsZ0K", this.getKey(to, this.toList)));
    this.postFields.add(new BasicNameValuePair("REQ0JourneyTime", time));
    this.postFields.add(new BasicNameValuePair("queryPageDisplayed", "yes"));
    this.postFields.add(new BasicNameValuePair("start", "Search connection"));
    this.postFields.add(new BasicNameValuePair("wDayExt0", "Mo|Tu|We|Th|Fr|Sa|Su"));

    this.lastTo = to;
    this.lastFrom = from;

    this.output = false;

    if (this.postRequest(this.sequenceUrl, this.postFields)) {
      if (this.result.contains("Your input is ambiguous")) {
        this.ambiguous = true;
        this.prepareSelectBoxes();
      } else if (this.result.contains("Connections sorted by")) {
        this.connectionsList = true;
        this.prepareConnectionsList();
      }

      return true;
    } else {
      return false;
    }
  }

  /**
   * Is ambiguous.
   *
   * @return the result
   */
  public boolean isAmbiguous() {
    return this.ambiguous;
  }

  /**
   * Getfrom list.
   *
   * @return the result
   */
  public String[] getfromList() {
    String[] types = new String[this.fromList.size()];

    for (int y = 0; y < this.fromList.size(); y++) {
      types[y] = this.fromList.get(y)[1];
    }

    return types;
  }

  /**
   * Getto list.
   *
   * @return the result
   */
  public String[] gettoList() {
    String[] types = new String[this.toList.size()];

    for (int y = 0; y < this.toList.size(); y++) {
      types[y] = this.toList.get(y)[1];
    }

    return types;
  }

  /**
   * Get connections.
   *
   * @return the result
   */
  public ArrayList<Connection> getConnections() {
    if (this.connections == null) {
      return new ArrayList<Connection>();
    }

    return this.connections;
  }

  /**
   * Get connections count.
   *
   * @return the result
   */
  public int getConnectionsCount() {
    if (this.connections == null) {
      return 0;
    }

    return this.connections.size();
  }

  /**
   * Get last to.
   *
   * @return the result
   */
  public String getLastTo() {
    return lastTo;
  }

  /**
   * Get last from.
   *
   * @return the result
   */
  public String getLastFrom() {
    return lastFrom;
  }

  /**
   * Has connections.
   *
   * @return the result
   */
  public boolean hasConnections() {
    return this.connectionsList;
  }
}
