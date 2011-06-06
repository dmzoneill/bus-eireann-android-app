package ie.buseireann;

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

import android.util.Log;

public class WebHelper
{
	// web pages
	private String baseUrl;
	private String sequenceUrl;

	// broswer
	private String userAgent;

	// http stuff
	private List< Cookie > cookies;
	private DefaultHttpClient httpclient;
	private HttpResponse response;
	private HttpEntity entity;
	private List< NameValuePair > postFields;

	// result holders
	private String lastError;
	private boolean error;
	private String result;
	private boolean output;

	// data
	private boolean ambiguous;
	private boolean connectionsList;
	private ArrayList< String[] > toList;
	private ArrayList< String[] > fromList;
	private ArrayList< Connection > connections;
	private String lastTo;
	private String lastFrom;

	// this instance
	private static WebHelper instance = null;

	private WebHelper()
	{
		this.baseUrl = "http://194.106.151.94/jplan/bin/query.exe/en?OK#focus";
		this.userAgent = "Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.9.1.4pre) Gecko/20090829 Ubuntu/9.04 (jaunty) Shiretoko/3.5.4pre";
		this.initHTTPClient();
	}

	public void initHTTPClient()
	{
		try
		{
			SchemeRegistry schemeRegistry = new SchemeRegistry();
			schemeRegistry.register( new Scheme( "http" , PlainSocketFactory.getSocketFactory() , 80 ) );
			schemeRegistry.register( new Scheme( "https" , new EasySSLSocketFactory() , 443 ) );

			HttpParams params = new BasicHttpParams();
			params.setParameter( ConnManagerPNames.MAX_TOTAL_CONNECTIONS , 30 );
			params.setParameter( ConnManagerPNames.MAX_CONNECTIONS_PER_ROUTE , new ConnPerRouteBean( 30 ) );
			params.setParameter( CoreProtocolPNames.USE_EXPECT_CONTINUE , false );
			HttpProtocolParams.setVersion( params , HttpVersion.HTTP_1_1 );

			ClientConnectionManager cm = new SingleClientConnManager( params , schemeRegistry );
			this.httpclient = new DefaultHttpClient( cm , params );

		}
		catch( Exception e )
		{
			this.error = true;
			this.lastError = e.getMessage();
		}
	}

	private boolean getRequest( String url )
	{
		try
		{
			HttpGet httpget = new HttpGet( url );
			httpget.setHeader( "User-Agent" , this.userAgent );
			this.response = this.httpclient.execute( httpget );
			this.readPage();
			return true;
		}
		catch( Exception e )
		{
			this.error = true;
			this.lastError = e.getMessage();
			return false;
		}
	}

	private boolean postRequest( String url , List< NameValuePair > fields )
	{
		try
		{
			HttpPost reqPost = new HttpPost( url );
			reqPost.setHeader( "User-Agent" , this.userAgent );
			reqPost.setEntity( new UrlEncodedFormEntity( fields , HTTP.UTF_8 ) );
			this.response = this.httpclient.execute( reqPost );
			this.readPage();
			return true;
		}
		catch( Exception e )
		{
			this.error = true;
			this.lastError = e.getMessage();
			return false;
		}
	}

	public String getResult()
	{
		return this.result;
	}

	private String removeHTML( String html )
	{
		html = html.trim();
		html = html.replaceAll( "\\<.*?>" , "" );
		html = html.trim();
		html = html.replaceAll( "&nbsp;" , "" );
		html = html.trim();
		html = html.replaceAll( "&amp;" , "" );
		html = html.trim();
		html = html.replaceAll( "\\n" , "" );
		html = html.trim();
		html = html.replaceAll( "\\n\\r" , "" );
		html = html.trim();
		html = html.replaceAll( "&euro;" , "€" );

		return html;
	}

