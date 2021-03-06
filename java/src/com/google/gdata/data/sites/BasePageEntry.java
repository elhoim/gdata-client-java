/* Copyright (c) 2008 Google Inc.
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


package com.google.gdata.data.sites;

import com.google.gdata.data.BaseEntry;
import com.google.gdata.data.ExtensionProfile;

/**
 * An entry representing a page in the site. These entries have namesand
 * correspond to non-anonymous nodes in jotspot.
 *
 * @param <E> concrete entry type
 * 
 */
public abstract class BasePageEntry<E extends BasePageEntry<E>> extends
    BaseContentEntry<E> {

  /**
   * Default mutable constructor.
   */
  public BasePageEntry() {
    super();
  }

  /**
   * Constructs a new instance by doing a shallow copy of data from an existing
   * {@link BaseEntry} instance.
   *
   * @param sourceEntry source entry
   */
  public BasePageEntry(BaseEntry<?> sourceEntry) {
    super(sourceEntry);
  }

  @Override
  public void declareExtensions(ExtensionProfile extProfile) {
    if (extProfile.isDeclared(BasePageEntry.class)) {
      return;
    }
    super.declareExtensions(extProfile);
    extProfile.declare(BasePageEntry.class, PageName.class);
  }

  /**
   * Returns the page name.
   *
   * @return page name
   */
  public PageName getPageName() {
    return getExtension(PageName.class);
  }

  /**
   * Sets the page name.
   *
   * @param pageName page name or <code>null</code> to reset
   */
  public void setPageName(PageName pageName) {
    if (pageName == null) {
      removeExtension(PageName.class);
    } else {
      setExtension(pageName);
    }
  }

  /**
   * Returns whether it has the page name.
   *
   * @return whether it has the page name
   */
  public boolean hasPageName() {
    return hasExtension(PageName.class);
  }

  @Override
  protected void validate() {
  }

  @Override
  public String toString() {
    return "{BasePageEntry " + super.toString() + "}";
  }

}

