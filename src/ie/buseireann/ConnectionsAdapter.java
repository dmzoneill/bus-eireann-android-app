package ie.buseireann;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ConnectionsAdapter extends BaseAdapter
{
	private static int selected;
	private Context context;

	public ConnectionsAdapter( ConnectionsActivity p , Context context )
	{
		ConnectionsAdapter.selected = -1;
		this.context = context;
	}

	public int getCount()
	{
		return WebHelper.getInstance().getConnectionsCount();
	}

	public Connection getItem( int position )
	{
		return WebHelper.getInstance().getConnections().get( position );
	}

	public long getItemId( int position )
	{
		return position;
	}

	public View getView( int position , View convertView , ViewGroup parent )
	{
		LinearLayout itemLayout;
		
		Connection con = WebHelper.getInstance().getConnections().get( position );

		itemLayout = (LinearLayout) LayoutInflater.from( context ).inflate( R.layout.connectionslist , parent , false );
		
		TextView connectionDate = (TextView) itemLayout.findViewById( R.id.connectionDate );
		connectionDate.setText( con.getDate() );
		
		TextView connectionDeparture = (TextView) itemLayout.findViewById( R.id.connectionDeparture );
		connectionDeparture.setText( con.getTimeDeparture() );
		
		TextView connectionArrival = (TextView) itemLayout.findViewById( R.id.connectionArrival );
		connectionArrival.setText( con.getTimeArrival() );
		
		TextView connectionDuration = (TextView) itemLayout.findViewById( R.id.connectionDuration );
		connectionDuration.setText( con.getDuration() );
		
		TextView connectionChanges = (TextView) itemLayout.findViewById( R.id.connectionChanges );
		connectionChanges.setText( con.getChanges() );
				
		return itemLayout;
	}
	
	
	public static void setSelected( int pos )
	{
		ConnectionsAdapter.selected = pos;		
	}	
	
	public static int getSelected()
	{
		return ConnectionsAdapter.selected;
	}
}
