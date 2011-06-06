package ie.buseireann;

import java.util.Calendar;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

public class Search extends Activity implements Runnable
{

	private ProgressDialog pd;
	private Thread thread;
	
	private TextView mTimeDisplay;
	private TextView mDateDisplay;

	private int mYear;
	private int mMonth;
	private int mDay;
	private int lDay;

	private int mHour;
	private int mMinute;
	
	private String searchTo;
	private String searchFrom;
	private String searchDate;
	private String searchTime;
	private String toIndex;
	private String fromIndex;
	
	private boolean searchMiddle;

	private static final int TIME_DIALOG_ID = 0;
	private static final int DATE_DIALOG_ID = 1;

	@Override
	public void onCreate( Bundle savedInstanceState )
	{
		super.onCreate( savedInstanceState );
		this.setContentView( R.layout.main );
		
		this.setTitle( "Bus Eireann Journey Planner" );

		this.mTimeDisplay = ( TextView ) findViewById( R.id.timeDisplay );
		this.mTimeDisplay.setOnClickListener( new OnClickListener()
		{
			@Override
			public void onClick( View arg0 )
			{
				showDialog( TIME_DIALOG_ID );
			}
		} );

		this.mTimeDisplay.setFocusable( false );
		
		
		final Calendar c = Calendar.getInstance();
		this.mHour = c.get( Calendar.HOUR_OF_DAY );
		this.mMinute = c.get( Calendar.MINUTE );

		this.updateTimeDisplay();

		this.mDateDisplay = ( TextView ) findViewById( R.id.dateDisplay );
		this.mDateDisplay.setOnClickListener( new View.OnClickListener()
		{
			@Override
			public void onClick( View v )
			{
				showDialog( DATE_DIALOG_ID );
			}
		} );
		
		this.mDateDisplay.setFocusable( false );

		this.mYear = c.get( Calendar.YEAR );
		this.mMonth = c.get( Calendar.MONTH );
		this.mDay = c.get( Calendar.DAY_OF_MONTH );
		this.lDay = c.get( Calendar.DAY_OF_WEEK );

		this.updateDateDisplay();

	}

	private void updateTimeDisplay()
	{
		this.mTimeDisplay.setText( new StringBuilder().append( pad( this.mHour ) ).append( ":" ).append( pad( this.mMinute ) ) );
	}

	private void updateDateDisplay()
	{
		int mon = this.mMonth + 1;
		String month = ( mon < 10 ? ( "0" + String.valueOf( mon ) ) : String.valueOf( mon ) );

		int da = this.mDay;
		String day = ( da < 10 ? ( "0" + String.valueOf( da ) ) : String.valueOf( da ) );

		String year = String.valueOf( this.mYear );
		year = year.substring( 2 , 4 );

		String theDay = "";

		switch ( this.lDay )
		{
			case 1:
				theDay = "Su";
				break;
			case 2:
				theDay = "Mo";
				break;
			case 3:
				theDay = "Tu";
				break;
			case 4:
				theDay = "We";
				break;
			case 5:
				theDay = "Th";
				break;
			case 6:
				theDay = "Fr";
				break;
			case 7:
				theDay = "Sa";
				break;
		}

		this.mDateDisplay.setText( new StringBuilder().append( theDay ).append( ", " ).append( day ).append( "." ).append( month ).append( "." ).append( year ) );
	}

	private static String pad( int c )
	{
		if ( c >= 10 )
		{
			return String.valueOf( c );
		}
		else
		{
			return "0" + String.valueOf( c );
		}
	}

	private TimePickerDialog.OnTimeSetListener mTimeSetListener = new TimePickerDialog.OnTimeSetListener()
	{
		public void onTimeSet( TimePicker view , int hourOfDay , int minute )
		{
			Search.this.mHour = hourOfDay;
			Search.this.mMinute = minute;
			Search.this.updateTimeDisplay();
		}
	};

	private DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener()
	{
		public void onDateSet( DatePicker view , int year , int monthOfYear , int dayOfMonth )
		{
			Search.this.mYear = year;
			Search.this.mMonth = monthOfYear;
			Search.this.mDay = dayOfMonth;
			
			Calendar calendar = Calendar.getInstance();
			calendar.set( Calendar.YEAR , year );
			calendar.set( Calendar.DAY_OF_MONTH , dayOfMonth );
			calendar.set( Calendar.MONTH , monthOfYear );
	
			Search.this.lDay = calendar.get( Calendar.DAY_OF_WEEK );
			
			Log.e( "DAY " , "" + Search.this.lDay );
			
			Search.this.updateDateDisplay();
		}
	};

