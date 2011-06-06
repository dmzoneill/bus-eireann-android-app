package ie.buseireann;

import android.app.ListActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

public class ConnectionsActivity extends ListActivity
{
	private ConnectionsAdapter adapter;
	private String title;
	
	@Override
	public void onCreate( Bundle savedInstanceState )
	{
		super.onCreate( savedInstanceState );
		this.setContentView( R.layout.connectionsview );
		
		this.title = "Search results";		
		this.setTitle( this.title );
		
		this.getListView();
		
		this.adapter = new ConnectionsAdapter( this , this );	
		this.setListAdapter( this.adapter );
		
		TextView connectionFrom = (TextView) this.findViewById( R.id.connectionFrom );
		connectionFrom.setText( WebHelper.getInstance().getLastFrom() );

		TextView connectionTo = (TextView) this.findViewById( R.id.connectionTo );
		connectionTo.setText( WebHelper.getInstance().getLastTo() );
	}

	@Override
	protected void onListItemClick( ListView l , View v , int position , long id )
	{				
		
	}		
	
	public void resetSearch( View v )
	{		
		this.finish();
	}
	
}
