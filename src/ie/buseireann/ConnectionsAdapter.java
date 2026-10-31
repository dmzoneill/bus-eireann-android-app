package ie.buseireann;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ConnectionsAdapter extends BaseAdapter {
  /** The selected. */
  private static int selected;

  /** The context. */
  private Context context;

  /**
   * Connections adapter.
   *
   * @param p the p
   * @param ctx the context
   */
  public ConnectionsAdapter(final ConnectionsActivity p, final Context ctx) {
    ConnectionsAdapter.selected = -1;
    this.context = context;
  }

  /**
   * Get count.
   *
   * @return the result
   */
  public int getCount() {
    return WebHelper.getInstance().getConnectionsCount();
  }

  /**
   * Get item.
   *
   * @param position the position
   * @return the result
   */
  public Connection getItem(final int position) {
    return WebHelper.getInstance().getConnections().get(position);
  }

  /**
   * Get item id.
   *
   * @param position the position
   * @return the result
   */
  public long getItemId(final int position) {
    return position;
  }

  /**
   * Get view.
   *
   * @param position the position
   * @param convertView the convert view
   * @param parent the parent
   * @return the result
   */
  public View getView(final int position, final View convertView, final ViewGroup parent) {
    LinearLayout itemLayout;

    Connection con = WebHelper.getInstance().getConnections().get(position);

    itemLayout =
        (LinearLayout)
            LayoutInflater.from(context).inflate(R.layout.connectionslist, parent, false);

    TextView connectionDate = (TextView) itemLayout.findViewById(R.id.connectionDate);
    connectionDate.setText(con.getDate());

    TextView connectionDeparture = (TextView) itemLayout.findViewById(R.id.connectionDeparture);
    connectionDeparture.setText(con.getTimeDeparture());

    TextView connectionArrival = (TextView) itemLayout.findViewById(R.id.connectionArrival);
    connectionArrival.setText(con.getTimeArrival());

    TextView connectionDuration = (TextView) itemLayout.findViewById(R.id.connectionDuration);
    connectionDuration.setText(con.getDuration());

    TextView connectionChanges = (TextView) itemLayout.findViewById(R.id.connectionChanges);
    connectionChanges.setText(con.getChanges());

    return itemLayout;
  }

  /**
   * Set selected.
   *
   * @param pos the pos
   */
  public static void setSelected(final int pos) {
    ConnectionsAdapter.selected = pos;
  }

  /**
   * Get selected.
   *
   * @return the result
   */
  public static int getSelected() {
    return ConnectionsAdapter.selected;
  }
}