	@Override
	protected Dialog onCreateDialog( int id )
	{
		switch ( id )
		{
			case TIME_DIALOG_ID:
				return new TimePickerDialog( this , this.mTimeSetListener , this.mHour , this.mMinute , false );

			case DATE_DIALOG_ID:
				return new DatePickerDialog( this , this.mDateSetListener , this.mYear , this.mMonth , this.mDay );
		}
		return null;
	}

	
	public void searchConnection( View v )
	{
		if( this.searchMiddle == true )
		{
			Spinner toSpinner = ( Spinner ) this.findViewById( R.id.toSpinnerInput );
			Spinner fromSpinner = ( Spinner ) this.findViewById( R.id.fromSpinnerInput );
			
			this.toIndex = ( String ) toSpinner.getSelectedItem();
			this.fromIndex = ( String ) fromSpinner.getSelectedItem();
			
			EditText timeText = ( EditText ) this.findViewById( R.id.timeDisplay );
			this.searchTime = timeText.getText().toString();
			
			EditText dateText = ( EditText ) this.findViewById( R.id.dateDisplay );
			this.searchDate = dateText.getText().toString();
			
		}
		else
		{
			EditText fromText = ( EditText ) this.findViewById( R.id.fromTextInput );
			this.searchFrom = fromText.getText().toString();
			
			EditText toText = ( EditText ) this.findViewById( R.id.toTextInput );
			this.searchTo = toText.getText().toString();
			
			EditText timeText = ( EditText ) this.findViewById( R.id.timeDisplay );
			this.searchTime = timeText.getText().toString();
			
			EditText dateText = ( EditText ) this.findViewById( R.id.dateDisplay );
			this.searchDate = dateText.getText().toString();			
		}
		
		this.thread = new Thread( this );		
		this.createCancelProgressDialog( "Searching connections" , "Standyby while we search for a connection" );
		this.thread.start();
	}
	
	
	public void resetSearch( View v )
	{		
		LinearLayout fromRow = ( LinearLayout ) this.findViewById( R.id.fromText );
		LinearLayout toRow = ( LinearLayout ) this.findViewById( R.id.toText );
		
		fromRow.setVisibility( LinearLayout.VISIBLE );
		toRow.setVisibility( LinearLayout.VISIBLE );
		
		LinearLayout fromRowSpin = ( LinearLayout ) this.findViewById( R.id.fromCombo );
		LinearLayout toRowSpin = ( LinearLayout ) this.findViewById( R.id.toCombo );
		
		fromRowSpin.setVisibility( LinearLayout.GONE );
		toRowSpin.setVisibility( LinearLayout.GONE );	
		
		Button resetSearch = ( Button ) this.findViewById( R.id.newSearchButton );
		resetSearch.setVisibility( Button.GONE );
		
		this.searchMiddle = false;	
	}
	
	
	public void run()
	{				
		try
		{
			WebHelper helper = WebHelper.getInstance();
			if( this.searchMiddle == true )
			{
				helper.searchConnection( this.searchDate , this.fromIndex , this.toIndex , this.searchTime , 1 );
			}
			else
			{
				helper.searchConnection( this.searchDate , this.searchFrom , this.searchTo , this.searchTime );
			}
			Search.this.searchHandler.sendEmptyMessage( 0 );
		}
		catch( Exception e )
		{
			Log.e( "BUSEIREANN-run" , e.getMessage() );
		}
	}
	
	
	private void createCancelProgressDialog( String title , String message )
	{
		this.pd = new ProgressDialog( this );
		this.pd.setTitle( title );
		this.pd.setMessage( message );
		this.pd.setButton( "Cancel" , new DialogInterface.OnClickListener()
		{
			public void onClick( DialogInterface dialog , int which )
			{
				try
				{
					Search.this.thread.interrupt();
				}
				catch( Exception e )
				{

				}
				return;
			}
		} );
		this.pd.show();
	}
	
	
	private Handler searchHandler = new Handler()
	{
		@Override
		public void handleMessage( Message msg )
		{
			try
			{
				Search.this.pd.dismiss();
				
				WebHelper helper = WebHelper.getInstance();
				
				if( helper.isAmbiguous() )
				{
					Search.this.prepareSelectFields();	
					Search.this.searchMiddle = true;
				}
				else if( helper.hasConnections() )
				{
					Intent myIntent = new Intent( Search.this , ConnectionsActivity.class );
					Search.this.startActivity( myIntent );
				}
				else
				{
					
				}
			}
			catch( Exception e )
			{
				Log.e( "BUSEIREANN-handler" , e.getMessage() );
			}
			
		}
	};
	
	
	private void prepareSelectFields()
	{

		Spinner toSpinner = ( Spinner ) this.findViewById( R.id.toSpinnerInput );
		Spinner fromSpinner = ( Spinner ) this.findViewById( R.id.fromSpinnerInput );
		
		LinearLayout fromRow = ( LinearLayout ) this.findViewById( R.id.fromText );
		LinearLayout toRow = ( LinearLayout ) this.findViewById( R.id.toText );
		
		Button resetSearch = ( Button ) this.findViewById( R.id.newSearchButton );
		resetSearch.setVisibility( Button.VISIBLE );
		
		fromRow.setVisibility( LinearLayout.GONE );
		toRow.setVisibility( LinearLayout.GONE );
		
		LinearLayout fromRowSpin = ( LinearLayout ) this.findViewById( R.id.fromCombo );
		LinearLayout toRowSpin = ( LinearLayout ) this.findViewById( R.id.toCombo );
		
		fromRowSpin.setVisibility( LinearLayout.VISIBLE );
		toRowSpin.setVisibility( LinearLayout.VISIBLE );		

		ArrayAdapter< String > spinnerArrayAdapter1 = new ArrayAdapter< String >( this , android.R.layout.simple_spinner_dropdown_item , WebHelper.getInstance().gettoList() );
		spinnerArrayAdapter1.setDropDownViewResource( R.layout.spinnertext );
		toSpinner.setAdapter( spinnerArrayAdapter1 );
		
		ArrayAdapter< String > spinnerArrayAdapter2 = new ArrayAdapter< String >( this , android.R.layout.simple_spinner_dropdown_item , WebHelper.getInstance().getfromList() );
		spinnerArrayAdapter2.setDropDownViewResource( R.layout.spinnertext );
		fromSpinner.setAdapter( spinnerArrayAdapter2 );

	}
}
