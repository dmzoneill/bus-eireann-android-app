package ie.buseireann;

import android.app.ListActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

public final class ConnectionsActivity extends ListActivity {
  /** The adapter. */
  private ConnectionsAdapter adapter;

  /** The title. */
  private String title;

  @Override
  public void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    this.setContentView(R.layout.connectionsview);

    this.title = "Search results";
    this.setTitle(this.title);

    this.getListView();

    this.adapter = new ConnectionsAdapter(this, this);
    this.setListAdapter(this.adapter);

    TextView connectionFrom = (TextView) this.findViewById(R.id.connectionFrom);
    connectionFrom.setText(WebHelper.getInstance().getLastFrom());

    TextView connectionTo = (TextView) this.findViewById(R.id.connectionTo);
    connectionTo.setText(WebHelper.getInstance().getLastTo());
  }

  @Override
  protected void onListItemClick(
      final ListView l, final View v, final int position, final long id) {
    super.onListItemClick(l, v, position, id);
  }

  /**
   * Reset search.
   *
   * @param v the v
   */
  public void resetSearch(final View v) {
    this.finish();
  }
}