	private void readPage()
	{
		this.cookies = this.httpclient.getCookieStore().getCookies();

		String html = "";
		try
		{
			InputStream in = this.response.getEntity().getContent();
			BufferedReader reader = new BufferedReader( new InputStreamReader( in ) );
			StringBuilder str = new StringBuilder();
			String line = null;
			while ( ( line = reader.readLine() ) != null )
			{
				if ( this.output == true )
				{
					Log.e( "WebHelper" , line );
				}
				str.append( line );
			}
			in.close();
			html = str.toString();
		}
		catch( Exception e )
		{
			html = e.getMessage();
			e.printStackTrace();
		}

		this.result = html;
	}

	public String getLastError()
	{
		this.error = false;
		return this.lastError;
	}

	public static WebHelper getInstance()
	{
		if ( WebHelper.instance == null )
		{
			WebHelper.instance = new WebHelper();
		}

		return WebHelper.instance;
	}

	public void prepareSelectBoxes()
	{
		this.toList = new ArrayList< String[] >();
		this.fromList = new ArrayList< String[] >();

		Pattern selectRegex = Pattern.compile( "<select[^>]*>(.*?)</select>" , Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.MULTILINE );
		Matcher selects = selectRegex.matcher( this.result );
		
		Pattern fromHiddenRegex = Pattern.compile( "<input type=\"hidden\" name=\"REQ0JourneyStopsS0K\" value=\"(.*?)\">" , Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.MULTILINE );
		Matcher fromHidden = fromHiddenRegex.matcher( this.result );
		
		while ( fromHidden.find() )
		{
			String[] hidden = fromHidden.group().split( "\"" );
			String[] data = { hidden[ 5 ] , this.lastFrom };
			this.fromList.add( data );
		}
		
		
		Pattern toHiddenRegex = Pattern.compile( "<input type=\"hidden\" name=\"REQ0JourneyStopsZ0K\" value=\"(.*?)\">" , Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.MULTILINE );
		Matcher toHidden = toHiddenRegex.matcher( this.result );
		
		while ( toHidden.find() )
		{
			String[] hidden = toHidden.group().split( "\"" );
			String[] data = { hidden[ 5 ] , this.lastTo };
			this.toList.add( data );
		}		

		while ( selects.find() )
		{
			Pattern optionRegex = Pattern.compile( "<option[^>]*>(.*?)</option>" , Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.MULTILINE );
			Matcher options = optionRegex.matcher( selects.group() );

			if ( selects.group().contains( "id=\"from\"" ) )
			{
				while ( options.find() )
				{
					String[] option = options.group().split( "\"" );
					String val = option[ 1 ];
					String name = option[ 2 ];
					int startHtml = name.indexOf( ">" );
					int endHtml = name.indexOf( "<" );
					name = name.substring( startHtml + 1 , endHtml ).trim();
					String[] data = { val , name };
					this.fromList.add( data );
				}
			}
			else if ( selects.group().contains( "id=\"to\"" ) )
			{
				while ( options.find() )
				{
					String[] option = options.group().split( "\"" );
					String val = option[ 1 ];
					String name = option[ 2 ];
					int startHtml = name.indexOf( ">" );
					int endHtml = name.indexOf( "<" );
					name = name.substring( startHtml + 1 , endHtml ).trim();
					String[] data = { val , name };
					this.toList.add( data );
				}
			}
		}
		
		
		Pattern formRegex = Pattern.compile( "<form[^>]*>" , Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.MULTILINE );
		Matcher form = formRegex.matcher( this.result );
		
		while( form.find() )
		{
			String[] parts = form.group().split( "\"" );
			this.sequenceUrl = parts[ 1 ];
		}
		
	}

