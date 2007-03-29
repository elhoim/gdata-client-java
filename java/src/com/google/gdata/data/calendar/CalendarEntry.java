/* Copyright (c) 2006 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package com.google.gdata.data.calendar;

import com.google.gdata.data.BaseEntry;
import com.google.gdata.data.ExtensionProfile;
import com.google.gdata.data.extensions.Where;
import com.google.gdata.data.extensions.When;

import java.util.List;

/**
 * This is used by com.google.calendar.forseti.CalendarsFeedProvider
 * to represent a single calendar, in a list of calendars.  Use
 * EventEntry to represent a single event, that would show up in a
 * CalendarEventEntry feed.
 *
 * 
 */
public class CalendarEntry extends BaseEntry<CalendarEntry> {

  /**
   * Constructs a new CalendarEntry instance
   */
  public CalendarEntry() {
    super();
  }


  /**
   * Constructs a new CalendarEntry instance by doing a shallow copy of data
   * from an existing BaseEntry instance.
   */
  public CalendarEntry(BaseEntry sourceEntry) {
    super(sourceEntry);
  }


  /**
   * Initializes an ExtensionProfile based upon the extensions expected
   * by an EventEntry.
   */
  public void declareExtensions(ExtensionProfile extProfile) {
    extProfile.declareEntryExtension(
      AccessLevelProperty.getDefaultDescription());
    extProfile.declareEntryExtension(ColorProperty.getDefaultDescription());
    extProfile.declareEntryExtension(HiddenProperty.getDefaultDescription());
    extProfile.declareEntryExtension(
      OverrideNameProperty.getDefaultDescription());
    extProfile.declareEntryExtension(SelectedProperty.getDefaultDescription());
    extProfile.declareEntryExtension(TimeZoneProperty.getDefaultDescription());
    extProfile.declareEntryExtension(When.getDefaultDescription());
    extProfile.declareEntryExtension(Where.getDefaultDescription());
  }


  /**
   * Returns the list of calendar locations
   */
  public List<Where> getLocations() {
    return getRepeatingExtension(Where.class);
  }


  /**
   * Adds a new calendar location.
   */
  public void addLocation(Where location) {
    getLocations().add(location);
  }

  /**
   * Returns the calendar accesslevel.
   */
  public AccessLevelProperty getAccessLevel() {
    return getExtension(AccessLevelProperty.class);
  }


  /**
   * Sets the calendar accesslevel.
   */
  public void setAccessLevel(AccessLevelProperty accesslevel) {
    setExtension(accesslevel);
  }

  /**
   * Returns the calendar color.
   */
  public ColorProperty getColor() {
    return getExtension(ColorProperty.class);
  }


  /**
   * Sets the calendar color.
   */
  public void setColor(ColorProperty color) {
    setExtension(color);
  }

  /**
   * Returns the calendar hidden property.
   */
  public HiddenProperty getHidden() {
    return getExtension(HiddenProperty.class);
  }


  /**
   * Sets the calendar hidden property.
   */
  public void setHidden(HiddenProperty hidden) {
    setExtension(hidden);
  }

  /**
   * Returns the calendar hidden property.
   */
  public OverrideNameProperty getOverrideName() {
    return getExtension(OverrideNameProperty.class);
  }


  /**
   * Sets the calendar hidden property.
   */
  public void setOverrideName(OverrideNameProperty name) {
    setExtension(name);
  }

  /**
   * Returns the calendar selected property.
   */
  public SelectedProperty getSelected() {
    return getExtension(SelectedProperty.class);
  }


  /**
   * Sets the calendar selected property.
   */
  public void setSelected(SelectedProperty selected) {
    setExtension(selected);
  }

  /**
   * Returns the calendar timeZone property.
   */
  public TimeZoneProperty getTimeZone() {
    return getExtension(TimeZoneProperty.class);
  }


  /**
   * Sets the calendar timeZone property.
   */
  public void setTimeZone(TimeZoneProperty timeZone) {
    setExtension(timeZone);
  }
}
