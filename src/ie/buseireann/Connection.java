package ie.buseireann;

public final class Connection {
  /** The placeFrom. */
  private String placeFrom;

  /** The placeTo. */
  private String placeTo;

  /** The timeArrival. */
  private String timeArrival;

  /** The timeDeparture. */
  private String timeDeparture;

  /** The duration. */
  private String duration;

  /** The changes. */
  private String changes;

  /** The date. */
  private String date;

  /** Connection. */
  public Connection() {
    this("", "", "", "", "", "", "");
  }

  /**
   * Connection.
   *
   * @param newPlaceFrom the place from
   * @param newPlaceTo the place to
   * @param newTimeArrival the time arrival
   * @param newTimeDeparture the time departure
   * @param newDuration the duration
   * @param newChanges the changes
   * @param newDate the date
   */
  public Connection(
      final String newPlaceFrom,
      final String newPlaceTo,
      final String newTimeArrival,
      final String newTimeDeparture,
      final String newDuration,
      final String newChanges,
      final String newDate) {
    this.placeFrom = newPlaceFrom;
    this.placeTo = newPlaceTo;
    this.timeArrival = newTimeArrival;
    this.timeDeparture = newTimeDeparture;
    this.duration = newDuration;
    this.changes = newChanges;
    this.date = newDate;
  }

  /**
   * Get place from.
   *
   * @return the result
   */
  public String getPlaceFrom() {
    return this.placeFrom;
  }

  /**
   * Set place from.
   *
   * @param newPlaceFrom the place from
   */
  public void setPlaceFrom(final String newPlaceFrom) {
    this.placeFrom = newPlaceFrom;
  }

  /**
   * Get place to.
   *
   * @return the result
   */
  public String getPlaceTo() {
    return this.placeTo;
  }

  /**
   * Set place to.
   *
   * @param newPlaceTo the place to
   */
  public void setPlaceTo(final String newPlaceTo) {
    this.placeTo = newPlaceTo;
  }

  /**
   * Get time arrival.
   *
   * @return the result
   */
  public String getTimeArrival() {
    return this.timeArrival;
  }

  /**
   * Set time arrival.
   *
   * @param newTimeArrival the time arrival
   */
  public void setTimeArrival(final String newTimeArrival) {
    this.timeArrival = newTimeArrival;
  }

  /**
   * Get time departure.
   *
   * @return the result
   */
  public String getTimeDeparture() {
    return this.timeDeparture;
  }

  /**
   * Set time departure.
   *
   * @param newTimeDeparture the time departure
   */
  public void setTimeDeparture(final String newTimeDeparture) {
    this.timeDeparture = newTimeDeparture;
  }

  /**
   * Get duration.
   *
   * @return the result
   */
  public String getDuration() {
    return this.duration;
  }

  /**
   * Set duration.
   *
   * @param newDuration the duration
   */
  public void setDuration(final String newDuration) {
    this.duration = newDuration;
  }

  /**
   * Get changes.
   *
   * @return the result
   */
  public String getChanges() {
    return this.changes;
  }

  /**
   * Set changes.
   *
   * @param newChanges the changes
   */
  public void setChanges(final String newChanges) {
    this.changes = newChanges;
  }

  /**
   * Get date.
   *
   * @return the result
   */
  public String getDate() {
    return this.date;
  }

  /**
   * Set date.
   *
   * @param newDate the date
   */
  public void setDate(final String newDate) {
    this.date = newDate;
  }
}