	public boolean searchConnection( String date , String from , String to , String time )
	{
		this.ambiguous = false;
		this.connectionsList = false;

		this.postFields = new ArrayList< NameValuePair >();
		this.postFields.add( new BasicNameValuePair( "REQ0HafasSearchForw" , "1" ) );
		this.postFields.add( new BasicNameValuePair( "REQ0HafasSkipLongChanges" , "1" ) );
		this.postFields.add( new BasicNameValuePair( "REQ0JourneyDate" , date ) );
		this.postFields.add( new BasicNameValuePair( "REQ0JourneyStopsS0A" , "1" ) );
		this.postFields.add( new BasicNameValuePair( "REQ0JourneyStopsS0G" , from ) );
		this.postFields.add( new BasicNameValuePair( "REQ0JourneyStopsS0ID" , "" ) );
		this.postFields.add( new BasicNameValuePair( "REQ0JourneyStopsZ0A" , "1" ) );
		this.postFields.add( new BasicNameValuePair( "REQ0JourneyStopsZ0G" , to ) );
		this.postFields.add( new BasicNameValuePair( "REQ0JourneyStopsZ0ID" , "" ) );
		this.postFields.add( new BasicNameValuePair( "REQ0JourneyTime" , time ) );
		this.postFields.add( new BasicNameValuePair( "queryPageDisplayed" , "yes" ) );
		this.postFields.add( new BasicNameValuePair( "start" , "Search connection" ) );
		this.postFields.add( new BasicNameValuePair( "wDayExt0" , "Mo|Tu|We|Th|Fr|Sa|Su" ) );
		
		this.lastTo = to;
		this.lastFrom = from;

		this.output = false;

		if ( this.postRequest( this.baseUrl , this.postFields ) == true )
		{
			if ( this.result.contains( "Your input is ambiguous" ) )
			{
				this.ambiguous = true;
				this.prepareSelectBoxes();
			}
			else if ( this.result.contains( "Connections sorted by" ) )
			{
				this.connectionsList = true;
				this.prepareConnectionsList();				
			}

			return true;
		}
		else
		{
			return false;
		}
	}
	
	
	public String getKey( String value , ArrayList< String[] > list )
	{		
		for( int y = 0 ; y < list.size() ; y++ )
		{
			if( list.get( y )[1].compareTo( value ) == 0 )
			{
				Log.e( "Found" , list.get( y )[0] );
				return list.get( y )[ 0 ];
			}
		}
		
		return "";		
	}
	
	
	private void prepareConnectionsList()
	{
		int i = 0;
		while( i < 7 )
		{
			String nextUrl = "";
			Pattern laterRegex = Pattern.compile( "<a[^>]*>later(.*?)</a>" , Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.MULTILINE );
			Matcher later = laterRegex.matcher( this.result );

			while ( later.find() )
			{
				String parts[] = later.group().split( "\"" );
				nextUrl = parts[ 1 ];
				break;
			}
			i++;
													
			
			if ( this.getRequest( nextUrl ) == true )
			{
				
			}
		}
		
		Pattern connectionsRegex = Pattern.compile( "<table[^>]*>(.*?)</table>" , Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.MULTILINE );
		Matcher connections = connectionsRegex.matcher( this.result );
		
		this.connections = new ArrayList< Connection >();
		
		while( connections.find() )
		{
			if( connections.group().contains( "summary=\"Connections overview\"" ) )
			{
				Pattern rowsRegex = Pattern.compile( "<tr[^>]*>(.*?)</tr>" , Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.MULTILINE );
				Matcher rows = rowsRegex.matcher( connections.group() );
										
				while( rows.find() )
				{
					int col = 0;
					
					Pattern colsRegex = Pattern.compile( "<td[^>]*>(.*?)</td>" , Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.MULTILINE );
					Matcher cols = colsRegex.matcher( rows.group() );
					
					String sFrom = "";
					String sTo = "";
					String sdate = "";
					String sdeparture = "";
					String sarrival = "";
					String sduration = "";
					String schanges = "";
					
					while( cols.find() )
					{
						String td = cols.group();
						
						if( col == 1 )
						{
							String[] places = td.split( "<br />" );
							sFrom = this.removeHTML( places[ 0 ] );
							sTo = this.removeHTML( places[ 1 ] );									
						}
						else if( col == 3 )
						{
							sdate = this.removeHTML( td );
						}
						else if( col == 5 )
						{
							String[] times = td.split( "<br />" );
							sdeparture = this.removeHTML( times[ 0 ] );
							sarrival = this.removeHTML( times[ 1 ] );
						}
						else if( col == 7 )
						{
							sduration = this.removeHTML( td );
						}
						else if( col == 8 )
						{
							schanges = this.removeHTML( td );
						}		
						
						col++;
					}							
					
					this.connections.add( new Connection( sFrom , sTo , sarrival , sdeparture , sduration , schanges , sdate ) );							
				}
			}
		}
		
		try
		{
			this.connections.remove( 0 );
		}
		catch( Exception p ){}
	}
	
	
	public boolean searchConnection( String date , String from , String to , String time , int seq )
	{
		this.ambiguous = false;

		this.postFields = new ArrayList< NameValuePair >();
		this.postFields.add( new BasicNameValuePair( "REQ0HafasSearchForw" , "1" ) );
		this.postFields.add( new BasicNameValuePair( "REQ0HafasSkipLongChanges" , "1" ) );
		this.postFields.add( new BasicNameValuePair( "REQ0JourneyDate" , date ) );
		this.postFields.add( new BasicNameValuePair( "REQ0JourneyStopsS0A" , "1" ) );
		this.postFields.add( new BasicNameValuePair( "REQ0JourneyStopsS0K" , this.getKey( from , this.fromList ) ) );
		this.postFields.add( new BasicNameValuePair( "REQ0JourneyStopsZ0A" , "1" ) );
		this.postFields.add( new BasicNameValuePair( "REQ0JourneyStopsZ0K" , this.getKey( to , this.toList ) ) );
		this.postFields.add( new BasicNameValuePair( "REQ0JourneyTime" , time ) );
		this.postFields.add( new BasicNameValuePair( "queryPageDisplayed" , "yes" ) );
		this.postFields.add( new BasicNameValuePair( "start" , "Search connection" ) );
		this.postFields.add( new BasicNameValuePair( "wDayExt0" , "Mo|Tu|We|Th|Fr|Sa|Su" ) );
		
		this.lastTo = to;
		this.lastFrom = from;

		this.output = false;

		if ( this.postRequest( this.sequenceUrl , this.postFields ) == true )
		{
			if ( this.result.contains( "Your input is ambiguous" ) )
			{
				this.ambiguous = true;
				this.prepareSelectBoxes();
			}
			else if ( this.result.contains( "Connections sorted by" ) )
			{
				this.connectionsList = true;
				this.prepareConnectionsList();				
			}		
			
			return true;
		}
		else
		{
			return false;
		}
	}
	
	

	public boolean isAmbiguous()
	{
		return this.ambiguous;
	}

	public String[] getfromList()
	{
		String[] types = new String[ this.fromList.size() ];

		for ( int y = 0; y < this.fromList.size(); y++ )
		{
			types[ y ] = this.fromList.get( y )[ 1 ];
		}

		return types;
	}

	public String[] gettoList()
	{
		String[] types = new String[ this.toList.size() ];

		for ( int y = 0; y < this.toList.size(); y++ )
		{
			types[ y ] = this.toList.get( y )[ 1 ];
		}

		return types;
	}
		
	public ArrayList< Connection > getConnections()
	{
		if( this.connections == null )
			return new ArrayList< Connection >();
		
		return this.connections;
	}
	
	
	public int getConnectionsCount()
	{
		if( this.connections == null )
			return 0;
		
		return this.connections.size();
	}

	public String getLastTo()
	{
		return lastTo;
	}

	public String getLastFrom()
	{
		return lastFrom;
	}
	
	public boolean hasConnections()
	{
		return this.connectionsList;
	}
}
