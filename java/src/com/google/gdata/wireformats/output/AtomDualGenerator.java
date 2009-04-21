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


package com.google.gdata.wireformats.output;

import com.google.gdata.data.IAtom;
import com.google.gdata.model.DefaultRegistry;
import com.google.gdata.model.transforms.atom.AtomVersionTransforms;
import com.google.gdata.model.transforms.atompub.AtompubVersionTransforms;
import com.google.gdata.wireformats.AltFormat;
import com.google.gdata.wireformats.WireFormat;

/**
 * A dual-mode generator for atom.
 * 
 * 
 */
public class AtomDualGenerator extends DualModeGenerator<IAtom> {
  
  static {
    // Register common transforms for Atom and AtomPub elements
    AtomVersionTransforms.addTransforms(DefaultRegistry.builder());
    AtompubVersionTransforms.addTransforms(DefaultRegistry.builder());
  }
  
  public AtomDualGenerator() {
    super(new AtomGenerator());
  }

  public Class<IAtom> getSourceType() {
    return IAtom.class;
  }

  public AltFormat getAltFormat() {
    return AltFormat.ATOM;
  }

  @Override
  public WireFormat getWireFormat() {
    return WireFormat.XML;
  }
}
