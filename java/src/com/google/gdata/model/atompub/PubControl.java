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


package com.google.gdata.model.atompub;

import com.google.gdata.model.DefaultRegistry;
import com.google.gdata.model.Element;
import com.google.gdata.model.ElementCreator;
import com.google.gdata.model.ElementKey;
import com.google.gdata.model.QName;
import com.google.gdata.util.Namespaces;

/**
 * Describes an Atom publication control status.
 *
 * 
 */
public class PubControl extends Element {

  /**
   * The key for this element.
   */
  public static final ElementKey<Void,
      PubControl> KEY = ElementKey.of(new QName(Namespaces.atomPubStandardNs,
      "control"), Void.class, PubControl.class);

  /*
   * Generate the default metadata for this element.
   */
  static {
    ElementCreator builder = DefaultRegistry.build(KEY);
    builder.addElement(Draft.KEY);
  }

  /**
   * Default mutable constructor.
   */
  public PubControl() {
    this(KEY);
  }

  /**
   * Create an instance using a different key.
   */
  public PubControl(ElementKey<Void, ? extends PubControl> key) {
    super(key);
  }

  /**
   * Constructs a new instance by doing a shallow copy of data from an existing
   * {@link Element} instance. Will use the given {@link ElementKey} as the key
   * for the element.
   *
   * @param key The key to use for this element.
   * @param source source element
   */
  public PubControl(ElementKey<Void, ? extends PubControl> key,
      Element source) {
    super(key, source);
  }

   @Override
   public PubControl lock() {
     return (PubControl) super.lock();
   }

  /**
   * Returns the draft tag.
   *
   * @return draft tag
   */
  public Draft getDraft() {
    return super.getElement(Draft.KEY);
  }

  /**
   * Sets the draft tag.
   *
   * @param draft draft tag or <code>null</code> to reset
   */
  public PubControl setDraft(Draft draft) {
    super.setElement(Draft.KEY, draft);
    return this;
  }

  /**
   * Returns whether it has the draft tag.
   *
   * @return whether it has the draft tag
   */
  public boolean hasDraft() {
    return super.hasElement(Draft.KEY);
  }

  @Override
  public String toString() {
    return "{PubControl}";
  }


  /**
   * Checks the value of the app:draft tag.
   *
   * @return true if the entry is a draft (false by default as per RFC5023)
   */
  public boolean isDraft() {
    return hasDraft() && Draft.Value.YES.equals(getElementValue(Draft.KEY));
  }

  /**
   * Sets the value of the app:draft tag.
   *
   * @param value sets the draft status to the specified value, false to clear.
   */
  public void setDraft(boolean value) {
    if (value) {
      setDraft(new Draft(Draft.Value.YES));
    } else {
      removeElement(Draft.KEY);
    }
  }
}
